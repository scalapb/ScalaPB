package scalapb

import com.google.protobuf.field_mask.FieldMask
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import protobuf_unittest.unittest.{NestedTestAllTypes, TestAllTypes}

class FieldMaskUtilsSpec extends AnyFlatSpec with Matchers {

  // https://github.com/protocolbuffers/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskUtilTest.java#L202-L212
  "applyFieldMask" should "apply field mask to a message" in {
    val message = NestedTestAllTypes(
      payload = Some(TestAllTypes(
        optionalInt32 = Some(1234)
      ))
    )
    val fieldMask = FieldMask(Seq("payload"))

    FieldMaskUtil.applyFieldMask(message, fieldMask).payload must be(
      Some(TestAllTypes(
        optionalInt32 = Some(1234)
      ))
    )
  }

  "containsFieldNumber" should "check that field is present" in {
    FieldMaskUtil.containsFieldNumber[NestedTestAllTypes](
      FieldMask(Seq("payload")),
      NestedTestAllTypes.PAYLOAD_FIELD_NUMBER
    ) must be(true)
    FieldMaskUtil.containsFieldNumber[NestedTestAllTypes](
      FieldMask(Seq("payload.optional_int32")),
      NestedTestAllTypes.PAYLOAD_FIELD_NUMBER
    ) must be(true)
    FieldMaskUtil.containsFieldNumber[NestedTestAllTypes](
      FieldMask(Seq("payload")),
      NestedTestAllTypes.CHILD_FIELD_NUMBER
    ) must be(false)
  }

  // https://github.com/protocolbuffers/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskUtilTest.java#L124-L147
  "fromFieldNumbers" should "construct TestAllTypes mask" in {
    FieldMaskUtil.fromFieldNumbers[TestAllTypes]() must be(Some(FieldMask()))
    FieldMaskUtil.fromFieldNumbers[TestAllTypes](
      TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER
    ) must be(Some(FieldMask(Seq("optional_int32"))))
    FieldMaskUtil.fromFieldNumbers[TestAllTypes](
      TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER,
      TestAllTypes.OPTIONAL_INT64_FIELD_NUMBER
    ) must be(Some(FieldMask(Seq("optional_int32", "optional_int64"))))
  }

  "fromFieldNumbers" should "return None for invalid field numbers" in {
    val invalidFieldNumber = 1000
    FieldMaskUtil.fromFieldNumbers[TestAllTypes](invalidFieldNumber) must be(None)
  }

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

}
