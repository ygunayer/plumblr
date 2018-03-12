name := "plumblr"
organization := "com.yalingunayer"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.9"
lazy val scalaTestVersion = "3.0.4"
lazy val asyncHttpClientVersion = "2.4.2"
lazy val java8CompatVersion = "0.8.0"
lazy val sprayVersion = "1.3.4"
lazy val akkaHttpVersion = "10.0.11"
lazy val progressBarVersion = "0.6.0"

libraryDependencies ++= Seq(
  "me.tongfei" % "progressbar" % progressBarVersion,
  "io.spray" %% "spray-json" % sprayVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.asynchttpclient" % "async-http-client" % asyncHttpClientVersion,
  "org.scala-lang.modules" %% "scala-java8-compat" % java8CompatVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)
