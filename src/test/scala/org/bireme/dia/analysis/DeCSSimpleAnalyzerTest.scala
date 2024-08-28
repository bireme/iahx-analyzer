package org.bireme.dia.analysis

import org.scalatest.flatspec.AnyFlatSpec

import java.text.Normalizer
import java.text.Normalizer.Form

import scala.language.postfixOps
import org.bireme.dia.tools.*

class DeCSSimpleAnalyzerTest extends AnyFlatSpec:
  private val deCSPath = "resources/decs/main"

  private def normalize(in: String): String =
    Normalizer.normalize(in.toLowerCase(), Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")

  private def areEquals(str1: Seq[String],
                        str2: Seq[String]): Boolean =
    str1.nonEmpty && str2.nonEmpty && str1.length == str2.length &&
      str1.zip(str2).forall((a, b) => a `equals` b)

  val analyzer: DeCSSimpleAnalyzer = DeCSSimpleAnalyzer(deCSPath)

  "The DeCSSimpleAnalyzer" should  "return the original sentence because it is not a DeCS term" in:

    val str1 = "O céu de hoje está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("o ceu de hoje esta azul!").sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "return the DeCS term, category e synonyms of Matadouros" in:
    val str1 = "Matadouros"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("matadouros","abattoirs", "mataderos").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the DeCS term, category and synonyms of lower case matadouros" in:
    val str1 = "matadouros"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("matadouros","abattoirs", "mataderos").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "not return the DeCS the term matadouros because it is inside of a sentence" in:
    val str1 = "O céu de hoje está azul. Vejam os matadouros !"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("o ceu de hoje esta azul. vejam os matadouros !").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the DeCS term, category and synonyms of matadouros and all related with sangue" in :
    val str1 = "matadouros/sangue"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("matadouros", "abattoirs", "mataderos",
     "sangue", "sangre", "blood", "abattoirs/blood", "matadouros/sangue", "mataderos/sangre").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the DeCS term, category and synonyms of matadouros and all related with sangre" in :
    val str1 = "matadouros/sangre"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("matadouros", "abattoirs", "mataderos",
      "sangue", "sangre", "blood", "abattoirs/blood", "matadouros/sangre","matadouros/sangue", "mataderos/sangre").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the DeCS term, category and synonyms of matadouros but no qualifier because xxx is not a one" in :
    val str1 = "matadouros/xxx"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("matadouros", "abattoirs", "mataderos", "matadouros/xxx").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the DeCS term, category and synonyms of matadouros but no qualifier because xxx is not a one xxx" in :
    val str1 = "matadouros/"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("matadouros", "abattoirs", "mataderos", "matadouros/").sorted

    assert(areEquals(result, expected))
