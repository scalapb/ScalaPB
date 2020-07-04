package scalapb.descriptors

import utest._

object EnumDescriptorSpec extends TestSuite {
  val tests = Tests {
    "findValueByNumberCreatingIfUnknown should remember unknown instances" - {
      val sd: scalapb.descriptors.EnumDescriptor =
        com.google.protobuf.descriptor.FieldDescriptorProto.Type.scalaDescriptor
      val d1 = sd.findValueByNumberCreatingIfUnknown(1235)
      val d2 = sd.findValueByNumberCreatingIfUnknown(1235)
      assert(d1 eq d2)
    }
  }
}
