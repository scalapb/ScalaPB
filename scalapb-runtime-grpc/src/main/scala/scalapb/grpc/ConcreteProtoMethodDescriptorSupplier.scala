package scalapb.grpc

import com.google.protobuf.Descriptors
import io.grpc.protobuf.{ProtoMethodDescriptorSupplier, ProtoServiceDescriptorSupplier}

class ConcreteProtoMethodDescriptorSupplier(
    fileDescriptor: Descriptors.FileDescriptor,
    serviceName: String,
    methodName: String) extends ProtoMethodDescriptorSupplier with ProtoServiceDescriptorSupplier {
  override def getMethodDescriptor: Descriptors.MethodDescriptor = getServiceDescriptor.findMethodByName(methodName)

  override def getServiceDescriptor: Descriptors.ServiceDescriptor = getFileDescriptor.findServiceByName(serviceName)

  override def getFileDescriptor: Descriptors.FileDescriptor = fileDescriptor
}
