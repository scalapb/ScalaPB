package scalapb

import com.google.protobuf.conformance.conformance._
import com.google.protobuf_test_messages.proto3.test_messages_proto3.TestAllTypesProto3
import munit.FunSuite

class JsonConformanceSpec extends FunSuite {
  private def jsonRequest(json: String, format: WireFormat = WireFormat.JSON): ConformanceResponse =
    ConformanceScala.handleTest(
      ConformanceRequest()
        .withMessageType("protobuf_test_messages.proto3.TestAllTypesProto3")
        .withJsonPayload(json)
        .withRequestedOutputFormat(format)
    )

  test("FieldMaskInvalidCharacter") {
    assert(jsonRequest("""{"optionalFieldMask":"foo,bar_bar"}""").result.isParseError)
    val response = jsonRequest("""{"optionalFieldMask":"foo,barBar,Foo"}""", WireFormat.PROTOBUF)
    assert(response.result.isProtobufPayload, response.toString)
    assertEquals(
      TestAllTypesProto3
        .parseFrom(response.getProtobufPayload.toByteArray)
        .getOptionalFieldMask
        .paths,
      Seq("foo", "bar_bar", "_foo")
    )
  }
  test("FieldMaskPathsDontRoundTrip, FieldMaskNumbersDontRoundTrip, FieldMaskTooManyUnderscore") {
    for (path <- Seq("fooBar", "foo_3_bar", "foo__bar")) {
      val message = TestAllTypesProto3(optionalFieldMask =
        Some(com.google.protobuf.field_mask.FieldMask(Seq(path)))
      )
      val response = ConformanceScala.handleTest(
        ConformanceRequest()
          .withMessageType("protobuf_test_messages.proto3.TestAllTypesProto3")
          .withProtobufPayload(message.toByteString)
          .withRequestedOutputFormat(WireFormat.JSON)
      )
      assert(response.result.isSerializeError, response.toString)
    }
  }
  test("BytesFieldBase64Url in protobuf and JSON output") {
    val binary = jsonRequest("""{"optionalBytes":"-_"}""", WireFormat.PROTOBUF)
    assert(binary.result.isProtobufPayload, binary.toString)
    assertEquals(
      TestAllTypesProto3.parseFrom(binary.getProtobufPayload.toByteArray).optionalBytes,
      com.google.protobuf.ByteString.copyFrom(Array[Byte](0xfb.toByte))
    )
    val json = jsonRequest("""{"optionalBytes":"-_"}""")
    assert(json.result.isJsonPayload, json.toString)
    assertEquals(json.getJsonPayload, """{"optionalBytes":"+w=="}""")
  }
  test("NullValueInOtherOneofNewFormat retains oneof presence") {
    val response = jsonRequest("""{"oneofNullValue":null}""")
    assert(response.result.isJsonPayload, response.toString)
    assertEquals(response.getJsonPayload, """{"oneofNullValue":null}""")
  }
  test("BoolFieldDoubleQuotedTrue and BoolFieldDoubleQuotedFalse") {
    for (value <- Seq("true", "false")) {
      val response = jsonRequest(s"""{"optionalBool":"$value"}""")
      assert(response.result.isParseError, response.toString)
    }
  }
  test("StringField rejects unpaired and reversed Unicode surrogates") {
    for (units <- Seq(Seq("D800"), Seq("DC00"), Seq("DE01", "D83D"))) {
      val escaped  = units.map(unit => 92.toChar.toString + "u" + unit).mkString
      val response = jsonRequest(s"""{"optionalString":"$escaped"}""")
      assert(response.result.isParseError, response.toString)
    }
  }
  test("FieldNameExtension preserves proto2 extension fields") {
    val json    = """{"[protobuf_test_messages.proto2.extension_int32]":1}"""
    val request = ConformanceRequest()
      .withMessageType("protobuf_test_messages.proto2.TestAllTypesProto2")
      .withJsonPayload(json)
    val response = ConformanceScala.handleTest(request.withRequestedOutputFormat(WireFormat.JSON))
    assert(response.result.isJsonPayload, response.toString)
    assertEquals(response.getJsonPayload, json)
    val binary = ConformanceScala.handleTest(request.withRequestedOutputFormat(WireFormat.PROTOBUF))
    assert(binary.result.isProtobufPayload, binary.toString)
    import com.google.protobuf_test_messages.proto2.test_messages_proto2.{
      TestAllTypesProto2,
      TestMessagesProto2Proto
    }
    assertEquals(
      TestAllTypesProto2
        .parseFrom(binary.getProtobufPayload.toByteArray)
        .extension(TestMessagesProto2Proto.extensionInt32),
      Some(1)
    )
  }
}
