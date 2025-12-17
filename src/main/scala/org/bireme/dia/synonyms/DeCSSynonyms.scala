package org.bireme.dia.synonyms

import org.apache.lucene.document.Document
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.search.{IndexSearcher, TermQuery, TopDocs}
import org.apache.lucene.store.MMapDirectory
import org.bireme.dia.tools.Tools

import java.io.File
import java.text.Normalizer
import java.text.Normalizer.Form
import scala.collection.mutable
import scala.util.matching.Regex

/**
 * Class that given a string, if it is a DeCS' term or qualifier it will return all other related information
 *
 * author: Heitor Barbieri This implementation is strongly based in the previous java version written by Vinicius Andrade Antonio
 * date: june 2024
 *
 * @param conf case class representing the configuration parameters
 */
class DeCSSynonyms(val conf: DeCSSynonymsConf):
  require(!conf.getCategory || conf.getDescriptor, "Category requires Descriptor to be enabled")
  require(!conf.getSynonyms || conf.getDescriptor, "Synonyms requires Descriptor to be enabled")
  require(!conf.splitWords  || conf.getDescriptor, "SplitWords requires Descriptor to be enabled")
  require(!conf.splitHifen  || conf.getDescriptor, "SplitHifen requires Descriptor to be enabled")
  require(!conf.getAbbreviation  || conf.getQualifiers, "Abbreviation requires Qualifiers to be enabled")
  require(!conf.getJoinDescrQual || (conf.getDescriptor && conf.getQualifiers), "JoinDescrQual requires Descriptor and Qualifiers to be enabled")

  private val indexDir = new File(conf.indexPath)
  private val ramDir: MMapDirectory = MMapDirectory(indexDir.toPath)
  private val reader: DirectoryReader = DirectoryReader.open(ramDir)
  private val searcher: IndexSearcher = IndexSearcher(reader)

  def close(): Unit = reader.close()

  private def normalize(in: String): String =
    Normalizer.normalize(in.trim.toLowerCase(), Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")

  def getSynonyms(term: String): Set[String] =
    val termLow: String = normalize(term)
    val (descriptor, qualifier) = getDescrQualif(termLow)
    val field: Option[String] = descriptor.map(des => if des.matches("[0-9]+") then "id" else "descriptor") orElse
      qualifier.map(qual => if qual.matches("[0-9]+") then "id" else "descriptor")
    val descriptorDoc: Option[Document] = descriptor.flatMap(des => field.flatMap(fld => decsKey(des, fld)))
    val qualifierDoc: Option[Document] =
      qualifier.flatMap(d => field.flatMap(fld => decsKey(s"${if fld.equals("id") then "" else "/"}$d", fld)))
    val synSet: mutable.Set[String] = if descriptorDoc.isEmpty && qualifierDoc.isEmpty then mutable.Set[String](term)
    else
      val descriptors: Option[Array[String]] = descriptorDoc.flatMap(dd => Option(dd.getValues("descriptor_full")).map(arr => arr.filter(_.nonEmpty)))
      val synSetAux: mutable.Set[String] = mutable.Set[String]()
      qualifierDoc.foreach(synSetAux addAll extractKeyValues(_))
      descriptorDoc.foreach(synSetAux addAll extractKeyValues(_))
      addPrecoded(descriptor, qualifier, synSetAux)
      addSplitWords(descriptorDoc, synSetAux)
      addSplitWords(qualifierDoc, synSetAux)
      addSplitHifen(descriptorDoc, synSetAux)
      addSplitHifen(qualifierDoc, synSetAux)
      addJoinDescrQualif(qualifierDoc,descriptors,synSetAux)
      addAbbreviation(qualifierDoc, descriptors, synSetAux)
      addSplitComma(synSetAux)
      synSetAux.foldLeft(mutable.Set[String]()):
        case (nSet, key) =>
          if key.trim.isEmpty then nSet
          else if key.head == '/' then
            nSet.addOne(key)
            nSet.addOne(key.tail)
          else nSet.addOne(key)

    val set1: Set[String] = if conf.toLowerCase then synSet.map(_.toLowerCase).toSet else synSet.toSet
    val set2: Set[String] = if conf.fixPrefixSuffix then set1.map(x => Tools.fixPrefixSuffix(x)) else set1
    val set3: Set[String] = set2.filter(_.nonEmpty)
    
    set3

  private def getDescrQualif(in: String): (Option[String], Option[String]) =
    val onlyDescrPattern1: Regex = "^\\^d(\\d+) *$".r                // ^d0003
    val onlyQualPattern1: Regex  = "^\\^s(\\d+) *$".r                // ^s1234
    val descQualPattern1: Regex  = "^\\^d(\\d+) *\\^s(\\d+) *$".r    // ^d0003^s1234 ou ^d0003  ^s1234

    if conf.acceptOnlyPrecoded then
      in match
        case onlyDescrPattern1(descriptor) => if conf.getDescriptor then (Some(descriptor.toInt.toString), None) else (None,None)
        case onlyQualPattern1(qualifier) => if conf.getQualifiers then (None, Some(qualifier.toInt.toString)) else (None,None)
        case descQualPattern1(descriptor,qualifier) => (if conf.getDescriptor then Some(descriptor.toInt.toString) else None,
                                                        if conf.getQualifiers then Some(qualifier.toInt.toString) else None)
        case _ => (None, None)
    else
      val onlyDescrPattern2: Regex = "^([^\\^/\\r]+?)/?$".r            // matadouro ou matadouro/
      val onlyQualPattern2: Regex  = "^/(.+)$".r                       // /sangue
      val descQualPattern2: Regex  = "^([^\\^/]+)/(.+)$".r             // matadouro/sangue

      in match
        case onlyDescrPattern1(descriptor) => if conf.getDescriptor then (Some(descriptor.toInt.toString), None) else (None,None)
        case onlyQualPattern1(qualifier) => if conf.getQualifiers then (None, Some(qualifier.toInt.toString)) else (None,None)
        case descQualPattern1(descriptor,qualifier) => (if conf.getDescriptor then Some(descriptor.toInt.toString) else None,
          if conf.getQualifiers then Some(qualifier.toInt.toString) else None)
        case onlyDescrPattern2(descriptor) => if conf.getDescriptor then (Some(descriptor), None) else (None, None)
        case onlyQualPattern2(qualifier) => if conf.getQualifiers then (None, Some(qualifier)) else (None, None)
        case descQualPattern2(descriptor, qualifier) => (if conf.getDescriptor then Some(descriptor) else None,
                                                         if conf.getQualifiers then Some(qualifier) else None)
        case _ => (None, None)

  private def addPrecoded(descriptor: Option[String],
                          qualifier: Option[String],
                          synSet: mutable.Set[String]): Unit =
    if conf.acceptOnlyPrecoded then
      descriptor.foreach:
        des =>
          synSet add s"d$des"
          qualifier.foreach:
            qual =>
              synSet add s"^d$des^s$qual"
              synSet add s"d${des}s$qual"
      qualifier.foreach:
        qual =>
          synSet add s"s$qual"

  private def addSplitWords(doc: Option[Document],
                            synSet: mutable.Set[String]): Unit =
    def cleanPrefSuf(elem: String): String =
      val str1: String = if elem.head == '/' then elem.tail else elem
      if str1.endsWith(",") then str1.init else str1

    if conf.splitWords then
      val keyValues: Option[Set[String]] = doc.map(extractKeyValues)
        keyValues.foreach:
          set =>
            set.foreach:
              desc =>
                val splitSpace: Array[String] = desc.trim.split("\\s+")
                if splitSpace.length > 1 then      // There is at least one space in the term and it was already included in synSet
                  synSet addAll splitSpace.map(cleanPrefSuf)

  private def addSplitHifen(doc: Option[Document],
                            synSet: mutable.Set[String]): Unit =
    if conf.splitHifen then
      val keyValues: Option[Set[String]] = doc.map(extractKeyValues)
      keyValues.foreach:
        set =>
          set.foreach:
            desc =>
              val descT: String = desc.trim
              val splitHifen: Array[String] = descT.split(" *- *", 2)
              if splitHifen.length > 1 then
                splitHifen.foreach:
                  term =>
                    synSet.add(term)

  private def addSplitComma(synSet: mutable.Set[String]): Unit =
    val auxSet: mutable.Set[String] = mutable.Set[String]()
    
    synSet.foreach:
      term =>
        val split = term.split(" *, *", 2)
        if split.length > 1 then auxSet addAll split
          
    synSet.addAll(auxSet)
    
  private def addJoinDescrQualif(qualifierDoc: Option[Document],
                                 descriptors: Option[Array[String]],
                                 synSet: mutable.Set[String]): Unit =
    if conf.getJoinDescrQual then
      val joinQlf: Option[Array[String]] = qualifierDoc.flatMap(qd => Option(qd.getValues("descriptor_full")))
      val join: Option[(Array[String], Array[String])] = descriptors.zip(joinQlf)
      join.foreach:
        case (desc, qual) =>
          val dqArray: Array[(String, String)] = desc.zip(qual)
          dqArray.foreach((d, q) => synSet add s"$d$q")

  private def addAbbreviation(qualifierDoc: Option[Document],
                              descriptors: Option[Array[String]],
                              synSet: mutable.Set[String]): Unit =
    if conf.getAbbreviation && conf.getJoinDescrQual then
      val abbr: Option[String] = qualifierDoc.flatMap(_.getValues("abbreviation").headOption)
      abbr.foreach(synSet.add)
      abbr.foreach(abb => descriptors.foreach(desc => synSet addAll desc.map(d => s"$d/$abb")))

  private def decsKey(key: String,
                      field: String): Option[Document] =
    key.trim match
      case "" => None
      case key2 =>
        val key3: String = key2.replaceAll("^0*","") // Remove zeros à esquerda do código para match com índice de ID do DeCS
        val query: TermQuery = new TermQuery(new Term(field, key3))
        val hits: TopDocs = searcher.search(query, 1)
        val docId: Option[Int] = hits.scoreDocs.headOption.map(_.doc)

        docId.map(id => searcher.storedFields().document(id))

  private def extractKeyValues(doc: Document): Set[String] =
    val keyValues: mutable.Set[String]  = mutable.Set[String]()

    //Option(doc.get("id")).foreach(keyValues add _)
    Option(doc.getValues("descriptor")).foreach(keyValues addAll  _) // add authorized terms in 3 languages
    if conf. getSynonyms then Option(doc.getValues("syn")).foreach(keyValues addAll  _) // add synonyms
    if conf.getCategory then Option(doc.getValues("category")).foreach(keyValues addAll _) // add categories
    keyValues.toSet