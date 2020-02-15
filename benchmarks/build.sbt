val ProtobufJavaVersion = "3.11.1"

val Scala213 = "2.13.1"

val Scala212 = "2.12.10"

lazy val root = project
  .in(file("."))
  .enablePlugins(JmhPlugin)
  .settings(
    skip in publish := true,
    crossScalaVersions := Seq(Scala213, Scala212),
    scalaVersion := Scala213,
    PB.protocVersion in Compile := "-v" + ProtobufJavaVersion,
    PB.targets in Compile := Seq(
      PB.gens.java(ProtobufJavaVersion)   -> (sourceManaged in Compile).value / "protos",
      scalapb.gen(javaConversions = true) -> (sourceManaged in Compile).value / "protos"
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    ),
    sourceGenerators in Compile += Def.task {
      val file    = (Compile / sourceDirectory).value / "templates" / "message.scala.tmpl"
      val content = IO.read(file)
      TestNames.testNames.map { m =>
        val out = (Compile / sourceManaged).value / "gen" / s"${m}Test.scala"
        IO.write(out, content.replace("${Message}", m))
        out
      }
    }
  )
