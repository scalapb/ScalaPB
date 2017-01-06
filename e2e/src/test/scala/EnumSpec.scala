import com.trueaccord.proto.e2e.enum._
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

}
