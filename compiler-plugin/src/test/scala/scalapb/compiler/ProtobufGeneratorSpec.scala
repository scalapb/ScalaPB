package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ProtobufGeneratorSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  "gRPC service generation" should "omit collection converters while preserving configured imports" in {
    val files = generateFileSet(
      Seq(
        "service.proto" -> """
                             |syntax = "proto3";
                             |package issue358;
                             |import "scalapb/scalapb.proto";
                             |option (scalapb.options) = {
                             |  import: "example.CustomType"
                             |};
                             |message Request {}
                             |message Response { repeated string values = 1; }
                             |service Test { rpc Get(Request) returns (Response); }
                             |""".stripMargin
      )
    )
    val file = files.find(_.getName == "service.proto").get
    for (scala3Sources <- Seq(false, true)) {
      val params =
        GeneratorParams(javaConversions = true, grpc = true, scala3Sources = scala3Sources)
      val implicits =
        new DescriptorImplicits(params, files, SecondaryOutputProvider.fromMap(Map.empty))
      val generator = new ProtobufGenerator(params, implicits)
      val generated = generator.generateMultipleScalaFilesForFileDescriptor(file)
      val service   = generated.find(_.getName.endsWith("/TestGrpc.scala")).get.getContent
      val response  = generated.find(_.getName.endsWith("/Response.scala")).get.getContent

      service must not include "scalapb.internal.compat.JavaConverters"
      service must include("import example.CustomType")
      response must include("scalapb.internal.compat.JavaConverters")
      response must include(".asJava")
      response must include(".asScala")
    }
  }
}
