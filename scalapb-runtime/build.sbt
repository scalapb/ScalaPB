sonatypeSettings

name := "scalapb-runtime"

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-1",
  "com.trueaccord.lenses" %% "lenses" % "0.4.1",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "org.scalatest" %% "scalatest" % (if (scalaVersion.value.startsWith("2.12")) "2.2.5-M1" else "2.2.5") % "test"
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "../protobuf"

