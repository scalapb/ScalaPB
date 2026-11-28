import com.google.protobuf.InvalidProtocolBufferException
import com.thesamet.proto.e2e.lazy_utf8_string_fields.LazyPresence
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.LazyField

class LazyUtf8PresenceSpec extends AnyFlatSpec with Matchers {
  "Lazy optional and oneof strings" should "distinguish absent and present empty values" in {
    val absent = LazyPresence.parseFrom(Array.emptyByteArray)
    absent.optionalText shouldBe None
    absent.choice.isEmpty shouldBe true

    for (value <- Seq("", "héllo 日本語")) {
      val original = LazyPresence(optionalText = Some(LazyField.from(value)))
        .withText(LazyField.from(value))
      val parsed = LazyPresence.parseFrom(original.toByteArray)
      parsed.optionalText.map(_.value) shouldBe Some(value)
      parsed.choice.text.map(_.value) shouldBe Some(value)
      parsed shouldBe original
    }

    val empty = LazyPresence.parseFrom(Array[Byte](0x0a, 0, 0x12, 0))
    empty.optionalText.map(_.value) shouldBe Some("")
    empty.choice.text.map(_.value) shouldBe Some("")
    empty.toByteArray shouldBe Array[Byte](0x0a, 0, 0x12, 0)
  }

  it should "preserve the selected oneof alternative and use the last value on the wire" in {
    val numeric = LazyPresence().withCount(0)
    LazyPresence.parseFrom(numeric.toByteArray) shouldBe numeric
    // An empty string is present and replaces the earlier numeric alternative.
    LazyPresence.parseFrom(Array[Byte](0x18, 7, 0x12, 0)).choice.text.map(_.value) shouldBe Some("")
    LazyPresence.parseFrom(Array[Byte](0x12, 0, 0x18, 7)).choice.count shouldBe Some(7)
  }

  it should "reject malformed UTF-8 before optional or oneof strings are accessed" in {
    for (tag <- Seq(0x0a, 0x12)) {
      intercept[InvalidProtocolBufferException] {
        LazyPresence.parseFrom(Array(tag.toByte, 1.toByte, 0xff.toByte))
      }
    }
  }
}
