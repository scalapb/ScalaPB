package scalapb.grpc

import com.google.protobuf.Descriptors
import io.grpc.protobuf.{ProtoMethodDescriptorSupplier, ProtoServiceDescriptorSupplier}

class ConcreteProtoMethodDescriptorSupplier(
    fileDescriptor: Descriptors.FileDescriptor,
    serviceDescriptor: Descriptors.ServiceDescriptor,
    methodDescriptor: Descriptors.MethodDescriptor
) extends ProtoMethodDescriptorSupplier
    with ProtoServiceDescriptorSupplier {
  override def getMethodDescriptor: Descriptors.MethodDescriptor   = methodDescriptor
  override def getServiceDescriptor: Descriptors.ServiceDescriptor = serviceDescriptor
  override def getFileDescriptor: Descriptors.FileDescriptor       = fileDescriptor
}
