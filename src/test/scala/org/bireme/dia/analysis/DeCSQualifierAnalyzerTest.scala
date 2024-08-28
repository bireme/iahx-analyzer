package org.bireme.dia.analysis

import org.scalatest.flatspec.AnyFlatSpec

import java.text.Normalizer
import java.text.Normalizer.Form

import scala.language.postfixOps
import org.bireme.dia.tools.*

class DeCSQualifierAnalyzerTest extends AnyFlatSpec:
  //private val deCSPath = "resources/decs/main"
  private val deCSPath = "resources/decsCompleto/main"

  private def normalize(in: String): String =
    Normalizer.normalize(in.toLowerCase(), Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")

  private def areEquals(str1: Seq[String],
                        str2: Seq[String]): Boolean =
    str1.nonEmpty && str2.nonEmpty && str1.length == str2.length &&
      str1.zip(str2).forall((a, b) => a `equals` b)

  val analyzer: DeCSQualifierAnalyzer = DeCSQualifierAnalyzer(deCSPath)

  "The DeCSQualifierAnalyzer" should "return the original precoded value ^dxxxxx" in :

    val str1 = "^d3727"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Set("^d3727", "bone", "bone fever", "break", "break bone fever", "break-bone", "break-bone fever", 
      "breakbone", "breakbone fever", "c01.920.500.270", "c01.925.081.270", "c01.925.782.350.250.214", 
      "c01.925.782.417.214", "classical", "classical dengue", "classical dengue fever", "classical dengue fevers", 
      "classical dengues", "classique", "d3727", "da", "de", "dengue", "dengue classical", "dengue classique", 
      "dengue fever", "dengue fever classical", "dengue fever, classical", "dengue, classical", "dengues", 
      "febre", "febre da dengue", "febre quebra", "febre quebra-ossos", "febre quebraossos", "fever", 
      "fever breakbone", "fever dengue", "fever, break-bone", "fever, breakbone", "fever, dengue", "fevers", "fiebre", 
      "fiebre dengue", "fievre", "fievre dengue", "fievre dengue classique", "fievre rouge", "infeccao", 
      "infeccao pelo virus da dengue", "infeccao por virus da dengue", "infeccao por virus de dengue", "ossos", "pelo",
      "por", "quebra", "quebra-ossos", "quebraossos", "rouge", "sp4.909.332.216.372", "virus").toSeq.sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "return the input term" in:
    val str = "febre quebra-ossos"
    val result: Seq[String] = Tools.getTokenList(str, analyzer).sorted
    val expected = Set("febre quebra-ossos").toSeq.sorted

    assert(areEquals(result, expected))