package scalapb.textformat

import com.google.protobuf.{ByteString, wrappers}
import munit.FunSuite
import scalapb.TextFormat

class UnicodeEscapeSpec extends FunSuite {
  private val slash = "\\"

  test("short Unicode escapes decode into UTF-8 for strings and bytes") {
    for ((digits, expected) <- Seq("0000" -> "\u0000", "1234" -> "\u1234", "FFFF" -> "\uffff")) {
      val input = "value: '" + slash + "u" + digits + "'"
      assertEquals(
        TextFormat.fromAscii(wrappers.StringValue, input),
        Right(wrappers.StringValue(expected))
      )
      assertEquals(
        TextFormat.fromAscii(wrappers.BytesValue, input),
        Right(wrappers.BytesValue(ByteString.copyFromUtf8(expected)))
      )
    }
    assertEquals(TextFormatUtils.unescapeText(slash + "u1234" + slash + "n!"), Right("\u1234\n!"))
  }

  test("short Unicode escapes reject incomplete digits and surrogate code points") {
    for (digits <- Seq("", "0", "123", "12xz", "d800", "dfff", "d801" + slash + "udc37")) {
      assert(TextFormatUtils.unescapeBytes(slash + "u" + digits).isLeft, digits)
    }
  }
  test("long Unicode escapes support supplementary characters without overflow") {
    for (codePoint <- Seq(0, 0x1234, 0x10437, 0x10ffff)) {
      val escape   = slash + "U" + f"$codePoint%08x"
      val expected = new String(Character.toChars(codePoint))
      assertEquals(TextFormatUtils.unescapeText(escape), Right(expected))
      assertEquals(TextFormatUtils.unescapeBytes(escape), Right(ByteString.copyFromUtf8(expected)))
    }
    for (digits <- Seq("", "0000123", "0000123x", "00110000", "ffffffff", "0000d800", "0000dfff")) {
      assert(TextFormatUtils.unescapeBytes(slash + "U" + digits).isLeft, digits)
    }
  }
}
