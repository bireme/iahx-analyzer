package comparison

import org.bireme.dia.analysis.DeCSSimpleAnalyzer
import org.bireme.dia.tools.Tools

import scala.collection.immutable.TreeSet

object AnalyzeText:
  @main def run1(): Unit =
    //val text: String = "^d000003"
    val text: String = "!@#$AxzdfgsdgfBWertwertX1234现从临床并发症的角度对肝硬化再代偿进行探讨|用于描述失代偿期肝硬化患者病情整体逆转的临床阶;"
    val analyzer: DeCSSimpleAnalyzer = DeCSSimpleAnalyzer("resources/decs/main")
    val output: TreeSet[String] = TreeSet.from(Tools.getTokenList(text, analyzer))
    val outStr: String = output.mkString("|")

    println(s"len=${text.length}   text=$text")
    println(s"len=${outStr.length} output=$outStr")
    output.foreach(out => println(s"len=${out.length}    out=$out"))