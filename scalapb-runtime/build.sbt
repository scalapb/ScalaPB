sonatypeSettings

name := "scalapb-runtime"

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "com.trueaccord.lenses" %% "lenses" % "0.4.1"
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "../protobuf"

