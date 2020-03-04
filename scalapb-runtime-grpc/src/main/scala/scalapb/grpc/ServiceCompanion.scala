package scalapb.grpc

import com.google.protobuf.Descriptors.ServiceDescriptor
import io.grpc.ServerServiceDefinition
import scala.concurrent.ExecutionContext

abstract class ServiceCompanion[A <: AbstractService] {
  @deprecated(
    "Use javaDescriptor instead. This name is going to be used for Scala descriptors.",
    "ScalaPB 0.5.47"
  )
  def descriptor: ServiceDescriptor = javaDescriptor

  def javaDescriptor: ServiceDescriptor

  def scalaDescriptor: scalapb.descriptors.ServiceDescriptor

  def bindService(serviceImpl: A, executionContext: ExecutionContext): ServerServiceDefinition
}
