package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2026Spec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  "Edition 2026 compiler validation" should "reject generated-name collisions and honor compatibility overrides" in {
    val fields = "message Collision { string value = 1; string has_value = 2; }"
    intercept[IllegalArgumentException] {
      generateFileSet(Seq("collision.proto" -> ("edition = \"2026\"; " + fields)))
    }
    val files = generateFileSet(
      Seq(
        "collision.proto" ->
          ("edition = \"2026\"; option features.enforce_naming_style = STYLE2024; " + fields)
      )
    )
    files
      .find(_.getName == "collision.proto")
      .get
      .findMessageTypeByName("Collision")
      .getFields
      .size must be(2)
  }

  it should "default symbols to local, require exports for imports and reject nested exports" in {
    val consumer =
      """edition = "2026"; import "types.proto"; message Consumer { Payload value = 1; }"""
    intercept[IllegalArgumentException] {
      generateFileSet(
        Seq(
          "types.proto"    -> """edition = "2026"; message Payload {}""",
          "consumer.proto" -> consumer
        )
      )
    }
    val files = generateFileSet(
      Seq(
        "types.proto"    -> """edition = "2026"; export message Payload {}""",
        "consumer.proto" -> consumer
      )
    )
    files
      .find(_.getName == "consumer.proto")
      .get
      .findMessageTypeByName("Consumer")
      .findFieldByNumber(1)
      .getMessageType
      .getName must be("Payload")
    intercept[IllegalArgumentException] {
      generateFileSet(Seq("nested.proto" -> """edition = "2026";
        export message Outer { export message Inner {} }
      """))
    }
  }

  it should "enforce structure limits and allow an explicit per-message compatibility override" in {
    val fields                  = (1 to 1501).map(n => s"int32 field$n = $n;").mkString("\n")
    def schema(options: String) = s"""edition = "2026"; message Large { $options $fields }"""
    intercept[IllegalArgumentException] {
      generateFileSet(Seq("limits.proto" -> schema("")))
    }
    val files = generateFileSet(
      Seq(
        "limits.proto" -> schema(
          "option features.enforce_proto_limits = LEGACY_NO_EXPLICIT_LIMITS;"
        )
      )
    )
    files
      .find(_.getName == "limits.proto")
      .get
      .findMessageTypeByName("Large")
      .getFields
      .size must be(1501)
  }

  it should "reject options removed in 2026" in {
    intercept[IllegalArgumentException] {
      generateFileSet(Seq("removed.proto" -> """edition = "2026";
        option cc_enable_arenas = true;
        message Payload {}
      """))
    }
  }
}
