package scalapb

import java.io.InputStream

import com.google.protobuf.{CodedOutputStream, CodedInputStream}

object LiteParser {
  def readMessage[A <: GeneratedMessage](input: CodedInputStream, message: A)(implicit
      cmp: GeneratedMessageCompanion[A]
  ): A = {
    val length    = input.readRawVarint32()
    val oldLimit  = input.pushLimit(length)
    val result: A = cmp.merge(message, input)
    input.checkLastTagWas(0)
    input.popLimit(oldLimit)
    result
  }

  def readMessage[A <: GeneratedMessage](input: CodedInputStream)(
      implicit cmp: GeneratedMessageCompanion[A]
  ): A = {
    val length    = input.readRawVarint32()
    val oldLimit  = input.pushLimit(length)
    val result: A = cmp.parseFrom(input)
    input.checkLastTagWas(0)
    input.popLimit(oldLimit)
    result
  }

  def parseDelimitedFrom[A <: GeneratedMessage](
      input: InputStream
  )(implicit cmp: GeneratedMessageCompanion[A]): Option[A] = {
    val b = input.read()
    if (b < 0) None
    else {
      val size = CodedInputStream.readRawVarint32(b, input)
      Some(cmp.parseFrom(CodedInputStream.newInstance(new LimitedInputStream(input, size))))
    }
  }

  def parseDelimitedFrom[A <: GeneratedMessage](
      input: CodedInputStream
  )(implicit cmp: GeneratedMessageCompanion[A]): Option[A] = {
    if (input.isAtEnd) None
    else Some(readMessage(input))
  }

  @inline
  def preferredCodedOutputStreamBufferSize(dataLength: Int) =
    dataLength min CodedOutputStream.DEFAULT_BUFFER_SIZE
}
