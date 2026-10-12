import com.thesamet.proto.e2e.groups_edition.EditionGroups
import com.google.protobuf.{CodedInputStream, InvalidProtocolBufferException}
import com.thesamet.proto.e2e.groups._
import com.thesamet.proto.e2e.groups_discard.GroupDiscard
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.UnknownFieldSet

class GroupsSpec extends AnyFlatSpec with Matchers {
  private def bytes(values: Int*): Array[Byte] = values.map(_.toByte).toArray

  private val message = GroupContainer(
    optionalgroup = Some(
      GroupContainer.OptionalGroup(
        value = Some(11),
        nestedgroup = Some(GroupContainer.OptionalGroup.NestedGroup(text = Some("nested")))
      )
    ),
    repeatedgroup = Seq(GroupContainer.RepeatedGroup(Some(22)), GroupContainer.RepeatedGroup()),
    requiredgroup = GroupContainer.RequiredGroup(Some(33)),
    tail = Some(44)
  )

  "Groups" should "round trip optional, repeated, required and nested fields" in {
    GroupContainer.parseFrom(message.toByteArray) must be(message)
    message.serializedSize must be(message.toByteArray.length)
    message.toByteArray.take(3).toSeq must be(bytes(11, 16, 11).toSeq)
    GroupContainer.scalaDescriptor
      .findFieldByNumber(1)
      .get
      .scalaType
      .isInstanceOf[
        scalapb.descriptors.ScalaType.Message
      ] must be(true)
  }

  it should "merge singular occurrences and continue after the end tag" in {
    // Two occurrences of OptionalGroup followed by the required group and a scalar.
    val wire   = bytes(11, 16, 7, 12, 11, 27, 34, 1, 120, 28, 12, 59, 60, 72, 9)
    val parsed = GroupContainer.parseFrom(wire)
    parsed.getOptionalgroup.value must be(Some(7))
    parsed.getOptionalgroup.getNestedgroup.text must be(Some("x"))
    parsed.tail must be(Some(9))
  }

  it should "preserve unknown groups through serialization and builder copies" in {
    val unknown = GroupUnknown.parseFrom(message.toByteArray)
    unknown.unknownFields.getField(1).get.group.head.getField(3).get.group.size must be(1)
    unknown.serializedSize must be(message.serializedSize)
    GroupContainer.parseFrom(unknown.toByteArray) must be(message)
    new UnknownFieldSet.Builder(unknown.unknownFields).result() must be(unknown.unknownFields)
    val changed = UnknownFieldSet.Field.groupLens.set(Seq(UnknownFieldSet.empty))(
      UnknownFieldSet.Field(varint = Seq(9L))
    )
    changed.varint must be(Seq(9L))
    changed.group must be(Seq(UnknownFieldSet.empty))
  }

  it should "reject truncated, mismatched and stray end tags for known and unknown groups" in {
    val malformed = Seq(
      bytes(11),
      bytes(11, 16, 1),
      bytes(11, 20),
      bytes(12),
      bytes(11, 27, 12, 28),
      bytes(11, 0, 12),
      bytes(11, 27, 28)
    )
    malformed.foreach { wire =>
      // Satisfy the required field first so a missing-required error cannot mask an end-tag bug.
      intercept[InvalidProtocolBufferException](GroupContainer.parseFrom(bytes(59, 60) ++ wire))
      intercept[InvalidProtocolBufferException](GroupUnknown.parseFrom(wire))
      intercept[InvalidProtocolBufferException](
        GroupUnknown.parseFrom(CodedInputStream.newInstance(wire))
      )
    }
  }

  it should "skip unknown groups when preservation is disabled" in {
    GroupDiscard.parseFrom(message.toByteArray).tail must be(Some(44))
    Seq(bytes(11), bytes(11, 20), bytes(12), bytes(11, 27, 12, 28)).foreach { wire =>
      intercept[InvalidProtocolBufferException](GroupDiscard.parseFrom(wire))
    }
  }

  it should "support optional and repeated group extensions" in {
    val extended = message
      .withExtension(GroupsProto.extensiongroup)(Some(ExtensionGroup(Some(1), Some(2))))
      .withExtension(GroupsProto.repeatedextensiongroup)(Seq(RepeatedExtensionGroup(Some(3))))
    val parsed = GroupContainer.parseFrom(extended.toByteArray)
    parsed.extension(GroupsProto.extensiongroup) must be(Some(ExtensionGroup(Some(1), Some(2))))
    parsed.extension(GroupsProto.repeatedextensiongroup) must be(
      Seq(RepeatedExtensionGroup(Some(3)))
    )
    val first =
      message.withExtension(GroupsProto.extensiongroup)(Some(ExtensionGroup(first = Some(7))))
    val second =
      message.withExtension(GroupsProto.extensiongroup)(Some(ExtensionGroup(second = Some(8))))
    GroupContainer
      .parseFrom(first.toByteArray ++ second.toByteArray)
      .extension(GroupsProto.extensiongroup) must be(Some(ExtensionGroup(Some(7), Some(8))))
  }

  "Editions DELIMITED" should "support singular, repeated and oneof groups with length-prefixed overrides and maps" in {
    val payload = EditionGroups.Payload(Some(7))
    val value   = EditionGroups(
      single = Some(payload),
      repeated = Seq(payload),
      choice = EditionGroups.Choice.Selected(payload),
      lengthPrefixed = Some(payload),
      entries = Map("key" -> payload)
    )
    val wire = value.toByteArray
    wire.take(4).toSeq must be(bytes(11, 8, 7, 12).toSeq)
    EditionGroups.parseFrom(wire) must be(value)
    value.serializedSize must be(wire.length)
    EditionGroups.parseFrom(GroupUnknown.parseFrom(wire).toByteArray) must be(value)
  }
  it should "merge repeated occurrences of a delimited oneof and allow a later alternative" in {
    val wire = bytes(27, 8, 7, 28, 27, 28)
    EditionGroups.parseFrom(wire).choice must be(
      EditionGroups.Choice.Selected(EditionGroups.Payload(Some(7)))
    )
    EditionGroups.parseFrom(wire ++ bytes(32, 9)).choice must be(EditionGroups.Choice.Number(9))
    intercept[InvalidProtocolBufferException](EditionGroups.parseFrom(bytes(27, 8, 7, 12)))
  }
}
