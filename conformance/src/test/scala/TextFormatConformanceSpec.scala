package scalapb

import com.google.protobuf.{ByteString, DynamicMessage, TextFormat => JavaTextFormat}
import com.google.protobuf.conformance.conformance._
import com.google.protobuf_test_messages.proto3.test_messages_proto3.TestAllTypesProto3
import munit.FunSuite

class TextFormatConformanceSpec extends FunSuite {
  private def checkText(input: String, expected: TestAllTypesProto3): Unit = {
    for (format <- Seq(WireFormat.PROTOBUF, WireFormat.TEXT_FORMAT)) {
      val response = ConformanceScala.handleTest(
        ConformanceRequest()
          .withMessageType("protobuf_test_messages.proto3.TestAllTypesProto3")
          .withTextPayload(input)
          .withRequestedOutputFormat(format)
          .withTestCategory(TestCategory.TEXT_FORMAT_TEST)
      )
      if (format == WireFormat.PROTOBUF) {
        assert(response.result.isProtobufPayload, response.toString)
        assertEquals(
          TestAllTypesProto3.parseFrom(response.getProtobufPayload.toByteArray),
          expected
        )
      } else {
        assert(response.result.isTextPayload, response.toString)
        val parsed = DynamicMessage.newBuilder(TestAllTypesProto3.javaDescriptor)
        JavaTextFormat.getParser.merge(response.getTextPayload, parsed)
        assertEquals(
          parsed.build(),
          DynamicMessage.parseFrom(TestAllTypesProto3.javaDescriptor, expected.toByteArray)
        )
      }
    }
  }

  test("StringLiteralShortUnicodeEscapeString and Bytes") {
    val escape = "\\" + "u1234"
    checkText("optional_string: '" + escape + "'", TestAllTypesProto3(optionalString = "\u1234"))
    checkText(
      "optional_bytes: '" + escape + "'",
      TestAllTypesProto3(optionalBytes = ByteString.copyFromUtf8("\u1234"))
    )
  }
  test("StringLiteralLongUnicodeEscapesString and Bytes") {
    val escape   = "\\" + "U00001234" + "\\" + "U00010437"
    val expected = "\u1234" + new String(Character.toChars(0x10437))
    checkText("optional_string: '" + escape + "'", TestAllTypesProto3(optionalString = expected))
    checkText(
      "optional_bytes: '" + escape + "'",
      TestAllTypesProto3(optionalBytes = ByteString.copyFromUtf8(expected))
    )
  }
  test("AnyField with expanded type URL") {
    checkText(
      "optional_any { [type.googleapis.com/protobuf_test_messages.proto3.TestAllTypesProto3] { optional_int32: 12345 } }",
      TestAllTypesProto3(optionalAny =
        Some(com.google.protobuf.any.Any.pack(TestAllTypesProto3(optionalInt32 = 12345)))
      )
    )
  }
  test("ScalarUnknownFields_Print and Drop") {
    val unknown =
      com.google.protobuf_test_messages.proto2.test_messages_proto2.UnknownToTestAllTypes(
        optionalInt32 = Some(123),
        optionalString = Some("hello"),
        optionalBool = Some(true)
      )
    for (printUnknown <- Seq(false, true)) {
      val response = ConformanceScala.handleTest(
        ConformanceRequest()
          .withMessageType("protobuf_test_messages.proto3.TestAllTypesProto3")
          .withProtobufPayload(unknown.toByteString)
          .withRequestedOutputFormat(WireFormat.TEXT_FORMAT)
          .withPrintUnknownFields(printUnknown)
      )
      assert(response.result.isTextPayload, response.toString)
      assertEquals(
        response.getTextPayload,
        if (printUnknown) "1001: 123\n1002: \"hello\"\n1006: 1\n" else ""
      )
    }
  }
  test("MessageUnknownFields_Print") {
    val unknown =
      com.google.protobuf_test_messages.proto2.test_messages_proto2.UnknownToTestAllTypes(
        nestedMessage = Some(
          com.google.protobuf_test_messages.proto2.test_messages_proto2
            .ForeignMessageProto2(c = Some(111))
        )
      )
    val response = ConformanceScala.handleTest(
      ConformanceRequest()
        .withMessageType("protobuf_test_messages.proto3.TestAllTypesProto3")
        .withProtobufPayload(unknown.toByteString)
        .withRequestedOutputFormat(WireFormat.TEXT_FORMAT)
        .withPrintUnknownFields(true)
    )
    assert(response.result.isTextPayload, response.toString)
    assertEquals(response.getTextPayload, "1003 {\n  1: 111\n}\n")
  }
}
