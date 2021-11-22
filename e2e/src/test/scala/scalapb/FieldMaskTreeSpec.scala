package scalapb

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import protobuf_unittest.unittest.{
  NestedTestAllTypes,
  TestAllTypes,
  TestRequired,
  TestRequiredMessage
}

class FieldMaskTreeSpec extends AnyFlatSpec with Matchers {

  // https://github.com/protocolbuffers/protobuf/blob/v3.14.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskTreeTest.java#L203-L312
  "applyToMessage" should "apply field mask to a message" in {
    val value = TestAllTypes(
      optionalInt32 = Some(1234),
      optionalNestedMessage = Some(
        TestAllTypes.NestedMessage(
          bb = Some(5678)
        )
      ),
      repeatedInt32 = List(4321),
      repeatedNestedMessage = List(
        TestAllTypes.NestedMessage(
          bb = Some(8765)
        )
      )
    )
    val source = NestedTestAllTypes(
      payload = Some(value),
      child = Some(
        NestedTestAllTypes(
          payload = Some(value)
        )
      )
    )
    FieldMaskTree(Seq("payload.optional_int32")).applyToMessage(source) must be(
      NestedTestAllTypes(
        payload = Some(
          TestAllTypes(
            optionalInt32 = Some(1234)
          )
        )
      )
    )
    FieldMaskTree(Seq("payload.optional_nested_message")).applyToMessage(source) must be(
      NestedTestAllTypes(
        payload = Some(
          TestAllTypes(
            optionalNestedMessage = Some(
              TestAllTypes.NestedMessage(
                bb = Some(5678)
              )
            )
          )
        )
      )
    )
    FieldMaskTree(Seq("payload.repeated_int32")).applyToMessage(source) must be(
      NestedTestAllTypes(
        payload = Some(
          TestAllTypes(
            repeatedInt32 = List(4321)
          )
        )
      )
    )
    FieldMaskTree(Seq("payload.repeated_nested_message")).applyToMessage(source) must be(
      NestedTestAllTypes(
        payload = Some(
          TestAllTypes(
            repeatedNestedMessage = List(
              TestAllTypes.NestedMessage(
                bb = Some(8765)
              )
            )
          )
        )
      )
    )
    FieldMaskTree(Seq("child.payload.optional_int32")).applyToMessage(source) must be(
      NestedTestAllTypes(
        child = Some(
          NestedTestAllTypes(
            payload = Some(
              TestAllTypes(
                optionalInt32 = Some(1234)
              )
            )
          )
        )
      )
    )
    FieldMaskTree(Seq("child.payload.optional_nested_message")).applyToMessage(source) must be(
      NestedTestAllTypes(
        child = Some(
          NestedTestAllTypes(
            payload = Some(
              TestAllTypes(
                optionalNestedMessage = Some(
                  TestAllTypes.NestedMessage(
                    bb = Some(5678)
                  )
                )
              )
            )
          )
        )
      )
    )
    FieldMaskTree(Seq("child.payload.repeated_int32")).applyToMessage(source) must be(
      NestedTestAllTypes(
        child = Some(
          NestedTestAllTypes(
            payload = Some(
              TestAllTypes(
                repeatedInt32 = List(4321)
              )
            )
          )
        )
      )
    )
    FieldMaskTree(Seq("child.payload.repeated_nested_message")).applyToMessage(source) must be(
      NestedTestAllTypes(
        child = Some(
          NestedTestAllTypes(
            payload = Some(
              TestAllTypes(
                repeatedNestedMessage = List(
                  TestAllTypes.NestedMessage(
                    bb = Some(8765)
                  )
                )
              )
            )
          )
        )
      )
    )
    FieldMaskTree(Seq("child", "payload")).applyToMessage(source) must be(source)
  }

  // https://github.com/protocolbuffers/protobuf/blob/v3.14.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskTreeTest.java#L174-L201
  "applyToMessage" should "thrown an exception when required field is not selected" in {
    val value = TestRequired(
      a = 4321,
      b = 8765,
      c = 233333
    )
    val source = TestRequiredMessage(
      requiredMessage = value
    )
    a[NoSuchElementException] shouldBe thrownBy(
      FieldMaskTree(Seq("required_message.b", "required_message.c")).applyToMessage(source)
    )
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
}
