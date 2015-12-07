package com.trueaccord.scalapb.grpc

import java.io.InputStream

import com.trueaccord.scalapb.grpc.ProtoInputStream.NotStarted
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

class Marshaller[T <: GeneratedMessage with Message[T]](companion: GeneratedMessageCompanion[T]) extends io.grpc.MethodDescriptor.Marshaller[T] {
  override def stream(t: T): InputStream = ProtoInputStream.newInstance(t)

  override def parse(inputStream: InputStream): T = inputStream match {
    /* Optimization for in-memory transport. */
    case ProtoInputStream(NotStarted(m: T @unchecked)) if m.companion == companion =>
      m
    case is => companion.parseFrom(is)
  }
}

