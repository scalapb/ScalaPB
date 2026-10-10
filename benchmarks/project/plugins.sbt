val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.10.0-SNAPSHOT")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.8")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % ScalapbVersion
