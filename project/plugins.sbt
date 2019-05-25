val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.27")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.21")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0")

//addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.3.9")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.6.0-RC4")

addSbtPlugin("com.47deg"  % "sbt-microsites" % "0.9.0")
