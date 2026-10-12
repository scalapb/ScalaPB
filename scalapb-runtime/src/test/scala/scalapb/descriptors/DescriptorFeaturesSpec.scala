package scalapb.descriptors

import com.google.protobuf.descriptor._
import com.google.protobuf.descriptor.FeatureSet._
import munit.FunSuite

class DescriptorFeaturesSpec extends FunSuite {
  private def field(
      name: String,
      number: Int,
      t: FieldDescriptorProto.Type = FieldDescriptorProto.Type.TYPE_INT32
  ) =
    FieldDescriptorProto(
      name = Some(name),
      number = Some(number),
      `type` = Some(t),
      label = Some(FieldDescriptorProto.Label.LABEL_OPTIONAL)
    )

  private def build(
      syntax: String,
      features: FeatureSet = FeatureSet(),
      fields: Seq[FieldDescriptorProto] = Seq(field("value", 1)),
      messageFeatures: FeatureSet = FeatureSet()
  ): FileDescriptor =
    FileDescriptor.buildFrom(
      FileDescriptorProto(
        name = Some("features.proto"),
        syntax = Some(syntax),
        edition = if (syntax == "editions") Some(Edition.EDITION_2023) else None,
        options = Some(FileOptions(features = Some(features))),
        messageType = Seq(
          DescriptorProto(
            name = Some("M"),
            field = fields,
            options = Some(MessageOptions(features = Some(messageFeatures))),
            enumType = Seq(
              EnumDescriptorProto(
                name = Some("E"),
                value = Seq(EnumValueDescriptorProto(name = Some("ZERO"), number = Some(0)))
              )
            )
          )
        )
      ),
      Nil
    )

  test("proto2, proto3 and Edition 2023 defaults") {
    for (syntax <- Seq("proto2", "proto3", "editions")) {
      val fd = build(
        syntax,
        fields = Seq(
          field("value", 1),
          field("values", 2).withLabel(FieldDescriptorProto.Label.LABEL_REPEATED),
          field("text", 3, FieldDescriptorProto.Type.TYPE_STRING)
        )
      )
      val fields = fd.messages.head.fields
      assertEquals(fields(0).hasPresence, syntax != "proto3")
      assert(!fields(0).isRequired)
      assert(fields(0).isOptional)
      assertEquals(fields(1).isPacked, syntax != "proto2")
      assert(!fields(1).hasPresence)
      assertEquals(fields(2).needsUtf8Check, syntax != "proto2")
      assertEquals(fd.messages.head.enums.head.isClosed, syntax == "proto2")
    }
  }

  test("file, message, field and enum overrides inherit independently") {
    val fd = build(
      "editions",
      FeatureSet(
        fieldPresence = Some(FieldPresence.IMPLICIT),
        enumType = Some(EnumType.CLOSED),
        utf8Validation = Some(Utf8Validation.NONE)
      ),
      fields = Seq(
        field("required", 1).withOptions(
          FieldOptions(features =
            Some(FeatureSet(fieldPresence = Some(FieldPresence.LEGACY_REQUIRED)))
          )
        ),
        field("implicit", 2),
        field("text", 3, FieldDescriptorProto.Type.TYPE_STRING),
        field("values", 4).withLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
      ),
      messageFeatures = FeatureSet(repeatedFieldEncoding = Some(RepeatedFieldEncoding.EXPANDED))
    )
    val m = fd.messages.head
    assert(m.fields(0).isRequired)
    assert(!m.fields(0).isOptional)
    assert(m.fields(0).hasPresence)
    assert(!m.fields(1).hasPresence)
    assert(!m.fields(2).needsUtf8Check)
    assert(!m.fields(3).isPacked)
    assert(m.enums.head.isClosed)
    val nested = DescriptorProto(
      name = Some("Nested"),
      field = Seq(field("value", 1)),
      options = Some(
        MessageOptions(features = Some(FeatureSet(fieldPresence = Some(FieldPresence.EXPLICIT))))
      )
    )
    val proto = fd.asProto.withMessageType(
      Seq(
        m.asProto
          .withNestedType(Seq(nested))
          .withEnumType(
            Seq(
              m.enums.head.asProto.withOptions(
                EnumOptions(features = Some(FeatureSet(enumType = Some(EnumType.OPEN))))
              )
            )
          )
      )
    )
    val changed = FileDescriptor.buildFrom(proto, Nil).messages.head
    assert(changed.nestedMessages.head.fields.head.hasPresence)
    assert(!changed.enums.head.isClosed)
  }

  test("legacy required, packed and proto3 optional remain supported") {
    val required = field("value", 1).withLabel(FieldDescriptorProto.Label.LABEL_REQUIRED)
    assert(build("proto2", fields = Seq(required)).messages.head.fields.head.isRequired)
    val packed = field("value", 1)
      .withLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
      .withOptions(FieldOptions(packed = Some(true)))
    assert(build("proto2", fields = Seq(packed)).messages.head.fields.head.isPacked)
    assert(
      !build(
        "proto3",
        fields = Seq(packed.withOptions(FieldOptions(packed = Some(false))))
      ).messages.head.fields.head.isPacked
    )
    assert(
      build(
        "proto3",
        fields = Seq(field("value", 1).withProto3Optional(true))
      ).messages.head.fields.head.hasPresence
    )
  }

  test("message presence, oneofs and map encoding exceptions") {
    val msg = field("child", 1, FieldDescriptorProto.Type.TYPE_MESSAGE).withTypeName("M")
    val fd  = build(
      "editions",
      FeatureSet(
        fieldPresence = Some(FieldPresence.IMPLICIT),
        messageEncoding = Some(MessageEncoding.DELIMITED)
      ),
      fields = Seq(
        msg,
        field("choice", 2).withOneofIndex(0),
        field("text", 3, FieldDescriptorProto.Type.TYPE_STRING)
          .withLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
      )
    )
    val proto = fd.asProto.withMessageType(
      Seq(
        fd.messages.head.asProto
          .withOneofDecl(Seq(OneofDescriptorProto(name = Some("choice_oneof"))))
      )
    )
    val m = FileDescriptor.buildFrom(proto, Nil).messages.head
    assert(m.fields(0).hasPresence)
    assertEquals(m.fields(0).messageEncoding, MessageEncoding.DELIMITED)
    assert(m.fields(1).hasPresence)
    assert(!m.fields(2).isPackable)
    val mapEntry = m.asProto.withOptions(MessageOptions(mapEntry = Some(true)))
    val map      = FileDescriptor.buildFrom(proto.withMessageType(Seq(mapEntry)), Nil).messages.head
    assertEquals(map.fields.head.messageEncoding, MessageEncoding.LENGTH_PREFIXED)
    val mapField = msg.withTypeName("Entry").withLabel(FieldDescriptorProto.Label.LABEL_REPEATED)
    val entry    =
      DescriptorProto(name = Some("Entry"), options = Some(MessageOptions(mapEntry = Some(true))))
    val mapContainer = FileDescriptor
      .buildFrom(
        proto.withMessageType(Seq(m.asProto.withField(Seq(mapField)).withOneofDecl(Nil), entry)),
        Nil
      )
      .messages
      .head
    assert(mapContainer.fields.head.isMapField)
    assert(!mapContainer.fields.head.hasPresence)
    assertEquals(mapContainer.fields.head.messageEncoding, MessageEncoding.LENGTH_PREFIXED)
    val group = msg.withType(FieldDescriptorProto.Type.TYPE_GROUP)
    assertEquals(
      build("proto2", fields = Seq(group)).messages.head.fields.head.messageEncoding,
      MessageEncoding.DELIMITED
    )
  }

  test("imported enum features belong to the defining file") {
    val imported  = build("proto2")
    val reference = field("enum_value", 1, FieldDescriptorProto.Type.TYPE_ENUM).withTypeName(".M.E")
    val importing = FileDescriptor.buildFrom(
      FileDescriptorProto(
        name = Some("importing.proto"),
        syntax = Some("editions"),
        edition = Some(Edition.EDITION_2023),
        dependency = Seq("features.proto"),
        messageType = Seq(DescriptorProto(name = Some("Consumer"), field = Seq(reference)))
      ),
      Seq(imported)
    )
    val enumField = importing.messages.head.fields.head
    assertEquals(enumField.resolvedFeatures.getEnumType, EnumType.OPEN)
    assert(enumField.scalaType.asInstanceOf[ScalaType.Enum].descriptor.isClosed)
  }

  test("future editions do not silently use Edition 2023 defaults") {
    val fd =
      FileDescriptor.buildFrom(build("editions").asProto.withEdition(Edition.EDITION_2024), Nil)
    intercept[DescriptorValidationException](fd.resolvedFeatures)
  }
}
