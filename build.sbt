ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "RestApiAssignment"
  )
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.8.5",
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "org.postgresql" % "postgresql" % "42.5.4",

  "joda-time" % "joda-time" % "2.12.5",

  "org.slf4j" % "slf4j-api" % "2.0.5",
  "ch.qos.logback" % "logback-classic" % "1.4.6",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
)
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.8"