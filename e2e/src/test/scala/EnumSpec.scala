import com.google.protobuf.{InvalidProtocolBufferException, UnknownFieldSet}
import com.trueaccord.proto.e2e.Enum
import com.trueaccord.proto.e2e.`enum`._
import com.trueaccord.proto.e2e.enum3._
import com.trueaccord.scalapb.GeneratedEnumCompanion
import org.scalatest._

class EnumSpec extends FlatSpec with MustMatchers with OptionValues {
  val red = EnumTest(color = Some(Color.RED))
  val green = EnumTest(color = Some(Color.GREEN))
  val blue = EnumTest(color = Some(Color.BLUE))
  val unrecognized = EnumTest(color = Some(Color.Unrecognized(37)))
  val noColor = EnumTest()
  val innerEnum = EnumTest(innerEnum = Some(EnumTest.InnerEnum.INNER_SUCCESS))
  val otherCase = EnumTest(innerEnum = Some(EnumTest.InnerEnum.OtherCase))

  "colors" should "serialize and parse" in {
    EnumTest.parseFrom(red.toByteArray) must be(red)
    EnumTest.parseFrom(green.toByteArray) must be(green)
    EnumTest.parseFrom(blue.toByteArray) must be(blue)
    EnumTest.parseFrom(noColor.toByteArray) must be(noColor)
    EnumTest.parseFrom(innerEnum.toByteArray) must be(innerEnum)
    EnumTest.parseFrom(otherCase.toByteArray) must be(otherCase)
    EnumTest.parseFrom(unrecognized.toByteArray) must be(unrecognized)
  }

  "isEnumValue" should "return correct values" in {
    red.color.get.isRed must be(true)
    red.color.get.isGreen must be(false)
    red.color.get.isBlue must be(false)
    red.color.get.isUnrecognized must be(false)

    green.color.get.isRed must be(false)
    green.color.get.isGreen must be(true)
    green.color.get.isBlue must be(false)
    green.color.get.isUnrecognized must be(false)

    blue.color.get.isRed must be(false)
    blue.color.get.isGreen must be(false)
    blue.color.get.isBlue must be(true)
    blue.color.get.isUnrecognized must be(false)

    unrecognized.color.get.isRed must be(false)
    unrecognized.color.get.isGreen must be(false)
    unrecognized.color.get.isBlue must be(false)
    unrecognized.color.get.isUnrecognized must be(true)

    innerEnum.getInnerEnum.isInnerSuccess must be(true)
    innerEnum.getInnerEnum.isOtherCase must be(false)
    otherCase.getInnerEnum.isInnerSuccess must be(false)
    otherCase.getInnerEnum.isOtherCase must be(true)
  }

  "pattern matching" should "work for enums" in {
    def colorWord(color: Option[Color]) = color match {
        case Some(Color.BLUE) => "blue"
        case Some(Color.GREEN) => "green"
        case Some(Color.RED) => "red"
        case Some(Color.Unrecognized(x)) => s"unrecognized:$x"
        case None => "none"
    }

    colorWord(blue.color) must be("blue")
    colorWord(red.color) must be("red")
    colorWord(green.color) must be("green")
    colorWord(unrecognized.color) must be("unrecognized:37")
    colorWord(noColor.color) must be("none")
  }

  "getColor" should "return first value" in {
    noColor.getColor must be(Color.RED)
  }

  "getOtherColor" should "return default value" in {
    noColor.getOtherColor must be(Color.BLUE)
    red.getOtherColor must be(Color.BLUE)
    green.getOtherColor must be(Color.BLUE)
    blue.getOtherColor must be(Color.BLUE)
    unrecognized.getOtherColor must be(Color.BLUE)
    blue.getOtherColor.isBlue must be(true)
  }

  "update" should "work correctly" in {
    red.update(_.color := Color.BLUE) must be(blue)
    noColor.update(_.color := Color.RED) must be(red)
  }

  "concatenated serialized" should "result in merged object" in {
    val bytes = (red.toByteArray ++ green.toByteArray ++ otherCase.toByteArray)
    val obj = EnumTest.parseFrom(bytes)
    obj must be(EnumTest(color = Some(Color.GREEN),
        innerEnum = Some(EnumTest.InnerEnum.OtherCase)))
  }

  "missing enum values in proto3" should "be preserved in parsing" in {
    val like = EnumTestLike(color = 18)  // same field number as `color` in EnumTest3.
    val e3 = EnumTest3.parseFrom(like.toByteArray)
    e3.color must be (Color3.Unrecognized(18))
    e3.color must not be (Color3.Unrecognized(19))
    e3.toByteArray must be (like.toByteArray)
  }

  "missing enum values in proto3 seq" should "be preserved in parsing" in {
    val e3 = EnumTest3(colorVector = Seq(Color3.C3_RED, Color3.Unrecognized(15), Color3.C3_BLUE))
    EnumTest3.parseFrom(e3.toByteArray) must be (e3)
  }

  "missing enum values in proto2" should "be preserved in parsing" in {
    val like = EnumTestLike(color = 18)  // same field number as `color` in EnumTest3.
    val e3 = EnumTest.parseFrom(like.toByteArray)
    e3.getColor must be (Color.Unrecognized(18))
    e3.getColor must not be (Color.Unrecognized(19))
    e3.toByteArray must be (like.toByteArray)
  }

  "color companion" should "be available implicitly" in {
    implicitly[GeneratedEnumCompanion[Color]] must be (Color)
  }

  "fromName" should "resolve values" in {
    Color.fromName("RED").value must be(Color.RED)
    Color.fromName("GREEN").value must be(Color.GREEN)
    Color.fromName("BLUE").value must be(Color.BLUE)
    Color.fromName("FUCHSIA") must be(None)
  }

  "toByteString" should "give the same byte array as toByteArray" in {
    val e3 = EnumTest3(colorVector = Seq(Color3.C3_RED, Color3.Unrecognized(15), Color3.C3_BLUE))
    e3.toByteString.toByteArray must be (e3.toByteArray)
  }

  "Unrecognized" should "be printable" in {
    // See https://github.com/scalapb/ScalaPB/issues/225
    unrecognized.toProtoString must be ("color: 37\n")
  }

  "Unrecognized" should "be fine" in {
    var x = Color.Unrecognized(117).scalaValueDescriptor  // Do not use 117 elsewhere we need to have it gc'ed.
    var y = Color.Unrecognized(117).scalaValueDescriptor
    x must be theSameInstanceAs y
    x = null
    y = null
    System.gc()
    x = Color.Unrecognized(117).scalaValueDescriptor
  }

  "Required Unrecognized enum" should "support conversion to Java" in {
    /*
    val scalaRequiredEnum = RequiredEnum(color = Color.Unrecognized(17))
    val javaRequiredEnum = RequiredEnum.toJavaProto(scalaRequiredEnum)
    RequiredEnum.fromJavaProto(javaRequiredEnum) must be (scalaRequiredEnum)
    RequiredEnum.parseFrom(javaRequiredEnum.toByteArray) must be (scalaRequiredEnum)
    */
  }

  val scalaRequiredUnrecognizedEnum = RequiredEnum(color = Color.Unrecognized(17))

  val scalaOptionalUnrecognizedEnum = EnumTest(color = Some(Color.Unrecognized(17)))
  val javaOptionalUnrecognizedEnum =
    Enum.EnumTest
      .newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().mergeVarintField(1, 17).build())
      .build()

  val scalaRepeatedUnrecognizedEnum = EnumTest(
    repeatedColor = Seq(Color.Unrecognized(17), Color.RED, Color.Unrecognized(22)))

  val scalaRepeatedUnrecognizedEnumModified = EnumTest(
    repeatedColor = Seq(Color.RED, Color.Unrecognized(17), Color.Unrecognized(22)))

  val javaRepeatedUnrecognizedEnum = Enum.EnumTest
    .newBuilder()
    .addRepeatedColor(Enum.Color.RED)
    .setUnknownFields(UnknownFieldSet.newBuilder().mergeVarintField(4, 17).mergeVarintField(4, 22).build())
    .build()

  "Required Unrecognized enum" should "not be parsable by Java or convertible to Java" in {
    // This documents that Java proto2 is unable to parse a message with required
    // unrecognized enum.
    // See https://github.com/scalapb/ScalaPB/issues/391
    intercept[InvalidProtocolBufferException] {
      val javaRequiredEnum = Enum.RequiredEnum.parseFrom(scalaRequiredUnrecognizedEnum.toByteArray)
    }
    intercept[IllegalArgumentException] {
      val javaRequiredEnum = RequiredEnum.toJavaProto(scalaRequiredUnrecognizedEnum)
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
    EnumTest.parseFrom(scalaOptionalUnrecognizedEnum.toByteArray) must be(scalaOptionalUnrecognizedEnum)
    Enum.EnumTest.parseFrom(scalaOptionalUnrecognizedEnum.toByteArray) must be(javaOptionalUnrecognizedEnum)
  }

  "Proto2 with repeated enum field of unrecognized values" should "not be convertable to Java" in {
    intercept[IllegalArgumentException] {
      EnumTest.toJavaProto(scalaRepeatedUnrecognizedEnum) must be(javaRepeatedUnrecognizedEnum)
    }
    // ScalaPB will ignore the unknown fields, and will only see the recognized enum value.
    EnumTest.fromJavaProto(javaRepeatedUnrecognizedEnum) must be(EnumTest(repeatedColor = Seq(Color.RED)))
  }

  "Proto2 with repeated enum field of unrecognized values" should "cause reorder of enum fields" in {
    Enum.EnumTest.parseFrom(scalaRepeatedUnrecognizedEnum.toByteArray) must be(javaRepeatedUnrecognizedEnum)
    EnumTest.parseFrom(javaRepeatedUnrecognizedEnum.toByteArray) must be(scalaRepeatedUnrecognizedEnumModified)
  }

  "Proto3 with optional unrecognized enum" should "support convertion to/from java" in {
    val proto3 = EnumTest3(color = Color3.Unrecognized(17))
    val asJava = EnumTest3.toJavaProto(proto3)
    EnumTest3.fromJavaProto(asJava) must be(proto3)
  }

  "Proto3 with optional unrecognized enum" should "have same binary representation that is usable" in {
    val proto3 = EnumTest3(color = Color3.Unrecognized(17))
    proto3.toByteArray must be(EnumTest3.toJavaProto(proto3).toByteArray)
    EnumTest3.parseFrom(proto3.toByteArray) must be(proto3)
  }

  "Proto3 with repeated unrecognized enum" should "support convertion to/from java" in {
    val proto3 = EnumTest3(colorVector = Seq(Color3.Unrecognized(17), Color3.C3_RED, Color3.Unrecognized(22)))
    EnumTest3.fromJavaProto(EnumTest3.toJavaProto(proto3)) must be(proto3)
  }

  "Proto3 with repeated unrecognized enum" should "have same binary representation that is usable" in {
    val proto3 = EnumTest3(colorVector = Seq(Color3.Unrecognized(17), Color3.C3_RED, Color3.Unrecognized(22)))
    EnumTest3.fromJavaProto(EnumTest3.toJavaProto(proto3)) must be(proto3)
    proto3.toByteArray must be(EnumTest3.toJavaProto(proto3).toByteArray)
    EnumTest3.parseFrom(proto3.toByteArray) must be(proto3)
  }
}
