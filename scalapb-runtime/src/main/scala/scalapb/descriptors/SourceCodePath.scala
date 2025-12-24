package scalapb.descriptors

import com.google.protobuf.descriptor._

object SourceCodePath {
  def get(fd: Descriptor): Seq[Int] = fd.containingMessage match {
    case None            => Seq(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, fd.index)
    case Some(container) =>
      get(container) ++ Seq(DescriptorProto.NESTED_TYPE_FIELD_NUMBER, fd.index)
  }

  def get(fd: EnumDescriptor): Seq[Int] = fd.containingMessage match {
    case None            => Seq(FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER, fd.index)
    case Some(container) => get(container) ++ Seq(DescriptorProto.ENUM_TYPE_FIELD_NUMBER, fd.index)
  }

  def get(fd: EnumValueDescriptor): Seq[Int] = {
    get(fd.containingEnum) ++ Seq(EnumDescriptorProto.VALUE_FIELD_NUMBER, fd.index)
  }

  def get(fd: FieldDescriptor): Seq[Int] = {
    get(fd.containingMessage) ++ Seq(DescriptorProto.FIELD_FIELD_NUMBER, fd.index)
  }

  def get(fd: ServiceDescriptor): Seq[Int] = {
    Seq(FileDescriptorProto.SERVICE_FIELD_NUMBER, fd.index)
  }

  def get(fd: MethodDescriptor): Seq[Int] = {
    get(fd.containingService) ++ Seq(ServiceDescriptorProto.METHOD_FIELD_NUMBER, fd.index)
  }
}
