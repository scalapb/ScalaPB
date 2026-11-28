addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.1.0-RC3")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.5.0")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" %
  sys.props.getOrElse("scalapb.version", "1.0.0-alpha.7")
