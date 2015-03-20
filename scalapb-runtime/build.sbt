sonatypeSettings

name := "scalapb-runtime"

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "com.trueaccord.lenses" %% "lenses" % "0.3",
  "org.parboiled" %% "parboiled" % "2.1.0"
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "../protobuf"

