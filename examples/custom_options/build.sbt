import com.trueaccord.scalapb.compiler.Version.scalapbVersion

scalaVersion := "2.12.7"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies ++= Seq(
  // For finding google/protobuf/descriptor.proto
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
  "org.typelevel" %% "cats-effect" % "1.0.0"
)

