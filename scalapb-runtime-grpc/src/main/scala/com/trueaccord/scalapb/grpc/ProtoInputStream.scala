package com.trueaccord.scalapb.grpc

import java.io.{ByteArrayInputStream, IOException, InputStream, OutputStream}

import com.google.common.io.ByteStreams
import com.google.protobuf.CodedOutputStream
import com.trueaccord.scalapb.GeneratedMessage
import com.trueaccord.scalapb.grpc.ProtoInputStream._

private class ProtoInputStream(var state: State) extends InputStream {
  @throws(classOf[IOException])
  def drainTo(target: OutputStream): Int = {
    val bytesWritten = state match {
      case NotStarted(message) =>
        message.writeTo(target)
        message.serializedSize
      case Partial(partial) =>
        ByteStreams.copy(partial, target).toInt
      case Done => throw new IllegalStateException()
    }
    state = Done
    bytesWritten
  }

  @throws(classOf[IOException])
  def read: Int = {
    state match {
      case NotStarted(message) =>
        state = Partial(new ByteArrayInputStream(message.toByteArray))
      case _ =>
    }
    state match {
      case Partial(partial) =>
        partial.read()
      case _ =>
        -1
    }
  }

  @throws(classOf[IOException])
  override def read(b: Array[Byte], off: Int, len: Int): Int =
    state match {
      case NotStarted(message) =>
        message.serializedSize match {
          case 0 =>
            state = Done
            -1
          case size if len >= size =>
            val stream = CodedOutputStream.newInstance(b, off, size)
            message.writeTo(stream)
            stream.flush()
            stream.checkNoSpaceLeft()
            state = Done
            size
          case _ =>
            val partial = new ByteArrayInputStream(message.toByteArray)
            state = Partial(partial)
            partial.read(b, off, len)
        }
      case Partial(partial) =>
        partial.read(b, off, len)
      case _ =>
        -1
    }

  @throws(classOf[IOException])
  override def available: Int = state match {
    case NotStarted(message) => message.serializedSize
    case Partial(partial) => partial.available()
    case _ => 0
  }
}

private object ProtoInputStream {
  sealed trait State
  case class NotStarted[A <: GeneratedMessage](message: A) extends State
  case class Partial(bytes: ByteArrayInputStream) extends State
  case object Done extends State

  def unapply(p: ProtoInputStream): Option[State] = Some(p.state)

  def newInstance(message: GeneratedMessage): InputStream = new ProtoInputStream(NotStarted(message))
}
