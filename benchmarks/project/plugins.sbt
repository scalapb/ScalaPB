val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.11.20")
//val ScalapbVersion = sys.env.getOrElse("SCALAPB", "0.0.0+2729-a8a88986+20260227-2252-SNAPSHOT")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.33")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % ScalapbVersion
