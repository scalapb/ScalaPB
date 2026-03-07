val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.0.0+2740-4e32bc69+20260305-2347-SNAPSHOT")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.33")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % ScalapbVersion
