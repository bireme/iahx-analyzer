package org.bireme.dia.tools

import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, OffsetAttribute, PayloadAttribute, PositionIncrementAttribute, TypeAttribute}
import org.apache.lucene.util.BytesRef
import org.bireme.dia.analysis.BVSStandardAnalyzer

import java.io.StringReader
import scala.annotation.tailrec
import scala.collection.mutable

object Tools:
  def fixPrefixSuffix(text: String,
                      fixPrefix: Boolean = true,
                      fixSuffix: Boolean = true): String =
    def trimEnd(str: String,
                func: Char => Boolean): String =
      @tailrec
      def helper(str: String): String =
        if (str.isEmpty || func(str.last)) str
        else helper(str.init)

      helper(str)

    val out1: String = if fixPrefix then text.dropWhile(ch => !Character.isLetterOrDigit(ch)) else text
    val out2: String = if fixSuffix then trimEnd(out1, Character.isLetterOrDigit) else out1

    out2

  def getTokenList(text: String,
                   analyzer: Analyzer): List[String] = getTokenList(analyzer.tokenStream("default", text))

  def getTokenList(stream: TokenStream): List[String] =
    val buffer: mutable.Buffer[String] = mutable.Buffer[String]()
    val term: CharTermAttribute = stream.addAttribute(classOf[CharTermAttribute])

    stream.reset()
    while stream.incrementToken() do
      println(s"term=[${term.toString}]")
      buffer += term.toString
    stream.close()
    buffer.toList

  def displayTokensWithFullDetails(analyzer: Analyzer,
                                   text: String) : Unit =
    val stream: TokenStream = analyzer.tokenStream("contents", new StringReader(text))
    val term: CharTermAttribute  = stream.addAttribute(classOf[CharTermAttribute])
    val posIncr: PositionIncrementAttribute = stream.addAttribute(classOf[PositionIncrementAttribute])
    val offset: OffsetAttribute  = stream.addAttribute(classOf[OffsetAttribute])
    val xtype: TypeAttribute = stream.addAttribute(classOf[TypeAttribute])
    val payload: PayloadAttribute = stream.addAttribute(classOf[PayloadAttribute])
    var position: Int = 0

    stream.reset()
    while (stream.incrementToken())
      val increment: Int = posIncr.getPositionIncrement
      if increment > 0 then
        position = position + increment
        println()
        print(s"$position:")

      val pl: BytesRef  = payload.getPayload
      if pl != null then
        print("[" + term.toString + ":" + offset.startOffset() + "->" + offset.endOffset() + ":" +
          xtype.`type`() + ":" + new String(pl.bytes) + "] ")
      else
        print("[" + term.toString + ":" + offset.startOffset() + "->" + offset.endOffset() + ":" +
          xtype.`type`() + "] ")
    println()

  @main
  def test(): Unit =
    val rule = "0123456789"
    val text = "Resumo A temperatura do ar é um fator climático que afeta a incidência da dengue, com efeitos variando conforme o tempo e o espaço. Investigamos a relação entre a temperatura mínima do ar e a incidência da doença em Minas Gerais, Brasil, e avaliamos a influência de variáveis socioeconômicas e geográficas nessa relação, calculando-se o risco relativo (RR). Este é um estudo de série temporal com análise conduzida em três etapas distintas: modelagem por uso de distributed lag non-linear model (modelos não-lineares distributivos com defasagem), metanálise dos modelos obtidos e metarregressão com dados geográficos e socioeconômicos. A temperatura mínima foi um fator de proteção quando em temperaturas frias extremas (RR = 0,65; IC95%: 0,56-0,76) e moderadas (RR = 0,71; IC95%: 0,64-0,79) e fator de risco em temperaturas de calor moderado (RR = 1,15; IC95%: 1,07-1,24), mas não em extremo (RR = 1,1; IC95%: 0,99-1,22). A heterogeneidade dos modelos foi elevada (I2 = 60%) e essa medida não foi alterada em metarregressão. Temperaturas frias moderadas e extremas causam efeito protetivo, enquanto moderadas quentes aumentam o risco. No entanto, a temperatura mínima do ar não explica nem a variabilidade da região, nem mesmo com as outras variáveis em metarregressão.,"
    val text1 = "com efeitos variando conforme o tempo e o espaço. Investigamos a relação entre"
    val text2 = "com efeitos variando conforme o temperamento. Investigamos a relação entre"
    val text3 = "com efeitos variando conforme a temperatura. Investigamos a relação entre"
    val text4 = "com efeitos variando conforme a temperatura Investigamos a relação entre"
    val text5 = "Resumo A temperatura do ar é um fator climático que afeta a incidência da dengue,"
    val analyzer = BVSStandardAnalyzer("resources/decs/main")

    println(text)
    println(rule * 10)
    displayTokensWithFullDetails(analyzer,text)

