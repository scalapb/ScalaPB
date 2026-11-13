package scalapb

import java.io.InputStream

import com.google.protobuf.{CodedOutputStream, CodedInputStream, InvalidProtocolBufferException}

object LiteParser {
  def readMessage[A <: GeneratedMessage](input: CodedInputStream, message: A)(implicit
      cmp: GeneratedMessageCompanion[A]
  ): A = {
    val length = input.readRawVarint32()
    readDelimitedMessage(input, length)(cmp.merge(message, input))
  }

  def readMessage[A <: GeneratedMessage](input: CodedInputStream)(implicit
      cmp: GeneratedMessageCompanion[A]
  ): A = {
    val length = input.readRawVarint32()
    readDelimitedMessage(input, length)(cmp.parseFrom(input))
  }

  private def checkFrameSize(size: Int, maxFrameSize: Int): Unit = {
    if (size < 0)
      throw new InvalidProtocolBufferException("Encountered a negative frame size.")
    if (size > maxFrameSize)
      throw new InvalidProtocolBufferException("Frame size exceeds the configured maximum.")
  }

  private def readDelimitedMessage[A](input: CodedInputStream, size: Int)(parse: => A): A = {
    checkFrameSize(size, Int.MaxValue)
    val oldLimit = input.pushLimit(size)
    try {
      val result = parse
      input.checkLastTagWas(0)
      if (input.getBytesUntilLimit != 0)
        throw new InvalidProtocolBufferException(
          "Delimited message did not consume its entire frame."
        )
      result
    } finally input.popLimit(oldLimit)
  }

  def parseDelimitedFrom[A <: GeneratedMessage](
      input: InputStream
  )(implicit cmp: GeneratedMessageCompanion[A]): Option[A] =
    parseDelimitedFrom(input, Int.MaxValue)

  /** Parses one frame, rejecting lengths greater than maxFrameSize before reading its body. A clean
    * EOF before the length prefix returns None; incomplete frames throw.
    */
  def parseDelimitedFrom[A <: GeneratedMessage](
      input: InputStream,
      maxFrameSize: Int
  )(implicit cmp: GeneratedMessageCompanion[A]): Option[A] = {
    require(maxFrameSize >= 0, "maxFrameSize must be non-negative")
    val b = input.read()
    if (b < 0) None
    else {
      val size = CodedInputStream.readRawVarint32(b, input)
      checkFrameSize(size, maxFrameSize)
      val codedInput = CodedInputStream.newInstance(new LimitedInputStream(input, size))
      Some(readDelimitedMessage(codedInput, size)(cmp.parseFrom(codedInput)))
    }
  }

  def parseDelimitedFrom[A <: GeneratedMessage](
      input: CodedInputStream
  )(implicit cmp: GeneratedMessageCompanion[A]): Option[A] =
    parseDelimitedFrom(input, Int.MaxValue)

  /** Parses one frame with a per-frame byte limit, independent of the stream's size limit. On
    * failure the previous pushed limit is restored, but the input position is not rolled back.
    */
  def parseDelimitedFrom[A <: GeneratedMessage](
      input: CodedInputStream,
      maxFrameSize: Int
  )(implicit cmp: GeneratedMessageCompanion[A]): Option[A] = {
    require(maxFrameSize >= 0, "maxFrameSize must be non-negative")
    if (input.isAtEnd) None
    else {
      val size = input.readRawVarint32()
      checkFrameSize(size, maxFrameSize)
      Some(readDelimitedMessage(input, size)(cmp.parseFrom(input)))
    }
  }

  @inline
  def preferredCodedOutputStreamBufferSize(dataLength: Int) =
    dataLength min CodedOutputStream.DEFAULT_BUFFER_SIZE
}
