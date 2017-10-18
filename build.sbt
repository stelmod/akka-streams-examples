name := "akka-streams-example"
organization := "com.stelmod"
scalaVersion := "2.12.3"
version      := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.6",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.6" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)
