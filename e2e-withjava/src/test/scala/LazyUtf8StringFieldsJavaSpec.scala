import com.thesamet.proto.e2e.lazy_utf8_string_fields_java._
import com.thesamet.proto.e2e.LazyUtf8StringFieldsJava
import com.google.protobuf.ByteString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.{LazyDecoder, LazyField}
import scala.language.implicitConversions

class LazyUtf8StringFieldsJavaSpec extends AnyFlatSpec with Matchers {
  "Lazy repeated fields" should "work correctly through java conversion" in {
    val original = LazyMessage(str = "hello", int = 42)

    val bytes         = original.toByteArray
    val javaParsed    = LazyUtf8StringFieldsJava.LazyMessage.parseFrom(bytes)
    val javaConverted = LazyMessage.toJavaProto(original)

    javaParsed.getStr() shouldBe "hello"
    javaConverted.getStr() shouldBe "hello"

    val scalaParsed    = LazyMessage.parseFrom(javaParsed.toByteArray())
    val scalaConverted = LazyMessage.fromJavaProto(javaConverted)

    scalaParsed.str.value shouldBe "hello"
    scalaConverted.str.value shouldBe "hello"

    // Real repeated fields, not just scalar strings, including non-ASCII values
    // that exercise the UTF-8 encode/decode bridge.
    val repeatedOriginal = LazyRepeatedStrings(items = Seq("a", "héllo", "日本語"))
    val repeatedJava     = LazyRepeatedStrings.toJavaProto(repeatedOriginal)
    repeatedJava.getItemsList().size() shouldBe 3
    repeatedJava.getItemsList().get(0) shouldBe "a"
    repeatedJava.getItemsList().get(1) shouldBe "héllo"
    repeatedJava.getItemsList().get(2) shouldBe "日本語"

    val repeatedScala = LazyRepeatedStrings.fromJavaProto(repeatedJava)
    repeatedScala.items.map(_.value) shouldBe Seq("a", "héllo", "日本語")

    // Empty repeated fields round-trip through addAll/get with no elements.
    LazyRepeatedStrings
      .fromJavaProto(LazyRepeatedStrings.toJavaProto(LazyRepeatedStrings()))
      .items shouldBe Seq.empty

    // A Java proto cannot be built from invalid UTF-8: the conversion throws
    // (mirroring the singular set<Name>Bytes behavior).
    val invalidUtf8 = ByteString.copyFrom(Array(0xc3.toByte, 0x28.toByte))
    val withInvalid = LazyRepeatedStrings(items = Seq(LazyField(invalidUtf8)))
    intercept[IllegalArgumentException] {
      LazyRepeatedStrings.toJavaProto(withInvalid)
    }
  }

  // Round-trips map<int32,string> and map<string,string> values between the Scala
  // and Java APIs; only the values are lazy, keys stay plain Strings.
  "Lazy map fields" should "convert correctly through java conversion" in {
    val original = LazyMapMessage(
      intToString = Map(1 -> "one", 2 -> "héllo"),
      strToStr = Map("日本語" -> "héllo")
    )

    val javaConverted = LazyMapMessage.toJavaProto(original)
    javaConverted.getIntToStringMap().size() shouldBe 2
    javaConverted.getIntToStringMap().get(2) shouldBe "héllo"
    javaConverted.getStrToStrMap().get("日本語") shouldBe "héllo"

    val scalaConverted = LazyMapMessage.fromJavaProto(javaConverted)
    scalaConverted.intToString(2).value shouldBe "héllo"
    scalaConverted.strToStr("日本語").value shouldBe "héllo"

    // Empty maps round-trip through putAll/get with no elements.
    val empty = LazyMapMessage(intToString = Map.empty, strToStr = Map.empty)
    LazyMapMessage.fromJavaProto(LazyMapMessage.toJavaProto(empty)).intToString shouldBe Map.empty
  }
}
