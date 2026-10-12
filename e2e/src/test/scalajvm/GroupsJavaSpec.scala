import com.thesamet.proto.e2e.groups_edition.EditionGroups
import com.google.protobuf.{ExtensionRegistry, InvalidProtocolBufferException}
import com.thesamet.proto.e2e.{Groups, GroupsEdition}
import com.thesamet.proto.e2e.groups._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class GroupsJavaSpec extends AnyFlatSpec with Matchers {
  "Groups" should "interoperate with Java including nested groups and extensions" in {
    val javaValue = Groups.GroupContainer
      .newBuilder()
      .setOptionalGroup(
        Groups.GroupContainer.OptionalGroup
          .newBuilder()
          .setValue(7)
          .setNestedGroup(
            Groups.GroupContainer.OptionalGroup.NestedGroup.newBuilder().setText("nested")
          )
      )
      .addRepeatedGroup(Groups.GroupContainer.RepeatedGroup.newBuilder().setValue(8))
      .addRepeatedGroup(Groups.GroupContainer.RepeatedGroup.getDefaultInstance)
      .setRequiredGroup(Groups.GroupContainer.RequiredGroup.newBuilder().setValue(9))
      .setTail(10)
      .build()
    val scalaValue = GroupContainer.parseFrom(javaValue.toByteArray)
    scalaValue.serializedSize must be(javaValue.getSerializedSize)
    scalaValue.toByteArray.toSeq must be(javaValue.toByteArray.toSeq)
    GroupContainer.toJavaProto(scalaValue) must be(javaValue)
    GroupContainer.fromJavaProto(javaValue) must be(scalaValue)

    val extended = scalaValue
      .withExtension(GroupsProto.extensiongroup)(Some(ExtensionGroup(Some(11), Some(12))))
      .withExtension(GroupsProto.repeatedextensiongroup)(Seq(RepeatedExtensionGroup(Some(13))))
    val registry = ExtensionRegistry.newInstance()
    Groups.registerAllExtensions(registry)
    val javaExtended = Groups.GroupContainer.parseFrom(extended.toByteArray, registry)
    javaExtended
      .getField(Groups.extensionGroup.getDescriptor)
      .asInstanceOf[Groups.ExtensionGroup]
      .getFirst must be(11)
    javaExtended
      .getRepeatedField(Groups.repeatedExtensionGroup.getDescriptor, 0)
      .asInstanceOf[Groups.RepeatedExtensionGroup]
      .getValue must be(13)
    GroupContainer.parseFrom(javaExtended.toByteArray) must be(extended)
  }

  it should "preserve Java unknown groups and agree on malformed input" in {
    val wire         = Array[Byte](11, 16, 7, 27, 32, 8, 28, 12, 11, 12)
    val javaUnknown  = Groups.GroupUnknown.parseFrom(wire)
    val scalaUnknown = GroupUnknown.parseFrom(wire)
    Groups.GroupUnknown.parseFrom(scalaUnknown.toByteArray) must be(javaUnknown)
    scalaUnknown.serializedSize must be(javaUnknown.getSerializedSize)
    Seq(Array[Byte](11), Array[Byte](11, 20), Array[Byte](12), Array[Byte](11, 27, 12, 28))
      .foreach { invalid =>
        intercept[InvalidProtocolBufferException](Groups.GroupUnknown.parseFrom(invalid))
        intercept[InvalidProtocolBufferException](GroupUnknown.parseFrom(invalid))
      }
  }

  "Editions groups" should "match Java encoding and conversions" in {
    val payload = EditionGroups.Payload(Some(7))
    val value   = EditionGroups(
      single = Some(payload),
      repeated = Seq(payload),
      choice = EditionGroups.Choice.Selected(payload),
      lengthPrefixed = Some(payload),
      entries = Map("key" -> payload)
    )
    val javaValue = GroupsEdition.EditionGroups.parseFrom(value.toByteArray)
    value.serializedSize must be(javaValue.getSerializedSize)
    value.toByteArray.toSeq must be(javaValue.toByteArray.toSeq)
    EditionGroups.fromJavaProto(javaValue) must be(value)
    EditionGroups.toJavaProto(value) must be(javaValue)
    javaValue.getDescriptorForType.findFieldByNumber(1).getType must be(
      com.google.protobuf.Descriptors.FieldDescriptor.Type.GROUP
    )
    javaValue.getDescriptorForType.findFieldByNumber(5).getType must be(
      com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE
    )
  }
}
