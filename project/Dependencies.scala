import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import Keys._

object Dependencies {
  object versions {
    val grpc                 = "1.64.0"
    val protobuf             = "4.26.0"
    val silencer             = "1.7.17"
    val collectionCompat     = "2.12.0"
    val coursier             = "2.1.10"
    val protocGen            = "0.9.7"
    val protobufRuntimeScala = "0.8.15"
    val commonsCodec         = "1.17.0"

    // For testing
    val annotationApi           = "1.3.2"
    val cats                    = "2.6.1"
    val mockito                 = "5.12.0"
    val munit                   = "1.0.0"
    val scalaTest               = "3.2.18"
    val scalaTestPlusMockito    = "3.1.0.0"
    val scalaTestPlusScalaCheck = "3.2.18.0"
    val utest                   = "0.7.10"
  }

  val Scala212 = "2.12.19"

  val Scala213 = "2.13.13"

  val Scala3 = "3.3.3"

  val silencer = Seq(
    sbt.compilerPlugin(
      "com.github.ghik" % "silencer-plugin" % versions.silencer cross CrossVersion.full
    ),
    "com.github.ghik" % "silencer-lib" % versions.silencer % Provided cross CrossVersion.full
  )

  val protobufJava = "com.google.protobuf" % "protobuf-java" % versions.protobuf
  val scalaCollectionCompat = Def.setting {
    "org.scala-lang.modules" %%% "scala-collection-compat" % versions.collectionCompat
  }
  val protobufRuntimeScala = Def.setting {
    "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % versions.protobufRuntimeScala
  }

  private val exclRule =
    ExclusionRule(organization = "org.scala-lang.modules") // Exclude scala-xml cross-version

  val coursier  = "io.get-coursier"       %% "coursier"   % versions.coursier
  val protocGen = ("com.thesamet.scalapb" %% "protoc-gen" % versions.protocGen).excludeAll(exclRule)
  val protocCacheCoursier =
    ("com.thesamet.scalapb" %% "protoc-cache-coursier" % versions.protocGen).excludeAll(exclRule)
  val protobufJavaUtil = "com.google.protobuf" % "protobuf-java-util" % versions.protobuf

  // grpc
  val grpcStub      = "io.grpc" % "grpc-stub"            % versions.grpc
  val grpcProtobuf  = "io.grpc" % "grpc-protobuf"        % versions.grpc
  val grpcNetty     = "io.grpc" % "grpc-netty"           % versions.grpc
  val grpcServices  = "io.grpc" % "grpc-services"        % versions.grpc
  val grpcProtocGen = "io.grpc" % "protoc-gen-grpc-java" % versions.grpc
  val grpcInprocess = "io.grpc" % "grpc-inprocess"       % versions.grpc

  // testing
  val scalaTest = Def.setting { "org.scalatest" %%% "scalatest" % versions.scalaTest }
  val scalaTestPlusScalaCheck = Def.setting {
    "org.scalatestplus" %%% "scalacheck-1-17" % versions.scalaTestPlusScalaCheck
  }
  val scalaTestPlusMockito = "org.scalatestplus" %% "mockito-1-10"  % versions.scalaTestPlusMockito
  val utest                = Def.setting { "com.lihaoyi" %%% "utest" % versions.utest }
  val munit                = Def.setting { "org.scalameta" %%% "munit" % versions.munit }
  val munitScalaCheck      = Def.setting { "org.scalameta" %%% "munit-scalacheck" % versions.munit }
  val mockitoCore          = "org.mockito"        % "mockito-core"  % versions.mockito
  val commonsCodec         = "commons-codec"      % "commons-codec" % versions.commonsCodec
  val cats                 = "org.typelevel"     %% "cats-core"     % versions.cats

  val annotationApi =
    "javax.annotation" % "javax.annotation-api" % versions.annotationApi // needed for grpc-java on JDK9
}
