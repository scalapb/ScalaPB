package scalapb.grpc

import com.google.protobuf.Descriptors
import io.grpc.protobuf.ProtoFileDescriptorSupplier

class ConcreteProtoFileDescriptorSupplier(descriptor: Descriptors.FileDescriptor) extends ProtoFileDescriptorSupplier {
  override def getFileDescriptor: Descriptors.FileDescriptor = descriptor
}
