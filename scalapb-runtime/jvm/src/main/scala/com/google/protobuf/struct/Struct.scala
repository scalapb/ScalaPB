// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.struct
import scala.collection.JavaConverters._

/** `Struct` represents a structured data value, consisting of fields
  * which map to dynamically typed values. In some languages, `Struct`
  * might be supported by a native representation. For example, in
  * scripting languages like JS a struct is represented as an
  * object. The details of that representation are described together
  * with the proto support for the language.
  *
  * The JSON representation for `Struct` is JSON object.
  *
  * @param fields
  *   Unordered map of dynamically typed values.
  */
@SerialVersionUID(0L)
final case class Struct(
    fields: scala.collection.immutable.Map[_root_.scala.Predef.String, com.google.protobuf.struct.Value] = scala.collection.immutable.Map.empty
    ) extends scalapb.GeneratedMessage with scalapb.Message[Struct] with scalapb.lenses.Updatable[Struct] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      fields.foreach { __item =>
        val __value = com.google.protobuf.struct.Struct._typemapper_fields.toBase(__item)
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      __size
    }
    final override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
      fields.foreach { __v =>
        val __m = com.google.protobuf.struct.Struct._typemapper_fields.toBase(__v)
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.struct.Struct = {
      val __fields = (scala.collection.immutable.Map.newBuilder[_root_.scala.Predef.String, com.google.protobuf.struct.Value] ++= this.fields)
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __fields += com.google.protobuf.struct.Struct._typemapper_fields.toCustom(_root_.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.struct.Struct.FieldsEntry.defaultInstance))
          case tag => _input__.skipField(tag)
        }
      }
      com.google.protobuf.struct.Struct(
          fields = __fields.result()
      )
    }
    def clearFields = copy(fields = scala.collection.immutable.Map.empty)
    def addFields(__vs: (_root_.scala.Predef.String, com.google.protobuf.struct.Value)*): Struct = addAllFields(__vs)
    def addAllFields(__vs: TraversableOnce[(_root_.scala.Predef.String, com.google.protobuf.struct.Value)]): Struct = copy(fields = fields ++ __vs)
    def withFields(__v: scala.collection.immutable.Map[_root_.scala.Predef.String, com.google.protobuf.struct.Value]): Struct = copy(fields = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => fields.map(com.google.protobuf.struct.Struct._typemapper_fields.toBase)(_root_.scala.collection.breakOut)
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(fields.map(com.google.protobuf.struct.Struct._typemapper_fields.toBase(_).toPMessage)(_root_.scala.collection.breakOut))
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.struct.Struct
}

object Struct extends scalapb.GeneratedMessageCompanion[com.google.protobuf.struct.Struct] with scalapb.JavaProtoSupport[com.google.protobuf.struct.Struct, com.google.protobuf.Struct] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.google.protobuf.struct.Struct] with scalapb.JavaProtoSupport[com.google.protobuf.struct.Struct, com.google.protobuf.Struct] = this
  def toJavaProto(scalaPbSource: com.google.protobuf.struct.Struct): com.google.protobuf.Struct = {
    val javaPbOut = com.google.protobuf.Struct.newBuilder
    javaPbOut
  .getMutableFields()
  .putAll(
    scalaPbSource.fields.map {
      __kv => (__kv._1, com.google.protobuf.struct.Value.toJavaProto(__kv._2))
  }.asJava)

    javaPbOut.build
  }
  def fromJavaProto(javaPbSource: com.google.protobuf.Struct): com.google.protobuf.struct.Struct = com.google.protobuf.struct.Struct(
    fields = javaPbSource.getFieldsMap.asScala.map(__pv => (__pv._1, com.google.protobuf.struct.Value.fromJavaProto(__pv._2))).toMap
  )
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.struct.Struct = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.google.protobuf.struct.Struct(
      __fieldsMap.getOrElse(__fields.get(0), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.struct.Struct.FieldsEntry]].map(com.google.protobuf.struct.Struct._typemapper_fields.toCustom)(_root_.scala.collection.breakOut)
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.struct.Struct] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.google.protobuf.struct.Struct(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.struct.Struct.FieldsEntry]]).getOrElse(_root_.scala.collection.Seq.empty).map(com.google.protobuf.struct.Struct._typemapper_fields.toCustom)(_root_.scala.collection.breakOut)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = StructProto.javaDescriptor.getMessageTypes.get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = StructProto.scalaDescriptor.messages(0)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = com.google.protobuf.struct.Struct.FieldsEntry
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq[_root_.scalapb.GeneratedMessageCompanion[_]](
    _root_.com.google.protobuf.struct.Struct.FieldsEntry
  )
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.google.protobuf.struct.Struct(
  )
  @SerialVersionUID(0L)
  final case class FieldsEntry(
      key: _root_.scala.Predef.String = "",
      value: scala.Option[com.google.protobuf.struct.Value] = None
      ) extends scalapb.GeneratedMessage with scalapb.Message[FieldsEntry] with scalapb.lenses.Updatable[FieldsEntry] {
      @transient
      private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
      private[this] def __computeSerializedValue(): _root_.scala.Int = {
        var __size = 0
        if (key != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, key) }
        if (value.isDefined) { __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(value.get.serializedSize) + value.get.serializedSize }
        __size
      }
      final override def serializedSize: _root_.scala.Int = {
        var read = __serializedSizeCachedValue
        if (read == 0) {
          read = __computeSerializedValue()
          __serializedSizeCachedValue = read
        }
        read
      }
      def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
        {
          val __v = key
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
      def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.struct.Struct.FieldsEntry = {
        var __key = this.key
        var __value = this.value
        var _done__ = false
        while (!_done__) {
          val _tag__ = _input__.readTag()
          _tag__ match {
            case 0 => _done__ = true
            case 10 =>
              __key = _input__.readString()
            case 18 =>
              __value = Option(_root_.scalapb.LiteParser.readMessage(_input__, __value.getOrElse(com.google.protobuf.struct.Value.defaultInstance)))
            case tag => _input__.skipField(tag)
          }
        }
        com.google.protobuf.struct.Struct.FieldsEntry(
            key = __key,
            value = __value
        )
      }
      def withKey(__v: _root_.scala.Predef.String): FieldsEntry = copy(key = __v)
      def getValue: com.google.protobuf.struct.Value = value.getOrElse(com.google.protobuf.struct.Value.defaultInstance)
      def clearValue: FieldsEntry = copy(value = None)
      def withValue(__v: com.google.protobuf.struct.Value): FieldsEntry = copy(value = Option(__v))
      def getFieldByNumber(__fieldNumber: _root_.scala.Int): scala.Any = {
        (__fieldNumber: @_root_.scala.unchecked) match {
          case 1 => {
            val __t = key
            if (__t != "") __t else null
          }
          case 2 => value.orNull
        }
      }
      def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
        require(__field.containingMessage eq companion.scalaDescriptor)
        (__field.number: @_root_.scala.unchecked) match {
          case 1 => _root_.scalapb.descriptors.PString(key)
          case 2 => value.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
        }
      }
      def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
      def companion = com.google.protobuf.struct.Struct.FieldsEntry
  }
  
  object FieldsEntry extends scalapb.GeneratedMessageCompanion[com.google.protobuf.struct.Struct.FieldsEntry] {
    implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.google.protobuf.struct.Struct.FieldsEntry] = this
    def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.struct.Struct.FieldsEntry = {
      require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
      val __fields = javaDescriptor.getFields
      com.google.protobuf.struct.Struct.FieldsEntry(
        __fieldsMap.getOrElse(__fields.get(0), "").asInstanceOf[_root_.scala.Predef.String],
        __fieldsMap.get(__fields.get(1)).asInstanceOf[scala.Option[com.google.protobuf.struct.Value]]
      )
    }
    implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.struct.Struct.FieldsEntry] = _root_.scalapb.descriptors.Reads{
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
        com.google.protobuf.struct.Struct.FieldsEntry(
          __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
          __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[scala.Option[com.google.protobuf.struct.Value]])
        )
      case _ => throw new RuntimeException("Expected PMessage")
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = com.google.protobuf.struct.Struct.javaDescriptor.getNestedTypes.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = com.google.protobuf.struct.Struct.scalaDescriptor.nestedMessages(0)
    def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
      var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
      (__number: @_root_.scala.unchecked) match {
        case 2 => __out = com.google.protobuf.struct.Value
      }
      __out
    }
    lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
    def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
    lazy val defaultInstance = com.google.protobuf.struct.Struct.FieldsEntry(
    )
    implicit class FieldsEntryLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.struct.Struct.FieldsEntry]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.google.protobuf.struct.Struct.FieldsEntry](_l) {
      def key: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.key)((c_, f_) => c_.copy(key = f_))
      def value: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.struct.Value] = field(_.getValue)((c_, f_) => c_.copy(value = Option(f_)))
      def optionalValue: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[com.google.protobuf.struct.Value]] = field(_.value)((c_, f_) => c_.copy(value = f_))
    }
    final val KEY_FIELD_NUMBER = 1
    final val VALUE_FIELD_NUMBER = 2
    implicit val keyValueMapper: _root_.scalapb.TypeMapper[com.google.protobuf.struct.Struct.FieldsEntry, (_root_.scala.Predef.String, com.google.protobuf.struct.Value)] =
      _root_.scalapb.TypeMapper[com.google.protobuf.struct.Struct.FieldsEntry, (_root_.scala.Predef.String, com.google.protobuf.struct.Value)](__m => (__m.key, __m.getValue))(__p => com.google.protobuf.struct.Struct.FieldsEntry(__p._1, Some(__p._2)))
  }
  
  implicit class StructLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.struct.Struct]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.google.protobuf.struct.Struct](_l) {
    def fields: _root_.scalapb.lenses.Lens[UpperPB, scala.collection.immutable.Map[_root_.scala.Predef.String, com.google.protobuf.struct.Value]] = field(_.fields)((c_, f_) => c_.copy(fields = f_))
  }
  final val FIELDS_FIELD_NUMBER = 1
  @transient
  private val _typemapper_fields: _root_.scalapb.TypeMapper[com.google.protobuf.struct.Struct.FieldsEntry, (_root_.scala.Predef.String, com.google.protobuf.struct.Value)] = implicitly[_root_.scalapb.TypeMapper[com.google.protobuf.struct.Struct.FieldsEntry, (_root_.scala.Predef.String, com.google.protobuf.struct.Value)]]
}
