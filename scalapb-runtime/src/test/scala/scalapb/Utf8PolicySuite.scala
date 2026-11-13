package scalapb

import com.google.protobuf.InvalidProtocolBufferException
import munit.FunSuite
import scalapb.utf8._

class Utf8PolicySuite extends FunSuite {
  private def field(number: Int, bytes: Array[Byte]): Array[Byte] =
    Array((number * 8 + 2).toByte, bytes.length.toByte) ++ bytes

  private val malformed = Seq(
    Array(0x80),                   // isolated continuation
    Array(0xc0, 0xaf),             // overlong encoding
    Array(0xe2, 0x82),             // truncated sequence
    Array(0xed, 0xa0, 0x80),       // surrogate
    Array(0xf4, 0x90, 0x80, 0x80), // above U+10FFFF
    Array(0x61, 0xff, 0x62)        // invalid byte within ASCII
  ).map(_.map(_.toByte))

  test("legacy, proto2 java_string_check_utf8 and proto3 reject malformed UTF-8") {
    malformed.foreach { bytes =>
      val wire = field(1, bytes)
      intercept[InvalidProtocolBufferException](LegacyStrings.parseFrom(wire))
      intercept[InvalidProtocolBufferException](CheckedStrings.parseFrom(wire))
      intercept[InvalidProtocolBufferException](Proto3Strings.parseFrom(wire))
    }
  }

  test("nonvalidating optional, repeated and oneof strings replace malformed bytes lossily") {
    malformed.foreach { bytes =>
      val wire   = field(1, bytes) ++ field(2, bytes) ++ field(3, bytes)
      val parsed = RelaxedStrings.parseFrom(wire)
      assert(parsed.getValue.contains('\ufffd'))
      assertEquals(parsed.values, Seq(parsed.getValue))
      assertEquals(parsed.choice.selected, parsed.value)
      assert(!java.util.Arrays.equals(parsed.toByteArray, wire))
      assertEquals(RelaxedStrings.parseFrom(parsed.toByteArray), parsed)
    }
  }

  test("map keys and values validate even in a nonvalidating proto2 file") {
    malformed.foreach { bytes =>
      intercept[InvalidProtocolBufferException](RelaxedStrings.parseFrom(field(4, field(1, bytes))))
      intercept[InvalidProtocolBufferException](RelaxedStrings.parseFrom(field(4, field(2, bytes))))
    }
  }

  test("Editions file inheritance and field overrides govern generated parsers") {
    malformed.foreach { bytes =>
      val wire   = field(1, bytes)
      val parsed = EditionStrings.parseFrom(wire)
      assert(parsed.getValue.contains('\ufffd'))
      assert(!java.util.Arrays.equals(parsed.toByteArray, wire))
      assertEquals(EditionStrings.parseFrom(parsed.toByteArray), parsed)
      intercept[InvalidProtocolBufferException](EditionStrings.parseFrom(field(2, bytes)))
      intercept[InvalidProtocolBufferException](EditionStrings.parseFrom(field(3, field(1, bytes))))
      intercept[InvalidProtocolBufferException](EditionStrings.parseFrom(field(3, field(2, bytes))))
    }
  }

  test("valid Unicode including supplementary characters round trips under either policy") {
    val value   = "ASCII \u00e9 \u6c34 \ud83d\ude80"
    val relaxed = RelaxedStrings(
      value = Some(value),
      values = Seq(value),
      choice = RelaxedStrings.Choice.Selected(value),
      entries = Map(value -> value)
    )
    assertEquals(RelaxedStrings.parseFrom(relaxed.toByteArray), relaxed)
    val strict = Proto3Strings(value)
    assertEquals(Proto3Strings.parseFrom(strict.toByteArray), strict)
  }
}
