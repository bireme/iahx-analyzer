package org.bireme.dia.analysis

import org.scalatest.flatspec.AnyFlatSpec

import java.text.Normalizer
import java.text.Normalizer.Form

import scala.language.postfixOps
import org.bireme.dia.tools.*

class BVSStandardAnalyzerTest extends AnyFlatSpec:
  private val deCSPath = "resources/decs/main"

  private def normalize(in: String): String =
    Normalizer.normalize(in.toLowerCase(), Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")

  private def areEquals(str1: Seq[String],
                        str2: Seq[String]): Boolean =
    str1.nonEmpty && str2.nonEmpty && str1.length == str2.length &&
      str1.zip(str2).forall((a, b) => a `equals` b)

  val analyzer: BVSStandardAnalyzer = BVSStandardAnalyzer(deCSPath)

  "The BVSStandardAnalyzer" should "only return the input text normalized, no descriptor and synonyms should be found" in :

    val str1 = "O céu de hoje está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "esta", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "remove no alphanumeric characters from the prefix/suffix of the tokens" in:

    val str1 = "O [céu] de hoje, está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "esta", "azul").sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "should not return synonyms of the descriptor Matadouros because it is not precodified" in :

    val str1 = "O [céu] de hoje nos matadouros está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "nos", "matadouros", "esta", "azul").sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "should return synonyms of the descriptor in the precod (^d28631) format" in :

    val str1 = "O [céu] de hoje nos ^d000003 está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "nos", "d000003" ,"j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "esta", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return not synonyms of the descriptor Matadouros follow by empty /" in :

    val str1 = "O [céu] de hoje nos ^d000003/ está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "nos", "d000003", "esta", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the supposed descriptor with ^D format because it is converted to lowercase" in :

    val str1 = "O [céu] de hoje nos ^D000003 está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "nos", "d000003", "j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "esta", "azul").sorted

    assert(areEquals(result, expected))

  //====================================================================================================================
  
  it should "should not return synonyms of the supposed descriptor with ^000003 format" in :

    val str1 = "O [céu] de hoje nos ^000003 está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "nos", "000003", "esta", "azul").sorted

    assert(areEquals(result, expected))

  //====================================================================================================================

  it should "should not return synonyms of the supposed descriptor with d000003 format" in :

    val str1 = "O [céu] de hoje nos ^000003 está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Set("ceu", "de", "hoje", "nos", "000003", "esta", "azul").toSeq.sorted

    assert(areEquals(result, expected))

  //====================================================================================================================

  it should "should treat a free/isolated comma" in :

    val str1 = "O [céu] de hoje , está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "esta", "azul").sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "should treat non alphanumerical tokens" in :

    val str1 = "O [céu] de hoje seu $#%&*, está azul!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("ceu", "de", "hoje", "seu", "esta", "azul").sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "should not give synonyms and other words to Dengue" in :

    val str1 = "A Dengue está presente!"
    val result: Seq[String] = Tools.getTokenList(str1, analyzer).sorted
    val expected = Seq("dengue", "esta", "presente").sorted

    assert(areEquals(result, expected))
//====================================================================================================================
