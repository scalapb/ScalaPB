import com.trueaccord.scalapb.compiler.Version.scalapbVersion

scalaVersion := "2.12.2"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies ++= Seq(
  // For finding google/protobuf/descriptor.proto
  "com.trueaccord.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"
)

