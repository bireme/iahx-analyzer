package org.bireme.dia.filters

import org.apache.lucene.analysis.core.WhitespaceTokenizer
import org.bireme.dia.synonyms.{DeCSSynonyms, DeCSSynonymsConf}
import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringReader
import java.nio.file.Files

class SynonymFilterTest extends AnyFlatSpec:
  "The SynonymFilter" should "skip irrelevant lookups and honor comma splitting" in:
    val indexPath = Files.createTempDirectory("synonym-filter-test")
    val conf = DeCSSynonymsConf(
      indexPath = indexPath.toString,
      getDescriptor = true,
      getQualifiers = true,
      acceptOnlyPrecoded = true,
      toLowerCase = true
    )
    val directory = org.apache.lucene.store.FSDirectory.open(indexPath)
    val writer = org.apache.lucene.index.IndexWriter(
      directory,
      org.apache.lucene.index.IndexWriterConfig(org.apache.lucene.analysis.standard.StandardAnalyzer())
    )
    val doc = org.apache.lucene.document.Document()
    doc.add(org.apache.lucene.document.StringField("id", "1", org.apache.lucene.document.Field.Store.YES))
    doc.add(org.apache.lucene.document.StringField("descriptor", "term", org.apache.lucene.document.Field.Store.YES))
    doc.add(org.apache.lucene.document.StringField("descriptor_full", "term", org.apache.lucene.document.Field.Store.YES))
    doc.add(org.apache.lucene.document.StoredField("syn", "alpha, beta"))
    val _ = writer.addDocument(doc)
    writer.close()
    directory.close()

    class CountingSynonyms(conf: DeCSSynonymsConf) extends DeCSSynonyms(conf):
      var lookupCount: Int = 0

      override def getSynonyms(term: String): Set[String] =
        lookupCount += 1
        Set(term)

    val synonyms = CountingSynonyms(conf)
    try
      val tokenizer = WhitespaceTokenizer()
      tokenizer.setReader(StringReader("ordinary words ^D000009 ^s022062"))

      val tokens = org.bireme.dia.tools.Tools.getTokenList(SynonymFilter(tokenizer, synonyms))

      assert(tokens == List("ordinary", "words", "^D000009", "^d000009", "^s022062"))
      assert(synonyms.lookupCount == 2)

      val uppercaseSynonyms = CountingSynonyms(conf.copy(toLowerCase = false))
      try
        val uppercaseTokenizer = WhitespaceTokenizer()
        uppercaseTokenizer.setReader(StringReader("^D000009 ^S022062"))

        val uppercaseTokens = org.bireme.dia.tools.Tools.getTokenList(
          SynonymFilter(uppercaseTokenizer, uppercaseSynonyms)
        )

        assert(uppercaseTokens == List("^D000009", "^S022062"))
        assert(uppercaseSynonyms.lookupCount == 2)
      finally uppercaseSynonyms.close()

      val commaConf = conf.copy(
        getQualifiers = false,
        getSynonyms = true,
        acceptOnlyPrecoded = false,
        toLowerCase = false
      )
      val noCommaSplit = DeCSSynonyms(commaConf.copy(splitComma = false))
      try assert(noCommaSplit.getSynonyms("term") == Set("term", "alpha, beta"))
      finally noCommaSplit.close()

      val commaSplit = DeCSSynonyms(commaConf.copy(splitComma = true))
      try assert(commaSplit.getSynonyms("term") == Set("term", "alpha, beta", "alpha", "beta"))
      finally commaSplit.close()
    finally
      synonyms.close()
      org.apache.lucene.util.IOUtils.rm(indexPath)
