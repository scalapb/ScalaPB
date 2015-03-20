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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest.Nested
import com.trueaccord.scalapb.{TextFormat, TextFormatError}
import org.scalatest._
import matchers.{Matcher, MatchResult}
import org.scalatest.prop._
import org.scalacheck.{Arbitrary, Gen}
import protobuf_unittest.UnittestProto.ForeignMessage
import protobuf_unittest.unittest.{TestAllTypes, TestOneof2}
import org.parboiled2.ParseError
import scala.io.Source
import scala.util._

class TextFormatSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers with TryValues {

  class ParseOk extends Matcher[String] {

    def apply(left: String) = {
      val r = TestAllTypes.fromAscii(left)
      MatchResult(r.isSuccess,
                  if (r.isSuccess) "Text parsed ok" else ("Text did not parse: " + r.failure.get),
                  "Text parsed ok")
    }
  }

  def parseOk = new ParseOk

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
      _.repeatedInt64  :+= -1,
      _.repeatedUint64 :+= -1,

      _.repeatedInt32  :+= 1  << 31,
      _.repeatedUint32 :+= 1  << 31,
      _.repeatedInt64  :+= 1L << 63,
      _.repeatedUint64 :+= 1L << 63,

      // Floats of various precisions and exponents.
      _.repeatedDouble :+= 123,
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
      _.repeatedString :+= "\0\001\007\b\f\n\r\t\013\\\'\"\u1234",
      _.repeatedBytes :+= bytes("\0\001\007\b\f\n\r\t\013\\\'\"\u00fe"))

    message.toString must be(canonicalExoticText)
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
    TestAllTypes.fromAscii(allFieldsSetText).success.get must be(TestUtil.getAllTypesAllSet)
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
    TestAllTypes.fromAscii(original).get.toString must be(canonical)
  }

  "testParseExotic" should "pass" in {
    TestAllTypes.fromAscii(exoticText).get.toString must be(canonicalExoticText)
  }

  "testParseNumericEnum" should "pass" in {
    TestAllTypes.fromAscii("optional_nested_enum: 2").get.getOptionalNestedEnum must be('bar)
  }

  "testParseComment" should "pass" in {
    val t = TestAllTypes.fromAscii(
      "# this is a comment\n" +
        "optional_int32: 1  # another comment\n" +
        "optional_int64: 2\n" +
        "# EOF comment").get
    t.getOptionalInt32 must be(1)
    t.getOptionalInt64 must be(2)
  }


  def assertParseSuccessWithOverwriteForbidden(text: String): TestAllTypes =
    TestAllTypes.fromAscii(text).get

  def assertParseErrorWithOverwriteForbidden(error: String, text: String): Unit = {
    parse(error, text).failure
  }

  def parse(error: String, text: String): Try[TestAllTypes] = {
    TestAllTypes.fromAscii(text)
  }

  def parse(text: String): Try[TestAllTypes] = {
    TestAllTypes.fromAscii(text)
  }

  "testParseErrors" should "pass" in {
      parse(
        "1:16: Expected \":\".",
        "optional_int32 123").failure
      parse(
        "1:23: Expected identifier. Found '?'",
        "optional_nested_enum: ?").failure
      parse(
        "1:18: Couldn't parse integer: Number must be positive: -1",
        "optional_uint32: -1").failure
      parse(
        "1:17: Couldn't parse integer: Number out of range for 32-bit signed " +
          "integer: 82301481290849012385230157",
        "optional_int32: 82301481290849012385230157").failure
      parse(
        "1:16: Expected \"true\" or \"false\".",
        "optional_bool: maybe").failure
      parse(
        "1:16: Expected \"true\" or \"false\".",
        "optional_bool: 2").failure
      parse(
        "1:18: Expected string.",
        "optional_string: 123").failure
      parse(
        "1:18: String missing ending quote.",
        "optional_string: \"ueoauaoe").failure
      parse(
        "1:18: String missing ending quote.",
        "optional_string: \"ueoauaoe\n" +
        "optional_int32: 123").failure
      parse(
        "1:18: Invalid escape sequence: '\\z'",
        "optional_string: \"\\z\"").failure
      parse(
        "1:18: String missing ending quote.",
        "optional_string: \"ueoauaoe\n" +
        "optional_int32: 123").failure
      parse(
        "1:2: Extension \"nosuchext\" not found in the ExtensionRegistry.",
        "[nosuchext]: 123").failure
      parse(
        "1:20: Extension \"protobuf_unittest.optional_int32_extension\" does " +
          "not extend message type \"protobuf_unittest.TestAllTypes\".",
        "[protobuf_unittest.optional_int32_extension]: 123").failure
      parse(
        "1:1: Message type \"protobuf_unittest.TestAllTypes\" has no field " +
          "named \"nosuchfield\".",
        "nosuchfield: 123").failure
      parse(
        "1:21: Expected \">\".",
        "OptionalGroup < a: 1").failure
      parse(
        "1:23: Enum type \"protobuf_unittest.TestAllTypes.NestedEnum\" has no " +
          "value named \"NO_SUCH_VALUE\".",
        "optional_nested_enum: NO_SUCH_VALUE").failure
      parse(
        "1:23: Enum type \"protobuf_unittest.TestAllTypes.NestedEnum\" has no " +
          "value with number 123.",
        "optional_nested_enum: 123").failure

      // Delimiters must match.
      parse(
        "1:22: Expected identifier. Found '}'",
        "OptionalGroup < a: 1 }").failure
      parse(
        "1:22: Expected identifier. Found '>'",
        "OptionalGroup { a: 1 >").failure

      // Additional by ScalaPB:
      parse(
        "1:18: Escape sequence at end of string.",
        "optional_string: \"hello\\\"").failure
      parse(
        "1:18: Hex with no values",
        "optional_string: \"hello\\xhello").failure
      parse(
        "1:18: Hex with no values",
        "optional_string: \"hello\\x").failure
  }

    // =================================================================

    "testEscape" should "pass" in {
      // Escape sequences.
      val kEscapeTestString =
          "\"A string with ' characters \n and \r newlines and \t tabs and \001 " +
          "slashes \\"

      // A representation of the above string with all the characters escaped.
      val kEscapeTestStringEscaped =
          "\\\"A string with \\' characters \\n and \\r newlines " +
          "and \\t tabs and \\001 slashes \\\\"

      "\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"" must be(
        TextFormat.escapeBytes(bytes("\0\001\007\b\f\n\r\t\013\\\'\"")))
      "\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\"" must be(
        TextFormat.escapeText("\0\001\007\b\f\n\r\t\013\\\'\""))
      bytes("\0\001\007\b\f\n\r\t\013\\\'\"") must be(
        TextFormat.unescapeBytes("\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\""))
      "\0\001\007\b\f\n\r\t\013\\\'\"" must be(
        TextFormat.unescapeText("\\000\\001\\a\\b\\f\\n\\r\\t\\v\\\\\\'\\\""))
      kEscapeTestStringEscaped must be(
        TextFormat.escapeText(kEscapeTestString))
      kEscapeTestString must be(
        TextFormat.unescapeText(kEscapeTestStringEscaped))

      // Unicode handling.
      "\\341\\210\\264" must be(TextFormat.escapeText("\u1234"))
      "\\341\\210\\264" must be(TextFormat.escapeBytes(bytes(0xe1, 0x88, 0xb4)))
      "\u1234" must be(TextFormat.unescapeText("\\341\\210\\264"))
      bytes(0xe1, 0x88, 0xb4) must be(
        TextFormat.unescapeBytes("\\341\\210\\264"))
      "\u1234" must be(TextFormat.unescapeText("\\xe1\\x88\\xb4"))
      bytes(0xe1, 0x88, 0xb4) must be(
        TextFormat.unescapeBytes("\\xe1\\x88\\xb4"));

      // Handling of strings with unescaped Unicode characters > 255.
      val zh = "\u9999\u6e2f\u4e0a\u6d77\ud84f\udf80\u8c50\u9280\u884c"
      val zhByteString = ByteString.copyFromUtf8(zh)
      zhByteString must be(TextFormat.unescapeBytes(zh))

      intercept[TextFormat.StringParsingException] {
        TextFormat.unescapeText("\\x");
      }

      intercept[TextFormat.StringParsingException] {
        TextFormat.unescapeText("\\z");
      }

      intercept[TextFormat.StringParsingException] {
        TextFormat.unescapeText("\\");
      }
    }

    def parseInt32[T](input: String): Int =
      new TextFormat(input).Int32.run().get

    def parseInt64[T](input: String): Long =
      new TextFormat(input).Int64.run().get

    def parseUInt32[T](input: String): Int =
      new TextFormat(input).UInt32.run().get

    def parseUInt64[T](input: String): Long =
      new TextFormat(input).UInt64.run().get

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
      intercept[ParseError] {
        parseInt32("2147483648")
      }

      intercept[ParseError] {
        parseInt32("-2147483649")
      }

      intercept[ParseError] {
        parseUInt32("4294967296")
      }

      intercept[ParseError] {
        parseUInt32("-1")
      }

      intercept[ParseError] {
        parseInt64("9223372036854775808")
      }

      intercept[ParseError] {
        parseInt64("-9223372036854775809")
      }

      intercept[ParseError] {
        parseUInt64("18446744073709551616")
      }

      intercept[ParseError] {
        parseUInt64("-1")
      }

      intercept[ParseError] {
        parseInt32("abcd")
      }
    }

    "testParseString" should "pass" in {
      val zh = "\u9999\u6e2f\u4e0a\u6d77\ud84f\udf80\u8c50\u9280\u884c";
      val p = TestAllTypes.fromAscii(
        "optional_string: \"" + zh + "\"").get
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
        "123456789012345678901234567890123456789012345678901234567890";

      val p = TestAllTypes.fromAscii(
        "optional_string: \"" + longText + "\"").get
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
      val good = TestAllTypes.fromAscii(goodText).get
      goodTextCanonical must be(good.toString)

      intercept[TextFormatError] {
        TestAllTypes.fromAscii("optional_bool:2").get
      }
      intercept[TextFormatError] {
        TestAllTypes.fromAscii("optional_bool: foo").get
      }
    }

    "testParseAdjacentStringLiterals" should "pass" in {
      TestAllTypes.fromAscii(
        "optional_string: \"foo\" 'corge' \"grault\"").get.getOptionalString must be(
           "foocorgegrault")
    }

    /*
    TODO(nadavsr): port these too.

    public void testPrintFieldValue() throws Exception {
      assertPrintFieldValue("\"Hello\"", "Hello", "repeated_string");
      assertPrintFieldValue("123.0",  123f, "repeated_float");
      assertPrintFieldValue("123.0",  123d, "repeated_double");
      assertPrintFieldValue("123",  123, "repeated_int32");
      assertPrintFieldValue("123",  123L, "repeated_int64");
      assertPrintFieldValue("true",  true, "repeated_bool");
      assertPrintFieldValue("4294967295", 0xFFFFFFFF, "repeated_uint32");
      assertPrintFieldValue("18446744073709551615",  0xFFFFFFFFFFFFFFFFL,
          "repeated_uint64");
      assertPrintFieldValue("\"\\001\\002\\003\"",
          ByteString.copyFrom(new byte[] {1, 2, 3}), "repeated_bytes");
    }

    private void assertPrintFieldValue(String expect, Object value,
        String fieldName) throws Exception {
      TestAllTypes.Builder builder = TestAllTypes.newBuilder();
      StringBuilder sb = new StringBuilder();
      TextFormat.printFieldValue(
          TestAllTypes.getDescriptor().findFieldByName(fieldName),
          value, sb);
      assertEquals(expect, sb.toString());
    }

    public void testShortDebugString() {
      assertEquals("optional_nested_message { bb: 42 } repeated_int32: 1"
          + " repeated_uint32: 2",
          TextFormat.shortDebugString(TestAllTypes.newBuilder()
              .addRepeatedInt32(1)
              .addRepeatedUint32(2)
              .setOptionalNestedMessage(
                  NestedMessage.newBuilder().setBb(42).build())
              .build()));
    }

    public void testShortDebugString_unknown() {
      assertEquals("5: 1 5: 0x00000002 5: 0x0000000000000003 5: \"4\" 5 { 10: 5 }"
          + " 8: 1 8: 2 8: 3 15: 12379813812177893520 15: 0xabcd1234 15:"
          + " 0xabcdef1234567890",
          TextFormat.shortDebugString(makeUnknownFieldSet()));
    }

    public void testPrintToUnicodeString() throws Exception {
      assertEquals(
          "optional_string: \"abc\u3042efg\"\n" +
          "optional_bytes: \"\\343\\201\\202\"\n" +
          "repeated_string: \"\u3093XYZ\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("abc\u3042efg")
              .setOptionalBytes(bytes(0xe3, 0x81, 0x82))
              .addRepeatedString("\u3093XYZ")
              .build()));

      // Double quotes and backslashes should be escaped
      assertEquals(
          "optional_string: \"a\\\\bc\\\"ef\\\"g\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("a\\bc\"ef\"g")
              .build()));

      // Test escaping roundtrip
      TestAllTypes message = TestAllTypes.newBuilder()
          .setOptionalString("a\\bc\\\"ef\"g")
          .build();
      TestAllTypes.Builder builder = TestAllTypes.newBuilder();
      TextFormat.merge(TextFormat.printToUnicodeString(message), builder);
      assertEquals(message.getOptionalString(), builder.getOptionalString());
    }

    public void testPrintToUnicodeStringWithNewlines() {
      // No newlines at start and end
      assertEquals("optional_string: \"test newlines\n\nin\nstring\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("test newlines\n\nin\nstring")
              .build()));

      // Newlines at start and end
      assertEquals("optional_string: \"\ntest\nnewlines\n\nin\nstring\n\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("\ntest\nnewlines\n\nin\nstring\n")
              .build()));

      // Strings with 0, 1 and 2 newlines.
      assertEquals("optional_string: \"\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("")
              .build()));
      assertEquals("optional_string: \"\n\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("\n")
              .build()));
      assertEquals("optional_string: \"\n\n\"\n",
          TextFormat.printToUnicodeString(TestAllTypes.newBuilder()
              .setOptionalString("\n\n")
              .build()));
    }

    public void testPrintToUnicodeString_unknown() {
      assertEquals(
          "1: \"\\343\\201\\202\"\n",
          TextFormat.printToUnicodeString(UnknownFieldSet.newBuilder()
              .addField(1,
                  UnknownFieldSet.Field.newBuilder()
                  .addLengthDelimited(bytes(0xe3, 0x81, 0x82)).build())
              .build()));
    }
    */

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
      assertParseErrorWithOverwriteForbidden(
          "1:17: Couldn't parse integer: For input string: \"[\"",
          "optional_int32: [1]\n");
    }

    // =======================================================================
    // test oneof
    "testOneofTextFormat" should "pass" in {
      val p = TestOneof2().update(
        _.fooLazyMessage.quxInt := 100,
        _.barString := "101",
        _.bazInt := 102,
        _.bazString := "103"
      )
      TestOneof2.fromAscii(p.toString).get must be(p)
    }

    "testOneofOverwriteAllowed" should "pass" in {
      val input = "foo_string: \"stringvalue\" foo_int: 123";
      val p = TestOneof2.fromAscii(input).get
      p.foo.isFooInt must be(true)
    }
}
