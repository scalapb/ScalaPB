package scalapb.descriptors

import org.scalatest._

class EnumDescriptorSpec extends FlatSpec with MustMatchers {
  "findValueByNumberCreatingIfUnknown" should "remember unknown instances" in {
    val sd: scalapb.descriptors.EnumDescriptor =
      com.google.protobuf.descriptor.FieldDescriptorProto.Type.scalaDescriptor
    val d1 = sd.findValueByNumberCreatingIfUnknown(1235)
    val d2 = sd.findValueByNumberCreatingIfUnknown(1235)
    d1 must be theSameInstanceAs(d2)
  }
}
