name := "IahxAnalyzer"
version := "2.0"
organization := "org.bireme"

scalaVersion := /*"3.4.2"*/ "3.5.0"
val luceneVersion = /*"9.10.0"*/ "9.7.0"//"9.11.1"
val scalaTestVersion = /*"3.2.18"*/ "3.2.19"
//val solrjVersion = "9.6.1"

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % luceneVersion,
  "org.apache.lucene" % "lucene-analysis-common" % luceneVersion,
  "org.apache.lucene" % "lucene-queryparser" % luceneVersion,
  "org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest-flatspec" % scalaTestVersion % "test",
  //"org.apache.solr" % "solr-solrj" % solrjVersion
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

scalacOptions ++= Seq(          // use ++= to add to existing options
  "-encoding", "utf8",          // if an option takes an arg, supply it on the same line
  "-feature",                   // then put the next option on a new line for easy editing
  "-language:implicitConversions",
  "-language:existentials",
  "-unchecked",
  "-Werror",
  "-deprecation"
)