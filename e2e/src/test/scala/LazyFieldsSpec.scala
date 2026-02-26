import com.google.protobuf.ByteString
import com.thesamet.proto.e2e.lazy_fields._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.LazyField

class LazyFieldsSpec extends AnyFlatSpec with Matchers {
  "LazyMessage" should "have lazy string fields" in {
    val lazyMessage = LazyMessage(
      str = LazyField(ByteString.copyFromUtf8("a lazy string")),
      int = 123
    )
    lazyMessage.str shouldBe a[LazyField[_]]
    lazyMessage.str.value shouldBe "a lazy string"
  }

  "NotLazyMessage" should "have normal string fields" in {
    val notLazyMessage = NotLazyMessage(
      notLazyString = "not a lazy string"
    )
    notLazyMessage.notLazyString shouldBe a[String]
    notLazyMessage.notLazyString shouldBe "not a lazy string"
  }
}
