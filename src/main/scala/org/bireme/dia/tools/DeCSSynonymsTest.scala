package org.bireme.dia.tools

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.WhitespaceTokenizer
import org.apache.lucene.analysis.miscellaneous.{ASCIIFoldingFilter, LengthFilter, WordDelimiterGraphFilter}
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, PositionIncrementAttribute}
import org.bireme.dia.synonyms.{DeCSSynonyms, DeCSSynonymsConf}

import java.io.StringReader

object DeCSSynonymsTest {

  def main(args: Array[String]): Unit = {
    val deCSPath: String = "resources/decs/main"
    val inputText: String = "^d57513^s22000"
    val showPosition: Boolean = true

    val conf: DeCSSynonymsConf = DeCSSynonymsConf(
      indexPath = deCSPath,
      getDescriptor = true,
      getQualifiers = true,
      getJoinDescrQual = true,
      getSynonyms = true,
      getCategory = true,
      getAbbreviation = true,
      acceptOnlyPrecoded = true,
      splitWords = true,
      splitHifen = true,
      toLowerCase = true
    )

    val sym: DeCSSynonyms = DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms(inputText).toSeq
    val text: String = result.mkString(" ")

    println(s"text=[$text]\n")

    val outTokens =
      tokenizeWithWordDelimiter(text, showPosition)

    outTokens.foreach(println)

    sym.close()
  }

  private def tokenizeWithWordDelimiter(
                                         text: String,
                                         showPosition: Boolean
                                       ): List[String] = {

    val tokenizer = new WhitespaceTokenizer()
    tokenizer.setReader(new StringReader(text))

    val flags: Int =
      WordDelimiterGraphFilter.GENERATE_WORD_PARTS |
        WordDelimiterGraphFilter.CATENATE_WORDS |
        WordDelimiterGraphFilter.CATENATE_NUMBERS |
        WordDelimiterGraphFilter.CATENATE_ALL |
        WordDelimiterGraphFilter.PRESERVE_ORIGINAL

    val filter1: TokenStream =
      new WordDelimiterGraphFilter(tokenizer, flags, null)

    val filter2: TokenStream =
      new ASCIIFoldingFilter(filter1)

    val filter3: LengthFilter =
      new LengthFilter(filter2, 2, 100)

    filter3.reset()

    val termAttr =
      filter3.getAttribute(classOf[CharTermAttribute])

    val posIncAttr =
      filter3.getAttribute(classOf[PositionIncrementAttribute])

    var position = 0

    val tokens =
      Iterator
        .continually(filter3.incrementToken())
        .takeWhile(identity)
        .map { _ =>
          position += posIncAttr.getPositionIncrement
          if (showPosition)
            s"${termAttr.toString}     [$position]"
          else
            termAttr.toString
        }
        .toSet
        .toList
        .sorted

    filter3.end()
    filter3.close()

    tokens
  }
}
