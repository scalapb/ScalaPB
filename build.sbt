val languageVersion = "2.11.2"

scalaVersion := languageVersion

resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
  )

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.scala-lang" % "scala-compiler" % languageVersion,
  "com.google.protobuf" % "protobuf-java" % "2.5.0"
)

fork in Test := true
