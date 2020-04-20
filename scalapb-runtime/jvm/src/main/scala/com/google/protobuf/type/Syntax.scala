// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.`type`

/** The syntax in which a protocol buffer element is defined.
  */
sealed abstract class Syntax(val value: _root_.scala.Int) extends _root_.scalapb.GeneratedEnum {
  type EnumType = Syntax
  override type EnumRecognizedType = com.google.protobuf.`type`.Syntax.Recognized
  def isSyntaxProto2: _root_.scala.Boolean = false
  def isSyntaxProto3: _root_.scala.Boolean = false
  def companion: _root_.scalapb.GeneratedEnumCompanion[Syntax] = com.google.protobuf.`type`.Syntax
  final override def asRecognized: _root_.scala.Option[com.google.protobuf.`type`.Syntax.Recognized] = if (isUnrecognized) _root_.scala.None else _root_.scala.Some(this.asInstanceOf[com.google.protobuf.`type`.Syntax.Recognized])
}

object Syntax extends _root_.scalapb.GeneratedEnumCompanion[Syntax] {
  sealed trait Recognized extends Syntax
  override type ValueRecognizedType = Recognized
  implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[Syntax] = this
  /** Syntax `proto2`.
    */
  @SerialVersionUID(0L)
  case object SYNTAX_PROTO2 extends Syntax(0) with Syntax.Recognized {
    val index = 0
    val name = "SYNTAX_PROTO2"
    override def isSyntaxProto2: _root_.scala.Boolean = true
  }
  
  /** Syntax `proto3`.
    */
  @SerialVersionUID(0L)
  case object SYNTAX_PROTO3 extends Syntax(1) with Syntax.Recognized {
    val index = 1
    val name = "SYNTAX_PROTO3"
    override def isSyntaxProto3: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  final case class Unrecognized(unrecognizedValue: _root_.scala.Int) extends Syntax(unrecognizedValue) with _root_.scalapb.UnrecognizedEnum
  
  lazy val values = scala.collection.immutable.Seq(SYNTAX_PROTO2, SYNTAX_PROTO3)
  def fromValue(__value: _root_.scala.Int): Syntax = __value match {
    case 0 => SYNTAX_PROTO2
    case 1 => SYNTAX_PROTO3
    case __other => Unrecognized(__other)
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = TypeProto.javaDescriptor.getEnumTypes.get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = TypeProto.scalaDescriptor.enums(0)
  def fromJavaValue(pbJavaSource: com.google.protobuf.Syntax): Syntax = fromValue(pbJavaSource.getNumber)
  def toJavaValue(pbScalaSource: Syntax): com.google.protobuf.Syntax = {
    _root_.scala.Predef.require(!pbScalaSource.isUnrecognized, "Unrecognized enum values can not be converted to Java")
    com.google.protobuf.Syntax.forNumber(pbScalaSource.value)
  }
}