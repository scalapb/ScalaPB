import com.trueaccord.proto.e2e.extensions._
import org.scalatest._

class ExtensionsSpec extends FlatSpec with MustMatchers with OptionValues {
  "BaseMessage.parseFrom" should "parse unknown fields" in {
    val helper = Helper(optInt = Some(37), optString = Some("foo"))
    val extended = BaseMessage.parseFrom(helper.toByteArray)
    extended.extension(Extension.optInt) must be (Some(37))
    extended.extension(Extension.optString) must be (Some("foo"))
  }

  "BaseMessage.parseFrom" should "parse unknown fields with duplication" in {
    val repeatedHelper = RepeatedHelper(optInt = Seq(37, 12), optString = Seq("foo", "bar"))
    val extended = BaseMessage.parseFrom(repeatedHelper.toByteArray)
    extended.extension(Extension.optInt) must be (Some(12))
    extended.extension(Extension.optString) must be (Some("bar"))
  }
}
