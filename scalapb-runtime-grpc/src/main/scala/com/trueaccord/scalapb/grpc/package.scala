package com.trueaccord.scalapb

package object grpc {
  @deprecated("Please use scalapb.grpc package instead of com.trueaccord.scalapb.grpc", "0.7.0")
  type AbstractService = _root_.scalapb.grpc.AbstractService

  @deprecated("Please use scalapb.grpc package instead of com.trueaccord.scalapb.grpc", "0.7.0")
  type ConcreteProtoFileDescriptorSupplier = _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier

  @deprecated("Please use scalapb.grpc package instead of com.trueaccord.scalapb.grpc", "0.7.0")
  val Grpc = _root_.scalapb.grpc.Grpc

  @deprecated("Please use scalapb.grpc package instead of com.trueaccord.scalapb.grpc", "0.7.0")
  type Marshaller[T <: GeneratedMessage with Message[T]] = _root_.scalapb.grpc.Marshaller[T]

  @deprecated("Please use scalapb.grpc package instead of com.trueaccord.scalapb.grpc", "0.7.0")
  val ProtoUtils = _root_.scalapb.grpc.ProtoUtils

  @deprecated("Please use scalapb.grpc package instead of com.trueaccord.scalapb.grpc", "0.7.0")
  type ServiceCompanion[A <: AbstractService] = _root_.scalapb.grpc.ServiceCompanion[A]
}
