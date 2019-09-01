package scalapb.grpc

import com.google.protobuf.Descriptors
import io.grpc.protobuf.{ProtoMethodDescriptorSupplier, ProtoServiceDescriptorSupplier}

class ConcreteProtoMethodDescriptorSupplier(
    methodDescriptor: Descriptors.MethodDescriptor
) extends ProtoMethodDescriptorSupplier
    with ProtoServiceDescriptorSupplier {
  override def getMethodDescriptor: Descriptors.MethodDescriptor   = methodDescriptor
  override def getServiceDescriptor: Descriptors.ServiceDescriptor = methodDescriptor.getService
  override def getFileDescriptor: Descriptors.FileDescriptor       = getServiceDescriptor.getFile
}

object ConcreteProtoMethodDescriptorSupplier {
  def fromMethodDescriptor(
      methodDescriptor: Descriptors.MethodDescriptor
  ): ConcreteProtoMethodDescriptorSupplier =
    new ConcreteProtoMethodDescriptorSupplier(methodDescriptor)
}
