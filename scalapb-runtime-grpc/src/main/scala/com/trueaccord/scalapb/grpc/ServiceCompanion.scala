package com.trueaccord.scalapb.grpc

import com.google.protobuf.Descriptors.ServiceDescriptor

abstract class ServiceCompanion {
  def descriptor: ServiceDescriptor
}
