package com.trueaccord.scalapb.grpc

import com.google.protobuf.Descriptors.ServiceDescriptor

abstract class ServiceCompanion[A <: AbstractService] {
  @deprecated("Use javaDescriptor instead. This name is going to be used for Scala descriptors.", "ScalaPB 0.5.47")
  def descriptor: ServiceDescriptor = javaDescriptor

  def javaDescriptor: ServiceDescriptor
}

