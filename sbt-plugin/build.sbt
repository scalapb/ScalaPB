import SonatypeKeys._

sonatypeSettings

sbtPlugin := true

scalaVersion := "2.10.4"

organization := "com.trueaccord.scalapb"

profileName := "com.trueaccord"

name := "sbt-scalapb"

addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "0.3.3")

crossScalaVersions := Seq("2.10.4")
