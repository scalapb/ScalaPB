// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.wrappers

import scala.collection.JavaConverters._

/** Wrapper message for `string`.
  *
  * The JSON representation for `StringValue` is JSON string.
  *
  * @param value
  *   The string value.
  */
@SerialVersionUID(0L)
final case class StringValue(
    value: String = ""
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[StringValue] with com.trueaccord.lenses.Updatable[StringValue] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (value != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, value) }
      __size
    }
    final override def serializedSize: Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
      {
        val __v = value
        if (__v != "") {
          _output__.writeString(1, __v)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.wrappers.StringValue = {
      var __value = this.value
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __value = _input__.readString()
          case tag => _input__.skipField(tag)
        }
      }
      com.google.protobuf.wrappers.StringValue(
          value = __value
      )
    }
    def withValue(__v: String): StringValue = copy(value = __v)
    def getField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = value
          if (__t != "") __t else null
        }
      }
    }
    override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.wrappers.StringValue
}

object StringValue extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.wrappers.StringValue] with com.trueaccord.scalapb.JavaProtoSupport[com.google.protobuf.wrappers.StringValue, com.google.protobuf.StringValue] with _root_.scala.Function1[String, com.google.protobuf.wrappers.StringValue] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.wrappers.StringValue] with com.trueaccord.scalapb.JavaProtoSupport[com.google.protobuf.wrappers.StringValue, com.google.protobuf.StringValue] with _root_.scala.Function1[String, com.google.protobuf.wrappers.StringValue] = this
  def toJavaProto(scalaPbSource: com.google.protobuf.wrappers.StringValue): com.google.protobuf.StringValue = {
    val javaPbOut = com.google.protobuf.StringValue.newBuilder
    javaPbOut.setValue(scalaPbSource.value)
    javaPbOut.build
  }
  def fromJavaProto(javaPbSource: com.google.protobuf.StringValue): com.google.protobuf.wrappers.StringValue = com.google.protobuf.wrappers.StringValue(
    value = javaPbSource.getValue
  )
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.wrappers.StringValue = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.google.protobuf.wrappers.StringValue(
      __fieldsMap.getOrElse(__fields.get(0), "").asInstanceOf[String]
    )
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WrappersProto.javaDescriptor.getMessageTypes.get(7)
  def messageCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__field)
  def enumCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__field)
  lazy val defaultInstance = com.google.protobuf.wrappers.StringValue(
  )
  implicit class StringValueLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.wrappers.StringValue]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.wrappers.StringValue](_l) {
    def value: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.value)((c_, f_) => c_.copy(value = f_))
  }
  final val VALUE_FIELD_NUMBER = 1
}
