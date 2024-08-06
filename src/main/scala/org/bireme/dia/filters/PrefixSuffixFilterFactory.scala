package org.bireme.dia.filters

import scala.jdk.CollectionConverters.*
import org.apache.lucene.analysis.{TokenFilterFactory, TokenStream}

class PrefixSuffixFilterFactory(params: java.util.Map[String,String]) extends TokenFilterFactory(params):

  def this(params: Map[String, String]) = this(params.asJava)
  
  val fixPrefix: Boolean = Option(params.get("fixPrefix")).getOrElse("true").toBoolean
  val fixSuffix: Boolean = Option(params.get("fixSuffix")).getOrElse("true").toBoolean

  override def create(in: TokenStream): TokenStream = PrefixSuffixFilter(in, fixPrefix=fixPrefix, fixSuffix=fixSuffix)

  override def normalize(input: TokenStream): TokenStream = super.normalize(input)