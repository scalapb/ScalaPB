package scalapb.compiler

import com.google.protobuf.DescriptorProtos.Edition
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import scalapb.ScalaPbCodeGenerator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ProtobufGeneratorSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  "Code generator response" should "serialize the supported edition range" in {
    val response = CodeGeneratorResponse.parseFrom(
      ScalaPbCodeGenerator.run(CodeGeneratorRequest.getDefaultInstance.toByteArray)
    )

    response.hasError must be(false)
    response.getSupportedFeatures must be(
      (CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL_VALUE |
        CodeGeneratorResponse.Feature.FEATURE_SUPPORTS_EDITIONS_VALUE).toLong
    )
    response.hasMinimumEdition must be(true)
    response.getMinimumEdition must be(Edition.EDITION_2023_VALUE)
    response.hasMaximumEdition must be(true)
    response.getMaximumEdition must be(Edition.EDITION_2023_VALUE)
  }

  "Scala 3 source generation" should "use question-mark wildcards in message and file companions" in {
    val files = generateFileSet(
      Seq(
        "wildcards.proto" -> """
                               |syntax = "proto3";
                               |package issue1919;
                               |message Outer {
                               |  message Inner {}
                               |  enum Kind { UNKNOWN = 0; }
                               |  Inner inner = 1;
                               |  Kind kind = 2;
                               |}
                               |""".stripMargin
      )
    )
    val file = files.find(_.getName == "wildcards.proto").get
    for (scala3Sources <- Seq(false, true)) {
      val params    = GeneratorParams(scala3Sources = scala3Sources)
      val implicits =
        new DescriptorImplicits(params, files, SecondaryOutputProvider.fromMap(Map.empty))
      val generated = new ProtobufGenerator(params, implicits)
        .generateMultipleScalaFilesForFileDescriptor(file)
      val message    = generated.find(_.getName.endsWith("/Outer.scala")).get.getContent
      val descriptor = generated.find(_.getName.endsWith("/WildcardsProto.scala")).get.getContent
      val wildcard   = if (scala3Sources) "?" else "_"
      val companions =
        s"Seq[_root_.scalapb.GeneratedMessageCompanion[$wildcard <: _root_.scalapb.GeneratedMessage]]"

      message must include(s"nestedMessagesCompanions: $companions")
      descriptor must include(s"messagesCompanions: $companions")
      descriptor must include(s"$companions(")
      message must include(s"_root_.scalapb.GeneratedMessageCompanion[$wildcard]")
      message must include(s"_root_.scalapb.GeneratedEnumCompanion[$wildcard]")
      val contextualDef = if (scala3Sources) "given" else "implicit def"
      message must include(s"$contextualDef messageCompanion:")
      message must include(s"$contextualDef messageReads:")
      message must include(s"$contextualDef enumCompanion:")
      if (scala3Sources) {
        message must include("_root_.scalapb.lenses.LensCompanion[")
        message must include("protected def makeLens[UpperPB]")
        message must not include "extension [UpperPB]"
        message must not include "implicit class"
        generated.foreach { source =>
          source.getContent must not include "[_]"
          source.getContent must not include "[_ <:"
        }
      }
    }
  }

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

      val contextualDef = if (scala3Sources) "given" else "implicit def"
      val contextualVal = if (scala3Sources) "given" else "implicit val"
      service must include(s"$contextualDef serviceCompanion:")
      service must include(s"$contextualVal stubFactory:")
      service must not include "scalapb.internal.compat.JavaConverters"
      service must include("import example.CustomType")
      response must include("scalapb.internal.compat.JavaConverters")
      response must include(".asJava")
      response must include(".asScala")
    }
  }
}
