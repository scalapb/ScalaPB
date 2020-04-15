// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.struct

/** `NullValue` is a singleton enumeration to represent the null value for the
  * `Value` type union.
  *
  *  The JSON representation for `NullValue` is JSON `null`.
  */
sealed abstract class NullValue(val value: _root_.scala.Int) extends _root_.scalapb.GeneratedEnum {
  type EnumType = NullValue
  override type EnumRecognizedType = com.google.protobuf.struct.NullValue.Recognized
  def isNullValue: _root_.scala.Boolean = false
  def companion: _root_.scalapb.GeneratedEnumCompanion[NullValue] = com.google.protobuf.struct.NullValue
  final override def asRecognized: _root_.scala.Option[com.google.protobuf.struct.NullValue.Recognized] = if (isUnrecognized) _root_.scala.None else _root_.scala.Some(this.asInstanceOf[com.google.protobuf.struct.NullValue.Recognized])
}

object NullValue extends _root_.scalapb.GeneratedEnumCompanion[NullValue] {
  sealed abstract class Recognized(override val value: _root_.scala.Int) extends NullValue(value)
  override type ValueRecognizedType = Recognized
  implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[NullValue] = this
  /** Null value.
    */
  @SerialVersionUID(0L)
  case object NULL_VALUE extends NullValue.Recognized(0) {
    val index = 0
    val name = "NULL_VALUE"
    override def isNullValue: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  final case class Unrecognized(unrecognizedValue: _root_.scala.Int) extends NullValue(unrecognizedValue) with _root_.scalapb.UnrecognizedEnum
  
  lazy val values = scala.collection.immutable.Seq(NULL_VALUE)
  def fromValue(__value: _root_.scala.Int): NullValue = __value match {
    case 0 => NULL_VALUE
    case __other => Unrecognized(__other)
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = StructProto.javaDescriptor.getEnumTypes.get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = StructProto.scalaDescriptor.enums(0)
}