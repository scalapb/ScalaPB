// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package scalapb.options

sealed abstract class MatchType(val value: _root_.scala.Int) extends _root_.scalapb.GeneratedEnum {
  type EnumType = MatchType
  def isContains: _root_.scala.Boolean = false
  def isExact: _root_.scala.Boolean = false
  def isPresence: _root_.scala.Boolean = false
  def companion: _root_.scalapb.GeneratedEnumCompanion[MatchType] = scalapb.options.MatchType
  final def asRecognized: _root_.scala.Option[scalapb.options.MatchType.Recognized] = if (isUnrecognized) _root_.scala.None else _root_.scala.Some(this.asInstanceOf[scalapb.options.MatchType.Recognized])
}

object MatchType extends _root_.scalapb.GeneratedEnumCompanion[MatchType] {
  sealed trait Recognized extends MatchType
  implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[MatchType] = this
  
  @SerialVersionUID(0L)
  case object CONTAINS extends MatchType(0) with MatchType.Recognized {
    val index = 0
    val name = "CONTAINS"
    override def isContains: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  case object EXACT extends MatchType(1) with MatchType.Recognized {
    val index = 1
    val name = "EXACT"
    override def isExact: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  case object PRESENCE extends MatchType(2) with MatchType.Recognized {
    val index = 2
    val name = "PRESENCE"
    override def isPresence: _root_.scala.Boolean = true
  }
  
  @SerialVersionUID(0L)
  final case class Unrecognized private[MatchType](unrecognizedValue: _root_.scala.Int) extends MatchType(unrecognizedValue) with _root_.scalapb.UnrecognizedEnum
  object Unrecognized {
    @deprecated("Could have lead to issues before. Use MatchType.fromValue instead. This might be private in the future.")
    def apply(unrecognizedValue: _root_.scala.Int): MatchType = fromValue(unrecognizedValue) 
  }
  lazy val values = scala.collection.immutable.Seq(CONTAINS, EXACT, PRESENCE)
  def fromValue(__value: _root_.scala.Int): MatchType = __value match {
    case 0 => CONTAINS
    case 1 => EXACT
    case 2 => PRESENCE
    case __other => new Unrecognized(__other)
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = ScalapbProto.javaDescriptor.getEnumTypes().get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = ScalapbProto.scalaDescriptor.enums(0)
}