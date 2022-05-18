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
}
