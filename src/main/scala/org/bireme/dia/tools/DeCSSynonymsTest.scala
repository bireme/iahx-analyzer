package org.bireme.dia.tools

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.WhitespaceTokenizer
import org.apache.lucene.analysis.miscellaneous.{ASCIIFoldingFilter, LengthFilter, WordDelimiterGraphFilter}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.bireme.dia.synonyms.{DeCSSynonyms, DeCSSynonymsConf}

import java.io.StringReader

object DeCSSynonymsTest {
  def main(args: Array[String]): Unit = {
    val deCSPath: String = "resources/decs/main"

    val inputText: String = "COVID-19"

    val conf: DeCSSynonymsConf = DeCSSynonymsConf(
      indexPath = deCSPath,
      getDescriptor = true,
      getQualifiers = true,
      getSynonyms = true,
      getCategory = true,
      getJoinDescrQual = true,
      getAbbreviation = true,
      acceptOnlyPrecoded = true,
      splitWords = true,
      splitHifen = true,
      toLowerCase= true
      )

    val sym: DeCSSynonyms = DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms(inputText).toSeq
    val text: String = result.mkString(" ")
    val outTokens: Seq[String] = tokenizeWithWordDelimiter(text).sorted

    outTokens.foreach(println)

    sym.close()
  }

  private def tokenizeWithWordDelimiter(text: String): List[String] = {
    // 1) Tokenização inicial por whitespace
    val tokenizer = new WhitespaceTokenizer()
    tokenizer.setReader(new StringReader(text))

    // 2) Configuração do WordDelimiterGraphFilter
    val flags: Int = {
      WordDelimiterGraphFilter.GENERATE_WORD_PARTS |
      WordDelimiterGraphFilter.CATENATE_WORDS |
      WordDelimiterGraphFilter.CATENATE_NUMBERS |
      WordDelimiterGraphFilter.CATENATE_ALL |
      WordDelimiterGraphFilter.PRESERVE_ORIGINAL
    }

    // 3) Cria filtro WordDelimiterGraphFilter
    val filter1: TokenStream =
      new WordDelimiterGraphFilter(tokenizer, flags, null)

    // 4) Cria filtro ASCIIFoldingFilter
    val filter2: TokenStream = new ASCIIFoldingFilter(filter1)

    // 5) Cria filtro LengthFilter
    val filter3: LengthFilter = new LengthFilter(filter2, 2, 100)

    // 6) Inicializar pipeline
    filter3.reset()

    // 7) Atributo do termo
    val termAttr = filter3.getAttribute(classOf[CharTermAttribute])

    // 8) Iterar pelos tokens
    val tokens: List[String] = Iterator
      .continually(filter3.incrementToken())
      .takeWhile(identity)
      .map(_ => termAttr.toString)
      .toSet.toList.sorted

    filter3.end()
    filter3.close()

    tokens
  }
}
