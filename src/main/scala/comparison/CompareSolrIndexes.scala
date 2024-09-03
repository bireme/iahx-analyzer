/*package comparison


import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.common.SolrDocumentList

import java.io.{BufferedWriter, FileWriter}
import scala.annotation.tailrec
import scala.collection.immutable.TreeSet
import scala.io.{BufferedSource, Source}
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}
import scala.jdk.CollectionConverters.*

object CompareSolrIndexes:
private def usage(): Unit =
  Console.err.println("Given an search expression file and two Solr indexes, check if the retrieved documents' ids for each expression are the same")
  Console.err.println("usage: CompareSolrIndexes <options>")
  Console.err.println("options:")
  Console.err.println("\t-url1=<index url1> - url of the first Solr server. For ex: http://localhost:8983/solr")
  Console.err.println("\t-url2=<index url2> - url of the first Solr server. For ex: http://otherhost.com:8983/solr")
  Console.err.println("\t-collection1=<col> - Solr index1 name")
  Console.err.println("\t-expressionFile=<path> - path to the search expression file. Expressions should be separated as follows:")
  Console.err.println("\t\t     [name of the first search]\nplants AND water\n[name of the second search]\ndengue OR zika")
  Console.err.println("\t-idField=<str> - name of the field to be used as id of the documents")
  Console.err.println("\t[-resultFile=<path>] - path to the output result file. If absent, the output will be displayed in the standard output")
  Console.err.println("\t-collection2=<col> - Solr index2 name. Default value is the same of collection1")
  System.exit(1)

def main(args: Array[String]): Unit =
  val parameters: Map[String, String] = args.foldLeft[Map[String, String]](Map()):
    case (map, par) =>
      val split: Array[String] = par.split(" *= *", 2)
      if (split.length == 1) map + ((split(0).substring(2), ""))
      else map + ((split(0).substring(1), split(1)))

  if (!Set("url1", "url2", "collection1", "expressionFile", "idField").forall(parameters.contains)) usage()

  val resultFileName: Option[String] = parameters.get("resultFile")
  val resultFile: Option[BufferedWriter] = resultFileName.map(name => BufferedWriter(FileWriter(name)))
  val collection1: String = parameters("collection1")
  val collection2: String = parameters("collection2")
  val solrClient1: Http2SolrClient = new Http2SolrClient.Builder(parameters("url1")).build()
  val solrClient2: Http2SolrClient = new Http2SolrClient.Builder(parameters("url2")).useHttp1_1(true).build()
  val solrQuery: SolrQuery = new SolrQuery("")
  val idField: String = parameters("idField")

  solrQuery.setFields(idField).setRows(1000)
  getExpressions(parameters("expressionFile")) match
    case Success(expressions) =>
      expressions.foreach:
        case (name, query) =>
          getDocuments(solrClient1, solrClient2, collection1, collection2, idField, solrQuery.setQuery(query)) match
            case Success(docs) =>
              val diff: Seq[String] = differentDocument(name, docs._1, docs._2)
              resultFile match
                case Some(writer) =>
                  if diff.isEmpty then
                    writer.append(s"[$name] - documents are the same, total:${docs._1.size}")
                    writer.newLine()
                  else
                    diff.foreach:
                      res =>
                        writer.append(res)
                        writer.newLine()
                case None =>
                  if diff.isEmpty then Console.println(s"[$name] - documents are the same, total:${docs._1.size}")
                  else diff.foreach(Console.println)
            case Failure(exception) =>
              resultFile match
                case Some(writer) =>
                  writer.append(exception.toString)
                  writer.newLine()
                case None =>
                  Console.err.println(s"Error in getting documents. QueryName=$name msg=${exception.toString}")
    case Failure(exception) =>
      resultFile match
        case Some(writer) =>
          writer.append(exception.toString)
          writer.newLine()
        case None => Console.err.println(exception.toString)
  solrClient1.close()
  solrClient2.close()

private def getExpressions(file: String): Try[Seq[(String,String)]] =
  Try:
    //val regex = "^\\s*?\\[([^\\]]+)\\]\\s*$".r
    val regex = "\\s*?\\[([^\\]]+)\\]\\s*".r
    val src: BufferedSource = Source.fromFile(file, "utf-8")
    val content: String = src.getLines().mkString("\n")

    src.close()
    getExpressions(content, regex.findAllMatchIn(content), None, Seq())

@tailrec
private def getExpressions(content: String,
                           iter: Iterator[Regex.Match],
                           previousMatch: Option[Regex.Match],
                           buffer: Seq[(String,String)]): Seq[(String,String)] =
  if iter.hasNext then
    val mat: Regex.Match = iter.next()
    previousMatch match
      case Some(pmat) =>
        val newBuffer: Seq[(String, String)] = buffer :+ (pmat.group(1), content.substring(pmat.end, mat.start))
        getExpressions(content, iter, Some(mat), newBuffer)
      case None => getExpressions(content, iter, Some(mat), buffer)
  else
    previousMatch match
      case Some(pmat) => buffer :+ (pmat.group(1), content.substring(pmat.end + 1))
      case None => buffer

private def getDocuments(solrClient1: Http2SolrClient,
                         solrClient2: Http2SolrClient,
                         collection1: String,
                         collection2: String,
                         idField: String,
                         solrQuery: SolrQuery): Try[(Set[String],Set[String])] =
  Try:
    (getDocuments(solrClient1, collection1, idField, solrQuery, start=0),
     getDocuments(solrClient2, collection2, idField, solrQuery, start=0))

private def getDocuments(solrClient: Http2SolrClient,
                         collection: String,
                         idField: String,
                         solrQuery: SolrQuery,
                         start: Int): Set[String] =
  solrQuery.setStart(start)

  val response = solrClient.query(collection, solrQuery)
  val results: SolrDocumentList = response.getResults
  val totalRetrieved: Long = results.size()

  if totalRetrieved == 0 then TreeSet()
  else TreeSet.from(results.iterator().asScala.map(doc => doc.get(idField).toString)) ++
    getDocuments(solrClient, collection, idField, solrQuery, start + totalRetrieved.toInt)

private def differentDocument(exprName: String,
                              seq1: Set[String],
                              seq2: Set[String]): Seq[String] =
  if seq1.isEmpty then
    if seq2.isEmpty then Seq()
    else s"[$exprName] index2: id[${seq2.head}]" +: differentDocument(exprName, seq1, seq2.tail)
  else
    if seq2.isEmpty then s"[$exprName] index1: id[${seq1.head}]" +: differentDocument(exprName, seq1.tail, seq2)
    else
      val h1: String = seq1.head
      val h2: String = seq2.head

      h1.compareTo(h2) match
        case x if x < 0 =>  // h1 < h2
          s"[$exprName] index1: id[$h1]" +: differentDocument(exprName, seq1.tail, seq2)
        case x if x == 0 => // h1 == h2
          differentDocument(exprName, seq1.tail, seq2.tail)
        case _ =>           // h1 > h2
          s"[$exprName] index2: id[$h2]" +: differentDocument(exprName, seq1, seq2.tail)
*/