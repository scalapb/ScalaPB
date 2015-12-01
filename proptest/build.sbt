resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
  )

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.0.0-b1",
  "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-1",
  "io.grpc" % "grpc-all" % "0.9.0" % "test",
  "com.trueaccord.lenses" %% "lenses" % "0.4.1",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "org.scalatest" %% "scalatest" % (if (scalaVersion.value.startsWith("2.12")) "2.2.5-M2" else "2.2.5") % "test"
)

libraryDependencies <+= (scalaVersion) { v => "org.scala-lang" % "scala-compiler" % v }

fork in Test := false
