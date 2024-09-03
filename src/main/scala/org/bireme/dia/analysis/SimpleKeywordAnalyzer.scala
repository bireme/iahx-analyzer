package org.bireme.dia.analysis

import org.apache.lucene.analysis.{Analyzer, LowerCaseFilter}
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.KeywordTokenizer
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter

class SimpleKeywordAnalyzer extends Analyzer:
  override protected def createComponents(fieldName: String): TokenStreamComponents =
    val source = KeywordTokenizer()
    val filter1 = ASCIIFoldingFilter(source)
    val filter2 = LowerCaseFilter(filter1)

    new TokenStreamComponents(source, filter2)
