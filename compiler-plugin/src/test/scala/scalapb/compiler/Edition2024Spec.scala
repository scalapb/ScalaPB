package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2024Spec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  "Edition 2024 compiler validation" should "enforce naming style and accept explicit legacy overrides" in {
    intercept[IllegalArgumentException] {
      generateFileSet(Seq("style.proto" -> """edition = "2024";
        package editions_test;
        message bad_name { int32 badName = 1; }
      """))
    }
    val files = generateFileSet(Seq("style.proto" -> """edition = "2024";
      package editions_test;
      option features.enforce_naming_style = STYLE_LEGACY;
      message bad_name { int32 badName = 1; }
    """))
    files.find(_.getName == "style.proto").get.findMessageTypeByName("bad_name") must not be null
  }

  it should "reject imports of local symbols and allow explicitly exported nested types" in {
    val importing = """edition = "2024";
      package editions_test;
      import "exported.proto";
      message Consumer { Outer.Inner value = 1; }
    """
    intercept[IllegalArgumentException] {
      generateFileSet(
        Seq(
          "exported.proto" -> """edition = "2024"; package editions_test;
          message Outer { message Inner {} }
        """,
          "consumer.proto" -> importing
        )
      )
    }
    val files = generateFileSet(
      Seq(
        "exported.proto" -> """edition = "2024"; package editions_test;
        message Outer { export message Inner {} }
      """,
        "consumer.proto" -> importing
      )
    )
    files
      .find(_.getName == "consumer.proto")
      .get
      .findMessageTypeByName("Consumer")
      .findFieldByNumber(1)
      .getMessageType
      .getFullName must be("editions_test.Outer.Inner")
  }
  it should "prevent option-only imports from exposing message types" in {
    intercept[IllegalArgumentException] {
      generateFileSet(
        Seq(
          "options.proto"  -> """edition = "2024"; package editions_test; message Payload {}""",
          "consumer.proto" -> """edition = "2024"; package editions_test;
          import option "options.proto";
          message Consumer { Payload payload = 1; }
        """
        )
      )
    }
  }
}
