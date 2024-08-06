package org.bireme.dia.synonyms

import org.scalatest.flatspec.AnyFlatSpec
import scala.language.postfixOps

class DeCSSynonymsTest extends AnyFlatSpec:
  private val deCSPath = "resources/decs/main"

  private def areEquals(str1: Seq[String],
                        str2: Seq[String]): Boolean =
    str1.nonEmpty && str2.nonEmpty && str1.length == str2.length &&
      str1.zip(str2).forall((a, b) => a `equals` b)
  
  "The DeCS Synonym engine" should  "return the input key because the input key is not a DeCS term" in:
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath  = deCSPath,
                                                 getDescriptor = true)
      val sym: DeCSSynonyms =  DeCSSynonyms(conf)
      val result: Seq[String] = sym.getSynonyms("xxxx").toSeq.sorted
      val expected = Set("xxxx").toSeq

      sym.close()
      assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors as they are in the index, i.e., not breaking it neither changing its characters case (upper/lower)" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
                       "Ejercicio en Circuitos",
                       "Exercícios em Circuitos").toSeq.sorted

    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath  = deCSPath,
                                                 getDescriptor = true,
                                                 getSynonyms   = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("Circuit-Based Exercise","Ejercicio en Circuitos","Exercícios em Circuitos",
      "Exercises, Circuit-Based","Ejercicio Basado en Circuito","Exercícios Baseados em Circuitos").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term and also split them in words" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getSynonyms = true,
                                                 splitWords = true,
                                                 fixPrefixSuffix = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("Circuit-Based Exercise", "Circuit-Based", "Exercise",
      "Ejercicio en Circuitos", "Ejercicio", "en", "Circuitos",
      "Exercícios em Circuitos", "Exercícios", "em",
      "Exercises, Circuit-Based", "Exercises",
      "Ejercicio Basado en Circuito", "Basado", "Circuito",
      "Exercícios Baseados em Circuitos", "Baseados").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term and also split them in words and in hifen" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getSynonyms = true,
                                                 splitWords = true,
                                                 splitHifen = true,
                                                 fixPrefixSuffix = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("Circuit-Based Exercise", "Circuit-Based", "Exercise", "Circuit", "Based",
      "Ejercicio en Circuitos", "Ejercicio", "en", "Circuitos",
      "Exercícios em Circuitos", "Exercícios", "em",
      "Exercises, Circuit-Based", "Exercises",
      "Ejercicio Basado en Circuito", "Basado", "Circuito",
      "Exercícios Baseados em Circuitos", "Baseados").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term but not split them in hifen as splitHifen is false" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getSynonyms = true,
                                                 splitHifen = false)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set[String]("Circuit-Based Exercise","Ejercicio en Circuitos","Exercícios em Circuitos",
      "Exercises, Circuit-Based","Ejercicio Basado en Circuito","Exercícios Baseados em Circuitos").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and synonyms of the term and also split them in words and in hifen. Finally put all in lower case" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getSynonyms = true,
                                                 splitWords = true,
                                                 splitHifen = true,
                                                 toLowerCase = true,
                                                 fixPrefixSuffix = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("circuit-based exercise", "circuit-based", "exercise", "circuit", "based",
      "ejercicio en circuitos", "ejercicio", "en", "circuitos",
      "exercícios em circuitos", "exercícios", "em",
      "exercises, circuit-based", "exercises",
      "ejercicio basado en circuito", "basado", "circuito",
      "exercícios baseados em circuitos", "baseados").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors and category of the term" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getCategory = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
                       "Ejercicio en Circuitos",
                       "Exercícios em Circuitos",
                       "G11.427.590.530.698.277.061").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors,synonyms and category of the term and also split them in words and in hifen. Finally put all in lower case" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getCategory = true,
                                                 getSynonyms = true,
                                                 splitWords = true,
                                                 splitHifen = true,
                                                 toLowerCase = true,
                                                 fixPrefixSuffix = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise").toSeq.sorted
    val expected = Set("circuit-based exercise", "circuit-based", "exercise", "circuit", "based",
      "ejercicio en circuitos", "ejercicio", "en", "circuitos",
      "exercícios em circuitos", "exercícios", "em",
      "exercises, circuit-based", "exercises",
      "ejercicio basado en circuito", "basado", "circuito",
      "exercícios baseados em circuitos", "baseados",
      "g11.427.590.530.698.277.061").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors and qualifiers and abbreviation in the form descriptor/qualifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 getAbbreviation = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/blood").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
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
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors and qualifiers in the form descriptor<space>/<space>qualifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise / blood").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors and qualifiers in the form descriptor/<space>qualifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/ blood").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors, qualifiers and abbreviation in the form descriptor<space>/qualifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 getAbbreviation = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise /blood").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
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
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors but ot the qualifiers because the qualifier flag is set to false" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/blood").toSeq.sorted
    val expected = Set("Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors because the qualifier flag is set to false" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise /blood").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors, qualifiers and abbreviation int the form ^ddescriptor^squalifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 getAbbreviation = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^d000009^s022062").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
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
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors, qualifiers and abbreviation int the form ^ddescriptor<space>^squalifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 getAbbreviation = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^d000009  ^s022062").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
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
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get descriptors and qualifiers int the form ^ddescriptor<space>^squalifier and abbreviation" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 getAbbreviation = true,
                                                 splitWords = true,
                                                 splitHifen = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^d000009  ^s022062").toSeq.sorted
    val expected = Set(
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
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only descriptors but not the qualifiers because they do not exist" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^d000009 ^s000999").toSeq.sorted
    val expected = Set("Circuit-Based Exercise",
      "Ejercicio en Circuitos",
      "Exercícios em Circuitos",
    ).toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the original string because the qualifier is not numeric" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
      getDescriptor = true,
      getQualifiers = true,
      getJoinDescrQual = true)
    val sym: DeCSSynonyms = DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^d000009 ^sxxxx").toSeq.sorted
    val expected = Set("^d000009 ^sxxxx").toSeq.sorted

    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "return the original string because the descriptor is not numeric" in :
    val conf: DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
      getDescriptor = true,
      getQualifiers = true,
      getJoinDescrQual = true)
    val sym: DeCSSynonyms = DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^dxxxxx ^s0001").toSeq.sorted
    val expected = Set("^dxxxxx ^s0001").toSeq.sorted

    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get only qualifiers and abbreviation in the form ^squalifier" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getQualifiers = true,
                                                 getAbbreviation = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^s022062").toSeq.sorted
    val expected = Set("blood", "sangre", "sangue").toSeq.sorted // Obs: the abbreviation alone never appears, only with descriptor
    
    sym.close()
    assert(areEquals(result, expected))
  //====================================================================================================================

  it should "get the descriptors, synonyms, qualifiers and category of the term and also split them in words and in hifen. Finally put all in lower case" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 getCategory = true,
                                                 getSynonyms = true,
                                                 getAbbreviation = true,
                                                 splitWords = true,
                                                 splitHifen = true,
                                                 toLowerCase = true,
                                                 fixPrefixSuffix = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/blood").toSeq.sorted
    val expected = Set("circuit-based exercise", "circuit-based", "exercise", "circuit", "based",
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
    
    sym.close()
    assert(areEquals(result, expected))

  //====================================================================================================================
  it should "get the descriptors and synonyms of Circuit-Based Exercise/" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/").toSeq.sorted
    val expected = Set("Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
//=====================================================================================================================

  it should "get the descriptors and synonyms of Circuit-Based Exercise /" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise /").toSeq.sorted
    val expected = Set("Circuit-Based Exercise", "Ejercicio en Circuitos", "Exercícios em Circuitos").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
//=====================================================================================================================

  it should "get the descriptors and synonyms of the qualifier /blood" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 toLowerCase = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("/blood").toSeq.sorted
    val expected = Set("blood", "sangre", "sangue").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
//=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and getCategory is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                   getDescriptor = false,
                                                   getCategory = true)
      val sym: DeCSSynonyms = DeCSSynonyms(conf)
 //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and getSynonyms is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                   getDescriptor = false,
                                                   getSynonyms = true)
      val sym: DeCSSynonyms = DeCSSynonyms(conf)
 //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and splitWords is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                   getDescriptor = false,
                                                   splitWords = true)
      val sym: DeCSSynonyms = DeCSSynonyms(conf)
 //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getDescriptor is false and splitHifen is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                   getDescriptor = false,
                                                   splitHifen = true)
      val sym: DeCSSynonyms = DeCSSynonyms(conf)
 //=====================================================================================================================

  it should "throw IllegalArgumentException exception as getQualifiers is false and getAbbreviation is set to true" in :
    assertThrows[IllegalArgumentException]:
      val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                   getDescriptor = true,
                                                   getQualifiers = false,
                                                   getAbbreviation = true)
      val sym: DeCSSynonyms = DeCSSynonyms(conf)
 //=====================================================================================================================

  it should "get only the qualifier /blood" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getQualifiers = true,
                                                 toLowerCase = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/blood").toSeq.sorted
    val expected = Set("blood", "sangre", "sangue").toSeq.sorted
    
    sym.close()
    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "return the original string because the term in not in precoded format (^d1213)" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 acceptOnlyPrecoded = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("Circuit-Based Exercise/blood").toSeq.sorted
    val expected = Seq("Circuit-Based Exercise/blood")
    
    sym.close()
    assert(areEquals(result, expected))
  //=====================================================================================================================

  it should "get the original string because it is not in precoded format (^d00001^s00044)" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 getJoinDescrQual = true,
                                                 acceptOnlyPrecoded = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^dCircuit-Based Exercise^sblood").toSeq.sorted
    val expected = Seq("^dCircuit-Based Exercise^sblood")
    sym.close()
    assert(areEquals(result, expected))
//====================================================================================================================

  it should "return the input string because it is not in precoded format (^d1213^s3456)" in :
    val conf:DeCSSynonymsConf = DeCSSynonymsConf(indexPath = deCSPath,
                                                 getDescriptor = true,
                                                 getQualifiers = true,
                                                 acceptOnlyPrecoded = true)
    val sym: DeCSSynonyms =  DeCSSynonyms(conf)
    val result: Seq[String] = sym.getSynonyms("^dCircuit-Based Exercise/blood").toSeq.sorted
    val expected = Seq("^dCircuit-Based Exercise/blood")
    
    sym.close()
    assert(areEquals(result, expected))
//====================================================================================================================