// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package scalapb

import com.google.protobuf.ByteString
import scalapb.textformat._
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{TryValues, MustMatchers, FlatSpec}
import org.scalatest.prop._
import protobuf_unittest.unittest.{TestAllTypes, TestOneof2}
import scala.io.Source
import scala.util._

class TextFormatSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers with TryValues {

  class ParseOk extends Matcher[String] {

    def apply(left: String) = {
      val r = TestAllTypes.validateAscii(left)
      MatchResult(r.isRight,
                  if (r.isLeft) "Text did not parse: " + r.left.get.msg else null,
                  "Text parsed ok")
    }
  }

  def parseOk = new ParseOk

  class FailParsingWith(error: String) extends Matcher[String] {

    def apply(left: String) = {
      val r = TestAllTypes.validateAscii(left)
      r match {
        case Right(_) =>
          MatchResult(false, "Parse did not fail", null)
        case Left(p: TextFormatError) =>
          MatchResult(p.msg.contains(error), s"Error '${p.msg}' did not contain: '$error'",
            s"Error contained '$error'")
      }
    }
  }

  def failParsingWith(error: String) = new FailParsingWith(error)

  /**
   * Helper to construct a ByteString from a String containing only 8-bit
   * characters.  The characters are converted directly to bytes, *not*
   * encoded using UTF-8.
   */
  def bytes(str: String): ByteString =
    ByteString.copyFrom(str.getBytes("ISO-8859-1"));

  def bytes(bytesAsInts: Int*): ByteString =
    ByteString.copyFrom(bytesAsInts.map(_.toByte).toArray)

  lazy val allFieldsSetText = Source.fromURL(getClass.getResource("/text_format_unittest_data_oneof_implemented.txt")).mkString

  /** Print TestAllTypes and compare with golden file. */
  "testPrintMessage" should "pass" in {
    // Java likes to add a trailing ".0" to floats and doubles.  C printf
    // (with %g format) does not.  Our golden files are used for both
    // C++ and Java TextFormat classes, so we need to conform.
    val javaText = TextFormat.printToString(TestUtil.getAllTypesAllSet)
      .replace(".0\n", "\n");

    javaText must be(allFieldsSetText)
  }

  "testPrintExotic" should "pass" in {
    val message = TestAllTypes().update(
      // Signed vs. unsigned numbers.
      _.repeatedInt32  :+= -1,
      _.repeatedUint32 :+= -1,
      _.repeatedInt64  :+= -1L,
      _.repeatedUint64 :+= -1L,

      _.repeatedInt32  :+= 1  << 31,
      _.repeatedUint32 :+= 1  << 31,
      _.repeatedInt64  :+= 1L << 63,
      _.repeatedUint64 :+= 1L << 63,

      // Floats of various precisions and exponents.
      _.repeatedDouble :+= 123.0,
      _.repeatedDouble :+= 123.5,
      _.repeatedDouble :+= 0.125,
      _.repeatedDouble :+= .125,
      _.repeatedDouble :+= -.125,
      _.repeatedDouble :+= 123e15,
      _.repeatedDouble :+= 123e15,
      _.repeatedDouble :+= -1.23e-17,
      _.repeatedDouble :+= .23e17,
      _.repeatedDouble :+= -23e15,
      _.repeatedDouble :+= 123.5e20,
      _.repeatedDouble :+= 123.5e-20,
      _.repeatedDouble :+= 123.456789,
      _.repeatedDouble :+= Double.PositiveInfinity,
      _.repeatedDouble :+= Double.NegativeInfinity,
      _.repeatedDouble :+= Double.NaN,

      // Strings and bytes that needing escaping.
      _.repeatedString :+= "\u0000\u0001\u0007\b\f\n\r\t\u000b\\\'\"\u1234",
      _.repeatedBytes :+= bytes("\u0000\u0001\u0007\b\f\n\r\t\u000b\\\'\"\u00fe"))

    TextFormat.printToString(message) must be(canonicalExoticText)
  }

  private val exoticText =
    "repeated_int32: -1\n" +
      "repeated_int32: -2147483648\n" +
      "repeated_int64: -1\n" +
      "repeated_int64: -9223372036854775808\n" +
      "repeated_uint32: 4294967295\n" +
      "repeated_uint32: 2147483648\n" +
      "repeated_uint64: 18446744073709551615\n" +
      "repeated_uint64: 9223372036854775808\n" +
      "repeated_double: 123.0\n" +
      "repeated_double: 123.5\n" +
      "repeated_double: 0.125\n" +
      "repeated_double: .125\n" +
      "repeated_double: -.125\n" +
      "repeated_double: 1.23E17\n" +
      "repeated_double: 1.23E+17\n" +
      "repeated_double: -1.23e-17\n" +
      "repeated_double: .23e+17\n" +
      "repeated_double: -.23E17\n" +
      "repeated_double: 1.235E22\n" +
      "repeated_double: 1.235E-18\n" +
      "repeated_double: 123.456789\n" +
      "repeated_double: Infinity\n" +
      "repeated_double: -Infinity\n" +
      "repeated_double: NaN\n" +
      "repeated_string: \"\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"" +
      "\\341\\210\\264\"\n" +
      "repeated_bytes: \"\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"\\376\"\n"

  private val canonicalExoticText =
    exoticText.replace(": .", ": 0.").replace(": -.", ": -0.")   // short-form double
      .replace("23e", "23E").replace("E+", "E").replace("0.23E17", "2.3E16")

  "testParseAllFieldsSet" should "pass" in {
    TestAllTypes.fromAscii(allFieldsSetText) must be(TestUtil.getAllTypesAllSet)
  }

  "testParseCompatibility" should "pass" in {
    val original = "repeated_float: inf\n" +
      "repeated_float: -inf\n" +
      "repeated_float: nan\n" +
      "repeated_float: inff\n" +
      "repeated_float: -inff\n" +
      "repeated_float: nanf\n" +
      "repeated_float: 1.0f\n" +
      "repeated_float: infinityf\n" +
      "repeated_float: -Infinityf\n" +
      "repeated_double: infinity\n" +
      "repeated_double: -infinity\n" +
      "repeated_double: nan\n";
    val canonical = "repeated_float: Infinity\n" +
      "repeated_float: -Infinity\n" +
      "repeated_float: NaN\n" +
      "repeated_float: Infinity\n" +
      "repeated_float: -Infinity\n" +
      "repeated_float: NaN\n" +
      "repeated_float: 1.0\n" +
      "repeated_float: Infinity\n" +
      "repeated_float: -Infinity\n" +
      "repeated_double: Infinity\n" +
      "repeated_double: -Infinity\n" +
      "repeated_double: NaN\n";
    TestAllTypes.fromAscii(original).toProtoString must be(canonical)
  }

  "testParseExotic" should "pass" in {
    TextFormat.printToString(TestAllTypes.fromAscii(exoticText)) must be(canonicalExoticText)
  }

  "testParseNumericEnum" should "pass" in {
    TestAllTypes.fromAscii("optional_nested_enum: 2").getOptionalNestedEnum must be('bar)
  }

  "testParseComment" should "pass" in {
    val t = TestAllTypes.fromAscii(
      "# this is a comment\n" +
        "optional_int32: 1  # another comment\n" +
        "optional_int64: 2\n" +
        "# EOF comment")
    t.getOptionalInt32 must be(1)
    t.getOptionalInt64 must be(2)
  }


  def assertParseSuccessWithOverwriteForbidden(text: String): TestAllTypes =
    TestAllTypes.fromAscii(text)

  def assertParseErrorWithOverwriteForbidden(error: String, text: String): Unit = {
    parseFailure(error, text)
  }

  def parseFailure(error: String, text: String): Unit = {
    intercept[TextFormatError] {
      TestAllTypes.fromAscii(text)
    }.msg must include(error)
  }

  "testParseErrors" should "pass" in {
    val EXPECTED_FIELD = "':', '{', '<', or '['"
    "optional_int32 123" must failParsingWith(EXPECTED_FIELD)
    "optional_nested_enum: ?" must failParsingWith(EXPECTED_FIELD)
    "optional_uint32: -1" must failParsingWith(
        "Number must be positive: -1 (line 1, column 18)")
    "optional_int32: 82301481290849012385230157" must failParsingWith(
        "Number out of range for 32-bit signed integer: 82301481290849012385230157")
    "optional_bool: maybe" must failParsingWith(
      "Invalid input 'maybe', expected 'true' or 'false' (line 1, column 16)")
    "optional_bool: 2" must failParsingWith(
      "Invalid input '2', expected 'true' or 'false' (line 1, column 16)")
    "optional_string: 123" must failParsingWith(
        "Invalid input '123', expected string (line 1, column 18)")
    "optional_string: \"ueoauaoe" must failParsingWith(EXPECTED_FIELD)
    "optional_string: \"ueoauaoe\noptional_int32: 123" must failParsingWith(
      EXPECTED_FIELD)
    "optional_string: \"\\z\"" must failParsingWith(
        "Invalid escape sequence: z (line 1, column 18)")
    "optional_string: \"ueoauaoe\noptional_int32: 123" must failParsingWith(
        EXPECTED_FIELD)
    "[nosuchext]: 123" must failParsingWith(
        "expected identifier")
    "[protobuf_unittest.optional_int32_extension]: 123" must failParsingWith(
        "expected identifier")
    "nosuchfield: 123" must failParsingWith(
        "Unknown field name 'nosuchfield' (line 1, column 1)")
    "optional_nested_enum: NO_SUCH_VALUE" must failParsingWith(
        "Expected Enum type \"NestedEnum\" has no value named \"NO_SUCH_VALUE\" (line 1, column 23)")
    "optional_nested_enum: 123" must failParsingWith(
      "Expected Enum type \"NestedEnum\" has no value with number 123 (line 1, column 23)")

    // Additional by ScalaPB:
    "optional_string: \"hello\\\"" must failParsingWith(
      EXPECTED_FIELD)
    "optional_string: \"hello\\xhello\"" must failParsingWith(
      "'\\x' with no digits (line 1, column 18)")
    "optional_string: \"hello\\x\"" must failParsingWith(
      "'\\x' with no digits (line 1, column 18)")
    "repeated_nested_message: { >" must failParsingWith(
      EXPECTED_FIELD)
    "repeated_nested_message { >" must failParsingWith(
      EXPECTED_FIELD)
    "repeated_nested_message < }" must failParsingWith(
      EXPECTED_FIELD)
  }

    // =================================================================

    "testEscape" should "pass" in {
      // Escape sequences.
      val kEscapeTestString =
          "\"A string with ' characters \n and \r newlines and \t tabs and \u0001 " +
          "slashes \\"

      // A representation of the above string with all the characters escaped.
      val kEscapeTestStringEscaped =
          "\\\"A string with \\' characters \\n and \\r newlines " +
          "and \\t tabs and \\001 slashes \\\\"

      "\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"" must be(
        TextFormatUtils.escapeBytes(bytes("\u0000\u0001\u0007\b\f\n\r\t\u000b\\\'\"")))
      "\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"" must be(
        TextFormatUtils.escapeText("\u0000\u0001\u0007\b\f\n\r\t\u000b\\\'\""))
      bytes("\u0000\u0001\u0007\b\f\n\r\t\u000b\\\'\"") must be(
        TextFormatUtils.unescapeBytes("\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"").right.get)
      "\u0000\u0001\u0007\b\f\n\r\t\u000b\\\'\"" must be(
        TextFormatUtils.unescapeText("\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"").right.get)
      kEscapeTestStringEscaped must be(
        TextFormatUtils.escapeText(kEscapeTestString))
      kEscapeTestString must be(
        TextFormatUtils.unescapeText(kEscapeTestStringEscaped).right.get)

      // Unicode handling.
      "\\341\\210\\264" must be(TextFormatUtils.escapeText("\u1234"))
      "\\341\\210\\264" must be(TextFormatUtils.escapeBytes(bytes(0xe1, 0x88, 0xb4)))
      "\u1234" must be(TextFormatUtils.unescapeText("\\341\\210\\264").right.get)
      bytes(0xe1, 0x88, 0xb4) must be(
        TextFormatUtils.unescapeBytes("\\341\\210\\264").right.get)
      "\u1234" must be(TextFormatUtils.unescapeText("\\xe1\\x88\\xb4").right.get)
      bytes(0xe1, 0x88, 0xb4) must be(
        TextFormatUtils.unescapeBytes("\\xe1\\x88\\xb4").right.get)

      // Handling of strings with unescaped Unicode characters > 255.
      val zh = "\u9999\u6e2f\u4e0a\u6d77\ud84f\udf80\u8c50\u9280\u884c"
      val zhByteString = ByteString.copyFromUtf8(zh)
      zhByteString must be(TextFormatUtils.unescapeBytes(zh).right.get)

      TextFormatUtils.unescapeText("\\x").left.get.msg must be("'\\x' with no digits")

      TextFormatUtils.unescapeText("\\z").left.get.msg must be("Invalid escape sequence: z")

      TextFormatUtils.unescapeText("\\").left.get.msg must be("Invalid escape sequence '\\' at end of string.")
    }

    def parseInt32[T](input: String): Int =
      AstUtils.parseInt32(ProtoAsciiParser.PrimitiveValue.parse(input).get.value).right.get.value

    def parseInt64[T](input: String): Long =
      AstUtils.parseInt64(ProtoAsciiParser.PrimitiveValue.parse(input).get.value).right.get.value

    def parseUInt32[T](input: String): Int =
      AstUtils.parseUint32(ProtoAsciiParser.PrimitiveValue.parse(input).get.value).right.get.value

    def parseUInt64[T](input: String): Long =
      AstUtils.parseUint64(ProtoAsciiParser.PrimitiveValue.parse(input).get.value).right.get.value

    "testParseInteger" should "pass" in {
                0 must be(parseInt32(          "0"))
                1 must be(parseInt32(          "1"))
               -1 must be(parseInt32(         "-1"))
            12345 must be(parseInt32(      "12345"))
           -12345 must be(parseInt32(     "-12345"))
       2147483647 must be(parseInt32( "2147483647"))
      -2147483648 must be(parseInt32("-2147483648"))

                      0 must be(parseUInt32(         "0"))
                      1 must be(parseUInt32(         "1"))
                  12345 must be(parseUInt32(     "12345"))
             2147483647 must be(parseUInt32("2147483647"))
      2147483648L.toInt must be(parseUInt32("2147483648"))
      4294967295L.toInt must be(parseUInt32("4294967295"))

                0L must be(parseInt64(          "0"))
                1L must be(parseInt64(          "1"))
               -1L must be(parseInt64(         "-1"))
            12345L must be(parseInt64(      "12345"))
           -12345L must be(parseInt64(     "-12345"))
       2147483647L must be(parseInt64( "2147483647"))
      -2147483648L must be(parseInt64("-2147483648"))
       4294967295L must be(parseInt64( "4294967295"))
       4294967296L must be(parseInt64( "4294967296"))
      9223372036854775807L must be (parseInt64("9223372036854775807"))
      -9223372036854775808L must be (
                   parseInt64("-9223372036854775808"))

                0L must be(parseUInt64(          "0"))
                1L must be(parseUInt64(          "1"))
            12345L must be(parseUInt64(      "12345"))
       2147483647L must be(parseUInt64( "2147483647"))
       4294967295L must be(parseUInt64( "4294967295"))
       4294967296L must be(parseUInt64( "4294967296"))
      9223372036854775807L must be(
                   parseUInt64("9223372036854775807"))
      -9223372036854775808L must be (
                   parseUInt64("9223372036854775808"))
      -1L must be(parseUInt64("18446744073709551615"))

      // Hex
      0x1234abcd must be(parseInt32("0x1234abcd"))
      -0x1234abcd must be(parseInt32("-0x1234abcd"))
      -1 must be(parseUInt64("0xffffffffffffffff"))
      0x7fffffffffffffffL must be(
                   parseInt64("0x7fffffffffffffff"))

      // Octal
      342391 must be(parseInt32("01234567"))

      // Out-of-range
      intercept[NoSuchElementException] {
        parseInt32("2147483648")
      }

      intercept[NoSuchElementException] {
        parseInt32("-2147483649")
      }

      intercept[NoSuchElementException] {
        parseUInt32("4294967296")
      }

      intercept[NoSuchElementException] {
        parseUInt32("-1")
      }

      intercept[NoSuchElementException] {
        parseInt64("9223372036854775808")
      }

      intercept[NoSuchElementException] {
        parseInt64("-9223372036854775809")
      }

      intercept[NoSuchElementException] {
        parseUInt64("18446744073709551616")
      }

      intercept[NoSuchElementException] {
        parseUInt64("-1")
      }

      intercept[NoSuchElementException] {
        parseInt32("abcd")
      }
    }

    "testParseString" should "pass" in {
      val zh = "\u9999\u6e2f\u4e0a\u6d77\ud84f\udf80\u8c50\u9280\u884c";
      val p = TestAllTypes.fromAscii(
        "optional_string: \"" + zh + "\"")
      p.getOptionalString must be(zh)
    }

    "testParseLongString" should "pass" in {
      val longText =
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890" +
        "123456789012345678901234567890123456789012345678901234567890"

      val p = TestAllTypes.fromAscii(
        "optional_string: \"" + longText + "\"")
      p.getOptionalString must be(longText)
    }

    "testParseBoolean" should "pass" in {
      val goodText =
          "repeated_bool: t  repeated_bool : 0\n" +
          "repeated_bool :f repeated_bool:1"
      val goodTextCanonical =
          "repeated_bool: true\n" +
          "repeated_bool: false\n" +
          "repeated_bool: false\n" +
          "repeated_bool: true\n"
      val good = TestAllTypes.fromAscii(goodText)
      goodTextCanonical must be(good.toProtoString)

      "optional_bool:2" must failParsingWith("")
      "optional_bool:foo" must failParsingWith("")
    }

    "testParseAdjacentStringLiterals" should "pass" in {
      TestAllTypes.fromAscii(
        "optional_string: \"foo\" 'corge' \"grault\"").getOptionalString must be(
           "foocorgegrault")
    }

  "testPrintFieldValue" should "pass" in {
    def printFieldValue(o: descriptors.PValue, fieldName: String): String = {
      val tg = new TextGenerator()
      Printer.printFieldValue(TestAllTypes.scalaDescriptor.findFieldByName(fieldName).get, o, tg)
      tg.result()
    }

    printFieldValue(descriptors.PString("hello"), "repeated_string") must be("\"hello\"")
    printFieldValue(descriptors.PFloat(123f), "repeated_float") must be("123.0")
    printFieldValue(descriptors.PDouble(123d), "repeated_double") must be("123.0")
    printFieldValue(descriptors.PInt(123), "repeated_int32") must be("123")
    printFieldValue(descriptors.PLong(123L), "repeated_int64") must be("123")
    printFieldValue(descriptors.PBoolean(true), "repeated_bool") must be("true")
    printFieldValue(descriptors.PInt(0xFFFFFFFF), "repeated_uint32") must be("4294967295")
    printFieldValue(descriptors.PLong(0xFFFFFFFFFFFFFFFFL), "repeated_uint64") must be("18446744073709551615")
    printFieldValue(descriptors.PByteString(ByteString.copyFrom(Array[Byte](1, 2, 3))), "repeated_bytes") must be(
      "\"\\001\\002\\003\"")
  }

  "testPrintToUnicodeString" should "pass" in {
    "optional_string: \"abc\u3042efg\"\n" +
      "optional_bytes: \"\\343\\201\\202\"\n" +
      "repeated_string: \"\u3093XYZ\"\n" must be(
    TextFormat.printToUnicodeString(TestAllTypes()
      .withOptionalString("abc\u3042efg")
      .withOptionalBytes(bytes(0xe3, 0x81, 0x82))
      .addRepeatedString("\u3093XYZ")))

    // Double quotes and backslashes should be escaped
      "optional_string: \"a\\\\bc\\\"ef\\\"g\"\n" must be(
      TextFormat.printToUnicodeString(TestAllTypes()
        .withOptionalString("a\\bc\"ef\"g")))

    // Test escaping roundtrip
    val message = TestAllTypes()
      .withOptionalString("a\\bc\\\"ef\"g")
    TestAllTypes.fromAscii(TextFormat.printToUnicodeString(message))
      .getOptionalString must be(message.getOptionalString)
  }

  "testPrintToUnicodeStringWithNewLines" should "pass" in {
      // No newlines at start and end
      "optional_string: \"test newlines\\n\\nin\\nstring\"\n" must be(
          TextFormat.printToUnicodeString(TestAllTypes()
              .withOptionalString("test newlines\n\nin\nstring")))

      // Newlines at start and end
      "optional_string: \"\\ntest\\nnewlines\\n\\nin\\nstring\\n\"\n" must be(
          TextFormat.printToUnicodeString(TestAllTypes()
              .withOptionalString("\ntest\nnewlines\n\nin\nstring\n")))

      // Strings with 0, 1 and 2 newlines.
      "optional_string: \"\"\n" must be(
          TextFormat.printToUnicodeString(TestAllTypes()
              .withOptionalString("")))

      "optional_string: \"\\n\"\n" must be(
          TextFormat.printToUnicodeString(TestAllTypes()
              .withOptionalString("\n")))

      "optional_string: \"\\n\\n\"\n" must be(
          TextFormat.printToUnicodeString(TestAllTypes()
              .withOptionalString("\n\n")))
    }

    "testParseNonRepeatedFields" should "pass" in {
      ("repeated_int32: 1\n" +
        "repeated_int32: 2\n") must parseOk
      ("repeated_nested_message { bb: 1 }\n" +
        "repeated_nested_message { bb: 2 }\n") must parseOk
    }

    "testParseShortRepeatedFormOfRepeatedFields" should "pass" in {
      "repeated_foreign_enum: [FOREIGN_FOO, FOREIGN_BAR]" must parseOk
      "repeated_int32: [ 1, 2 ]\n" must parseOk
      "repeated_nested_message [{ bb: 1 }, { bb: 2 }]\n" must parseOk
    }

    "testParseShortRepeatedFormOfNonRepeatedFields" should "pass" in {
      "optional_int32: [1]" must failParsingWith(
          "Invalid input '[', expected type_int32 (line 1, column 17)")
    }

    // =======================================================================
    // test oneof
    "testOneofTextFormat" should "pass" in {
      val p = TestOneof2().update(
        _.fooLazyMessage.quxInt := 100L,
        _.barString := "101",
        _.bazInt := 102,
        _.bazString := "103"
      )
      TestOneof2.fromAscii(p.toProtoString) must be(p)
    }

    "testOneofOverwriteAllowed" should "pass" in {
      val input = "foo_string: \"stringvalue\" foo_int: 123";
      val p = TestOneof2.fromAscii(input)
      p.foo.isFooInt must be(true)
    }
}
