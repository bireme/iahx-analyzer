package org.bireme.dia.analysis

import org.scalatest.flatspec.AnyFlatSpec

import java.text.Normalizer
import java.text.Normalizer.Form

import scala.language.postfixOps
import org.bireme.dia.tools.*

class DeCSQualifierAnalyzerTest extends AnyFlatSpec:
  private val deCSPath = "resources/decs/main"
  //private val deCSPath = "resources/decsCompleto/main"

  private def normalize(in: String): String =
    Normalizer.normalize(in.toLowerCase(), Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")

  private def areEquals(str1: Seq[String],
                        str2: Seq[String]): Boolean =
    str1.nonEmpty && str2.nonEmpty && str1.length == str2.length &&
      str1.zip(str2).forall((a, b) => a `equals` b)

  val analyzer: DeCSQualifierAnalyzer = DeCSQualifierAnalyzer(deCSPath)

  "The DeCSQualifierAnalyzer" should "return the original precoded value ^dxxxxx" in :

    val str1 = "^d007"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Set("^d007").toSeq.sorted

    assert(areEquals(result, expected))
//====================================================================================================================