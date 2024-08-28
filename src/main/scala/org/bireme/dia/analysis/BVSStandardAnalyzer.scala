package org.bireme.dia.analysis

import org.apache.lucene.analysis.{Analyzer, TokenFilter}
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{LowerCaseFilter, WhitespaceTokenizer}
import org.apache.lucene.analysis.miscellaneous.{ASCIIFoldingFilter, LengthFilter, WordDelimiterGraphFilter, WordDelimiterIterator}
import org.bireme.dia.filters.{PrefixSuffixFilter, SynonymFilter}
import org.bireme.dia.synonyms.{DeCSSynonyms, DeCSSynonymsConf}

import scala.util.Try

class BVSStandardAnalyzer(indexPath: String) extends Analyzer:
  private val getDescriptor: Boolean = true // If true it will include the descriptor field in the generated tokens
  private val getQualifiers: Boolean = true // If true it will include the qualifiers field in the generated tokens
  private val getJoinDescrQual: Boolean = true   // If true it will include descriptor/qualifier and descriptor/abbreviation in the generated tokens
  private val getSynonyms: Boolean = true   // If true it will include the synonyms field in the generated tokens
  private val getCategory: Boolean = true   // If true it will include the category field in the generated tokens
  private val getAbbreviation: Boolean = true     // If true it will include the abbreviation field in the generated tokens
  private val acceptOnlyPrecoded: Boolean = true  // If true it will include descriptor and/or qualifiers only if they were in the format ^dxxx^syyy
  private val splitWords: Boolean = true    // If true it will split compound tokens (more than one word separated by spaces) and include the subtokens in the generated tokens
  private val splitHifen: Boolean = true    // if true it will split tokens with hifens into subtokens and include them in the generated tokens
  private val toLowerCase: Boolean = true   // if true it will convert all tokens into lower case characters
  private val fixSplitted: Boolean = true   // if true it will remove \W characters from splitted terms
  private val fixPrefSuff: Boolean = true   // if true it will remove \W characters from prefix and suffix of tokens

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

  private val table: Array[Byte] = Array.ofDim[Byte](256)
  for pos <- 0 until 256 do
    if pos == 32 then table(pos) = 4             // Não delimita em espaços
    // else if pos == 45                         // VA/HB 20161124
    //   table[pos] = 1                          // Não delimita em hifens
    else if pos == 46 then table(pos) = 1        // Não delimita em pontos
    else if pos == 47 then table(pos) = 1        // Não delimita em /
    else table(pos) = WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE(pos)

  private val wordDelimiterConfig: Int = WordDelimiterGraphFilter.GENERATE_WORD_PARTS +
                                         WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS +
                                         WordDelimiterGraphFilter.STEM_ENGLISH_POSSESSIVE +
                                         WordDelimiterGraphFilter.CATENATE_ALL +
                                         0 //WordDelimiterGraphFilter.PRESERVE_ORIGINAL
  /*
        GENERATE_WORD_PARTS : If set, causes parts of words to be generated: "PowerShot" => "Power" "Shot"
        GENERATE_NUMBER_PARTS : If set, causes number subwords to be generated: "500-42" => "500" "42"
        CATENATE_WORDS : If set, causes maximum runs of word parts to be catenated: "wi-fi" => "wifi"
        CATENATE_NUMBERS : If set, causes maximum runs of number parts to be catenated: "500-42" => "50042"
        CATENATE_ALL : If set, causes all subword parts to be catenated: "wi-fi-4000" => "wifi4000"
        SPLIT_ON_CASE_CHANGE : If set, causes "PowerShot" to be two tokens; ("Power-Shot" remains two parts regards)
        PRESERVE_ORIGINAL :  If set, includes original words in subwords: "500-42" => "500" "42" "500-42"
        SPLIT_ON_NUMERICS : If set, causes "j2se" to be three tokens; "j" "2" "se"
        STEM_ENGLISH_POSSESSIVE : If set, causes trailing "'s" to be removed for each subword: "O'Neil's" => "O", "Neil"
        IGNORE_KEYWORDS : If set, suppresses processing terms with KeywordAttribute#isKeyword()=true.
   */

  def this() = this("resources/decs/main")

  override def close(): Unit =
    Try:
      syns.close()
      super.close()

  override protected def createComponents(fieldName: String): TokenStreamComponents =
    val source: WhitespaceTokenizer = WhitespaceTokenizer()
    val filter1: TokenFilter = if toLowerCase then LowerCaseFilter(ASCIIFoldingFilter(source)) else ASCIIFoldingFilter(source)
    val filter2: SynonymFilter = SynonymFilter(filter1, syns)
    val filter3: WordDelimiterGraphFilter = WordDelimiterGraphFilter(filter2, false, table, wordDelimiterConfig, null)
    val filter4: TokenFilter = if fixPrefSuff then PrefixSuffixFilter(ASCIIFoldingFilter(filter3)) else ASCIIFoldingFilter(filter3)
    val filter5: LengthFilter = LengthFilter(filter4, 2, 100)

    TokenStreamComponents(source, filter5)

  override def toString: String =
    "class: org.bireme.dia.analysis.BVSStandardAnalyzer" +
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
    "\n\tWhitespaceTokenizer -> ASCIIFoldingFilter -> LowerCaseFilter ? -> SynonymFilter -> WordDelimiterGraphFilter -> " +
    "ASCIIFoldingFilter -> PrefixSuffixFilter ? -> LengthFilter"