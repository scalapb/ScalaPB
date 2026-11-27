import com.google.protobuf.InvalidProtocolBufferException
import com.thesamet.proto.e2e.lazy_utf8_string_fields._
import com.thesamet.proto.e2e.lazy_option_combinations.{LazyDefaults, EagerDefaults}
import com.thesamet.proto.e2e.lazy_relaxed_utf8.LazyRelaxedUtf8
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.LazyField

class LazyOptionCombinationsSpec extends AnyFlatSpec with Matchers {
  "Lazy unboxed strings" should "use defaults and round-trip explicit values" in {
    LazyUnboxed.parseFrom(Array.emptyByteArray).text.value shouldBe ""
    LazyUnboxed.parseFrom(Array[Byte](10, 0)).text.value shouldBe ""
    for (value <- Seq("", "héllo 日本語")) {
      val original = LazyUnboxed(text = LazyField.from(value))
      LazyUnboxed.parseFrom(original.toByteArray) shouldBe original
      LazyUnboxed.fromAscii(original.toProtoString) shouldBe original
      LazyUnboxed.messageReads.read(original.toPMessage) shouldBe original
    }
  }

  "Lazy proto2 strings" should "preserve defaults and enforce required fields" in {
    val default = LazyDefaults.defaultInstance
    default.requiredText.value shouldBe "héllo"
    default.optionalText shouldBe None
    default.getOptionalText.value shouldBe "日本語"
    default.unboxedText.value shouldBe "fallback"
    intercept[InvalidProtocolBufferException] {
      LazyDefaults.parseFrom(Array.emptyByteArray)
    }
    for (optional <- Seq(None, Some(LazyField.from("")), Some(LazyField.from("日本語")))) {
      val original = default.copy(optionalText = optional, unboxedText = LazyField.from(""))
      LazyDefaults.parseFrom(original.toByteArray) shouldBe original
      LazyDefaults.fromAscii(original.toProtoString) shouldBe original
      LazyDefaults.messageReads.read(original.toPMessage) shouldBe original
    }
  }

  "Explicit unboxed defaults" should "preserve empty strings and bytes" in {
    for (value <- Seq("", "fallback", "héllo")) {
      val original =
        EagerDefaults(text = value, data = com.google.protobuf.ByteString.copyFromUtf8(value))
      EagerDefaults.parseFrom(original.toByteArray) shouldBe original
      EagerDefaults.fromAscii(original.toProtoString) shouldBe original
    }
  }

  "Lazy reflection and text format" should "round-trip every field shape" in {
    for (
      original <- Seq(
        LazyPresence(),
        LazyPresence(optionalText = Some(LazyField.from(""))).withText(LazyField.from("日本語")),
        LazyPresence().withCount(0)
      )
    ) {
      LazyPresence.fromAscii(original.toProtoString) shouldBe original
      LazyPresence.messageReads.read(original.toPMessage) shouldBe original
    }
    val repeated = LazyRepeated(items = Seq(LazyField.from(""), LazyField.from("héllo")))
    LazyRepeated.fromAscii(repeated.toProtoString) shouldBe repeated
    LazyRepeated.messageReads.read(repeated.toPMessage) shouldBe repeated
    val mapped = LazyDictionary(intToString = Map(1 -> LazyField.from("日本語")))
    LazyDictionary.fromAscii(mapped.toProtoString) shouldBe mapped
    LazyDictionary.messageReads.read(mapped.toPMessage) shouldBe mapped
    val nested = LazyWithRecursion(
      data = LazyField.from("outer"),
      nested = Some(LazyWithRecursion(data = LazyField.from("inner")))
    )
    LazyWithRecursion.fromAscii(nested.toProtoString) shouldBe nested
    LazyWithRecursion.messageReads.read(nested.toPMessage) shouldBe nested
  }

  "Lazy relaxed UTF-8" should "preserve repeated and oneof bytes even after decoding" in {
    val wire   = Array(10, 1, 255, 26, 1, 255).map(_.toByte)
    val parsed = LazyRelaxedUtf8.parseFrom(wire)
    parsed.toByteArray shouldBe wire
    parsed.items.head.value shouldBe "\uFFFD"
    parsed.choice.text.get.value shouldBe "\uFFFD"
    parsed.toByteArray shouldBe wire
    intercept[InvalidProtocolBufferException] {
      LazyRelaxedUtf8.parseFrom(Array(18, 5, 8, 1, 18, 1, 255).map(_.toByte))
    }
    val valid = LazyRelaxedUtf8(
      items = Seq(LazyField.from("héllo")),
      mapped = Map(1 -> LazyField.from("日本語"))
    ).withText(LazyField.from(""))
    LazyRelaxedUtf8.parseFrom(valid.toByteArray) shouldBe valid
    LazyRelaxedUtf8.fromAscii(valid.toProtoString) shouldBe valid
    LazyRelaxedUtf8.messageReads.read(valid.toPMessage) shouldBe valid
  }
}
