val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.11.21")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.1.0-RC2")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.8")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % ScalapbVersion
