val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.12.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.0")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.9")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.1")

addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")

addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.8")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.9.0")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.3.6")

addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.2")
