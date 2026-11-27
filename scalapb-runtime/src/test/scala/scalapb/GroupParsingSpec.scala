package scalapb

import com.google.protobuf.{CodedInputStream, InvalidProtocolBufferException}

class GroupParsingSpec extends munit.FunSuite {
  private def bytes(values: Int*): Array[Byte] = values.map(_.toByte).toArray

  test("empty group payloads serialize and merge on every platform") {
    val empty = com.google.protobuf.struct.Value()
    assertEquals(empty.toByteString, com.google.protobuf.ByteString.EMPTY)
    val original = empty.withBoolValue(true)
    val input    = CodedInputStream.newInstance(bytes(12))
    assertEquals(LiteParser.readGroup(input, 1, original), original)
  }

  test(
    "group readers preserve oneof order and leave the enclosing input positioned after the end tag"
  ) {
    val body  = bytes(32, 1, 8, 0)
    val input = CodedInputStream.newInstance(body ++ bytes(44, 48, 9))
    assertEquals(
      LiteParser.readGroup[com.google.protobuf.struct.Value](input, 5),
      com.google.protobuf.struct.Value.parseFrom(body)
    )
    assertEquals(input.readTag(), 48)
    assertEquals(input.readInt32(), 9)
  }

  test("group readers use the existing coded-input companion entry point") {
    val value  = com.google.protobuf.struct.Value
    val legacy = new GeneratedMessageCompanion[com.google.protobuf.struct.Value] {
      def parseFrom(input: CodedInputStream)          = value.parseFrom(input)
      def javaDescriptor                              = value.javaDescriptor
      def scalaDescriptor                             = value.scalaDescriptor
      def nestedMessagesCompanions                    = value.nestedMessagesCompanions
      def messageReads                                = value.messageReads
      def messageCompanionForFieldNumber(number: Int) = value.messageCompanionForFieldNumber(number)
      def enumCompanionForFieldNumber(number: Int)    = value.enumCompanionForFieldNumber(number)
      def defaultInstance                             = value.defaultInstance
    }
    // The lower-numbered null_value must win over the earlier bool_value in the oneof.
    val body  = bytes(32, 1, 8, 0)
    val input = CodedInputStream.newInstance(body ++ bytes(44))
    assertEquals(LiteParser.readGroup(input, 5)(legacy), value.parseFrom(body))
  }

  test("group readers reject mismatched, truncated and invalid wire tags") {
    Seq(bytes(), bytes(12), bytes(11, 44), bytes(15)).foreach { body =>
      intercept[InvalidProtocolBufferException] {
        LiteParser
          .readGroup[com.google.protobuf.struct.Value](CodedInputStream.newInstance(body), 5)
      }
    }
  }

  test("ordinary message boundaries reject a stray end-group tag") {
    val companion = com.google.protobuf.struct.Value
    val wire      = bytes(12)
    val input     = CodedInputStream.newInstance(wire)
    assertEquals(companion.parseFrom(input), companion.defaultInstance)
    input.checkLastTagWas(12)
    intercept[InvalidProtocolBufferException](companion.parseFrom(wire))
    intercept[InvalidProtocolBufferException](
      companion.parseFrom(new java.io.ByteArrayInputStream(wire))
    )
    intercept[InvalidProtocolBufferException] {
      LiteParser
        .readMessage[com.google.protobuf.struct.Value](CodedInputStream.newInstance(bytes(1, 12)))
    }
    val payload = com.google.protobuf.ByteString.copyFrom(wire)
    intercept[InvalidProtocolBufferException] {
      GeneratedExtension.readMessageFromByteString(companion)(payload)
    }
    intercept[InvalidProtocolBufferException] {
      com.google.protobuf.any
        .Any("type.googleapis.com/google.protobuf.Value", payload)
        .unpack[com.google.protobuf.struct.Value]
    }
  }

  test("end-group predicate recognizes only the end-group wire type") {
    for (number <- Seq(1, 5, 100, 536870911); wireType <- 0 to 7)
      assertEquals(WireType.isEndGroup((number << 3) | wireType), wireType == 4)
  }

  test("unknown group copies and extension byte lenses retain every wire kind") {
    val body  = bytes(8, 1, 17, 1, 0, 0, 0, 0, 0, 0, 0, 26, 1, 120, 37, 2, 0, 0, 0, 43, 44)
    val input = CodedInputStream.newInstance(body ++ bytes(52))
    val group = UnknownFieldSet.readGroup(input, 52)
    val field = UnknownFieldSet.Field(group = Seq(group))
    val copy  = field.copy(Seq(1L), Seq.empty, Seq.empty, Seq.empty)
    assertEquals(copy.group, field.group)
    val encoded = UnknownFieldSet.Field.groupBytesLens.get(field)
    assertEquals(UnknownFieldSet.Field.groupBytesLens.set(encoded)(UnknownFieldSet.Field()), field)
    val roundTrip = UnknownFieldSet.readGroup(group.toByteString.newCodedInput(), 0)
    assertEquals(roundTrip, group)
    assertEquals(group.toByteString.size, group.serializedSize)
  }
}
