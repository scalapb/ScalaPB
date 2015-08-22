sonatypeSettings

name := "scalapb-runtime"

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.0.0-alpha-3",
  "com.trueaccord.lenses" %% "lenses" % "0.4",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "../protobuf"

