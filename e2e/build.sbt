import scalapb.compiler.Version.grpcJavaVersion

crossScalaVersions in ThisBuild := Seq("2.11.12", "2.12.10", "2.13.1")

scalaVersion in ThisBuild := "2.12.10"

val grpcArtifactId = "protoc-gen-grpc-java"

def grpcExeFileName = {
  val os = if (scala.util.Properties.isMac){
    "osx-x86_64"
  } else if (scala.util.Properties.isWin){
    "windows-x86_64"
  } else {
    "linux-x86_64"
  }
  s"${grpcArtifactId}-${grpcJavaVersion}-${os}.exe"
}

lazy val grpcExeUrl =
  url(s"https://repo1.maven.org/maven2/io/grpc/${grpcArtifactId}/${grpcJavaVersion}/${grpcExeFileName}")

val grpcExePath = SettingKey[xsbti.api.Lazy[File]]("grpcExePath")


val commonSettings = Seq(
    scalacOptions in Test ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
      case Some((2, v)) if v >= 11 && v < 12 =>
        Seq("-Ywarn-unused-import")
      case Some((2, v)) if v == 13 =>
        Seq("-Ywarn-unused:imports")
    }.toList.flatten,
    javacOptions ++= Seq("-Xlint:deprecation"),
    PB.protocOptions in Compile ++= Seq(
        s"--plugin=protoc-gen-java_rpc=${grpcExePath.value.get}",
    ),
    unmanagedSourceDirectories in Compile ++= {
      val base = (baseDirectory in Compile).value / "src" / "main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v < 13 =>
          Seq(base / "scala-pre-2.13")
        case _ =>
          Nil
      }
    },
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "io.grpc" % "grpc-netty" % grpcJavaVersion, //netty transport of grpc
      "io.grpc" % "grpc-protobuf" % grpcJavaVersion, //protobuf message encoding for java implementation
      "io.grpc" % "grpc-services" % grpcJavaVersion,
      "io.grpc" % "grpc-services" % grpcJavaVersion % "protobuf",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.Version.scalapbVersion % "protobuf",
      "javax.annotation" % "javax.annotation-api" % "1.3.2",  // needed for grpc-java on JDK9
    ),

    grpcExePath := xsbti.api.SafeLazyProxy {
      val exe: File = (baseDirectory in ThisBuild).value / ".bin" / grpcExeFileName
      if (!exe.exists) {
        import scala.sys.process._

        exe.getParentFile().mkdirs()
        println("grpc protoc plugin (for Java) does not exist. Downloading.")

        grpcExeUrl #> (exe) !

        exe.setExecutable(true)
      } else {
        println("grpc protoc plugin (for Java) exists.")
      }
      exe
    },

    fork in Test := true,  // For https://github.com/scala/bug/issues/9237
  )

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    PB.protoSources in Compile += (PB.externalIncludePath in Compile).value / "grpc" / "reflection",
    PB.protocOptions in Compile ++= Seq(
        s"--java_rpc_out=${((sourceManaged in Compile).value).getAbsolutePath}"
    ),
    PB.targets in Compile := Seq(
      PB.gens.java -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions = true) -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.Version.scalapbVersion
    ))

lazy val noJava = (project in file("nojava"))
  .settings(commonSettings)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.Version.scalapbVersion % "protobuf"
    )
  )

// addCompilerPlugin(scalafixSemanticdb)
