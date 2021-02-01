package scalapb

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import protobuf_unittest.unittest.NestedTestAllTypes

class FieldMaskTreeSpec extends AnyFlatSpec with Matchers {

  def isValidFor(paths: Seq[String]): Boolean = {
    FieldMaskTree(paths).isValidFor[NestedTestAllTypes]
  }

  "isValid" should "pass for valid NestedTestAllTypes masks" in {
    isValidFor(Seq("payload")) must be(true)
    isValidFor(Seq("payload.optional_int32")) must be(true)
    isValidFor(Seq("payload.repeated_int32")) must be(true)
    isValidFor(Seq("payload.optional_nested_message")) must be(true)
    isValidFor(Seq("payload.optional_nested_message.bb")) must be(true)
  }

  "isValid" should "fail for invalid NestedTestAllTypes masks" in {
    isValidFor(Seq("nonexist")) must be(false)
    isValidFor(Seq("payload.nonexist")) must be(false)
    isValidFor(Seq("payload,nonexist")) must be(false)
    isValidFor(Seq("payload.repeated_nested_message.bb")) must be(false)
    isValidFor(Seq("payload.optional_int32.bb")) must be(false)
  }

  "containsField" should "check that field is present" in {
    FieldMaskTree(Seq("payload")).containsField[NestedTestAllTypes](
      NestedTestAllTypes.PAYLOAD_FIELD_NUMBER
    ) must be(true)
    FieldMaskTree(Seq("payload.optional_int32")).containsField[NestedTestAllTypes](
      NestedTestAllTypes.PAYLOAD_FIELD_NUMBER
    ) must be(true)
    FieldMaskTree(Seq("payload")).containsField[NestedTestAllTypes](
      NestedTestAllTypes.CHILD_FIELD_NUMBER
    ) must be(false)
  }

}
