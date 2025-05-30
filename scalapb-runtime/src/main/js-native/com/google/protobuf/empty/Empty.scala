// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!

package com.google.protobuf.empty

/** A generic empty message that you can re-use to avoid defining duplicated
  * empty messages in your APIs. A typical example is to use it as the request
  * or the response type of an API method. For instance:
  *
  *     service Foo {
  *       rpc Bar(google.protobuf.Empty) returns (google.protobuf.Empty);
  *     }
  */
@SerialVersionUID(0L)
final case class Empty(
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[Empty] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      __size += unknownFields.serializedSize
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var __size = __serializedSizeMemoized
      if (__size == 0) {
        __size = __computeSerializedSize() + 1
        __serializedSizeMemoized = __size
      }
      __size - 1
      
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      unknownFields.writeTo(_output__)
    }
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = throw new MatchError(__fieldNumber)
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = throw new MatchError(__field)
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion: com.google.protobuf.empty.Empty.type = com.google.protobuf.empty.Empty
    // @@protoc_insertion_point(GeneratedMessage[google.protobuf.Empty])
}

object Empty extends scalapb.GeneratedMessageCompanion[com.google.protobuf.empty.Empty] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.google.protobuf.empty.Empty] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.empty.Empty = {
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          val _ = _unknownFields__.parseField(tag, _input__)
      }
    }
    com.google.protobuf.empty.Empty(
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.empty.Empty] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      com.google.protobuf.empty.Empty(
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = com.google.protobuf.empty.EmptyProto.javaDescriptor.getMessageTypes().get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = com.google.protobuf.empty.EmptyProto.scalaDescriptor.messages(0)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.google.protobuf.empty.Empty(
  )
  implicit class EmptyLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.empty.Empty]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.google.protobuf.empty.Empty](_l) {
  }
  def of(
  ): _root_.com.google.protobuf.empty.Empty = _root_.com.google.protobuf.empty.Empty(
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[google.protobuf.Empty])
}
