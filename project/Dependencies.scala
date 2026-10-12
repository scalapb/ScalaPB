import sbt.*
import Keys.*

object Dependencies {
  object versions {
    val grpc                 = "1.84.2"
    val protobuf             = "4.35.0"
    val collectionCompat     = "2.14.0"
    val coursier             = "2.1.24"
    val protocGen            = "0.10.0"
    val protobufRuntimeScala = "0.9.0"
    val commonsCodec         = "1.22.1"

    // For testing
    val annotationApi           = "1.3.2"
    val cats                    = "2.6.1"
    val mockito                 = "5.24.0"
    val munit                   = "1.3.6"
    val munitScalaCheck         = "1.3.1"
    val scalaTest               = "3.2.20"
    val scalaTestPlusMockito    = "3.1.0.0"
    val scalaTestPlusScalaCheck = "3.2.20.0"
    val guava                   = "33.7.2-jre"
  }

  val Scala212 = "2.12.21"

  val Scala213 = "2.13.18"

  val Scala3 = "3.3.8"

  val protobufJava          = "com.google.protobuf" % "protobuf-java" % versions.protobuf
  val scalaCollectionCompat = Def.setting {
    "org.scala-lang.modules" %% "scala-collection-compat" % versions.collectionCompat
  }
  val protobufRuntimeScala = Def.setting {
    "com.thesamet.scalapb" %% "protobuf-runtime-scala" % versions.protobufRuntimeScala
  }

  private val exclRule =
    ExclusionRule(organization = "org.scala-lang.modules") // Exclude scala-xml cross-version

  val coursier  = "io.get-coursier"       %% "coursier"   % versions.coursier
  val protocGen = ("com.thesamet.scalapb" %% "protoc-gen" % versions.protocGen).excludeAll(exclRule)
  val protocCacheCoursier =
    // Coursier must retain its scala-xml dependency: newer Coursier releases use
    // an XML ABI that the older ScalaTest-provided version does not implement.
    "com.thesamet.scalapb" %% "protoc-cache-coursier" % versions.protocGen
  val protobufJavaUtil = "com.google.protobuf" % "protobuf-java-util" % versions.protobuf

  // grpc
  val grpcStub      = "io.grpc" % "grpc-stub"            % versions.grpc
  val grpcProtobuf  = "io.grpc" % "grpc-protobuf"        % versions.grpc
  val grpcNetty     = "io.grpc" % "grpc-netty"           % versions.grpc
  val grpcServices  = "io.grpc" % "grpc-services"        % versions.grpc
  val grpcProtocGen = "io.grpc" % "protoc-gen-grpc-java" % versions.grpc
  val grpcInprocess = "io.grpc" % "grpc-inprocess"       % versions.grpc

  // Keep the Netty modules selected by gRPC on the same patched release.
  val grpcNettyOverrides = Seq(
    "netty-buffer",
    "netty-codec-base",
    "netty-codec-compression",
    "netty-codec-http",
    "netty-codec-http2",
    "netty-codec-socks",
    "netty-common",
    "netty-handler",
    "netty-handler-proxy",
    "netty-resolver",
    "netty-transport",
    "netty-transport-native-unix-common"
  ).map("io.netty" % _ % "4.2.19.Final")

  // Keep the CLI POM safe as well as the local build. Coursier 2.1.24 is the
  // supported cross-published version for Scala 2.12 and 2.13.
  val coursierSecurityDependencies = Seq(
    "io.airlift"          % "aircompressor" % "2.0.3",
    "org.apache.commons"  % "commons-lang3" % "3.18.0",
    "org.codehaus.plexus" % "plexus-utils"  % "4.0.3",
    "org.jsoup"           % "jsoup"         % "1.23.2"
  )

  // Build/test tools also bring these libraries transitively. Overrides do not
  // introduce JVM dependencies into the JS or Native projects.
  val buildSecurityOverrides = coursierSecurityDependencies ++ Seq(
    "com.fasterxml.jackson.core" % "jackson-annotations" % "2.22",
    "com.fasterxml.jackson.core" % "jackson-core"        % "2.18.11",
    "com.fasterxml.jackson.core" % "jackson-databind"    % "2.18.11"
  ) ++ Seq(
    "jline-reader",
    "jline-terminal",
    "jline-terminal-jna",
    "jline-terminal-jni",
    "jline-native"
  )
    .map("org.jline" % _ % "3.30.17")

  // Spark is only used to execute documentation examples. Its transitive versions
  // lag the security fixes, so select compatible patched families here.
  val docsSecurityDependencies = Seq(
    "io.undertow"                    % "undertow-core"           % "2.4.4.Final",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"    % "2.18.11",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.18.11",
    "com.google.guava"               % "guava"                   % versions.guava,
    "io.airlift"                     % "aircompressor"           % "2.0.3",
    "io.netty"                       % "netty-all"               % "4.1.139.Final",
    "org.apache.commons"             % "commons-compress"        % "1.28.0",
    "org.apache.commons"             % "commons-lang3"           % "3.18.0",
    "org.apache.logging.log4j"       % "log4j-1.2-api"           % "2.25.5",
    "org.apache.logging.log4j"       % "log4j-slf4j2-impl"       % "2.25.5",
    "org.apache.zookeeper"           % "zookeeper"               % "3.9.6",
    "at.yawk.lz4"                    % "lz4-java"                % "1.11.4"
  )

  // testing
  val scalaTest               = Def.setting { "org.scalatest" %% "scalatest" % versions.scalaTest }
  val scalaTestPlusScalaCheck = Def.setting {
    "org.scalatestplus" %% "scalacheck-1-19" % versions.scalaTestPlusScalaCheck
  }
  val scalaTestPlusMockito = "org.scalatestplus" %% "mockito-1-10" % versions.scalaTestPlusMockito
  val munit                = Def.setting { "org.scalameta" %% "munit" % versions.munit }
  val munitScalaCheck      = Def.setting {
    "org.scalameta" %% "munit-scalacheck" % versions.munitScalaCheck
  }
  val mockitoCore  = "org.mockito"      % "mockito-core"  % versions.mockito
  val commonsCodec = "commons-codec"    % "commons-codec" % versions.commonsCodec
  val cats         = "org.typelevel"   %% "cats-core"     % versions.cats
  val guava        = "com.google.guava" % "guava"         % versions.guava

  val annotationApi =
    "javax.annotation" % "javax.annotation-api" % versions.annotationApi // needed for grpc-java on JDK9
}
