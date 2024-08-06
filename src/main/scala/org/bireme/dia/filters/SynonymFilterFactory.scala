package org.bireme.dia.filters

import scala.jdk.CollectionConverters.*
import org.apache.lucene.analysis.{TokenFilterFactory, TokenStream}
import org.bireme.dia.synonyms.{DeCSSynonyms, DeCSSynonymsConf}

class SynonymFilterFactory(params: java.util.Map[String,String]) extends TokenFilterFactory(params):

  def this(params: Map[String, String]) = this(params.asJava)

  private val parameters: Map[String, String] = Option(params).map(_.asScala.toMap).getOrElse(Map.empty[String,String])
  private val conf = DeCSSynonymsConf(
    indexPath = parameters.getOrElse("indexPath", "resources/decs/main"),
    getDescriptor = parameters.getOrElse("getDescriptor", "false").toBoolean,
    getQualifiers = parameters.getOrElse("getQualifiers", "false").toBoolean,
    getJoinDescrQual = parameters.getOrElse("getJoinDescrQual", "false").toBoolean,
    getSynonyms = parameters.getOrElse("getSynonyms", "false").toBoolean,
    getCategory = parameters.getOrElse("getCategory", "false").toBoolean,
    getAbbreviation = parameters.getOrElse("getAbbreviation", "false").toBoolean,
    acceptOnlyPrecoded = parameters.getOrElse("acceptOnlyPrecoded", "false").toBoolean,
    splitWords = parameters.getOrElse("splitWords", "false").toBoolean,
    splitHifen = parameters.getOrElse("splitHifen", "false").toBoolean,
    toLowerCase = parameters.getOrElse("toLowerCase", "false").toBoolean,
    fixPrefixSuffix = parameters.getOrElse("fixPrefixSuffix", "false").toBoolean
  )

  override def create(in: TokenStream): TokenStream = SynonymFilter(in, DeCSSynonyms(conf))

  override def normalize(input: TokenStream): TokenStream = super.normalize(input)
