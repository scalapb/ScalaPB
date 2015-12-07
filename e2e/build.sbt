import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings

PB.scalapbVersion in PB.protobufConfig := com.trueaccord.scalapb.Version.scalapbVersion

PB.javaConversions in PB.protobufConfig := true

PB.runProtoc in PB.protobufConfig := { args0 =>
  val args = args0 ++ Array(
    s"--plugin=protoc-gen-java_rpc=${grpcExePath.value.get}",
    s"--java_rpc_out=${((sourceManaged in Compile).value / "compiled_protobuf").getAbsolutePath}"
  )
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
}

val grpcVersion = "0.9.0"

val grpcArtifactId = "protoc-gen-grpc-java"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "io.grpc" % "grpc-all" % grpcVersion,
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.Version.scalapbVersion,
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.Version.scalapbVersion % PB.protobufConfig
)

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

grpcExePath := xsbti.SafeLazy {
  val exe: File = baseDirectory.value / ".bin" / grpcExeFileName
  if (!exe.exists) {
    println("grpc protoc plugin (for Java) does not exist. Downloading.")
    IO.download(grpcExeUrl, exe)
    exe.setExecutable(true)
  } else {
    println("grpc protoc plugin (for Java) exists.")
  }
  exe
}

// TODO add `grpc: SettingKey[Boolean]` to sbt-scalapb
PB.protocOptions in PB.protobufConfig := {
  val conf = (PB.generatedTargets in PB.protobufConfig).value
  val scalaOpts = conf.find(_._2.endsWith(".scala")) match {
    case Some(targetForScala) =>
      Seq(s"--scala_out=grpc,java_conversions:${targetForScala._1.absolutePath}")
    case None =>
      Nil
  }
  val javaOpts = conf.find(_._2.endsWith(".java")) match {
    case Some(targetForJava) =>
      Seq(s"--java_out=${targetForJava._1.absolutePath}")
    case None =>
      Nil
  }
  scalaOpts ++ javaOpts
}
