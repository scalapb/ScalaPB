val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

//addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.0")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.1")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.0")

addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.5")

addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.6")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.7.0")

addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.5.0")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.16")

// For https://github.com/lightbend/mima/issues/422
resolvers += Resolver.url(
  "typesafe sbt-plugins",
  url("https://dl.bintray.com/typesafe/sbt-plugins")
)(Resolver.ivyStylePatterns)
