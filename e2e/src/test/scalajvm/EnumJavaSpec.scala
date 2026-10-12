import com.google.protobuf.{InvalidProtocolBufferException, UnknownFieldSet}
import com.thesamet.proto.e2e.Enum
import com.thesamet.proto.e2e.`enum`._
import com.thesamet.proto.e2e.enum3._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class EnumJavaSpec extends AnyFlatSpec with Matchers with OptionValues {

  "Required Unrecognized enum" should "not work in Java" in {
    val scalaRequiredEnum = RequiredEnum(color = Color.Unrecognized(17))
    assertThrows[IllegalArgumentException] {
      RequiredEnum.toJavaProto(scalaRequiredEnum)
    }
    assertThrows[InvalidProtocolBufferException] {
      Enum.RequiredEnum.parseFrom(scalaRequiredEnum.toByteArray)
    }
  }

  val javaOptionalUnrecognizedEnum =
    Enum.EnumTest
      .newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().mergeVarintField(1, 17).build())
      .build()

  val javaRepeatedUnrecognizedEnum = Enum.EnumTest
    .newBuilder()
    .addRepeatedColor(Enum.Color.RED)
    .setUnknownFields(
      UnknownFieldSet.newBuilder().mergeVarintField(4, 17).mergeVarintField(4, 22).build()
    )
    .build()

  val scalaRequiredUnrecognizedEnum = RequiredEnum(color = Color.Unrecognized(17))

  val scalaOptionalUnrecognizedEnum = EnumTest(color = Some(Color.Unrecognized(17)))

  val scalaRepeatedUnrecognizedEnum = EnumTest(
    repeatedColor = Seq(Color.Unrecognized(17), Color.RED, Color.Unrecognized(22))
  )

  val scalaRepeatedUnrecognizedEnumModified = EnumTest(
    repeatedColor = Seq(Color.RED, Color.Unrecognized(17), Color.Unrecognized(22))
  )

  "Required Unrecognized enum" should "not be parsable by Java or convertible to Java" in {
    // This documents that Java proto2 is unable to parse a message with required
    // unrecognized enum.
    // See https://github.com/scalapb/ScalaPB/issues/391
    intercept[InvalidProtocolBufferException] {
      Enum.RequiredEnum.parseFrom(scalaRequiredUnrecognizedEnum.toByteArray)
    }
    intercept[IllegalArgumentException] {
      RequiredEnum.toJavaProto(scalaRequiredUnrecognizedEnum)
    }
  }

  "Proto2 with optional unrecognized enum" should "not be convertible to Java" in {
    intercept[IllegalArgumentException] {
      EnumTest.toJavaProto(scalaOptionalUnrecognizedEnum) must be(javaOptionalUnrecognizedEnum)
    }
    // ScalaPB will not see the unknown field:
    EnumTest.fromJavaProto(javaOptionalUnrecognizedEnum) must be(EnumTest())
  }

  "Proto2 with optional unrecognized enum" should "have same binary representation that is usable" in {
    scalaOptionalUnrecognizedEnum.toByteArray must be(javaOptionalUnrecognizedEnum.toByteArray)
    EnumTest.parseFrom(scalaOptionalUnrecognizedEnum.toByteArray) must be(
      scalaOptionalUnrecognizedEnum
    )
    Enum.EnumTest.parseFrom(scalaOptionalUnrecognizedEnum.toByteArray) must be(
      javaOptionalUnrecognizedEnum
    )
  }

  "Proto2 with repeated enum field of unrecognized values" should "not be convertable to Java" in {
    intercept[IllegalArgumentException] {
      EnumTest.toJavaProto(scalaRepeatedUnrecognizedEnum) must be(javaRepeatedUnrecognizedEnum)
    }
    // ScalaPB will ignore the unknown fields, and will only see the recognized enum value.
    EnumTest.fromJavaProto(javaRepeatedUnrecognizedEnum) must be(
      EnumTest(repeatedColor = Seq(Color.RED))
    )
  }

  "Proto2 with repeated enum field of unrecognized values" should "cause reorder of enum fields" in {
    Enum.EnumTest.parseFrom(scalaRepeatedUnrecognizedEnum.toByteArray) must be(
      javaRepeatedUnrecognizedEnum
    )
    EnumTest.parseFrom(javaRepeatedUnrecognizedEnum.toByteArray) must be(
      scalaRepeatedUnrecognizedEnumModified
    )
  }

  "Proto3 with optional unrecognized enum" should "support conversion to/from java" in {
    val proto3 = EnumTest3(color = Color3.Unrecognized(17))
    val asJava = EnumTest3.toJavaProto(proto3)
    EnumTest3.fromJavaProto(asJava) must be(proto3)
  }

  "Proto3 with optional unrecognized enum" should "have same binary representation that is usable" in {
    val proto3 = EnumTest3(color = Color3.Unrecognized(17))
    proto3.toByteArray must be(EnumTest3.toJavaProto(proto3).toByteArray)
    EnumTest3.parseFrom(proto3.toByteArray) must be(proto3)
  }

  "Proto3 with repeated unrecognized enum" should "support conversion to/from java" in {
    val proto3 =
      EnumTest3(colorVector = Seq(Color3.Unrecognized(17), Color3.C3_RED, Color3.Unrecognized(22)))
    EnumTest3.fromJavaProto(EnumTest3.toJavaProto(proto3)) must be(proto3)
  }

  "Proto3 with repeated unrecognized enum" should "have same binary representation that is usable" in {
    val proto3 =
      EnumTest3(colorVector = Seq(Color3.Unrecognized(17), Color3.C3_RED, Color3.Unrecognized(22)))
    EnumTest3.fromJavaProto(EnumTest3.toJavaProto(proto3)) must be(proto3)
    proto3.toByteArray must be(EnumTest3.toJavaProto(proto3).toByteArray)
    EnumTest3.parseFrom(proto3.toByteArray) must be(proto3)
  }
}
