package scalapb

import java.util.Base64

import com.google.protobuf.ByteString
import com.google.protobuf.struct.{ListValue, Struct, Value}
import com.google.protobuf.test.unittest_import.{ImportEnum, ImportMessage}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import protobuf_unittest.fromstruct_two_level_nesting.{
  ContainerMessage,
  DeepEnum,
  DeepMessage,
  ImportMessage => ContainerMessageImportMessage
}
import protobuf_unittest.unittest.TestAllTypes.NestedMessage
import protobuf_unittest.unittest.{ForeignEnum, ForeignMessage, TestAllTypes}
import scalapb.StructUtils.StructParsingError

import scala.util.Random
import scala.annotation.nowarn

@nowarn("cat=deprecation")
class StructUtilsSpec extends AnyFlatSpec with Matchers with EitherValues {

  /** Helper to construct a ByteString from a String containing only 8-bit characters. The
    * characters are converted directly to bytes, *not* encoded using UTF-8.
    */
  def bytes(bytesAsInts: Int*): ByteString =
    ByteString.copyFrom(bytesAsInts.map(_.toByte).toArray)

  "toStruct of empty message" should "pass" in {
    StructUtils.toStruct(TestAllTypes()) must be(
      Struct(Map.empty)
    )
  }

  "toStruct with int field" should "pass" in {
    val someIntValue = Random.nextInt()
    StructUtils.toStruct(TestAllTypes().withOptionalInt32(someIntValue)) must be(
      Struct(Map("optional_int32" -> Value(Value.Kind.NumberValue(someIntValue.toDouble))))
    )
  }

  "toStruct with long field" should "pass" in {
    // Using StringValue to not lose precision like json encoding does
    val someLongValue = Random.nextLong()
    StructUtils.toStruct(TestAllTypes().withOptionalInt64(someLongValue)) must be(
      Struct(Map("optional_int64" -> Value(Value.Kind.StringValue(someLongValue.toString))))
    )
  }

  "toStruct with double field" should "pass" in {
    val someDoubleValue = Random.nextDouble()
    StructUtils.toStruct(TestAllTypes().withOptionalDouble(someDoubleValue)) must be(
      Struct(Map("optional_double" -> Value(Value.Kind.NumberValue(someDoubleValue.toDouble))))
    )
  }

  "toStruct with float field" should "pass" in {
    val someFloatValue = Random.nextFloat()
    StructUtils.toStruct(TestAllTypes().withOptionalFloat(someFloatValue)) must be(
      Struct(Map("optional_float" -> Value(Value.Kind.NumberValue(someFloatValue.toDouble))))
    )
  }

  "toStruct with string field" should "pass" in {
    val someStringValue = Random.alphanumeric.take(Random.nextInt(500)).mkString
    StructUtils.toStruct(TestAllTypes().withOptionalString(someStringValue)) must be(
      Struct(Map("optional_string" -> Value(Value.Kind.StringValue(someStringValue))))
    )
  }

  "toStruct with byte string field" should "pass" in {
    val someBytesValue = bytes(0xe3, 0x81, 0x82)
    StructUtils.toStruct(TestAllTypes().withOptionalBytes(someBytesValue)) must be(
      Struct(
        Map(
          "optional_bytes" -> Value(
            Value.Kind.StringValue(
              new String(Base64.getEncoder.encode(someBytesValue.toByteArray()))
            )
          )
        )
      )
    )
  }

  "toStruct with boolean field" should "pass" in {
    val someBooleanValue = Random.nextBoolean()
    StructUtils.toStruct(TestAllTypes().withOptionalBool(someBooleanValue)) must be(
      Struct(Map("optional_bool" -> Value(Value.Kind.BoolValue(someBooleanValue))))
    )
  }

  "toStruct with enum field" should "pass" in {
    // using name and not full name to mimic json which is the closest counterpart to Struct
    StructUtils.toStruct(TestAllTypes().withOptionalForeignEnum(ForeignEnum.FOREIGN_BAR)) must be(
      Struct(
        Map("optional_foreign_enum" -> Value(Value.Kind.StringValue(ForeignEnum.FOREIGN_BAR.name)))
      )
    )
  }

  "toStruct with repeated field" should "pass" in {
    val someRepeatedIntValue = Seq(Random.nextInt(), Random.nextInt())
    StructUtils.toStruct(TestAllTypes().withRepeatedInt32(someRepeatedIntValue)) must be(
      Struct(
        Map(
          "repeated_int32" -> Value(
            Value.Kind.ListValue(
              ListValue(someRepeatedIntValue.map(i => Value(Value.Kind.NumberValue(i.toDouble))))
            )
          )
        )
      )
    )
  }

  "toStruct with empty nested message field" should "pass" in {
    StructUtils.toStruct(TestAllTypes().withOptionalNestedMessage(NestedMessage())) must be(
      Struct(Map("optional_nested_message" -> Value(Value.Kind.StructValue(Struct(Map.empty)))))
    )
  }

  "toStruct with non-empty nested message field" should "pass" in {
    val someIntValue = Random.nextInt()
    StructUtils.toStruct(
      TestAllTypes().withOptionalNestedMessage(NestedMessage().withBb(someIntValue))
    ) must be(
      Struct(
        Map(
          "optional_nested_message" -> Value(
            Value.Kind.StructValue(
              Struct(Map("bb" -> Value(Value.Kind.NumberValue(someIntValue.toDouble))))
            )
          )
        )
      )
    )
  }

  implicit val testAllTypesCompanion: GeneratedMessageCompanion[TestAllTypes] =
    TestAllTypes.messageCompanion

  "fromStruct of empty message" should "pass" in {
    StructUtils.fromStruct(Struct(Map.empty)).right.value must be(
      TestAllTypes()
    )
  }

  "fromStruct with int field" should "pass" in {
    val someIntValue = Random.nextInt()
    StructUtils
      .fromStruct(
        Struct(Map("optional_int32" -> Value(Value.Kind.NumberValue(someIntValue.toDouble))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalInt32(someIntValue)
    )
  }

  "fromStruct with long field" should "pass" in {
    val someLongValue = Random.nextLong()
    StructUtils
      .fromStruct(
        Struct(Map("optional_int64" -> Value(Value.Kind.StringValue(someLongValue.toString))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalInt64(someLongValue)
    )
  }
  "fromStruct with double field" should "pass" in {
    val someDoubleValue = Random.nextDouble()
    StructUtils
      .fromStruct(
        Struct(Map("optional_double" -> Value(Value.Kind.NumberValue(someDoubleValue.toDouble))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalDouble(someDoubleValue)
    )
  }

  "fromStruct with float field" should "pass" in {
    val someFloatValue = Random.nextFloat()
    StructUtils
      .fromStruct(
        Struct(Map("optional_float" -> Value(Value.Kind.NumberValue(someFloatValue.toDouble))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalFloat(someFloatValue)
    )
  }

  "fromStruct with string field" should "pass" in {
    val someStringValue = Random.alphanumeric.take(Random.nextInt(500)).mkString
    StructUtils
      .fromStruct(Struct(Map("optional_string" -> Value(Value.Kind.StringValue(someStringValue)))))
      .right
      .value must be(
      TestAllTypes().withOptionalString(someStringValue)
    )
  }

  "fromStruct with byte string field" should "pass" in {
    val someBytesValue = bytes(0xe3, 0x81, 0x82)
    StructUtils
      .fromStruct(
        Struct(
          Map(
            "optional_bytes" -> Value(
              Value.Kind.StringValue(
                new String(Base64.getEncoder.encode(someBytesValue.toByteArray()))
              )
            )
          )
        )
      )
      .right
      .value must be(
      TestAllTypes().withOptionalBytes(someBytesValue)
    )
  }

  "fromStruct with boolean field" should "pass" in {
    val someBooleanValue = Random.nextBoolean()
    StructUtils
      .fromStruct(Struct(Map("optional_bool" -> Value(Value.Kind.BoolValue(someBooleanValue)))))
      .right
      .value must be(
      TestAllTypes().withOptionalBool(someBooleanValue)
    )
  }

  "fromStruct with enum field" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(
          Map(
            "optional_foreign_enum" -> Value(Value.Kind.StringValue(ForeignEnum.FOREIGN_BAR.name))
          )
        )
      )
      .right
      .value must be(
      TestAllTypes().withOptionalForeignEnum(ForeignEnum.FOREIGN_BAR)
    )
  }

  "fromStruct with repeated field" should "pass" in {
    val someRepeatedIntValue = Seq(Random.nextInt(), Random.nextInt())
    StructUtils
      .fromStruct(
        Struct(
          Map(
            "repeated_int32" -> Value(
              Value.Kind.ListValue(
                ListValue(someRepeatedIntValue.map(i => Value(Value.Kind.NumberValue(i.toDouble))))
              )
            )
          )
        )
      )
      .right
      .value must be(
      TestAllTypes().withRepeatedInt32(someRepeatedIntValue)
    )
  }

  "fromStruct with empty nested message field" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(Map("optional_nested_message" -> Value(Value.Kind.StructValue(Struct(Map.empty)))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalNestedMessage(NestedMessage())
    )
  }

  "fromStruct with non-empty nested message field" should "pass" in {
    val someIntValue = Random.nextInt()
    StructUtils
      .fromStruct(
        Struct(
          Map(
            "optional_nested_message" -> Value(
              Value.Kind.StructValue(
                Struct(Map("bb" -> Value(Value.Kind.NumberValue(someIntValue.toDouble))))
              )
            )
          )
        )
      )
      .right
      .value must be(
      TestAllTypes().withOptionalNestedMessage(NestedMessage().withBb(someIntValue))
    )
  }

  "fromStruct with empty import message field" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(Map("optional_import_message" -> Value(Value.Kind.StructValue(Struct(Map.empty)))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalImportMessage(ImportMessage())
    )
  }

  "fromStruct with empty foreign message field" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(Map("optional_foreign_message" -> Value(Value.Kind.StructValue(Struct(Map.empty)))))
      )
      .right
      .value must be(
      TestAllTypes().withOptionalForeignMessage(ForeignMessage())
    )
  }

  "ser-deser of empty message" should "pass" in {
    val types = TestAllTypes()
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with int field" should "pass" in {
    val types = TestAllTypes().withOptionalInt32(Random.nextInt())
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with long field" should "pass" in {
    val types = TestAllTypes().withOptionalInt64(Random.nextLong())
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with double field" should "pass" in {
    val types = TestAllTypes().withOptionalDouble(Random.nextDouble())
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with float field" should "pass" in {
    val types = TestAllTypes().withOptionalFloat(Random.nextFloat())
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with string field" should "pass" in {
    val types =
      TestAllTypes().withOptionalString(Random.alphanumeric.take(Random.nextInt(500)).mkString)
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with byte string field" should "pass" in {
    val types = TestAllTypes().withOptionalBytes(bytes(0xe3, 0x81, 0x82))
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with boolean field" should "pass" in {
    val types = TestAllTypes().withOptionalBool(Random.nextBoolean())
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with enum field" should "pass" in {
    val types = TestAllTypes().withOptionalForeignEnum(ForeignEnum.FOREIGN_BAR)
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with repeated field" should "pass" in {
    val types = TestAllTypes().withRepeatedInt32(Seq(Random.nextInt(), Random.nextInt()))
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with empty nested message field" should "pass" in {
    val types = TestAllTypes().withOptionalNestedMessage(NestedMessage())
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "ser-deser with non-empty nested message field" should "pass" in {
    val types = TestAllTypes().withOptionalNestedMessage(NestedMessage().withBb(Random.nextInt()))
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  // Tests added for precaution to verify using `fd` is correct in recursion for Repeated field
  "fromStruct with repeated field of messages" should "pass" in {
    val someIntValueA = Random.nextInt()
    val someIntValueB = Random.nextInt()
    val types         = TestAllTypes().withRepeatedForeignMessage(
      Seq(ForeignMessage().withC(someIntValueA), ForeignMessage().withC(someIntValueB))
    )
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  "fromStruct with repeated field of enum" should "pass" in {
    val types =
      TestAllTypes().withRepeatedImportEnum(Seq(ImportEnum.IMPORT_BAR, ImportEnum.IMPORT_BAZ))
    StructUtils.fromStruct(StructUtils.toStruct(types)).right.value must be(types)
  }

  // Tests added for precaution to verify recursion and companions is correct
  "fromStruct with two level deep message" should "pass" in {
    implicit val containerMessageCompanion = ContainerMessage.messageCompanion
    val types                              = ContainerMessage().withImportMessage(
      ContainerMessageImportMessage().withB(DeepMessage().withA(Random.nextInt()))
    )
    StructUtils.fromStruct[ContainerMessage](StructUtils.toStruct(types)).right.value must be(types)
  }

  "fromStruct with two level deep enum" should "pass" in {
    implicit val containerMessageCompanion = ContainerMessage.messageCompanion
    val types                              =
      ContainerMessage().withImportMessage(ContainerMessageImportMessage().withC(DeepEnum.DEEP_BAZ))
    StructUtils.fromStruct[ContainerMessage](StructUtils.toStruct(types)).right.value must be(types)
  }

  // failures
  "fromStruct with missing enum field" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(Map("optional_foreign_enum" -> Value(Value.Kind.StringValue("non_existent"))))
      )
      .left
      .value must be(
      StructParsingError(
        """Field "protobuf_unittest.TestAllTypes.optional_foreign_enum" is of type enum "protobuf_unittest.ForeignEnum" but received invalid enum value "non_existent""""
      )
    )
  }

  "fromStruct with faulty int field" should "pass" in {
    val someFaultyInt = Random.nextFloat().toDouble
    StructUtils
      .fromStruct(Struct(Map("optional_int32" -> Value(Value.Kind.NumberValue(someFaultyInt)))))
      .left
      .value must be(
      StructParsingError(
        s"""Field "protobuf_unittest.TestAllTypes.optional_int32" is of type "Int" but received "NumberValue($someFaultyInt)""""
      )
    )
  }

  "fromStruct with faulty long field" should "pass" in {
    val someFaultyLong = "Hi"
    StructUtils
      .fromStruct(Struct(Map("optional_int64" -> Value(Value.Kind.StringValue(someFaultyLong)))))
      .left
      .value must be(
      StructParsingError(
        """Field "protobuf_unittest.TestAllTypes.optional_int64" is of type long but received invalid long value "Hi""""
      )
    )
  }

  "fromStruct with int field value for string field key" should "pass" in {
    val someFaultyString = Random.nextInt().toDouble
    StructUtils
      .fromStruct(Struct(Map("optional_string" -> Value(Value.Kind.NumberValue(someFaultyString)))))
      .left
      .value must be(
      StructParsingError(
        s"""Field "protobuf_unittest.TestAllTypes.optional_string" is of type "String" but received "NumberValue($someFaultyString)""""
      )
    )
  }

  "fromStruct with boolean field value for string field key" should "pass" in {
    val someBooleanValue = Random.nextBoolean()
    StructUtils
      .fromStruct(Struct(Map("optional_string" -> Value(Value.Kind.BoolValue(someBooleanValue)))))
      .left
      .value must be(
      StructParsingError(
        s"""Field "protobuf_unittest.TestAllTypes.optional_string" is of type "String" but received "BoolValue($someBooleanValue)""""
      )
    )
  }

  "fromStruct with message field value for string field key" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(Map("optional_string" -> Value(Value.Kind.StructValue(Struct(Map.empty)))))
      )
      .left
      .value must be(
      StructParsingError(
        s"""Field "protobuf_unittest.TestAllTypes.optional_string" is of type "String" but received "StructValue(Struct(Map(),UnknownFieldSet(Map())))""""
      )
    )
  }

  "fromStruct with repeated field value for single field key" should "pass" in {
    StructUtils
      .fromStruct(
        Struct(
          Map(
            "optional_string" -> Value(
              Value.Kind.ListValue(ListValue(Seq(Value(Value.Kind.StringValue("Hi")))))
            )
          )
        )
      )
      .left
      .value must be(
      StructParsingError(
        """Field "protobuf_unittest.TestAllTypes.optional_string" is of type "String" but received "ListValue(ListValue(List(Value(StringValue(Hi),UnknownFieldSet(Map()))),UnknownFieldSet(Map())))""""
      )
    )
  }

  "fromStruct with string field value for int field key" should "pass" in {
    StructUtils
      .fromStruct(Struct(Map("optional_int32" -> Value(Value.Kind.StringValue("Hi")))))
      .left
      .value must be(
      StructParsingError(
        s"""Field "protobuf_unittest.TestAllTypes.optional_int32" is of type "Int" but received "StringValue(Hi)""""
      )
    )
  }

  "fromStruct with empty optional int field" should "pass" in {
    StructUtils
      .fromStruct(Struct(Map("optional_int32" -> Value(Value.Kind.Empty))))
      .right
      .value must be(
      TestAllTypes()
    )
  }
}
