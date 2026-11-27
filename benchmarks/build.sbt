val ProtobufJavaVersion = "4.32.0"

val Scala213 = "2.13.18"

val Scala212 = "2.12.21"

val Scala3 = "3.3.9"

lazy val root = project
  .in(file("."))
  .enablePlugins(JmhPlugin)
  .settings(
    publish / skip     := true,
    crossScalaVersions := Seq(Scala213, Scala212, Scala3),
    scalaVersion       := Scala213,
    // Plain version form (no "-v" prefix): sbt-protoc 1.x strips the legacy
    // prefix with a deprecation warning and this build needs protoc >= 4.x
    // anyway, so keep it explicit.
    Compile / PB.protocVersion := ProtobufJavaVersion,
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
      // Messages living in a proto file with a non-default java_outer_classname
      // (e.g. the proto2 no-validation file) map to their own outer class.
      val javaOuterClassnames = Map("LazyFieldsNoValidation" -> "ProtosNoValidation")
      TestNames.testNames.map { m =>
        val out = (Compile / sourceManaged).value / "gen" / s"${m}Test.scala"
        IO.write(
          out,
          content
            .replace("${Message}", m)
            .replace("${JavaOuter}", javaOuterClassnames.getOrElse(m, "Protos"))
        )
        out
      }
    }
  )
