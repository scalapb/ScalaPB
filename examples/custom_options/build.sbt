import scalapb.compiler.Version.scalapbVersion

scalaVersion := "2.12.10"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)

libraryDependencies ++= Seq(
  // For finding google/protobuf/descriptor.proto
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"
)

