package com.trueaccord.scalapb.grpc

import com.google.protobuf.InvalidProtocolBufferException
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import io.grpc.Metadata

object ProtoUtils {
  def metadataMarshaller[T <: GeneratedMessage with Message[T]](implicit companion: GeneratedMessageCompanion[T]): Metadata.BinaryMarshaller[T] =
    new Metadata.BinaryMarshaller[T] {
      override def toBytes(value: T) = value.toByteArray

      override def parseBytes(serialized: Array[Byte]) = {
        try {
          companion.parseFrom(serialized)
        } catch {
          case ipbe: InvalidProtocolBufferException =>
            throw new IllegalArgumentException(ipbe)
        }
      }
    }

  def keyForProto[T <: GeneratedMessage with Message[T]](implicit companion: GeneratedMessageCompanion[T]): Metadata.Key[T] =
    Metadata.Key.of(companion.descriptor.getFullName + Metadata.BINARY_HEADER_SUFFIX,
      metadataMarshaller[T])
}
