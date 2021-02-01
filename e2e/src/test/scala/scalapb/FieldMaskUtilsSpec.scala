package scalapb

import com.google.protobuf.field_mask.FieldMask
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import protobuf_unittest.unittest.{NestedTestAllTypes, TestAllTypes}

class FieldMaskUtilsSpec extends AnyFlatSpec with Matchers {

  def isValid(paths: Seq[String]): Boolean = {
    FieldMaskUtil.isValid[NestedTestAllTypes](FieldMask(paths))
  }

  // https://github.com/protocolbuffers/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskUtilTest.java#L41-L75
  "isValid" should "pass for valid NestedTestAllTypes masks" in {
    isValid(Seq("payload")) must be(true)
    isValid(Seq("payload.optional_int32")) must be(true)
    isValid(Seq("payload.repeated_int32")) must be(true)
    isValid(Seq("payload.optional_nested_message")) must be(true)
    isValid(Seq("payload.optional_nested_message.bb")) must be(true)
  }

  "isValid" should "fail for invalid NestedTestAllTypes masks" in {
    isValid(Seq("nonexist")) must be(false)
    isValid(Seq("payload.nonexist")) must be(false)
    isValid(Seq("payload,nonexist")) must be(false)
    isValid(Seq("payload.repeated_nested_message.bb")) must be(false)
    isValid(Seq("payload.optional_int32.bb")) must be(false)
  }

  // https://github.com/protocolbuffers/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskUtilTest.java#L124-L147
  "fromFieldNumbers" should "construct TestAllTypes mask" in {
    FieldMaskUtil.fromFieldNumbers[TestAllTypes]() must be(FieldMask())
    FieldMaskUtil.fromFieldNumbers[TestAllTypes](
      TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER
    ) must be(FieldMask(Seq("optional_int32")))
    FieldMaskUtil.fromFieldNumbers[TestAllTypes](
      TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER,
      TestAllTypes.OPTIONAL_INT64_FIELD_NUMBER
    ) must be(FieldMask(Seq("optional_int32", "optional_int64")))
  }

  "fromFieldNumbers" should "throw an exception for invalid field" in {
    val invalidFieldNumber = 1000
    a[NullPointerException] shouldBe thrownBy(
      FieldMaskUtil.fromFieldNumbers[TestAllTypes](invalidFieldNumber)
    )
  }

  "selectFieldNumbers" should "construct NestedTestAllTypes mask using a predicate" in {
    FieldMaskUtil.selectFieldNumbers[NestedTestAllTypes](_ => true) must be(
      FieldMask(Seq("child", "payload", "repeated_child"))
    )
    FieldMaskUtil.selectFieldNumbers[NestedTestAllTypes](_ => false) must be(FieldMask())
    FieldMaskUtil.selectFieldNumbers[NestedTestAllTypes](
      Set(
        NestedTestAllTypes.CHILD_FIELD_NUMBER,
        NestedTestAllTypes.PAYLOAD_FIELD_NUMBER,
      )
    ) must be(FieldMask(Seq("child", "payload")))
  }

  "containsField" should "check that field is present" in {
    FieldMaskUtil.containsField[NestedTestAllTypes](
      FieldMask(Seq("payload")),
      NestedTestAllTypes.PAYLOAD_FIELD_NUMBER
    ) must be(true)
    FieldMaskUtil.containsField[NestedTestAllTypes](
      FieldMask(Seq("payload.optional_int32")),
      NestedTestAllTypes.PAYLOAD_FIELD_NUMBER
    ) must be(true)
    FieldMaskUtil.containsField[NestedTestAllTypes](
      FieldMask(Seq("payload")),
      NestedTestAllTypes.CHILD_FIELD_NUMBER
    ) must be(false)
  }

}
