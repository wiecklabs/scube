name := "scube"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.1"

scalacOptions in ThisBuild ++= Seq(
    "-language:_",
    "-feature",
    "-unchecked",
    "-deprecation")

resolvers ++= Seq(
    "Maven Central" at "http://repo1.maven.org/maven2",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

testOptions in Test := Nil

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.2" % "test",
  "org.joda" % "joda-convert" % "1.2" % "test",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "ch.qos.logback" % "logback-classic" % "1.0.1",
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  "com.typesafe" % "config" % "1.0.0")
