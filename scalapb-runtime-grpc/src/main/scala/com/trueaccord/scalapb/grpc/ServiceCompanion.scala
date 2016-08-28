package com.trueaccord.scalapb.grpc

import com.google.protobuf.Descriptors.ServiceDescriptor

abstract class ServiceCompanion[A <: AbstractService] {
  def descriptor: ServiceDescriptor
}

