package scalapb

import com.google.protobuf.{ByteString, InvalidProtocolBufferException}

/** Wire inspection used by generated closed-enum map parsers before applying type mappers. */
object ClosedEnumMapEntry {
  def isUnknown[A <: GeneratedEnum](
      bytes: ByteString,
      companion: GeneratedEnumCompanion[A]
  ): Boolean = {
    val input = bytes.newCodedInput()
    var value = 0 // Map enum values always have a zero-valued default.
    while (!input.isAtEnd) {
      val tag = input.readTag()
      if (tag == 16) value = input.readEnum()
      else if (!input.skipField(tag))
        throw new InvalidProtocolBufferException("Unexpected end-group in map entry.")
    }
    companion.fromValue(value).isUnrecognized
  }
}
