package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor.Type

object Types {
  val WIRETYPE_VARINT = 0
  val WIRETYPE_FIXED64 = 1
  val WIRETYPE_LENGTH_DELIMITED = 2
  val WIRETYPE_START_GROUP = 3
  val WIRETYPE_END_GROUP = 4
  val WIRETYPE_FIXED32 = 5

  case class TypeInfo(capitalizedType: String, fixedSize: Option[Int], wireType: Int)

  private val TYPES = Map(
    Type.BOOL -> TypeInfo("Bool", Some(1), WIRETYPE_VARINT),
    Type.BYTES -> TypeInfo("Bytes", None, WIRETYPE_LENGTH_DELIMITED),
    Type.DOUBLE -> TypeInfo("Double", Some(8), WIRETYPE_FIXED64),
    Type.ENUM -> TypeInfo("Enum", None, WIRETYPE_VARINT),
    Type.FIXED32 -> TypeInfo("Fixed32", Some(4), WIRETYPE_FIXED32),
    Type.FIXED64 -> TypeInfo("Fixed64", Some(8), WIRETYPE_FIXED64),
    Type.FLOAT -> TypeInfo("Float", Some(4), WIRETYPE_FIXED32),
    Type.GROUP -> TypeInfo("Group", None, WIRETYPE_START_GROUP),
    Type.INT32 -> TypeInfo("Int32", None, WIRETYPE_VARINT),
    Type.INT64 -> TypeInfo("Int64", None, WIRETYPE_VARINT),
    Type.MESSAGE -> TypeInfo("Message", None, WIRETYPE_LENGTH_DELIMITED),
    Type.SFIXED32 -> TypeInfo("SFixed32", Some(4), WIRETYPE_FIXED32),
    Type.SFIXED64 -> TypeInfo("SFixed64", Some(8), WIRETYPE_FIXED64),
    Type.SINT32 -> TypeInfo("SInt32", None, WIRETYPE_VARINT),
    Type.SINT64 -> TypeInfo("SInt64", None, WIRETYPE_VARINT),
    Type.STRING -> TypeInfo("String", None, WIRETYPE_LENGTH_DELIMITED),
    Type.UINT32 -> TypeInfo("UInt32", None, WIRETYPE_VARINT),
    Type.UINT64 -> TypeInfo("UInt64", None, WIRETYPE_VARINT)
  )

  def capitalizedType(t: Type) = TYPES(t).capitalizedType

  def fixedSize(t: Type) = TYPES(t).fixedSize

  def wireType(t: Type) = TYPES(t).wireType
}
