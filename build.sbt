resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
  )

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test"
)


fork in Test := true

