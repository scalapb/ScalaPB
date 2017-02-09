// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.`type`

import scala.collection.JavaConverters._

/** A protocol buffer option, which can be attached to a message, field,
  * enumeration, etc.
  *
  * @param name
  *   The option's name. For example, `"java_package"`.
  * @param value
  *   The option's value. For example, `"com.google.protobuf"`.
  */
@SerialVersionUID(0L)
final case class OptionProto(
    name: String = "",
    value: scala.Option[com.google.protobuf.any.Any] = None
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[OptionProto] with com.trueaccord.lenses.Updatable[OptionProto] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (name != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, name) }
      if (value.isDefined) { __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(value.get.serializedSize) + value.get.serializedSize }
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
        val __v = name
        if (__v != "") {
          _output__.writeString(1, __v)
        }
      };
      value.foreach { __v =>
        _output__.writeTag(2, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.`type`.OptionProto = {
      var __name = this.name
      var __value = this.value
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __name = _input__.readString()
          case 18 =>
            __value = Some(_root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, __value.getOrElse(com.google.protobuf.any.Any.defaultInstance)))
          case tag => _input__.skipField(tag)
        }
      }
      com.google.protobuf.`type`.OptionProto(
          name = __name,
          value = __value
      )
    }
    def withName(__v: String): OptionProto = copy(name = __v)
    def getValue: com.google.protobuf.any.Any = value.getOrElse(com.google.protobuf.any.Any.defaultInstance)
    def clearValue: OptionProto = copy(value = None)
    def withValue(__v: com.google.protobuf.any.Any): OptionProto = copy(value = Some(__v))
    def getField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = name
          if (__t != "") __t else null
        }
        case 2 => value.orNull
      }
    }
    override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.`type`.OptionProto
}

object OptionProto extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.`type`.OptionProto] with com.trueaccord.scalapb.JavaProtoSupport[com.google.protobuf.`type`.OptionProto, com.google.protobuf.Option] with _root_.scala.Function2[String, scala.Option[com.google.protobuf.any.Any], com.google.protobuf.`type`.OptionProto] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.`type`.OptionProto] with com.trueaccord.scalapb.JavaProtoSupport[com.google.protobuf.`type`.OptionProto, com.google.protobuf.Option] with _root_.scala.Function2[String, scala.Option[com.google.protobuf.any.Any], com.google.protobuf.`type`.OptionProto] = this
  def toJavaProto(scalaPbSource: com.google.protobuf.`type`.OptionProto): com.google.protobuf.Option = {
    val javaPbOut = com.google.protobuf.Option.newBuilder
    javaPbOut.setName(scalaPbSource.name)
    scalaPbSource.value.map(com.google.protobuf.any.Any.toJavaProto(_)).foreach(javaPbOut.setValue)
    javaPbOut.build
  }
  def fromJavaProto(javaPbSource: com.google.protobuf.Option): com.google.protobuf.`type`.OptionProto = com.google.protobuf.`type`.OptionProto(
    name = javaPbSource.getName,
    value = if (javaPbSource.hasValue) Some(com.google.protobuf.any.Any.fromJavaProto(javaPbSource.getValue)) else None
  )
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.`type`.OptionProto = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.google.protobuf.`type`.OptionProto(
      __fieldsMap.getOrElse(__fields.get(0), "").asInstanceOf[String],
      __fieldsMap.get(__fields.get(1)).asInstanceOf[scala.Option[com.google.protobuf.any.Any]]
    )
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = TypeProto.javaDescriptor.getMessageTypes.get(4)
  def messageCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = {
    require(__field.getContainingType() == javaDescriptor, "FieldDescriptor does not match message type.")
    var __out: _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = null
    __field.getNumber match {
      case 2 => __out = com.google.protobuf.any.Any
    }
  __out
  }
  def enumCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__field)
  lazy val defaultInstance = com.google.protobuf.`type`.OptionProto(
  )
  implicit class OptionProtoLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.`type`.OptionProto]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.`type`.OptionProto](_l) {
    def name: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.name)((c_, f_) => c_.copy(name = f_))
    def value: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.any.Any] = field(_.getValue)((c_, f_) => c_.copy(value = Some(f_)))
    def optionalValue: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[com.google.protobuf.any.Any]] = field(_.value)((c_, f_) => c_.copy(value = f_))
  }
  final val NAME_FIELD_NUMBER = 1
  final val VALUE_FIELD_NUMBER = 2
}
