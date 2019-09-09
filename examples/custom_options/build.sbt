import scalapb.compiler.Version.scalapbVersion

scalaVersion := "2.12.9"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies ++= Seq(
  // For finding google/protobuf/descriptor.proto
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"
)

