import com.thesamet.proto.any._
import com.google.protobuf.any.Any
import com.thesamet.proto.e2e.`enum`.EnumTest
import com.thesamet.proto.e2e.`enum`.Color
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class AnySpec extends AnyFlatSpec with Matchers {
  val green = EnumTest(color = Some(Color.GREEN))

  "Any" should "have standard fields" in {
    val t = AnyTestMessage()
      .update(
        _.myAny.typeUrl := "https://example.com",
        _.myAny.value   := com.google.protobuf.ByteString.copyFromUtf8("foo")
      )
    t.myAny.get.is[com.google.protobuf.any.Any] must be(false)
  }

  "Any.pack" should "be the inverse of unpack" in {
    val t = AnyTestMessage()
      .update(_.myAny := Any.pack(green))

    t.myAny.get.is[com.google.protobuf.any.Any] must be(false)
    t.myAny.get.is[EnumTest] must be(true)
    t.myAny.get.unpack[EnumTest] must be(green)
    t.update(_.myAny.typeUrl := "foobar/com.thesamet.proto.e2e.EnumTest")
      .myAny
      .get
      .unpack[EnumTest] must be(green)
    intercept[IllegalArgumentException] {
      t.update(_.myAny.typeUrl := "foobar/com.thesamet.proto.e2e.EnumTestWrong")
        .myAny
        .get
        .unpack[EnumTest]
    }
  }
}
