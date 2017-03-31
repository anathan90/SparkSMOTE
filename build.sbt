name := "SMOTE"

version := "1.2"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.3.0"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.3.0"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

resolvers ++= Seq("Akka Repository" at "http://repo.akka.io/releases/")

resolvers ++= Seq("Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases")
