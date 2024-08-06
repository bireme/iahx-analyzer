package org.bireme.dia.filters

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{TokenFilter, TokenStream}
import org.bireme.dia.tools.Tools

class PrefixSuffixFilter(in: TokenStream,
                         fixPrefix: Boolean = true,
                         fixSuffix: Boolean = true) extends TokenFilter(in):
  private val termAtt = addAttribute(classOf[CharTermAttribute])

  override def close(): Unit =
    //syns.close()
    super.close()

  override def incrementToken(): Boolean =
    if in.incrementToken() then
      val currentTerm: String = termAtt.toString.trim
      val outTerm: String = Tools.fixPrefixSuffix(currentTerm, fixPrefix, fixSuffix)

      termAtt.setEmpty()
      termAtt.append(outTerm)
      true
    else false