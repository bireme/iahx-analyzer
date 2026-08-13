package org.bireme.dia.tools

import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import com.mongodb.client.model.Projections
import org.bson.Document as MongoDocument
import org.apache.lucene.document.{Field, StoredField, StringField, TextField, Document as LuceneDocument}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.store.FSDirectory
import org.bireme.dia.analysis.SimpleKeywordAnalyzer

import java.io.File
import java.util
import scala.util.control.NonFatal
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Using

object IndexDeCS:
  private val categoryFields: Seq[String] = Seq("Código Hierárquico", "Código Hierárquico Qualificador")
  private val descriptorFields: Seq[String] = Seq(
    "Descritor Inglês",
    "Descritor Espanhol",
    "Descritor Português",
    "Descritor Espanhol-Espanha",
    "Descritor Francês"
  )
  private val synonymFields: Seq[String] = Seq(
    "Sinônimos Inglês",
    "Sinônimos Espanhol",
    "Sinônimos Português",
    "Sinônimos Espanhol-Espanha",
    "Sinônimos Francês"
  )
  private val mongoFields: Seq[String] = Seq("Mfn", "Abreviação") ++ categoryFields ++ descriptorFields ++ synonymFields
  private val requiredParams: Set[String] = Set("database", "collection", "index")
  private val optionalParams: Set[String] = Set(
    "host", "port", "user", "password", "ram-buffer-mb", "max-segments",
    "progress-every", "mongo-batch-size", "allow-empty"
  )

  private case class ExportStats(processed: Long, indexed: Long, failed: Long)

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
    System.err.println("\t[-ram-buffer-mb=<number>] - Lucene RAM buffer in MB. Default is 64")
    System.err.println("\t[-max-segments=<number>]  - forceMerge target. Use 0 to skip. Default is 1")
    System.err.println("\t[-progress-every=<number>] - Progress interval in documents. Default is 5000")
    System.err.println("\t[-mongo-batch-size=<number>] - MongoDB cursor batch size. Default is server-managed")
    System.err.println("\t[-allow-empty=<boolean>] - Allow replacing the index from an empty collection. Default is false")
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

    if !params.keySet.subsetOf(requiredParams ++ optionalParams) ||
       !requiredParams.forall(name => params.get(name).exists(_.trim.nonEmpty))
    then usage()

    val port: Int = params.get("port").map(_.toInt).getOrElse(27017)
    val ramBufferMb: Double = params.get("ram-buffer-mb").map(_.toDouble).getOrElse(64.0)
    val maxSegments: Int = params.get("max-segments").map(_.toInt).getOrElse(1)
    val progressEvery: Int = params.get("progress-every").map(_.toInt).getOrElse(5000)
    val mongoBatchSize: Int = params.get("mongo-batch-size").map(_.toInt).getOrElse(0)
    val allowEmpty: Boolean = params.get("allow-empty").map(_.toBoolean).getOrElse(false)
    require(port > 0 && port <= 65535, "port must be between 1 and 65535")
    require(ramBufferMb.isFinite && ramBufferMb > 0, "ram-buffer-mb must be finite and greater than zero")
    require(maxSegments >= 0, "max-segments must be zero or greater")
    require(progressEvery >= 0, "progress-every must be zero or greater")
    require(mongoBatchSize >= 0, "mongo-batch-size must be zero or greater")

    val totalStart: Long = System.nanoTime()
    val usrPswStr: String = params.get("user").flatMap {
      usr => params.get("password").map(psw => s"$usr:$psw@")
    }.getOrElse("")
    val mongoUri: String = s"mongodb://$usrPswStr${params.getOrElse("host", "localhost")}:$port"

    val (stats, setupEnd, exportEnd, mergeEnd, commitEnd) = Using.Manager:
      use =>
        val mongoClient: MongoClient = use(MongoClients.create(mongoUri))
        val dbase: MongoDatabase = mongoClient.getDatabase(params("database"))
        val collName: String = params("collection").trim
        val coll: MongoCollection[MongoDocument] = dbase.getCollection(collName)
        val indexDir: FSDirectory = use(FSDirectory.open(File(params("index")).toPath))
        val iwc: IndexWriterConfig = IndexWriterConfig(new SimpleKeywordAnalyzer())
          .setOpenMode(OpenMode.CREATE)
          .setRAMBufferSizeMB(ramBufferMb)
          .setCommitOnClose(false)
        val writer: IndexWriter = use(IndexWriter(indexDir, iwc))
        val setupEnd: Long = System.nanoTime()

        val stats: ExportStats = exportDocs(coll, writer, progressEvery, mongoBatchSize)
        val exportEnd: Long = System.nanoTime()
        if stats.processed == 0 && !allowEmpty then
          throw IllegalStateException("MongoDB collection is empty; the existing index was preserved")
        if stats.failed > 0 then
          throw IllegalStateException(s"${stats.failed} document(s) could not be indexed")

        if maxSegments > 0 then writer.forceMerge(maxSegments)
        val mergeEnd: Long = System.nanoTime()
        writer.commit()
        val commitEnd: Long = System.nanoTime()

        (stats, setupEnd, exportEnd, mergeEnd, commitEnd)
    .get
    val closeEnd: Long = System.nanoTime()

    println(
      f"Finished: processed=${stats.processed}%d indexed=${stats.indexed}%d failed=${stats.failed}%d " +
        f"setup=${seconds(totalStart, setupEnd)}%.3fs export=${seconds(setupEnd, exportEnd)}%.3fs " +
        f"forceMerge=${seconds(exportEnd, mergeEnd)}%.3fs commit=${seconds(mergeEnd, commitEnd)}%.3fs " +
        f"close=${seconds(commitEnd, closeEnd)}%.3fs " +
        f"total=${seconds(totalStart, closeEnd)}%.3fs"
    )

  private def exportDocs(coll: MongoCollection[MongoDocument],
                         writer: IndexWriter,
                         progressEvery: Int,
                         mongoBatchSize: Int): ExportStats =
    val projection = Projections.fields(Projections.include(mongoFields*), Projections.excludeId())
    val find = coll.find().projection(projection)
    if mongoBatchSize > 0 then
      val _ = find.batchSize(mongoBatchSize)
    val cursor = find.iterator()
    var processed: Long = 0
    var indexed: Long = 0
    var failed: Long = 0

    try
      while cursor.hasNext do
        val mDoc: MongoDocument = cursor.next()
        processed += 1
        val lDoc: Option[LuceneDocument] = try Some(toLuceneDocument(mDoc))
        catch
          case NonFatal(exception) =>
            failed += 1
            exception.printStackTrace()
            System.err.println(s"Exporting error. Mfn=${mDoc.getString("Mfn")} Error=${exception.getMessage}")
            None

        lDoc.foreach:
          doc =>
            val _ = writer.addDocument(doc)
            indexed += 1

        if progressEvery > 0 && processed % progressEvery == 0 then
          println(s"Processed $processed documents")
    finally cursor.close()

    ExportStats(processed, indexed, failed)

  private def toLuceneDocument(mDoc: MongoDocument): LuceneDocument =
    val lDoc: LuceneDocument = LuceneDocument()
    val id: String = Option(mDoc.getString("Mfn")).getOrElse("").trim
    val id2: String = if id.isEmpty then id else id.toInt.toString
    if id2.nonEmpty then lDoc.add(new StringField("id", id2, Field.Store.YES))

    categoryFields.foreach:
      field =>
        Option(mDoc.get(field)).foreach:
          case category: String =>
            if category.trim.nonEmpty then lDoc.add(StoredField("category", category))
          case al =>
            val categories: mutable.Seq[String] = al.asInstanceOf[util.ArrayList[String]].asScala
            categories.foreach(category =>
              if category.trim.nonEmpty then lDoc.add(StoredField("category", category)))

    descriptorFields.foreach:
      name =>
        Option(mDoc.get(name)).map(_.asInstanceOf[String].trim).foreach:
          descriptor => lDoc.add(TextField("descriptor", descriptor, Field.Store.YES))

    descriptorFields.foreach:
      name =>
        Option(mDoc.get(name)).map(_.asInstanceOf[String].trim).foreach:
          descriptor => lDoc.add(StringField("descriptor_full", descriptor, Field.Store.YES))

    val seenSynonyms: mutable.HashSet[String] = mutable.HashSet[String]()
    synonymFields.foreach:
      name =>
        Option(mDoc.get(name)).foreach:
          case synonym: String => addSynonym(lDoc, seenSynonyms, synonym)
          case al =>
            al.asInstanceOf[util.ArrayList[String]].asScala.foreach(synonym => addSynonym(lDoc, seenSynonyms, synonym))

    Option(mDoc.get("Abreviação")).foreach:
      case abbreviation: String =>
        if abbreviation.trim.nonEmpty then lDoc.add(new StringField("abbreviation", abbreviation, Field.Store.YES))

    lDoc

  private def addSynonym(lDoc: LuceneDocument,
                         seenSynonyms: mutable.Set[String],
                         synonym: String): Unit =
    if synonym.trim.nonEmpty && seenSynonyms.add(synonym) then lDoc.add(StoredField("syn", synonym))

  private def seconds(start: Long, end: Long): Double = (end - start) / 1_000_000_000.0
