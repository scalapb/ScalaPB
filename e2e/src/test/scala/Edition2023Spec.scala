import com.thesamet.proto.e2e.edition2023.Edition2023Message
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2023Spec extends AnyFlatSpec with Matchers {
  "Edition 2023 messages" should "preserve explicit presence and repeated fields" in {
    val absent  = Edition2023Message()
    val present = Edition2023Message(value = Some(""), numbers = Seq(1, 2, 3))

    Edition2023Message.parseFrom(absent.toByteArray).value must be(None)
    Edition2023Message.parseFrom(present.toByteArray) must be(present)
    present.toByteArray.toSeq must be(Seq[Byte](10, 0, 18, 3, 1, 2, 3))
  }
}
