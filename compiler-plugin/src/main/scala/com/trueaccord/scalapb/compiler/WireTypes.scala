package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor.Type

object WireType {
  val WIRETYPE_VARINT = 0
  val WIRETYPE_FIXED64 = 1
  val WIRETYPE_LENGTH_DELIMITED = 2
  val WIRETYPE_START_GROUP = 3
  val WIRETYPE_END_GROUP = 4
  val WIRETYPE_FIXED32 = 5

  def fromType(fieldType: Type) = fieldType match {
    case Type.DOUBLE => WIRETYPE_FIXED64
    case Type.FLOAT => WIRETYPE_FIXED32
    case Type.INT64 => WIRETYPE_VARINT
    case Type.UINT64 => WIRETYPE_VARINT
    case Type.INT32 => WIRETYPE_VARINT
    case Type.FIXED64 => WIRETYPE_FIXED64
    case Type.FIXED32 => WIRETYPE_FIXED32
    case Type.BOOL => WIRETYPE_VARINT
    case Type.STRING => WIRETYPE_LENGTH_DELIMITED
    case Type.GROUP => WIRETYPE_START_GROUP
    case Type.MESSAGE => WIRETYPE_LENGTH_DELIMITED
    case Type.BYTES => WIRETYPE_LENGTH_DELIMITED
    case Type.UINT32 => WIRETYPE_VARINT
    case Type.ENUM => WIRETYPE_VARINT
    case Type.SFIXED32 => WIRETYPE_FIXED32
    case Type.SFIXED64 => WIRETYPE_FIXED64
    case Type.SINT32 => WIRETYPE_VARINT
    case Type.SINT64 => WIRETYPE_VARINT
  }
}
