package com.trueaccord.scalapb

import java.io.InputStream

import com.google.protobuf.{CodedOutputStream, CodedInputStream}

object LiteParser {
  def parseFrom[A <: GeneratedMessage with Message[A]](companion: GeneratedMessageCompanion[A], input: CodedInputStream): A = {
    companion.defaultInstance.mergeFrom(input)
  }

  def readMessage[A](input: CodedInputStream, message: Message[A]): A = {
    val length = input.readRawVarint32()
    val oldLimit = input.pushLimit(length)
    val result: A = message.mergeFrom(input)
    input.checkLastTagWas(0)
    input.popLimit(oldLimit)
    result
  }

  def parseDelimitedFrom[A <: GeneratedMessage with Message[A]](companion: GeneratedMessageCompanion[A], input: InputStream): Option[A] = {
    val b = input.read()
    if (b < 0) None
    else {
      val size = CodedInputStream.readRawVarint32(b, input)
      Some(parseFrom(companion, CodedInputStream.newInstance(new LimitedInputStream(input, size))))
    }
  }

  def parseDelimitedFrom[A <: GeneratedMessage with Message[A]](companion: GeneratedMessageCompanion[A],
                                                                input: CodedInputStream): Option[A] = {
    if (input.isAtEnd) None
    else Some(readMessage(input, companion.defaultInstance))
  }

  @inline
  def preferredCodedOutputStreamBufferSize(dataLength: Int) =
    dataLength min CodedOutputStream.DEFAULT_BUFFER_SIZE
}

