package scalapb

import com.google.protobuf.{CodedInputStream, InvalidProtocolBufferException}

class GroupParsingSpec extends munit.FunSuite {
  private def bytes(values: Int*): Array[Byte] = values.map(_.toByte).toArray

  test("legacy companion fallback preserves field order and nested group boundaries") {
    // Field 2 before field 1 (and repeated field 2) must not be sorted or coalesced.
    val body  = bytes(16, 1, 8, 2, 16, 3, 27, 16, 4, 8, 5, 28)
    val input = CodedInputStream.newInstance(body ++ bytes(44, 48, 9))
    assertEquals(LiteParser.readGroupBody(input, 44).toSeq, body.toSeq)
    assertEquals(input.readTag(), 48)
    assertEquals(input.readInt32(), 9)
  }

  test("companions with only the old parseFrom entry point can read groups") {
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
    assertEquals(legacy.parseFrom(input, 44), value.parseFrom(body))
  }

  test("legacy fallback rejects mismatched, truncated and invalid wire tags") {
    Seq(bytes(), bytes(12), bytes(11, 44), bytes(15)).foreach { body =>
      intercept[InvalidProtocolBufferException] {
        LiteParser.readGroupBody(CodedInputStream.newInstance(body), 44)
      }
    }
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
