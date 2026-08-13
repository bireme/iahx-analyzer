name := "IahxAnalyzer"
version := "2.0"
organization := "org.bireme"

scalaVersion := "3.3.8"
val luceneVersion = "9.12.2"
val scalaTestVersion = "3.2.19"
val mongoVersion = "5.9.0" //"5.6.1"
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
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", "versions", _*) => MergeStrategy.first
  case PathList("META-INF", files @ _*) =>
    files.map(_.toLowerCase) match {
      case "manifest.mf" :: Nil => MergeStrategy.discard
      case file :: Nil if file.endsWith(".sf") || file.endsWith(".rsa") || file.endsWith(".dsa") =>
        MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  case _ => MergeStrategy.first
}

Compile / packageOptions += Package.ManifestAttributes("Multi-Release" -> "true")

scalacOptions ++= Seq(          // use ++= to add to existing options
  "-encoding", "utf8",          // if an option takes an arg, supply it on the same line
  "-feature",                   // then put the next option on a new line for easy editing
  "-unchecked",
  "-Werror",
  "-deprecation",
  "-Wunused:all",
  "-Wvalue-discard"
)

assemblyJarName := "iahx-analyzer.jar"
