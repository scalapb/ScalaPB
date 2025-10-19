package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ProtoValidationSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  def runValidation(
      generatorParams: GeneratorParams,
      secondaryOutput: SecondaryOutputProvider,
      files: (String, String)*
  ): Unit = {
    val fileset    = generateFileSet(files)
    val validation = new ProtoValidation(
      new DescriptorImplicits(generatorParams, fileset, secondaryOutput)
    )
    validation.validateFiles(fileset)
    ()
  }

  def runValidation(files: (String, String)*): Unit = {
    runValidation(new GeneratorParams(), SecondaryOutputProvider.fromMap(Map.empty), files: _*)
  }

  "simple message" should "validate" in {
    runValidation(
      "file.proto" ->
        """
          |syntax = "proto2";
          |message Foo { };
    """.stripMargin
    )
  }

  "UNRECOGNIZED enum value" should "fail validation" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |enum MyEnum { UNRECOGNIZED = 3; };
          """.stripMargin
      )
    }.message must include(
      "The enum value 'UNRECOGNIZED' in MyEnum is not allowed due to conflict with the catch-all Unrecognized"
    )
  }

  "oneof checker" should "validate if oneof and case messages are top-level" in {
    runValidation(
      "file.proto" ->
        """
          |syntax = "proto2";
          |message Case1 {}
          |message MyOneof {
          |  oneof sealed_value {
          |    Case1 case1 = 1;
          |  }
          |}
        """.stripMargin
    )
  }

  it should "validate if oneof and case messages are nested in same parent message" in {
    runValidation(
      "file.proto" ->
        """
          |syntax = "proto2";
          |message Parent {
          |  message Case1 {}
          |  message MyOneof {
          |    oneof sealed_value {
          |      Case1 case1 = 1;
          |    }
          |  }
          |}
        """.stripMargin
    )
  }

  it should "fail when case message is nested, but oneof is top-level" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Parent {
            |  message Case1 {}
            |}
            |message MyOneof {
            |  oneof sealed_value {
            |    Parent.Case1 case1 = 1;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs must be in the same containing message")
  }

  it should "fail when case message is top-level, but oneof is nested" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message Parent {
            |  message MyOneof {
            |    oneof sealed_value {
            |      Case1 case1 = 1;
            |    }
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs must be in the same containing message")
  }

  it should "fail when fields outside oneof" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |  optional int32 extra = 2;
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs must have all their fields inside a single oneof")
  }

  it should "fail when there are non-message cases" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |    int32 extra = 2;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs must have all their fields be message types")
  }

  it should "fail when oneof contains another oneof" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
            |message Another {
            |  oneof sealed_value {
            |    MyOneof my_oneof = 1;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs may not be a case within")
  }

  it should "fail when non distinct case types" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |    Case1 case2 = 2;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneof cases must be of a distinct message type")
  }

  it should "fail when there are nested message types" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |  message Foo {}
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs may not contain nested messages")
  }

  it should "fail when there are nested enum types" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |  enum Foo { UNKNOWN = 0; }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs may not contain nested enums")
  }

  it should "fail when a case is shared between two oneofs" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
            |message AnotherOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("message may belong to at most one sealed oneof")
  }

  it should "fail when a sealed oneof field is typemapped" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |import "scalapb/scalapb.proto";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
            |message Use {
            |  optional MyOneof usage = 1 [(scalapb.field).type="SomeType"];
            |}
          """.stripMargin
      )
    }.message must include("Sealed oneofs can not be type mapped. Use regular oneofs instead.")
  }

  it should "fail when a sealed oneof case is typemapped" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |import "scalapb/scalapb.proto";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1 [(scalapb.field).type="SomeType"];
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneof cases may not have custom types.")
  }

  it should "fail when sealed_oneof_extends used outside of a sealed oneof type" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |import "scalapb/scalapb.proto";
            |message Foo {
            |  option (scalapb.message).sealed_oneof_extends = "SomeTrait";
            |}
          """.stripMargin
      )
    }.message must include(
      ": `sealed_oneof_extends` option can only be used on a sealed oneof. Did you mean `extends`?"
    )
  }

  it should "fail when sealed_oneof_companion_extends used outside of a sealed oneof type" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |import "scalapb/scalapb.proto";
            |message Foo {
            |  option (scalapb.message).sealed_oneof_companion_extends = "SomeTrait";
            |}
          """.stripMargin
      )
    }.message must include(
      ": `sealed_oneof_companion_extends` option can only be used on a sealed oneof. Did you mean `companion_extends`?"
    )
  }

  it should "fail when sealed oneof and a case are in different files" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto2";
            |package mypkg;
            |import "file2.proto";
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
          """.stripMargin,
        "file2.proto" ->
          """
            |syntax = "proto2";
            |package mypkg;
            |message Case1 {}
          """.stripMargin
      )
    }.message must include(
      "mypkg.MyOneof.case1: all sealed oneof cases must be defined in the same file as the sealed oneof field."
    )
  }

  // package scoped options
  "package scoped options" should "fail when multiple scoped option objects found for same package" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            };""".stripMargin,
        "file2.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            };""".stripMargin
      )
    }.message must be(
      "Multiple files contain package-scoped options for package 'a.b': file1.proto, file2.proto"
    )
  }

  it should "fail when package scoped option defined when no package specified" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            };""".stripMargin
      )
    }.message must be(
      "file1.proto: a package statement is required when package-scoped options are used"
    )
  }

  it should "fail when package scoped option defined an object name" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            |  object_name: "FOO"
            };""".stripMargin
      )
    }.message must be(
      "file1.proto: object_name is not allowed in package-scoped options."
    )
  }

  it should "fail when oneof contains a no_box message" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |message NoBox {
            |  option (scalapb.message).no_box = true;
            |}
            |
            |message Msg {
            |  oneof foo {
            |    NoBox field = 1;
            |  }
            |}
            """.stripMargin
      )
    }.message must be(
      "a.b.Msg.field: message fields in oneofs are not allowed to have no_box set."
    )
  }

  "required fields proto" should "fail when oneof contains a required message" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |message NoBox {
            |}
            |
            |message Msg {
            |  oneof foo {
            |    NoBox field = 1 [(scalapb.field).required = true];
            |  }
            |}
            """.stripMargin
      )
    }.message must be(
      "a.b.Msg.field: setting required is not allowed on oneof fields."
    )
  }

  it should "fail when no_box and required are in contradiction" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |message Test {
            |  Msg req = 1 [(scalapb.field).required = true, (scalapb.field).no_box=false];
            |}
            |
            |message Msg {
            |}
            """.stripMargin
      )
    }.getMessage must be(
      "a.b.Test.req: setting no_box to false is not allowed while setting required to true."
    )
  }

  it should "fail when required is set on repeated field" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |message Test {
            |  repeated Msg req = 1 [(scalapb.field).required = true];
            |}
            |
            |message Msg {
            |}
            """.stripMargin
      )
    }.getMessage must be(
      "a.b.Test.req: required is not allowed on repeated fields."
    )
  }

  it should "fail when required is set on non-message field" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |message Test {
            |  int32 req = 1 [(scalapb.field).required = true];
            |}
            |
            |message Msg {
            |}
            """.stripMargin
      )
    }.getMessage must be(
      "a.b.Test.req: required can only be applied to message fields."
    )
  }

  "preprocessors" should "fail when preprocessor name is invalid" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |import "scalapb/scalapb.proto";
            |package a.b;
            |
            |option (scalapb.options) = {
            |  preprocessors: ["."]
            };
            """.stripMargin
      )
    }.message must be(
      "file1.proto: Invalid preprocessor name: '.'"
    )
  }

  it should "fail when preprocessor name is missing" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |import "scalapb/scalapb.proto";
            |package a.b;
            |
            |option (scalapb.options) = {
            |  preprocessors: ["foo"]
            };
            """.stripMargin
      )
    }.message must be(
      "file1.proto: Preprocessor 'foo' was not found."
    )
  }
}
