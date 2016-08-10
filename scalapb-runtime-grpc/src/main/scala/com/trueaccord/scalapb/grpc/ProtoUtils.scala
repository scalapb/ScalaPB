package com.trueaccord.scalapb.grpc

import com.google.protobuf.InvalidProtocolBufferException
import com.trueaccord.scalapb.GeneratedMessage
import io.grpc.Metadata

object ProtoUtils {
  def metadataMarshaller[T <: GeneratedMessage](instance: T): Metadata.BinaryMarshaller[T] =
    new Metadata.BinaryMarshaller[T] {
      override def toBytes(value: T) = value.toByteArray

      override def parseBytes(serialized: Array[Byte]) = {
        try {
          instance.companion.parseFrom(serialized).asInstanceOf[T]
        } catch {
          case ipbe: InvalidProtocolBufferException =>
            throw new IllegalArgumentException(ipbe)
        }
      }
    }

  def keyForProto[T <: GeneratedMessage](instance: T): Metadata.Key[T] =
    Metadata.Key.of(instance.companion.descriptor.getFullName + Metadata.BINARY_HEADER_SUFFIX,
      metadataMarshaller(instance))
}
