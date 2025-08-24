val ProtobufJavaVersion = "3.11.1"

val Scala213 = "2.13.1"

val Scala212 = "2.12.10"

lazy val root = project
  .in(file("."))
  .enablePlugins(JmhPlugin)
  .settings(
    publish / skip             := true,
    crossScalaVersions         := Seq(Scala213, Scala212),
    scalaVersion               := Scala213,
    Compile / PB.protocVersion := "-v" + ProtobufJavaVersion,
    Compile / PB.targets       := Seq(
      PB.gens.java(ProtobufJavaVersion)   -> (Compile / sourceManaged).value / "protos",
      scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value / "protos"
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    ),
    Compile / sourceGenerators += Def.task {
      val file    = (Compile / sourceDirectory).value / "templates" / "message.scala.tmpl"
      val content = IO.read(file)
      TestNames.testNames.map { m =>
        val out = (Compile / sourceManaged).value / "gen" / s"${m}Test.scala"
        IO.write(out, content.replace("${Message}", m))
        out
      }
    }
  )
