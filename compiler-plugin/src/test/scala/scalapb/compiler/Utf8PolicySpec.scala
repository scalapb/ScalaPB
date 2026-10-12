package scalapb.compiler

import com.google.protobuf.Descriptors.FileDescriptor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Utf8PolicySpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  private def generated(files: Seq[FileDescriptor], name: String): String = {
    val params    = GeneratorParams()
    val implicits =
      new DescriptorImplicits(params, files, SecondaryOutputProvider.fromMap(Map.empty))
    new ProtobufGenerator(params, implicits)
      .generateMultipleScalaFilesForFileDescriptor(files.find(_.getName == name).get)
      .map(_.getContent)
      .mkString("\n")
  }

  "UTF-8 policy" should "preserve legacy strict parsing and resolve proto2 and proto3 policy" in {
    for (
      syntax <- Seq("proto2", "proto3"); optIn <- Seq(false, true); javaCheck <- Seq(false, true)
    ) {
      val files = generateFileSet(
        Seq("strings.proto" -> s"""
                                  |syntax = "$syntax";
                                  |import "scalapb/scalapb.proto";
                                  |option (scalapb.options).use_schema_utf8_validation = $optIn;
                                  |option java_string_check_utf8 = $javaCheck;
                                  |message Strings { optional string value = 1; }
                                  |""".stripMargin)
      )
      val strict = !optIn || syntax == "proto3" || javaCheck
      generated(files, "strings.proto") must include(
        if (strict) "_input__.readStringRequireUtf8()" else "_input__.readString()"
      )
    }
  }

  it should "resolve Editions file inheritance, field overrides and strict map strings" in {
    val files = generateFileSet(
      Seq("strings.proto" -> """
                               |edition = "2023";
                               |import "scalapb/scalapb.proto";
                               |option (scalapb.options).use_schema_utf8_validation = true;
                               |option features.utf8_validation = NONE;
                               |message Strings {
                               |  string inherited = 1;
                               |  string checked = 2 [features.utf8_validation = VERIFY];
                               |  repeated string repeated_value = 3;
                               |  oneof choice { string selected = 4; }
                               |  map<string, string> entries = 5;
                               |}
                               |""".stripMargin)
    )
    val code = generated(files, "strings.proto")
    code must include("__inherited = _root_.scala.Option(_input__.readString())")
    code must include("__checked = _root_.scala.Option(_input__.readStringRequireUtf8())")
    code must include("__repeatedValue += _input__.readString()")
    code must include("Selected(_input__.readString())")
    code must include("__key = _root_.scala.Option(_input__.readStringRequireUtf8())")
    code must include("__value = _root_.scala.Option(_input__.readStringRequireUtf8())")
  }

  it should "validate Editions defaults and allow a field to disable validation" in {
    for (edition <- Seq("2023", "2024")) {
      val files = generateFileSet(
        Seq("defaults.proto" -> s"""
                                   |edition = "$edition";
                                   |import "scalapb/scalapb.proto";
                                   |option (scalapb.options).use_schema_utf8_validation = true;
                                   |message Strings {
                                   |  string checked = 1;
                                   |  string relaxed = 2 [features.utf8_validation = NONE];
                                   |}
                                   |""".stripMargin)
      )
      val code = generated(files, "defaults.proto")
      code must include("__checked = _root_.scala.Option(_input__.readStringRequireUtf8())")
      code must include("__relaxed = _root_.scala.Option(_input__.readString())")
    }
  }

  it should "inherit the ScalaPB opt-in from package options and allow file opt-out" in {
    val files = generateFileSet(
      Seq(
        "options.proto" -> """
                             |syntax = "proto2";
                             |package utf8;
                             |import "scalapb/scalapb.proto";
                             |option (scalapb.options) = { scope: PACKAGE use_schema_utf8_validation: true };
                             |""".stripMargin,
        "relaxed.proto" -> """syntax = "proto2"; package utf8;
                             |message Relaxed { optional string value = 1; }
                             |""".stripMargin,
        "strict.proto" -> """syntax = "proto2"; package utf8;
                            |import "scalapb/scalapb.proto";
                            |option (scalapb.options).use_schema_utf8_validation = false;
                            |message Strict { optional string value = 1; }
                            |""".stripMargin
      )
    )
    generated(files, "relaxed.proto") must include("_input__.readString()")
    generated(files, "strict.proto") must include("_input__.readStringRequireUtf8()")
  }

  it should "honor inherited Java VERIFY overrides after request deserialization" in {
    import com.google.protobuf.{DescriptorProtos => P, ExtensionRegistry, JavaFeaturesProto => J}
    import scalapb.options.Scalapb
    for (
      edition    <- Seq(P.Edition.EDITION_2023, P.Edition.EDITION_2024);
      javaVerify <- Seq(false, true)
    ) {
      val features = P.FeatureSet
        .newBuilder()
        .setUtf8Validation(P.FeatureSet.Utf8Validation.NONE)
        .setExtension(
          J.java_,
          J.JavaFeatures
            .newBuilder()
            .setUtf8Validation(
              if (javaVerify) J.JavaFeatures.Utf8Validation.VERIFY
              else J.JavaFeatures.Utf8Validation.DEFAULT
            )
            .build()
        )
        .build()
      val proto = P.FileDescriptorProto
        .newBuilder()
        .setName("java_utf8.proto")
        .setSyntax("editions")
        .setEdition(edition)
        .setOptions(
          P.FileOptions
            .newBuilder()
            .setFeatures(features)
            .setExtension(
              Scalapb.options,
              Scalapb.ScalaPbOptions
                .newBuilder()
                .setUseSchemaUtf8Validation(true)
                .build()
            )
        )
        .addMessageType(
          P.DescriptorProto
            .newBuilder()
            .setName("Strings")
            .addField(
              P.FieldDescriptorProto
                .newBuilder()
                .setName("value")
                .setNumber(1)
                .setType(P.FieldDescriptorProto.Type.TYPE_STRING)
                .setLabel(P.FieldDescriptorProto.Label.LABEL_OPTIONAL)
            )
            .addField(
              P.FieldDescriptorProto
                .newBuilder()
                .setName("overridden")
                .setNumber(2)
                .setType(P.FieldDescriptorProto.Type.TYPE_STRING)
                .setLabel(P.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                .setOptions(
                  P.FieldOptions
                    .newBuilder()
                    .setFeatures(
                      P.FeatureSet
                        .newBuilder()
                        .setExtension(
                          J.java_,
                          J.JavaFeatures
                            .newBuilder()
                            .setUtf8Validation(
                              if (javaVerify) J.JavaFeatures.Utf8Validation.DEFAULT
                              else J.JavaFeatures.Utf8Validation.VERIFY
                            )
                            .build()
                        )
                    )
                )
            )
        )
        .build()
      val registry = ExtensionRegistry.newInstance()
      scalapb.ScalaPbCodeGenerator.registerExtensions(registry)
      val parsed = P.FileDescriptorProto.parseFrom(proto.toByteArray, registry)
      val file   = FileDescriptor.buildFrom(parsed, Array.empty[FileDescriptor])
      val code   = generated(Seq(file), "java_utf8.proto")
      code must include(
        if (javaVerify) "__value = _root_.scala.Option(_input__.readStringRequireUtf8())"
        else "__value = _root_.scala.Option(_input__.readString())"
      )
      code must include(
        if (javaVerify) "__overridden = _root_.scala.Option(_input__.readString())"
        else "__overridden = _root_.scala.Option(_input__.readStringRequireUtf8())"
      )
    }
  }

}
