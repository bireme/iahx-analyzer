package org.bireme.dia.filters

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{TokenFilter, TokenStream}
import org.apache.lucene.util.AttributeSource
import org.bireme.dia.synonyms.DeCSSynonyms
import org.bireme.dia.tools.Tools

import scala.collection.mutable
import scala.compiletime.uninitialized

class SynonymFilter(in: TokenStream,
                    syns: DeCSSynonyms) extends TokenFilter(in):   // remove \W characters from prefix and suffix of tokens

  private val synonymSet: mutable.Set[String] = new mutable.TreeSet[String]()
  private val termAtt = addAttribute(classOf[CharTermAttribute])
  private var current: AttributeSource.State = uninitialized

  override def close(): Unit =
    //syns.close()
    super.close()

  override def incrementToken(): Boolean =
    if synonymSet.isEmpty then
      if in.incrementToken() then
        val currentTerm: String = termAtt.toString.trim
        val currentTerm2: String = if syns.conf.toLowerCase then currentTerm.toLowerCase else currentTerm
        val currentTerm3: String = if syns.conf.fixPrefixSuffix then Tools.fixPrefixSuffix(currentTerm2) else currentTerm2

        if currentTerm3.isEmpty then incrementToken()
        else
          synonymSet.add(currentTerm3)
          addAliasesToSet(currentTerm)
          val syn: String = synonymSet.head.trim
          synonymSet.remove(syn)
          //restoreState(current)
          termAtt.setEmpty()
          termAtt.append(syn)
          true
      else false
    else
      val syn: String = synonymSet.head
      //val contain: Boolean = synonymSet.contains(syn)
      synonymSet.remove(syn)
      //restoreState(current)
      termAtt.setEmpty()
      termAtt.append(syn)
      //posIncrAtt.setPositionIncrement(0)
      true

  private def addAliasesToSet(currentTerm: String): Boolean =
    val synonyms: Set[String] = syns.getSynonyms(currentTerm)

    synonyms.foreach(elem => synonymSet.add(elem.trim))
    synonyms.isEmpty