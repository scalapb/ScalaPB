val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.15.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.17")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.3")

addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")

addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.8")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.10.0")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.5.2")

addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.4")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.7")

libraryDependencies += {
  val javaVersion = sys.props("java.version")
  val oshiVersion = "6.4.13"
  if (javaVersion.startsWith("1.8")) {
    "com.github.oshi" % "oshi-core" % oshiVersion
  } else {
    "com.github.oshi" % "oshi-core-java11" % oshiVersion
  }
}
