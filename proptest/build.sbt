resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
  )

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "com.trueaccord.lenses" %% "lenses" % "0.3",
  "org.scalacheck" %% "scalacheck" % "1.11.6" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.github.os72" % "protoc-jar" % "3.0.0-a3"
)

libraryDependencies <+= (scalaVersion) { v => "org.scala-lang" % "scala-compiler" % v }

fork in Test := false
