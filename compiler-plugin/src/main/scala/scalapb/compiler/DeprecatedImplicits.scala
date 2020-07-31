package scalapb.compiler

import com.google.protobuf.Descriptors._

abstract class DeprecatedImplicits {
  this: DescriptorImplicits =>
  import language.implicitConversions

  @deprecated(
    "MethodDescriptorPimp class has been deprecated, use ExtendedMethodDescriptor instead.",
    "0.11.0"
  )
  implicit final def MethodDescriptorPimp(self: MethodDescriptor): ExtendedMethodDescriptor =
    new ExtendedMethodDescriptor(self)

  @deprecated(
    "ServiceDescriptorPimp class has been deprecated, use ExtendedServiceDescriptor instead.",
    "0.11.0"
  )
  implicit final def ServiceDescriptorPimp(self: ServiceDescriptor): ExtendedServiceDescriptor =
    new ExtendedServiceDescriptor(self)

  @deprecated(
    "FieldDescriptorPimp class has been deprecated, use ExtendedFieldDescriptor instead.",
    "0.11.0"
  )
  implicit final def FieldDescriptorPimp(self: FieldDescriptor): ExtendedFieldDescriptor =
    new ExtendedFieldDescriptor(self)

  @deprecated(
    "MessageDescriptorPimp class has been deprecated, use ExtendedMessageDescriptor instead.",
    "0.11.0"
  )
  implicit final def MessageDescriptorPimp(self: Descriptor): ExtendedMessageDescriptor =
    new ExtendedMessageDescriptor(self)

  @deprecated(
    "OneofDescriptorPimp class has been deprecated, use ExtendedOneofDescriptor instead.",
    "0.11.0"
  )
  implicit final def OneofDescriptorPimp(self: OneofDescriptor): ExtendedOneofDescriptor =
    new ExtendedOneofDescriptor(self)

  @deprecated(
    "EnumDescriptorPimp class has been deprecated, use ExtendedEnumDescriptor instead.",
    "0.11.0"
  )
  implicit final def EnumDescriptorPimp(self: EnumDescriptor): ExtendedEnumDescriptor =
    new ExtendedEnumDescriptor(self)

  @deprecated(
    "EnumValueDescriptorPimp class has been deprecated, use ExtendedEnumValueDescriptor instead.",
    "0.11.0"
  )
  implicit final def EnumValueDescriptorPimp(
      self: EnumValueDescriptor
  ): ExtendedEnumValueDescriptor =
    new ExtendedEnumValueDescriptor(self)

  @deprecated(
    "FileDescriptorPimp class has been deprecated, use ExtendedEnumValueDescriptor instead.",
    "0.11.0"
  )
  implicit final def FileDescriptorPimp(self: FileDescriptor): ExtendedFileDescriptor =
    new ExtendedFileDescriptor(self)
}
