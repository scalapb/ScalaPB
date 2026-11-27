package scalapb.descriptors

import munit._

class EnumDescriptorSpec extends FunSuite {
  test("findValueByNumberCreatingIfUnknown should remember unknown instances") {
    val sd: scalapb.descriptors.EnumDescriptor =
      com.google.protobuf.descriptor.FieldDescriptorProto.Type.scalaDescriptor
    val d1 = sd.findValueByNumberCreatingIfUnknown(1235)
    val d2 = sd.findValueByNumberCreatingIfUnknown(1235)
    assertEquals(d1, d2)
  }

  test("enum openness follows the defining syntax and Edition file/enum features") {
    import com.google.protobuf.descriptor._
    def enumDescriptor(
        syntax: String,
        fileType: Option[FeatureSet.EnumType],
        enumType: Option[FeatureSet.EnumType]
    ) = {
      val proto = FileDescriptorProto(
        name = Some("enum.proto"),
        syntax = Some(syntax),
        edition = if (syntax == "editions") Some(Edition.EDITION_2023) else None,
        options = Some(FileOptions(features = Some(FeatureSet(enumType = fileType)))),
        enumType = Seq(
          EnumDescriptorProto(
            name = Some("Value"),
            value = Seq(EnumValueDescriptorProto(name = Some("ZERO"), number = Some(0))),
            options = Some(EnumOptions(features = Some(FeatureSet(enumType = enumType))))
          )
        )
      )
      FileDescriptor.buildFrom(proto, Nil).enums.head
    }
    assert(enumDescriptor("", None, None).isClosed)
    assert(enumDescriptor("proto2", None, None).isClosed)
    assert(!enumDescriptor("proto3", None, None).isClosed)
    assert(!enumDescriptor("editions", None, None).isClosed)
    assert(enumDescriptor("editions", Some(FeatureSet.EnumType.CLOSED), None).isClosed)
    assert(
      !enumDescriptor(
        "editions",
        Some(FeatureSet.EnumType.CLOSED),
        Some(FeatureSet.EnumType.OPEN)
      ).isClosed
    )
    assert(
      enumDescriptor(
        "editions",
        Some(FeatureSet.EnumType.OPEN),
        Some(FeatureSet.EnumType.CLOSED)
      ).isClosed
    )
  }

}
