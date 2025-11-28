package org.bireme.dia.tools

import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import org.bson.Document as MongoDocument
import org.apache.lucene.document.{Field, StoredField, StringField, TextField, Document as LuceneDocument}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.bireme.dia.analysis.SimpleKeywordAnalyzer

import java.io.File
import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

object IndexDeCS:
  private def usage(): Unit =
    System.err.println("Import a DeCS collection from the MongoDB into a Lucene index")
    System.err.println("usage: IndexDeCS <options>")
    System.err.println("options:")
    System.err.println("\t-database=<name>   - MongoDB database name")
    System.err.println("\t-collection=<name> - MongoDB collection name")
    System.err.println("\t-index=<path>      - Path to the Lucene DeCS index")
    System.err.println("\t[-host=<string>]   - MongoDB host. Default is localhost")
    System.err.println("\t[-port=<number>]   - MongoDB port. Default is 27017")
    System.err.println("\t[-user=<name>])    - MongoDB user name")
    System.err.println("\t[-password=<pwd>]  - MongoDB user password")
    System.exit(1)

  def main(args: Array[String]): Unit =
    val params: Map[String, String] = args.foldLeft[Map[String, String]](Map()):
      case (map, par) =>
        val split = par.split(" *= *", 2)
        val split0 = split.head

        split.size match
          case 1 if split0.length > 2 => map + (split0.substring(2) -> "")
          case 2 if split0.length > 1 => map + (split0.substring(1) -> split(1))
          case _ => usage(); map

    if (!Set("database", "collection", "index").forall(params.contains)) usage()

    val usrPswStr: String = params.get("user").flatMap {
      usr => params.get("password").map(psw => s"$usr:$psw@")
    }.getOrElse("")
    val port: Int = params.get("port").map(_.toInt).getOrElse(27017)
    val mongoUri: String = s"mongodb://$usrPswStr${params.getOrElse("host", "localhost")}:$port"
    val mongoClient: MongoClient = MongoClients.create(mongoUri)
    val dbase: MongoDatabase = mongoClient.getDatabase(params("database"))
    val collName: String = params("collection").trim
    val coll: MongoCollection[MongoDocument] = dbase.getCollection(collName)
    val indexDir: FSDirectory = FSDirectory.open(File(params("index")).toPath)
    val iwc: IndexWriterConfig = IndexWriterConfig(new SimpleKeywordAnalyzer())
    val writer: IndexWriter = IndexWriter(indexDir, iwc)

    writer.deleteAll()  // Apaga TODOS os documentos do índice
    writer.commit()     // Força a gravação das mudanças

    exportDocs(coll, writer) match
      case Success(_) =>
        println("Exporting finished.")
        writer.forceMerge(1)
        writer.close()
        System.exit(0)
      case Failure(exception) =>
        println(s"Error(s) during document exporting: ${exception.getMessage}")
        writer.forceMerge(1)
        writer.close()
        System.exit(-1)

  private def exportDocs(coll: MongoCollection[MongoDocument],
                         writer: IndexWriter): Try[Unit] =
    Try:
      coll.find().asScala.zipWithIndex.foreach:
        case (mDoc, index) =>
          if (index % 100 == 0) println(s"+++$index")
          exportDoc(mDoc, writer) match
            case Success(_) => ()
            case Failure(exception) =>
              exception.printStackTrace()
              println(s"Exporting error. Id=${mDoc.getString("Id")} Error=${exception.getMessage}")

  private def exportDoc(mDoc: MongoDocument,
                        writer: IndexWriter): Try[Unit] =
    Try:
      val lDoc: LuceneDocument = LuceneDocument()
      val id: String = Option(mDoc.getString("Mfn")).getOrElse("").trim
      val id2: String = if id.isEmpty then id
                        else id.toInt.toString
      if id2.nonEmpty then lDoc.add(new StringField("id", id2, Field.Store.YES))

      // adiciona categorias do descritor/qualificador
      Seq("Código Hierárquico", "Código Hierárquico Qualificador") foreach:
        field =>
          Option(mDoc.get(field)).foreach:
            case category: String =>
              if category.trim.nonEmpty then lDoc.add(StoredField("category", category))
            case al =>
              val arr: mutable.Seq[String] = al.asInstanceOf[util.ArrayList[String]].asScala
              arr.foreach(category =>
                if category.trim.nonEmpty then lDoc.add(StoredField("category", category)))

      // adiciona o descritor nos 3 idiomas
      val descSeq: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
      //val descr: Seq[String] = mDoc.asScala.keys.filter(_.startsWith("Descritor")).toSeq
      val descr: Seq[String] = Seq("Descritor Inglês", "Descritor Espanhol", "Descritor Português",
        "Descritor Espanhol-Espanha", "Descritor Francês")

      descr.foreach:
        name =>
          Option(mDoc.get(name)).map(_.asInstanceOf[String]).foreach:
            descriptor =>
              lDoc.add(TextField("descriptor", descriptor.trim, Field.Store.YES))
              descSeq.addOne(descriptor)

      // adiciona o descritor nos 3 idiomas sem tokenize
      descSeq.clear()
      descr.foreach:
        name =>
          Option(mDoc.get(name)).map(_.asInstanceOf[String]).foreach:
            descriptor =>
              lDoc.add(StringField("descriptor_full", descriptor.trim, Field.Store.YES))
              descSeq.addOne(descriptor)

      // adiciona sinonimos nos 3 idiomas
      val synSeq: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
      val syn: Seq[String] = Seq("Sinônimos Inglês", "Sinônimos Espanhol", "Sinônimos Português", 
        "Sinônimos Espanhol-Espanha", "Sinônimos Francês")
      
      //mDoc.asScala.keys.filter(_.startsWith("Sinônimos")).foreach:
      syn.foreach:
        name =>
          Option(mDoc.get(name)).foreach:
            case syn: String =>
              if syn.trim.nonEmpty && !synSeq.contains(syn) then
                lDoc.add(StoredField("syn", syn))
                synSeq.addOne(syn)

            case al =>
              val arr: mutable.Seq[String] = al.asInstanceOf[util.ArrayList[String]].asScala
              arr.foreach:
                syn =>
                  if syn.trim.nonEmpty && !synSeq.contains(syn) then
                    lDoc.add(StoredField("syn", syn))
                    synSeq.addOne(syn)

      // adiciona abreviacao dos qualificadores (diagnostico = di)
      Option(mDoc.get("Abreviação")).foreach:
        case abbreviation: String =>
          if abbreviation.trim.nonEmpty then lDoc.add(new StringField("abbreviation", abbreviation, Field.Store.YES))

      writer.addDocument(lDoc)
      ()
