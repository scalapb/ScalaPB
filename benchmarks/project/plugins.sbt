val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.10.0-SNAPSHOT")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.27")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % ScalapbVersion
