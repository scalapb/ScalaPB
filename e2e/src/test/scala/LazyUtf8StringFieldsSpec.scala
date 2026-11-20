import java.util.concurrent.atomic.AtomicInteger
import com.google.protobuf.{ByteString, InvalidProtocolBufferException}
import com.thesamet.proto.e2e.lazy_utf8_string_fields._
import com.thesamet.proto.e2e.lazy_utf8_string_fields_custom_types._
import com.thesamet.proto.e2e.lazy_utf8_string_fields_eager_file._
import com.thesamet.proto.e2e.lazy_utf8_string_fields_lazy_file._
import com.thesamet.proto.e2e.lazy_utf8_string_fields_no_validation._
import com.thesamet.proto.e2e.lazy_utf8_string_fields_proto2._
import com.thesamet.pb.PersonId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.descriptors.PString
import scalapb.{LazyDecoder, LazyField}

import scala.collection.immutable.HashSet
import scala.language.implicitConversions

class LazyUtf8StringFieldsSpec extends AnyFlatSpec with Matchers {
  "NotLazyMessage" should "have normal string fields" in {
    val notLazyMessage = NotLazyMessage(
      notLazyString = "not a lazy string"
    )
    notLazyMessage.notLazyString shouldBe a[String]
    notLazyMessage.notLazyString shouldBe "not a lazy string"
  }

  "LazyField" should "be decoded only once" in {
    val callCount = new AtomicInteger(0)
    implicit val countingDecoder: LazyDecoder[String] = new LazyDecoder[String] {
      def decode(bytes: ByteString): String = {
        callCount.incrementAndGet()
        bytes.toStringUtf8()
      }
    }

    // We test LazyField directly, since parseFrom does not pick up local implicits.
    // LazyField.apply however, does.
    val lazyField = LazyField(ByteString.copyFromUtf8("a lazy string"))

    // Field is not evaluated yet.
    lazyField.toByteString shouldBe a[ByteString]
    callCount.get() shouldBe 0

    // First access evaluates the field.
    lazyField.value shouldBe "a lazy string"
    callCount.get() shouldBe 1

    // Second access does not re-evaluate.
    lazyField.value shouldBe "a lazy string"
    callCount.get() shouldBe 1
  }

  "Lazy repeated fields" should "work correctly through serialization" in {
    val originalItems = Seq("hello", "world", "again")
    val original = LazyRepeated(items = originalItems)
    
    val bytes = original.toByteArray
    val parsed = LazyRepeated.parseFrom(bytes)

    def f(str: Seq[String]): Int = str.length

    f(parsed.items) shouldBe parsed.items.length

    parsed.items should contain theSameElementsAs originalItems
  }

  /**
   * This is necessary for more intuitive dictionary behavior.
   * We sacrifice potential performance to avoid potential problems
   * with untyped collections: Map[Any, T] in this specifically.
   *
   * See related "LazyField untyped collections" spec.
   * */
  "Lazy dictionary fields" should "parse keys as raw strings" in {
    import scalapb.LazyEncoder
    val originalStringToInt = Map("hello" -> 1, "world" -> 2)
    val originalIntToString = Map(1 -> "hello", 2 -> "world")
    val original = LazyDictionary(stringToInt = originalStringToInt, intToString = originalIntToString)
    
    val bytes = original.toByteArray
    val parsed = LazyDictionary.parseFrom(bytes)

    // keys parses as raw strings
    parsed.stringToInt.keys.head shouldBe a[String]
    parsed.stringToInt should contain theSameElementsAs originalStringToInt

    // values parses as LazyField
    parsed.intToString.values.head shouldBe a[LazyField[_]]
    parsed.intToString should contain theSameElementsAs originalIntToString
  }

  "Lazy nested messages" should "work correctly through serialization" in {
    val nested = LazyWithRecursion(data = LazyField(ByteString.copyFromUtf8("inside")))
    val original = LazyWithRecursion(data = LazyField(ByteString.copyFromUtf8("outside")), nested = Some(nested))

    val bytes = original.toByteArray
    val parsed = LazyWithRecursion.parseFrom(bytes)

    parsed.data shouldBe a [LazyField[_]]
    parsed.nested.get.data shouldBe a [LazyField[_]]

    parsed.data.value shouldBe "outside"
    parsed.nested.get.data.value shouldBe "inside"
  }

  "LazyField" should "implicitly convert to and from its underlying type" in {
    val original = LazyWithRecursion(data = "a lazy string")
    original.data shouldBe a [LazyField[_]]

    original.data shouldBe "a lazy string"
    original.data.toUpperCase shouldBe "A LAZY STRING"
  }

  "LazyField for strings" should "concatenate with other strings" in {
    val lazyString = LazyField.from("world")
    val original = LazyWithRecursion(data = lazyString)

    s"hello, $lazyString" shouldBe "hello, world"
    "hello, " + lazyString shouldBe "hello, world"
    original.toProtoString shouldBe
      """data: "world"
        |""".stripMargin
    original.toString shouldBe "LazyWithRecursion(world,None,UnknownFieldSet(Map()))"
  }

  "Lenses" should "work with LazyField" in {
    val original = LazyWithRecursion(data = "a lazy string", nested = Some(LazyWithRecursion(data = "nested string", nested = Some(LazyWithRecursion(data = "another one nested string")))))

    val updated = original.update(_.nested.nested.data := "updated string")

    updated.nested.get.nested.get.data shouldBe "updated string"

  }

  "LazyField equality" should "work for LazyField ant case classes" in {
    val lazy1 = LazyField(ByteString.copyFromUtf8("string"))
    val lazy2 = LazyField(ByteString.copyFromUtf8("string"))
    val lazy3 = LazyField(ByteString.copyFromUtf8("another"))
    
    (lazy1 == lazy2) shouldBe true
    (lazy2 == lazy1) shouldBe true
    (lazy1 == lazy3) shouldBe false
    (lazy3 == lazy1) shouldBe false
    
    val decoded = LazyWithRecursion(data = "string")
    decoded.data.value // force decoding
    val encoded = LazyWithRecursion.parseFrom(decoded.toByteArray)
    val another = LazyWithRecursion(data = "another")

    (decoded == encoded) shouldBe true
    (encoded == decoded) shouldBe true
    (encoded == another) shouldBe false

    val set1 = Set(decoded, encoded)
    set1.size shouldBe 1
    set1(decoded) shouldBe true
    set1(encoded) shouldBe true
    set1(another) shouldBe false
    val set2 = Set(encoded, decoded)
    set2.size shouldBe 1
    set2(decoded) shouldBe true
    set2(encoded) shouldBe true
    set2(another) shouldBe false
  }

  "LazyField in typed collections" should "behave correctly due to explicit conversion" in {
    val s: String = "foobar"
    val lazyS: LazyField[String] = LazyField(ByteString.copyFromUtf8(s))

    val scalaSet = Set[String](s, lazyS)
    scalaSet.size shouldBe 1

    val scalaMap = Map[String, String](
      s -> "string",
      lazyS.toString -> "lazy" // compiler forces explicit conversion for tuples
    )
    scalaMap.size shouldBe 1
    scalaMap(s) shouldBe "lazy"
  }

  // This test specifies counterintuitive behavior
  // Set[Any] or Map[Any, T] is not recommended for usage with LazyField!
  "LazyField in untyped collections" should "exhibit asymmetric equality behavior" in {
    val s: String = "foobar"
    val lazyS: LazyField[String] = LazyField(ByteString.copyFromUtf8(s))

    lazyS.equals(s) shouldBe true // LazyField is compared to a String
    s.equals(lazyS) shouldBe false // String is compared to a LazyField

    val anySet1 = Set[Any](lazyS, s)
    anySet1.size shouldBe 2

    val anySet2 = Set[Any](s, lazyS)
    anySet2.size shouldBe 1 // equals is not commutative so, so the order of the arguments is important

    val anyMap1 = Map[Any, String](lazyS -> "lazy", s -> "string")
    anyMap1.size shouldBe 2
    anyMap1(s) shouldBe "string"
    anyMap1(lazyS) shouldBe "lazy"

    val anyMap2 = Map[Any, String](s -> "string", lazyS -> "lazy")
    anyMap2.size shouldBe 1
    anyMap2(s) shouldBe "lazy"
  }

  "getField" should "get raw value" in {
    val original = LazyWithRecursion(data = "a lazy string")
    original.getFieldByNumber(1) shouldBe "a lazy string"
    original.getField(LazyWithRecursion.scalaDescriptor.findFieldByNumber(1).get) shouldBe PString("a lazy string")
  }

  "defaults" should "work in proto2" in {
    val lazyMessageProto2 = LazyMessageProto2.defaultInstance
    lazyMessageProto2.str shouldBe "default"
  }

  "MessageLevelLazyMaps" should "have lazy map string values when laziness is enabled on the message in an eager file" in {
    val m = MessageLevelLazyMaps(
      scalar = "s",
      stringToInt = Map("one" -> 1),
      intToString = Map(1 -> "one"),
      strToStr = Map("one" -> "1")
    )

    // The message-level option already works for plain string fields.
    m.scalar shouldBe a[LazyField[_]]
    // Map keys are never lazy.
    m.stringToInt.keys.head shouldBe a[String]
    // Values follow the message-level option even though the file-level option is false.
    m.intToString.values.head shouldBe a[LazyField[_]]
    m.strToStr.values.head shouldBe a[LazyField[_]]

    MessageLevelLazyMaps.parseFrom(m.toByteArray).intToString.values.head shouldBe a[LazyField[_]]
  }

  "MessageLevelEagerMaps" should "have eager map string values when laziness is disabled on the message in a lazy file" in {
    val m = MessageLevelEagerMaps(
      scalar = "s",
      stringToInt = Map("one" -> 1),
      intToString = Map(1 -> "one"),
      strToStr = Map("one" -> "1")
    )

    m.scalar shouldBe a[String]
    m.stringToInt.keys.head shouldBe a[String]
    // Values follow the message-level option even though the file-level option is true.
    m.intToString.values.head shouldBe a[String]
    m.strToStr.values.head shouldBe a[String]

    MessageLevelEagerMaps.parseFrom(m.toByteArray).intToString.values.head shouldBe a[String]
  }

  "FileEagerDefaultMaps" should "keep map string values eager without the message-level option" in {
    val m = FileEagerDefaultMaps(intToString = Map(1 -> "one"))

    m.intToString.values.head shouldBe a[String]
  }

  "LazyCustomTypes" should "preserve custom string types when laziness is enabled" in {
    val m = LazyCustomTypes(
      displayName = "display",
      personId = PersonId("p-1"),
      personIds = Map(1 -> PersonId("p-2")),
      friendIds = Seq(PersonId("f-1"))
    )

    // The plain sibling field stays lazy inside the same message.
    m.displayName shouldBe a[LazyField[_]]
    // Custom-typed fields take priority over laziness.
    m.personId shouldBe a[PersonId]
    m.personId shouldBe PersonId("p-1")
    m.personIds(1) shouldBe a[PersonId]
    m.personIds(1) shouldBe PersonId("p-2")
    m.friendIds.head shouldBe a[PersonId]
    m.friendIds.head shouldBe PersonId("f-1")

    val parsed = LazyCustomTypes.parseFrom(m.toByteArray)
    parsed.personId shouldBe PersonId("p-1")
    parsed.personIds(1) shouldBe PersonId("p-2")
    parsed.friendIds.head shouldBe PersonId("f-1")
  }

  "MessageLevelLazyCustomMap" should "preserve custom map value types when laziness is enabled on the message" in {
    val m = MessageLevelLazyCustomMap(personIds = Map(1 -> PersonId("p-1")))

    m.personIds(1) shouldBe a[PersonId]
    m.personIds(1) shouldBe PersonId("p-1")

    MessageLevelLazyCustomMap.parseFrom(m.toByteArray).personIds(1) shouldBe PersonId("p-1")
  }

  "LazyMessageProto2" should "keep presence-bearing custom string types eager" in {
    val m = LazyMessageProto2(str = "s", int = 1, personId = Some(PersonId("p-1")))

    m.personId shouldBe Some(PersonId("p-1"))
    LazyMessageProto2.parseFrom(m.toByteArray).personId shouldBe Some(PersonId("p-1"))
  }

  private def bytesOf(bs: Int*): Array[Byte] = bs.map(_.toByte).toArray

  "Lazy string fields with the default UTF-8 policy" should "reject malformed UTF-8 on parse" in {
    // field 1, length 1, 0xff
    an[InvalidProtocolBufferException] should be thrownBy LazyWithRecursion.parseFrom(bytesOf(0x0a, 0x01, 0xff))
    an[InvalidProtocolBufferException] should be thrownBy LazyMessage.parseFrom(bytesOf(0x0a, 0x01, 0xff))
    an[InvalidProtocolBufferException] should be thrownBy LazyRepeated.parseFrom(bytesOf(0x0a, 0x01, 0xff))
    // int_to_string entry {key: 1, value: 0xff}
    an[InvalidProtocolBufferException] should be thrownBy LazyDictionary.parseFrom(
      bytesOf(0x12, 0x05, 0x08, 0x01, 0x12, 0x01, 0xff)
    )
    // Opted-in schema policy, field without NONE override: still validates.
    an[InvalidProtocolBufferException] should be thrownBy LazyNoUtf8Validation.parseFrom(bytesOf(0x12, 0x01, 0xff))
  }

  "Lazy string fields with utf8_validation = NONE" should "keep the original malformed bytes" in {
    val input = bytesOf(0x0a, 0x01, 0xff)
    val parsed = LazyNoUtf8Validation.parseFrom(input)

    parsed.unchecked.get.toByteString shouldBe ByteString.copyFrom(input, 2, 1)
    parsed.toByteArray shouldBe input
    parsed.unchecked.get.value should include("\uFFFD")
  }

  "Lazy string fields" should "parse valid UTF-8 under any policy" in {
    val text = "h\u00e9llo \u4e16\u754c \uD83D\uDE00"

    val strict = LazyWithRecursion.parseFrom(LazyWithRecursion(data = text).toByteArray)
    strict.data.value shouldBe text

    val noValidation = LazyNoUtf8Validation.parseFrom(
      LazyNoUtf8Validation(unchecked = Some(LazyField.from(text)), checked = Some(LazyField.from(text))).toByteArray
    )
    noValidation.unchecked.get.value shouldBe text
    noValidation.checked.get.value shouldBe text
  }


  "Lazy string fields" should "keep equals consistent with hashCode for lossy values" in {
    val a = LazyField.from("\uD800")
    val b = LazyField[String](a.bytes)

    (a == b) shouldBe false
    (b == a) shouldBe false
    HashSet(a, b).size shouldBe 2
    HashSet(b, a).size shouldBe 2
  }

  "Lazy string fields" should "treat distinct malformed bytes decoding to the same value as equal" in {
    val a = LazyField[String](ByteString.copyFrom(bytesOf(0xff)))
    val b = LazyField[String](ByteString.copyFrom(bytesOf(0xfe)))

    a.value shouldBe "\uFFFD"
    b.value shouldBe "\uFFFD"
    (a == b) shouldBe true
    (b == a) shouldBe true
    a.hashCode shouldBe b.hashCode
    HashSet(a, b).size shouldBe 1
    HashSet(b, a).size shouldBe 1
  }

  "Lazy string fields" should "satisfy the equals/hashCode contract for any pair" in {
    def samples: Seq[LazyField[String]] = Seq(
      LazyField.from("\uD800"),
      LazyField[String](LazyField.from("\uD800").bytes),
      LazyField[String](ByteString.copyFrom(bytesOf(0xff))),
      LazyField[String](ByteString.copyFrom(bytesOf(0xfe))),
      LazyField.from("\uFFFD"),
      LazyField.from("string"),
      LazyField[String](ByteString.copyFromUtf8("string")),
      LazyField.from(""),
      LazyField[String](ByteString.EMPTY)
    )
    for {
      a <- samples
      b <- samples
    } {
      (a == b) shouldBe (b == a)
      if (a == b) {
        a.hashCode shouldBe b.hashCode
        HashSet(a, b).size shouldBe 1
        HashSet(b, a).size shouldBe 1
      } else {
        HashSet(a, b).size shouldBe 2
        HashSet(b, a).size shouldBe 2
      }
    }
  }
}
