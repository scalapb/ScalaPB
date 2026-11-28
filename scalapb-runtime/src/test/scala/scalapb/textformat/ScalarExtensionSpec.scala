package scalapb.textformat

import com.google.protobuf.{ByteString, CodedOutputStream}
import munit.FunSuite
import scalapb.{TextFormat, UnknownFieldSet}
import scalapb.textformat.fixtures._

class ScalarExtensionSpec extends FunSuite {
  private val registry = TypeRegistry.empty
    .addFile(ScalarExtensionsProto.scalaDescriptor)
    .addFile(ScalarEditionProto.scalaDescriptor)
  private val prefix                                     = "scalapb.textformat.fixtures."
  private def field(name: String, value: String): String = s"[$prefix$name]: $value"
  private def parse(text: String): ScalarBase            =
    TextFormat.fromAscii(ScalarBase, text, registry).toOption.get
  private def print(message: ScalarBase, unknown: Boolean = false): String =
    TextFormat.printToString(message, registry, unknown)

  test("all scalar wire types reuse the ordinary text codecs and generated accessors") {
    val text = Seq(
      "i32"                -> "-2147483648",
      "u32"                -> "4294967295",
      "s32"                -> "-123",
      "f32"                -> "4294967295",
      "sf32"               -> "-456",
      "i64"                -> "-9223372036854775808",
      "u64"                -> "18446744073709551615",
      "s64"                -> "-789",
      "f64"                -> "18446744073709551615",
      "sf64"               -> "-987",
      "flag"               -> "true",
      "str"                -> "'hello'",
      "blob"               -> "'\\377\\000'",
      "flt"                -> "-0.0",
      "dbl"                -> "1.25",
      "closed"             -> "ONE",
      "ScalarScope.nested" -> "42",
      "open"               -> "123"
    ).map { case (name, value) => field(name, value) }.mkString("\n")
    val message = parse(text)
    import ScalarExtensionsProto._
    assertEquals(message.extension(i32), Some(Int.MinValue))
    assertEquals(message.extension(u32), Some(-1))
    assertEquals(message.extension(s32), Some(-123))
    assertEquals(message.extension(f32), Some(-1))
    assertEquals(message.extension(sf32), Some(-456))
    assertEquals(message.extension(i64), Some(Long.MinValue))
    assertEquals(message.extension(u64), Some(-1L))
    assertEquals(message.extension(s64), Some(-789L))
    assertEquals(message.extension(f64), Some(-1L))
    assertEquals(message.extension(sf64), Some(-987L))
    assertEquals(message.extension(flag), Some(true))
    assertEquals(message.extension(str), Some("hello"))
    assertEquals(message.extension(blob), Some(ByteString.copyFrom(Array(255.toByte, 0.toByte))))
    assertEquals(java.lang.Float.floatToRawIntBits(message.extension(flt).get), Int.MinValue)
    assertEquals(message.extension(dbl), Some(1.25))
    assertEquals(message.extension(closed), Some(ClosedScalarEnum.ONE))
    assertEquals(message.extension(ScalarScope.nested), Some(42))
    assertEquals(message.extension(ScalarEditionProto.open).get.value, 123)
    assertEquals(parse(print(message)), message)
    assertEquals(parse(print(message, true)), message)
  }

  test("registration is explicit for external declarations and scoped by extendee") {
    val text = field("i32", "1")
    assert(TextFormat.fromAscii(ScalarBase, text).isLeft)
    assert(TextFormat.fromAscii(ScalarOther, text, registry).isLeft)
    assert(TextFormat.fromAscii(ScalarBase, field("unknown", "1"), registry).isLeft)
    val withMessage = TypeRegistry.empty.addMessage(ScalarScope)
    assertEquals(TextFormat.fromAscii(ScalarBase, text, withMessage), Right(parse(text)))
    assertEquals(
      TextFormat
        .fromAscii(ScalarBase, text, registry.addFile(ScalarExtensionsProto.scalaDescriptor)),
      Right(parse(text))
    )
    assertEquals(parse("child { " + text + " }").child, Some(parse(text)))
    val split = "[scalapb.textformat.fixt # comment\n ures.i 32]: 1"
    assertEquals(parse(split), parse(text))
  }

  test(
    "repeated scalars accept lists and occurrences, while singular values use the last occurrence"
  ) {
    val text = Seq(
      field("ints", "[1, -2]"),
      field("ints", "3"),
      field("flags", "[true, false]"),
      field("strings", "['a', 'b']"),
      field("i32", "1"),
      field("i32", "2")
    ).mkString("\n")
    val message = parse(text)
    assertEquals(message.extension(ScalarExtensionsProto.ints), Seq(1, -2, 3))
    assertEquals(message.extension(ScalarExtensionsProto.flags), Seq(true, false))
    assertEquals(message.extension(ScalarExtensionsProto.strings), Seq("a", "b"))
    assertEquals(message.extension(ScalarExtensionsProto.i32), Some(2))
    assertEquals(parse(print(message)).extension(ScalarExtensionsProto.i32), Some(2))
    assertEquals(parse(print(message)).extension(ScalarExtensionsProto.ints), Seq(1, -2, 3))
  }

  test(
    "packed values print symbolically and unknown closed enums and unrelated wire types remain unknown"
  ) {
    val bytes  = new java.io.ByteArrayOutputStream()
    val output = CodedOutputStream.newInstance(bytes)
    output.writeBytes(116, ByteString.copyFrom(Array[Byte](1, 2, 3)))
    output.writeBytes(117, ByteString.copyFrom(Array[Byte](0, 42, 1)))
    output.writeInt32(115, 42)
    output.writeInt32(100, 7)
    output.writeFixed32(100, 99)
    output.flush()
    val message = ScalarBase.parseFrom(bytes.toByteArray)
    val text    = print(message, true)
    assert(text.contains(field("ints", "1")))
    assert(text.contains(field("enums", "ZERO")))
    assert(text.contains(field("enums", "ONE")))
    assert(text.contains("117: 42"))
    assert(text.contains("115: 42"))
    assert(text.contains("100: 0x00000063"))
    assert(!text.contains(field("closed", "42")))
    val known = parse(print(message))
    assertEquals(known.extension(ScalarExtensionsProto.ints), Seq(1, 2, 3))
    assertEquals(
      known.extension(ScalarExtensionsProto.enums),
      Seq(ClosedScalarEnum.ZERO, ClosedScalarEnum.ONE)
    )
    assertEquals(known.extension(ScalarExtensionsProto.i32), Some(7))
  }

  test("scalar extensions enforce ranges, enum openness and value shape") {
    for (
      (name, value) <- Seq(
        "i32"    -> "2147483648",
        "u32"    -> "-1",
        "u64"    -> "18446744073709551616",
        "closed" -> "42",
        "closed" -> "MISSING",
        "open"   -> "2147483648",
        "flag"   -> "2",
        "str"    -> "123",
        "ints"   -> "[{}]",
        "i32"    -> "[1]",
        "i32"    -> "{}"
      )
    ) {
      assert(
        TextFormat.fromAscii(ScalarBase, field(name, value), registry).isLeft,
        s"$name: $value"
      )
    }
    val empty = ScalarBase().withUnknownFields(UnknownFieldSet.empty)
    assertEquals(print(empty), "")
  }
}
