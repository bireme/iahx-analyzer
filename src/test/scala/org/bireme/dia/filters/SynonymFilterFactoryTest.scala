package org.bireme.dia.filters

import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.core.{KeywordTokenizer, WhitespaceTokenizer}
import org.bireme.dia.synonyms.DeCSSynonymsConf
import org.bireme.dia.tools.Tools
import org.scalatest.flatspec.AnyFlatSpec

import java.io.StringReader
import scala.collection.immutable.Seq

class SynonymFilterFactoryTest extends AnyFlatSpec:
  private val deCSPath = "resources/decs/main"
  private val stream: Tokenizer = KeywordTokenizer()
  private val stream2: Tokenizer = WhitespaceTokenizer()
  private val allConf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                           getDescriptor = true,
                                                           getQualifiers = true,
                                                           getJoinDescrQual = true,
                                                           getSynonyms = true,
                                                           getCategory = true,
                                                           getAbbreviation = true,
                                                           //acceptOnlyPrecoded = false,
                                                           splitWords = true,
                                                           splitHifen = true,
                                                           toLowerCase = true,
                                                           fixPrefixSuffix = true)

  private def areEquals(in: Seq[String],
                        str2: Seq[String]): Boolean =
    in.nonEmpty && str2.nonEmpty && in.length == str2.length &&
      in.zip(str2).forall((a, b) => a `equals` b)

  private def conf2map(conf: DeCSSynonymsConf): Map[String,String] =
    Map(
      "indexPath" -> conf.indexPath,
      "getDescriptor" -> conf.getDescriptor.toString,
      "getQualifiers" -> conf.getQualifiers.toString,
      "getJoinDescrQual" -> conf.getJoinDescrQual.toString,
      "getSynonyms" -> conf.getSynonyms.toString,
      "getCategory" -> conf.getCategory.toString,
      "getAbbreviation" -> conf.getAbbreviation.toString,
      "acceptOnlyPrecoded" -> conf.acceptOnlyPrecoded.toString,
      "splitWords" -> conf.splitWords.toString,
      "splitHifen" -> conf.splitHifen.toString,
      "toLowerCase" -> conf.toLowerCase.toString,
      "fixPrefixSuffix" -> conf.fixPrefixSuffix.toString
    )

  "The SynonymFilterFactory" should "only return the input text normalized, no descriptor and synonyms should be found" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "O céu de hoje está azul!"
    
    stream.close()
    stream.setReader(StringReader(in))
    
    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================


  it should "get only descriptors as they are in the index, i.e., not breaking it neither changing its characters case (upper/lower)" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getSynonyms = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos",
      "Exercises, Circuit-Based", "Ejercicio Basado en Circuito", "Exercícios Baseados em Circuitos").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term and also split them in words" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getSynonyms = true,
                                                  splitWords = true,
                                                  fixPrefixSuffix = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "Circuit-Based Exercise", "Circuit-Based", "Exercise",
      "Ejercicio en Circuitos", "Ejercicio", "en", "Circuitos",
      "Exercícios em Circuitos", "Exercícios", "em",
      "Exercises, Circuit-Based", "Exercises",
      "Ejercicio Basado en Circuito", "Basado", "Circuito",
      "Exercícios Baseados em Circuitos", "Baseados").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term and also split them in words and in hifen" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getSynonyms = true,
                                                  splitWords = true,
                                                  splitHifen = true,
                                                  fixPrefixSuffix = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "Circuit-Based Exercise", "Circuit-Based", "Exercise", "Circuit", "Based",
      "Ejercicio en Circuitos", "Ejercicio", "en", "Circuitos",
      "Exercícios em Circuitos", "Exercícios", "em",
      "Exercises, Circuit-Based", "Exercises",
      "Ejercicio Basado en Circuito", "Basado", "Circuito",
      "Exercícios Baseados em Circuitos", "Baseados").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term but not split them in hifen as splitWords is false" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getSynonyms = true,
                                                  splitHifen = true,
                                                  fixPrefixSuffix = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set[String]("Circuit", "Based",
      "Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos",
      "Exercises, Circuit-Based", "Ejercicio Basado en Circuito", "Exercícios Baseados em Circuitos").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term and also split them in words and in hifen. Finally put all in lower case" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getSynonyms = true,
                                                  splitWords = true,
                                                  splitHifen = true,
                                                  toLowerCase = true,
                                                  fixPrefixSuffix = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set("circuit-based exercise", "circuit-based", "exercise", "circuit", "based",
      "ejercicio en circuitos", "ejercicio", "en", "circuitos",
      "exercícios em circuitos", "exercícios", "em",
      "exercises, circuit-based", "exercises",
      "ejercicio basado en circuito", "basado", "circuito",
      "exercícios baseados em circuitos", "baseados").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and category of the term" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getCategory = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "G11.427.590.530.698.277.061").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors,synonyms and category of the term and also split them in words and in hifen. Finally put all in lower case" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getSynonyms = true,
                                                  getCategory = true,
                                                  splitWords = true,
                                                  splitHifen = true,
                                                  toLowerCase = true,
                                                  fixPrefixSuffix = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set("circuit-based exercise", "circuit-based", "exercise", "circuit", "based",
      "ejercicio en circuitos", "ejercicio", "en", "circuitos",
      "exercícios em circuitos", "exercícios", "em",
      "exercises, circuit-based", "exercises",
      "ejercicio basado en circuito", "basado", "circuito",
      "exercícios baseados em circuitos", "baseados",
      "g11.427.590.530.698.277.061").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors and qualifiers and abbreviation in the form descriptor/qualifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "blood", "sangre", "sangue",
      "Circuit-Based Exercise/blood",
      "Ejercicio en Circuitos/sangre",
      "Exercícios em Circuitos/sangue",
      "Circuit-Based Exercise/BL",
      "Ejercicio en Circuitos/BL",
      "Exercícios em Circuitos/BL"
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors and qualifiers in the form descriptor<space>/<space>qualifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise / blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors and qualifiers in the form descriptor/<space>qualifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/ blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors, qualifiers and abbreviation in the form descriptor<space>/qualifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise /blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "blood", "sangre", "sangue",
      "Circuit-Based Exercise/blood",
      "Ejercicio en Circuitos/sangre",
      "Exercícios em Circuitos/sangue",
      "Circuit-Based Exercise/BL",
      "Ejercicio en Circuitos/BL",
      "Exercícios em Circuitos/BL"
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors but ot the qualifiers because the qualifier flag is set to false" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in, "Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors because the qualifier flag is set to false" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise /blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors, qualifiers and abbreviation int the form ^ddescriptor^squalifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^d000009^s022062"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "blood", "sangre", "sangue",
      "Circuit-Based Exercise/blood",
      "Ejercicio en Circuitos/sangre",
      "Exercícios em Circuitos/sangue",
      "Circuit-Based Exercise/BL",
      "Ejercicio en Circuitos/BL",
      "Exercícios em Circuitos/BL"
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors, qualifiers and abbreviation int the form ^ddescriptor<space>^squalifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^d000009  ^s022062"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "blood", "sangre", "sangue",
      "Circuit-Based Exercise/blood",
      "Ejercicio en Circuitos/sangre",
      "Exercícios em Circuitos/sangue",
      "Circuit-Based Exercise/BL",
      "Ejercicio en Circuitos/BL",
      "Exercícios em Circuitos/BL"
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get descriptors and qualifiers int the form ^ddescriptor<space>^squalifier and abbreviation" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true,
                                                  splitWords = true,
                                                  splitHifen = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^d000009  ^s022062"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "Circuit-Based", "Exercise", "Circuit", "Based",
      "Ejercicio", "en", "Circuitos",
      "Exercícios", "em", "Circuitos",
      "blood", "sangre", "sangue",
      "Circuit-Based Exercise/blood",
      "Ejercicio en Circuitos/sangre",
      "Exercícios em Circuitos/sangue",
      "Circuit-Based Exercise/BL",
      "Ejercicio en Circuitos/BL",
      "Exercícios em Circuitos/BL"
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors but not the qualifiers because they do not exist" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^d000009 ^s000999"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the original string because the qualifier is not numeric" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^d000009 ^sxxxx"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in).toSeq.sorted

    assert(areEquals(result, expected))
  //======================================================================= =============================================

  it should "return the original string because the descriptor is not numeric" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^dxxxxx ^s0001"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in).toSeq.sorted
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only qualifiers and abbreviation in the form ^squalifier" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getQualifiers = true,
                                                  getAbbreviation = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^s022062"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in, "blood", "sangre", "sangue").toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors, synonyms, qualifiers and category of the term and also split them in words and in hifen. Finally put all in lower case" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  getSynonyms = true,
                                                  getCategory = true,
                                                  getAbbreviation = true,
                                                  splitWords = true,
                                                  splitHifen = true,
                                                  toLowerCase = true,
                                                  fixPrefixSuffix = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      "circuit-based exercise", "circuit-based", "exercise", "circuit", "based",
      "ejercicio en circuitos", "ejercicio", "en", "circuitos",
      "exercícios em circuitos", "exercícios", "em",
      "exercises, circuit-based", "exercises",
      "ejercicio basado en circuito", "basado", "circuito",
      "exercícios baseados em circuitos", "baseados",
      "g11.427.590.530.698.277.061",
      "blood", "sangre", "sangue",
      "circuit-based exercise/blood",
      "ejercicio en circuitos/sangre",
      "exercícios em circuitos/sangue",
      "circuit-based exercise/bl",
      "ejercicio en circuitos/bl",
      "exercícios em circuitos/bl",
      "q50.040.020q05.010"
    ).toSeq.sorted

    assert(areEquals(result, expected))

  //====================================================================================================================
  it should "get the descriptors and synonyms of Circuit-Based Exercise/" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in,"Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos").toSeq.sorted

    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "get the descriptors and synonyms of Circuit-Based Exercise /" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise /"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in, "Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos").toSeq.sorted

    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "get the descriptors and synonyms of the qualifier /blood" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  toLowerCase = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in, "blood", "sangre", "sangue").toSeq.sorted

    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and getCategory is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                    getDescriptor = false,
                                                    getCategory = true)
      val symFact = SynonymFilterFactory(conf2map(conf))
      Tools.getTokenList(symFact.create(stream)).sorted
  //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and getSynonyms is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                    getDescriptor = false,
                                                    getSynonyms = true)
      val symFact = SynonymFilterFactory(conf2map(conf))
      Tools.getTokenList(symFact.create(stream)).sorted
  //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and splitWords is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                    getDescriptor = false,
                                                    splitWords = true)
      val symFact = SynonymFilterFactory(conf2map(conf))
      Tools.getTokenList(symFact.create(stream)).sorted
  //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and splitHifen is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                    getDescriptor = false,
                                                    splitHifen = true)
      val symFact = SynonymFilterFactory(conf2map(conf))
      Tools.getTokenList(symFact.create(stream)).sorted
  //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getQualifiers is false and getAbbreviation is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                    getDescriptor = true,
                                                    getQualifiers = false,
                                                    getAbbreviation = true)
      val symFact = SynonymFilterFactory(conf2map(conf))
      Tools.getTokenList(symFact.create(stream)).sorted
  //=====================================================================================================================

  it should "get only the qualifier /blood" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getQualifiers = true,
                                                  toLowerCase = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(in.toLowerCase, "blood", "sangre", "sangue").toSeq.sorted

    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "return only the input because the term in not in precoded format (^d1213)" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  acceptOnlyPrecoded = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "Circuit-Based Exercise/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Seq(in)

    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "get the term and qualifiers from a string in precoded format (^dCircuit-Based Exercise^sblood)" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  acceptOnlyPrecoded = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^d000009^s022062"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Set(
      in,
      "Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
      "blood", "sangre", "sangue",
      "Circuit-Based Exercise/blood",
      "Ejercicio en Circuitos/sangre",
      "Exercícios em Circuitos/sangue"
    ).toSeq.sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the input string because the term in not in precoded format (^d1213^sblood)" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                  getDescriptor = true,
                                                  getQualifiers = true,
                                                  getJoinDescrQual = true,
                                                  acceptOnlyPrecoded = true)
    val symFact = SynonymFilterFactory(conf2map(conf))
    val in: String = "^dCircuit-Based Exercise/blood"

    stream.close()
    stream.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream)).sorted
    val expected = Seq(in)

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "only return the input text normalized, no descriptor and synonyms should be found - 2" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O céu de hoje está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "do not remove no alphanumeric characters from the prefix/suffix of the tokens" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje, está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the descriptor Matadouros" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the descriptor Matadouros follow by empty /" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros/ está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the word matadouros follow by # but not synonyms and other term fields" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros# está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "matadouros", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the descriptor Matadouros follow by empty space and /" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros / está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the descriptor Matadouros follow by the qualifier sangue" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros/sangue está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "está", "azul",
      "j01.576.423.200.700.100", "abattoirs", "mataderos", "matadouros",
      "abatedouros", "slaughterhouses",
      "q50.040.020q05.010", "blood", "sangre", "sangue",
      "abattoirs/blood", "mataderos/sangre", "matadouros/sangue",
      "abattoirs/bl", "mataderos/bl", "matadouros/bl"
    ).sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the descriptor Matadouros follow by the qualifier in another language sangre" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros/sangre está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "está", "azul",
      "j01.576.423.200.700.100", "abattoirs", "mataderos", "matadouros",
      "abatedouros", "slaughterhouses",
      "q50.040.020q05.010", "blood", "sangre", "sangue",
      "abattoirs/blood", "mataderos/sangre", "matadouros/sangre", "matadouros/sangue",
      "abattoirs/bl", "mataderos/bl", "matadouros/bl"
    ).sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return only descriptor Matadouros because xxx is not a qualifier" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos matadouros/xxx está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "está", "azul",
      "j01.576.423.200.700.100", "abattoirs", "mataderos", "matadouros",
      "abatedouros", "slaughterhouses", "matadouros/xxx"
    ).sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should not return synonyms of Abatedouros because it is a synonym and not a descriptor" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos abatedouros está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "abatedouros", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the descriptor in the precod (^d28631) format but not the precod itself" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos ^d000003 está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "d000003", "de", "hoje", "nos", "j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should return synonyms of the supposed descriptor with ^D format because it is converted to lowercase" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos ^D000003 está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "d000003", "de", "hoje", "nos", "j01.576.423.200.700.100", "abattoirs", "mataderos",
      "matadouros", "abatedouros", "slaughterhouses", "está", "azul").sorted

    assert(areEquals(result, expected))

  //====================================================================================================================

  it should "should not return synonyms of the supposed descriptor with ^000003 format" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos ^000003 está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "nos", "000003", "está", "azul").sorted

    assert(areEquals(result, expected))

  //====================================================================================================================

  it should "should not return synonyms of the supposed descriptor with d000003 format" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje nos d000003 está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Set("o", "céu", "de", "hoje", "nos", "d000003", "está", "azul").toSeq.sorted

    assert(areEquals(result, expected))

  //====================================================================================================================

  it should "should treat a free/isolated comma" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje , está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "está", "azul").sorted

    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "should treat non alphanumerical tokens" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "O [céu] de hoje seu $#%&*, está azul!"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("o", "céu", "de", "hoje", "seu", "está", "azul").sorted

    assert(areEquals(result, expected))
//===================================================================================================================

  it should "should return the input keys fixed" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = " (page  and  electronic  address,  ®Facebook,  ®Instagram,  ®Twitter  and  ®Youtube), "
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("page", "and", "electronic", "address", "facebook", "instagram", "twitter", "and", "youtube").sorted

    assert(areEquals(result, expected))
//====================================================================================================================

  it should "should return the input precoded term and its correlated" in :
    val symFact = SynonymFilterFactory(conf2map(allConf))
    val in = "^d0003"
    stream2.close()
    stream2.setReader(StringReader(in))

    val result: Seq[String] = Tools.getTokenList(symFact.create(stream2)).sorted
    val expected = Seq("d0003", "abatedouros", "abattoirs", "j01.576.423.200.700.100", "mataderos", "matadouros",
      "slaughterhouses").sorted

    assert(areEquals(result, expected))
//====================================================================================================================
