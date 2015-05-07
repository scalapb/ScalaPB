sonatypeSettings

name := "scalapb-runtime"

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.0.0-alpha-2",
  "com.trueaccord.lenses" %% "lenses" % "0.4"
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "../protobuf"

