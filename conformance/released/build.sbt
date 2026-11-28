// Standalone verification: use published generator/runtime artifacts, not the parent build.
scalaVersion := "2.13.18"
name := "scalapb-released-conformance"
publish / skip := true
val scalapbVersion = sys.props.getOrElse("scalapb.version", "1.0.0-alpha.7")
val jsonVersion = sys.props.getOrElse("scalapb.json4s.version", "1.0.0-alpha.4")
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-json4s" % jsonVersion,
  "com.google.protobuf" % "protobuf-java" % "4.36.2" % "protobuf",
  "org.scalameta" %% "munit" % "1.3.1" % Test,
  "org.scalatest" %% "scalatest" % "3.2.20" % Test
)
Compile / unmanagedSourceDirectories := Seq(baseDirectory.value / ".." / "src" / "main" / "scala")
Test / unmanagedSourceDirectories := Seq(
  baseDirectory.value / ".." / "src" / "test" / "scala",
  baseDirectory.value / ".." / ".." / "e2e-json" / "src" / "test" / "scala"
)
Compile / PB.protoSources := Seq(
  baseDirectory.value / ".." / "src" / "main" / "protobuf",
  baseDirectory.value / ".." / "upstream" / "editions",
  baseDirectory.value / ".." / ".." / "e2e-json" / "src" / "main" / "protobuf"
)
Compile / PB.targets := Seq(
  scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value,
  PB.gens.java -> (Compile / sourceManaged).value
)
PB.protocVersion := "4.36.2"
Compile / mainClass := Some("scalapb.ConformanceScala")
assembly / assemblyJarName := "conformance.jar"
assembly / assemblyMergeStrategy := {
  case x if x.endsWith("module-info.class") => MergeStrategy.discard
  case x => (assembly / assemblyMergeStrategy).value(x)
}
