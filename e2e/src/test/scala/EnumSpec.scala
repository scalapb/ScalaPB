import com.trueaccord.proto.e2e.EnumPB._
import org.scalatest._

class EnumSpec extends FlatSpec with MustMatchers {
  val red = EnumTest(color = Some(Color.RED))
  val green = EnumTest(color = Some(Color.GREEN))
  val blue = EnumTest(color = Some(Color.BLUE))
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

    green.color.get.isRed must be(false)
    green.color.get.isGreen must be(true)
    green.color.get.isBlue must be(false)

    blue.color.get.isRed must be(false)
    blue.color.get.isGreen must be(false)
    blue.color.get.isBlue must be(true)

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
        case None => "none"
    }

    colorWord(blue.color) must be("blue")
    colorWord(red.color) must be("red")
    colorWord(green.color) must be("green")
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

}