scalaVersion := "2.11.8"

val grpcVersion = "1.0.1"

val grpcArtifactId = "protoc-gen-grpc-java"

def grpcExeFileName = {
  val os = if (scala.util.Properties.isMac){
    "osx-x86_64"
  } else if (scala.util.Properties.isWin){
    "windows-x86_64"
  } else {
    "linux-x86_64"
  }
  s"${grpcArtifactId}-${grpcVersion}-${os}.exe"
}

lazy val grpcExeUrl =
  url(s"http://repo1.maven.org/maven2/io/grpc/${grpcArtifactId}/${grpcVersion}/${grpcExeFileName}")

val grpcExePath = SettingKey[xsbti.api.Lazy[File]]("grpcExePath")


val commonSettings = Seq(
    scalacOptions ++= Seq("-deprecation"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    PB.protocOptions in Compile ++= Seq(
        s"--plugin=protoc-gen-java_rpc=${grpcExePath.value.get}",
        s"--java_rpc_out=${((sourceManaged in Compile).value).getAbsolutePath}"
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.1" % "test",
      "io.grpc" % "grpc-netty" % grpcVersion, //netty transport of grpc
      "io.grpc" % "grpc-protobuf" % grpcVersion, //protobuf message encoding for java implementation
      "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
      "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.Version.scalapbVersion % "protobuf",
      "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.3"
    ),
    grpcExePath := xsbti.SafeLazy {
      val exe: File = (baseDirectory in ThisBuild).value / ".bin" / grpcExeFileName
      if (!exe.exists) {
        println("grpc protoc plugin (for Java) does not exist. Downloading.")
        IO.download(grpcExeUrl, exe)
        exe.setExecutable(true)
      } else {
        println("grpc protoc plugin (for Java) exists.")
      }
      exe
    })

val collectionType: String = sys.props.getOrElse("collectionType", "scala.collection.Seq")

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    PB.targets in Compile := Seq(
      PB.gens.java -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions = true, collectionType = collectionType) -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.Version.scalapbVersion
    ))

lazy val noJava = (project in file("nojava"))
  .settings(commonSettings)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )
