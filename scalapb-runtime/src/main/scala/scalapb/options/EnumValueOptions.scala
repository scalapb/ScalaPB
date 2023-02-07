// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package scalapb.options

/** @param extends
  *   Additional classes and traits to mix in to an individual enum value.
  * @param scalaName
  *   Name in Scala to use for this enum value.
  * @param annotations
  *   Custom annotations to add to the generated case object for this enum value.
  */
@SerialVersionUID(0L)
final case class EnumValueOptions(
    `extends`: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    scalaName: _root_.scala.Option[_root_.scala.Predef.String] = _root_.scala.None,
    annotations: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[EnumValueOptions] with _root_.scalapb.ExtendableMessage[EnumValueOptions] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      `extends`.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
      }
      if (scalaName.isDefined) {
        val __value = scalaName.get
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
      };
      annotations.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(3, __value)
      }
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
      `extends`.foreach { __v =>
        val __m = __v
        _output__.writeString(1, __m)
      };
      scalaName.foreach { __v =>
        val __m = __v
        _output__.writeString(2, __m)
      };
      annotations.foreach { __v =>
        val __m = __v
        _output__.writeString(3, __m)
      };
      unknownFields.writeTo(_output__)
    }
    def clearExtends = copy(`extends` = _root_.scala.Seq.empty)
    def addExtends(__vs: _root_.scala.Predef.String *): EnumValueOptions = addAllExtends(__vs)
    def addAllExtends(__vs: Iterable[_root_.scala.Predef.String]): EnumValueOptions = copy(`extends` = `extends` ++ __vs)
    def withExtends(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumValueOptions = copy(`extends` = __v)
    def getScalaName: _root_.scala.Predef.String = scalaName.getOrElse("")
    def clearScalaName: EnumValueOptions = copy(scalaName = _root_.scala.None)
    def withScalaName(__v: _root_.scala.Predef.String): EnumValueOptions = copy(scalaName = Option(__v))
    def clearAnnotations = copy(annotations = _root_.scala.Seq.empty)
    def addAnnotations(__vs: _root_.scala.Predef.String *): EnumValueOptions = addAllAnnotations(__vs)
    def addAllAnnotations(__vs: Iterable[_root_.scala.Predef.String]): EnumValueOptions = copy(annotations = annotations ++ __vs)
    def withAnnotations(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumValueOptions = copy(annotations = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => `extends`
        case 2 => scalaName.orNull
        case 3 => annotations
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(`extends`.iterator.map(_root_.scalapb.descriptors.PString).toVector)
        case 2 => scalaName.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 3 => _root_.scalapb.descriptors.PRepeated(annotations.iterator.map(_root_.scalapb.descriptors.PString).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion: scalapb.options.EnumValueOptions.type = scalapb.options.EnumValueOptions
    // @@protoc_insertion_point(GeneratedMessage[scalapb.EnumValueOptions])
}

object EnumValueOptions extends scalapb.GeneratedMessageCompanion[scalapb.options.EnumValueOptions] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[scalapb.options.EnumValueOptions] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): scalapb.options.EnumValueOptions = {
    val __extends: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    var __scalaName: _root_.scala.Option[_root_.scala.Predef.String] = _root_.scala.None
    val __annotations: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __extends += _input__.readStringRequireUtf8()
        case 18 =>
          __scalaName = Option(_input__.readStringRequireUtf8())
        case 26 =>
          __annotations += _input__.readStringRequireUtf8()
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    scalapb.options.EnumValueOptions(
        `extends` = __extends.result(),
        scalaName = __scalaName,
        annotations = __annotations.result(),
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[scalapb.options.EnumValueOptions] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      scalapb.options.EnumValueOptions(
        `extends` = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty),
        scalaName = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[_root_.scala.Option[_root_.scala.Predef.String]]),
        annotations = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ScalapbProto.javaDescriptor.getMessageTypes().get(5)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ScalapbProto.scalaDescriptor.messages(5)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = scalapb.options.EnumValueOptions(
    `extends` = _root_.scala.Seq.empty,
    scalaName = _root_.scala.None,
    annotations = _root_.scala.Seq.empty
  )
  implicit class EnumValueOptionsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, scalapb.options.EnumValueOptions]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, scalapb.options.EnumValueOptions](_l) {
    def `extends`: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.`extends`)((c_, f_) => c_.copy(`extends` = f_))
    def scalaName: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.getScalaName)((c_, f_) => c_.copy(scalaName = Option(f_)))
    def optionalScalaName: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[_root_.scala.Predef.String]] = field(_.scalaName)((c_, f_) => c_.copy(scalaName = f_))
    def annotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.annotations)((c_, f_) => c_.copy(annotations = f_))
  }
  final val EXTENDS_FIELD_NUMBER = 1
  final val SCALA_NAME_FIELD_NUMBER = 2
  final val ANNOTATIONS_FIELD_NUMBER = 3
  def of(
    `extends`: _root_.scala.Seq[_root_.scala.Predef.String],
    scalaName: _root_.scala.Option[_root_.scala.Predef.String],
    annotations: _root_.scala.Seq[_root_.scala.Predef.String]
  ): _root_.scalapb.options.EnumValueOptions = _root_.scalapb.options.EnumValueOptions(
    `extends`,
    scalaName,
    annotations
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[scalapb.EnumValueOptions])
}
