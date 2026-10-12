package scalapb

import java.io.InputStream

import com.google.protobuf.{CodedOutputStream, CodedInputStream, InvalidProtocolBufferException}

object LiteParser {
  // Compatibility path for companions generated before group-aware parsing was added.
  // Preserve field order: reconstructing through UnknownFieldSet could change oneof semantics.
  private[scalapb] def readGroupBody(input: CodedInputStream, expectedEndTag: Int): Array[Byte] = {
    val bytes                       = new java.io.ByteArrayOutputStream
    val output                      = CodedOutputStream.newInstance(bytes)
    def copyBody(endTag: Int): Unit = {
      var tag = input.readTag()
      while (tag != 0 && WireType.getTagWireType(tag) != WireType.WIRETYPE_END_GROUP) {
        val number = WireType.getTagFieldNumber(tag)
        WireType.getTagWireType(tag) match {
          case WireType.WIRETYPE_VARINT  => output.writeUInt64(number, input.readUInt64())
          case WireType.WIRETYPE_FIXED64 => output.writeFixed64(number, input.readFixed64())
          case WireType.WIRETYPE_LENGTH_DELIMITED => output.writeBytes(number, input.readBytes())
          case WireType.WIRETYPE_FIXED32     => output.writeFixed32(number, input.readFixed32())
          case WireType.WIRETYPE_START_GROUP =>
            output.writeTag(number, WireType.WIRETYPE_START_GROUP)
            copyBody((number << 3) | WireType.WIRETYPE_END_GROUP)
            output.writeTag(number, WireType.WIRETYPE_END_GROUP)
          case wireType =>
            throw new com.google.protobuf.InvalidProtocolBufferException(
              s"Protocol message tag had invalid wire type: $wireType"
            )
        }
        tag = input.readTag()
      }
      input.checkLastTagWas(endTag)
    }
    copyBody(expectedEndTag)
    output.flush()
    bytes.toByteArray
  }

  def readGroup[A <: GeneratedMessage](input: CodedInputStream, fieldNumber: Int)(implicit
      cmp: GeneratedMessageCompanion[A]
  ): A = cmp.parseFrom(input, (fieldNumber << 3) | WireType.WIRETYPE_END_GROUP)

  def readGroup[A <: GeneratedMessage](input: CodedInputStream, fieldNumber: Int, message: A)(
      implicit cmp: GeneratedMessageCompanion[A]
  ): A = {
    val parsed = readGroup[A](input, fieldNumber)
    cmp.merge(message, parsed.toByteString.newCodedInput())
  }

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
