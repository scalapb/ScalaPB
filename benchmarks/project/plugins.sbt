val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.11.21")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.1.0-RC2")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.8")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % ScalapbVersion

// compilerplugin depends on protoc-gen 0.10.0 (-> protoc-bridge 0.10.0), while
// sbt-protoc 1.1.0-RC2 depends on protoc-bridge 0.9.10. The newer protoc-bridge
// is selected and works with both, so silence the early-semver eviction error.
libraryDependencySchemes += "com.thesamet.scalapb" %% "protoc-bridge" % "always"
