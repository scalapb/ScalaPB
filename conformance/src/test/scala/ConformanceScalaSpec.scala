package scalapb

import com.google.protobuf.ByteString
import com.google.protobuf.conformance.conformance._
import com.google.protobuf_test_messages.proto2.TestMessagesProto2
import com.google.protobuf_test_messages.proto3.TestMessagesProto3
import munit.FunSuite

class ConformanceScalaSpec extends FunSuite {
  private def request(name: String, bytes: Array[Byte]) =
    ConformanceRequest(messageType = name, requestedOutputFormat = WireFormat.PROTOBUF)
      .withProtobufPayload(ByteString.copyFrom(bytes))

  test("failure-set handshake uses the current protocol") {
    val response =
      ConformanceScala.handleTest(ConformanceRequest(messageType = "conformance.FailureSet"))
    assertEquals(FailureSet.parseFrom(response.getProtobufPayload.toByteArray), FailureSet())
  }

  test("Editions are explicitly skipped until code generation supports their features") {
    Seq(
      "protobuf_test_messages.editions.proto2.TestAllTypesProto2",
      "protobuf_test_messages.editions.proto3.TestAllTypesProto3",
      "protobuf_test_messages.editions.TestAllTypesEdition2023",
      "protobuf_test_messages.edition_unstable.TestAllTypesEditionUnstable"
    ).foreach { name =>
      assert(ConformanceScala.handleTest(request(name, Array.emptyByteArray)).result.isSkipped)
    }
  }

  test("unknown message types report a protocol error without terminating dispatch") {
    assert(
      ConformanceScala
        .handleTest(request("unknown.Type", Array.emptyByteArray))
        .result
        .isRuntimeError
    )
    assert(
      ConformanceScala
        .handleTest(
          request(
            "protobuf_test_messages.proto3.TestAllTypesProto3",
            Array.emptyByteArray
          )
        )
        .result
        .isProtobufPayload
    )
  }

  test("unsupported JSPB is skipped and missing payload is a runtime error") {
    val base = request("protobuf_test_messages.proto3.TestAllTypesProto3", Array.emptyByteArray)
    assert(ConformanceScala.handleTest(base.withJspbPayload("[]")).result.isSkipped)
    assert(
      ConformanceScala.handleTest(base.withRequestedOutputFormat(WireFormat.JSPB)).result.isSkipped
    )
    assert(ConformanceScala.handleTest(base.clearPayload).result.isRuntimeError)
  }

  test("text serialization failures are protocol responses, not process failures") {
    val response = ConformanceScala.handleTest(
      ConformanceRequest(
        messageType = "protobuf_test_messages.proto2.TestAllTypesProto2",
        requestedOutputFormat = WireFormat.TEXT_FORMAT
      ).withTextPayload("optional_nested_enum: 1")
    )
    // Current enum reflection throws; accept successful serialization once it is fixed.
    assert(response.result.isSerializeError || response.result.isTextPayload)
  }

  // Compare semantic messages: protobuf does not require byte-identical serialization.
  test("proto2 duplicate scalars and unknown fields round trip like Java") {
    val bytes    = Array[Byte](8, 1, 8, 2, 0xf8.toByte, 0x07, 3)
    val expected = TestMessagesProto2.TestAllTypesProto2.parseFrom(bytes)
    val response = ConformanceScala.handleTest(
      request(
        "protobuf_test_messages.proto2.TestAllTypesProto2",
        bytes
      )
    )
    assertEquals(
      TestMessagesProto2.TestAllTypesProto2.parseFrom(response.getProtobufPayload),
      expected
    )
  }

  test("proto3 mixed packed and unpacked repeated values round trip like Java") {
    // repeated_int32 = 31: unpacked 1, packed [2, 3], unpacked 4.
    val bytes    = Array[Byte](0xf8.toByte, 1, 1, 0xfa.toByte, 1, 2, 2, 3, 0xf8.toByte, 1, 4)
    val expected = TestMessagesProto3.TestAllTypesProto3.parseFrom(bytes)
    val response = ConformanceScala.handleTest(
      request(
        "protobuf_test_messages.proto3.TestAllTypesProto3",
        bytes
      )
    )
    assertEquals(
      TestMessagesProto3.TestAllTypesProto3.parseFrom(response.getProtobufPayload),
      expected
    )
  }

  test("truncated scalar payload is rejected by both Java and ScalaPB") {
    val bytes = Array[Byte](8, 0x80.toByte)
    intercept[com.google.protobuf.InvalidProtocolBufferException] {
      TestMessagesProto3.TestAllTypesProto3.parseFrom(bytes)
    }
    assert(
      ConformanceScala
        .handleTest(
          request(
            "protobuf_test_messages.proto3.TestAllTypesProto3",
            bytes
          )
        )
        .result
        .isParseError
    )
  }
}
