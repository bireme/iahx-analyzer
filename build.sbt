name := "IahxAnalyzer"
version := "2.0"
organization := "org.bireme"

scalaVersion := "3.3.6"
val luceneVersion = "9.12.2"
val scalaTestVersion = "3.2.19"
val mongoVersion = "5.5.1"
//val solrjVersion = "9.6.1"

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % luceneVersion,
  "org.apache.lucene" % "lucene-analysis-common" % luceneVersion,
  "org.apache.lucene" % "lucene-queryparser" % luceneVersion,
  "org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
  "org.mongodb" % "mongodb-driver-sync" % mongoVersion,
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

assemblyJarName := "iahx-analyzer.jar"
