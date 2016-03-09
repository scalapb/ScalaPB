package com.trueaccord.scalapb.grpc

import java.io.{ByteArrayInputStream, InputStream}

import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

class Marshaller[T <: GeneratedMessage with Message[T]](companion: GeneratedMessageCompanion[T]) extends io.grpc.MethodDescriptor.Marshaller[T] {
  override def stream(t: T): InputStream = new ByteArrayInputStream(t.toByteArray)

  override def parse(inputStream: InputStream): T =
    companion.parseFrom(inputStream)
}

