package org.bireme.dia.analysis

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{KeywordTokenizer, LowerCaseFilter}
import org.apache.lucene.analysis.miscellaneous.{ASCIIFoldingFilter, LengthFilter}
import org.bireme.dia.filters.{PrefixSuffixFilter, SynonymFilter}
import org.bireme.dia.synonyms.{DeCSSynonyms, DeCSSynonymsConf}

import scala.util.Try

class DeCSQualifierAnalyzer(indexPath: String) extends Analyzer:
  private val getDescriptor: Boolean = true // If true it will include the descriptor field in the generated tokens
  private val getQualifiers: Boolean = true // If true it will include the qualifiers field in the generated tokens
  private val getJoinDescrQual: Boolean = true // If true it will include descriptor/qualifier and descriptor/abbreviation in the generated tokens
  private val getSynonyms: Boolean = true   // If true it will include the synonyms field in the generated tokens
  private val getCategory: Boolean = false  // If true it will include the category field in the generated tokens
  private val getAbbreviation: Boolean = false    // If true it will include the abbreviation field in the generated tokens
  private val acceptOnlyPrecoded: Boolean = false // If true it will include descriptor and/or qualifiers if they were in the format ^dxxx^syyy
  private val splitWords: Boolean = false   // If true it will split compound tokens (more than one word separated by spaces) and include the subtokens in the generated tokens
  private val splitHifen: Boolean = false   // if true it will split tokens with hifens into subtokens and include them in the generated tokens
  private val toLowerCase: Boolean = true   // if true it will convert all tokens into lower case characters
  private val fixSplitted: Boolean = false   // if true it will remove \W characters from splitted terms
  private val fixPrefSuff: Boolean = false  // if true it will remove \W characters from prefix and suffix of tokens

  private val conf = DeCSSynonymsConf(indexPath = indexPath,
                                      getDescriptor = getDescriptor,
                                      getQualifiers = getQualifiers,
                                      getJoinDescrQual = getJoinDescrQual,
                                      getSynonyms = getSynonyms,
                                      getCategory = getCategory,
                                      getAbbreviation = getAbbreviation,
                                      acceptOnlyPrecoded = acceptOnlyPrecoded,
                                      splitWords = splitWords,
                                      splitHifen = splitHifen,
                                      toLowerCase = toLowerCase,
                                      fixPrefixSuffix = fixSplitted)
  private val syns: DeCSSynonyms = DeCSSynonyms(conf)

  def this() = this("resources/decs/main")

  def this(params: java.util.Map[String,String]) = this("")

  override def close(): Unit =
    Try:
      syns.close()
      super.close()

  override protected def createComponents(fieldName: String): TokenStreamComponents =
    val source = KeywordTokenizer()
    val filter1 = if toLowerCase then LowerCaseFilter(ASCIIFoldingFilter(source)) else ASCIIFoldingFilter(source)
    val filter2 = SynonymFilter(filter1, syns)
    val filter3 = if fixPrefSuff then PrefixSuffixFilter(ASCIIFoldingFilter(filter2)) else ASCIIFoldingFilter(filter2)
    val filter4 = LengthFilter(filter3, 2, 80)

    new TokenStreamComponents(source, filter4)

  override def toString: String =
    "class: org.bireme.dia.analysis.DeCSQualifierAnalyzer" +
      "\nparameters:" +
      s"\n\tindexPath=$indexPath  (DeCS path)" +
      s"\n\tgetDescriptor=$getDescriptor\t\t(include only DeCS' qualifiers fields)" +
      s"\n\tgetQualifiers=$getQualifiers\t\t(include DeCS' qualifiers fields)" +
      s"\n\tgetJoinDescrQual=$getJoinDescrQual\t\t(include DeCS' descriptor/qualifier)" +
      s"\n\tgetSynonyms=$getSynonyms\t\t(include DeCS' category fields)" +
      s"\n\tgetCategory=$getCategory\t(only process precod (^d28631) terms)" +
      s"\n\tgetAbbreviation=$getAbbreviation\t\t(include DeCS' synonyms fields)" +
      s"\n\tacceptOnlyPrecoded=$acceptOnlyPrecoded\t(include decs' abbreviation fields)" +
      s"\n\tsplitWords=$splitWords\t\t(split terms with spaces and include them)" +
      s"\n\tsplitHifen=$splitHifen\t\t(split terms with hifen and include them)" +
      s"\n\ttoLowerCase=$toLowerCase\t(convert the generated tokens into lower case)" +
      s"\n\tfixPrefSuff=$fixPrefSuff\t(remove \\W characters from prefix and suffix of tokens)" +
      "\nfilters:" +
      "\n\tKeywordTokenizer -> ASCIIFoldingFilter -> LowerCaseFilter ? -> SynonymFilter -> ASCIIFoldingFilter -> " +
      "PrefixSuffixFilter ? -> LengthFilter"
