val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.20.1")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.7")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.10")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")

addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.6.1")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.8")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")

addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.8")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.11.0")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.8.2")

addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.4")
