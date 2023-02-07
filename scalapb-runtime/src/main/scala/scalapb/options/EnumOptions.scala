// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package scalapb.options

/** @param extends
  *   Additional classes and traits to mix in to the base trait
  * @param companionExtends
  *   Additional classes and traits to mix in to the companion object.
  * @param type
  *   All instances of this enum will be converted to this type. An implicit TypeMapper
  *   must be present.
  * @param baseAnnotations
  *   Custom annotations to add to the generated enum's base class.
  * @param recognizedAnnotations
  *   Custom annotations to add to the generated trait.
  * @param unrecognizedAnnotations
  *   Custom annotations to add to the generated Unrecognized case class.
  */
@SerialVersionUID(0L)
final case class EnumOptions(
    `extends`: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    companionExtends: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    `type`: _root_.scala.Option[_root_.scala.Predef.String] = _root_.scala.None,
    baseAnnotations: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    recognizedAnnotations: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    unrecognizedAnnotations: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[EnumOptions] with _root_.scalapb.ExtendableMessage[EnumOptions] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      `extends`.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
      }
      companionExtends.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
      }
      if (`type`.isDefined) {
        val __value = `type`.get
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(3, __value)
      };
      baseAnnotations.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(4, __value)
      }
      recognizedAnnotations.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(5, __value)
      }
      unrecognizedAnnotations.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(6, __value)
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
      companionExtends.foreach { __v =>
        val __m = __v
        _output__.writeString(2, __m)
      };
      `type`.foreach { __v =>
        val __m = __v
        _output__.writeString(3, __m)
      };
      baseAnnotations.foreach { __v =>
        val __m = __v
        _output__.writeString(4, __m)
      };
      recognizedAnnotations.foreach { __v =>
        val __m = __v
        _output__.writeString(5, __m)
      };
      unrecognizedAnnotations.foreach { __v =>
        val __m = __v
        _output__.writeString(6, __m)
      };
      unknownFields.writeTo(_output__)
    }
    def clearExtends = copy(`extends` = _root_.scala.Seq.empty)
    def addExtends(__vs: _root_.scala.Predef.String *): EnumOptions = addAllExtends(__vs)
    def addAllExtends(__vs: Iterable[_root_.scala.Predef.String]): EnumOptions = copy(`extends` = `extends` ++ __vs)
    def withExtends(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumOptions = copy(`extends` = __v)
    def clearCompanionExtends = copy(companionExtends = _root_.scala.Seq.empty)
    def addCompanionExtends(__vs: _root_.scala.Predef.String *): EnumOptions = addAllCompanionExtends(__vs)
    def addAllCompanionExtends(__vs: Iterable[_root_.scala.Predef.String]): EnumOptions = copy(companionExtends = companionExtends ++ __vs)
    def withCompanionExtends(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumOptions = copy(companionExtends = __v)
    def getType: _root_.scala.Predef.String = `type`.getOrElse("")
    def clearType: EnumOptions = copy(`type` = _root_.scala.None)
    def withType(__v: _root_.scala.Predef.String): EnumOptions = copy(`type` = Option(__v))
    def clearBaseAnnotations = copy(baseAnnotations = _root_.scala.Seq.empty)
    def addBaseAnnotations(__vs: _root_.scala.Predef.String *): EnumOptions = addAllBaseAnnotations(__vs)
    def addAllBaseAnnotations(__vs: Iterable[_root_.scala.Predef.String]): EnumOptions = copy(baseAnnotations = baseAnnotations ++ __vs)
    def withBaseAnnotations(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumOptions = copy(baseAnnotations = __v)
    def clearRecognizedAnnotations = copy(recognizedAnnotations = _root_.scala.Seq.empty)
    def addRecognizedAnnotations(__vs: _root_.scala.Predef.String *): EnumOptions = addAllRecognizedAnnotations(__vs)
    def addAllRecognizedAnnotations(__vs: Iterable[_root_.scala.Predef.String]): EnumOptions = copy(recognizedAnnotations = recognizedAnnotations ++ __vs)
    def withRecognizedAnnotations(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumOptions = copy(recognizedAnnotations = __v)
    def clearUnrecognizedAnnotations = copy(unrecognizedAnnotations = _root_.scala.Seq.empty)
    def addUnrecognizedAnnotations(__vs: _root_.scala.Predef.String *): EnumOptions = addAllUnrecognizedAnnotations(__vs)
    def addAllUnrecognizedAnnotations(__vs: Iterable[_root_.scala.Predef.String]): EnumOptions = copy(unrecognizedAnnotations = unrecognizedAnnotations ++ __vs)
    def withUnrecognizedAnnotations(__v: _root_.scala.Seq[_root_.scala.Predef.String]): EnumOptions = copy(unrecognizedAnnotations = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => `extends`
        case 2 => companionExtends
        case 3 => `type`.orNull
        case 4 => baseAnnotations
        case 5 => recognizedAnnotations
        case 6 => unrecognizedAnnotations
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(`extends`.iterator.map(_root_.scalapb.descriptors.PString).toVector)
        case 2 => _root_.scalapb.descriptors.PRepeated(companionExtends.iterator.map(_root_.scalapb.descriptors.PString).toVector)
        case 3 => `type`.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 4 => _root_.scalapb.descriptors.PRepeated(baseAnnotations.iterator.map(_root_.scalapb.descriptors.PString).toVector)
        case 5 => _root_.scalapb.descriptors.PRepeated(recognizedAnnotations.iterator.map(_root_.scalapb.descriptors.PString).toVector)
        case 6 => _root_.scalapb.descriptors.PRepeated(unrecognizedAnnotations.iterator.map(_root_.scalapb.descriptors.PString).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion: scalapb.options.EnumOptions.type = scalapb.options.EnumOptions
    // @@protoc_insertion_point(GeneratedMessage[scalapb.EnumOptions])
}

object EnumOptions extends scalapb.GeneratedMessageCompanion[scalapb.options.EnumOptions] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[scalapb.options.EnumOptions] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): scalapb.options.EnumOptions = {
    val __extends: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    val __companionExtends: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    var __type: _root_.scala.Option[_root_.scala.Predef.String] = _root_.scala.None
    val __baseAnnotations: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    val __recognizedAnnotations: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    val __unrecognizedAnnotations: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __extends += _input__.readStringRequireUtf8()
        case 18 =>
          __companionExtends += _input__.readStringRequireUtf8()
        case 26 =>
          __type = Option(_input__.readStringRequireUtf8())
        case 34 =>
          __baseAnnotations += _input__.readStringRequireUtf8()
        case 42 =>
          __recognizedAnnotations += _input__.readStringRequireUtf8()
        case 50 =>
          __unrecognizedAnnotations += _input__.readStringRequireUtf8()
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    scalapb.options.EnumOptions(
        `extends` = __extends.result(),
        companionExtends = __companionExtends.result(),
        `type` = __type,
        baseAnnotations = __baseAnnotations.result(),
        recognizedAnnotations = __recognizedAnnotations.result(),
        unrecognizedAnnotations = __unrecognizedAnnotations.result(),
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[scalapb.options.EnumOptions] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      scalapb.options.EnumOptions(
        `extends` = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty),
        companionExtends = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty),
        `type` = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).flatMap(_.as[_root_.scala.Option[_root_.scala.Predef.String]]),
        baseAnnotations = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty),
        recognizedAnnotations = __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty),
        unrecognizedAnnotations = __fieldsMap.get(scalaDescriptor.findFieldByNumber(6).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ScalapbProto.javaDescriptor.getMessageTypes().get(4)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ScalapbProto.scalaDescriptor.messages(4)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = scalapb.options.EnumOptions(
    `extends` = _root_.scala.Seq.empty,
    companionExtends = _root_.scala.Seq.empty,
    `type` = _root_.scala.None,
    baseAnnotations = _root_.scala.Seq.empty,
    recognizedAnnotations = _root_.scala.Seq.empty,
    unrecognizedAnnotations = _root_.scala.Seq.empty
  )
  implicit class EnumOptionsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, scalapb.options.EnumOptions]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, scalapb.options.EnumOptions](_l) {
    def `extends`: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.`extends`)((c_, f_) => c_.copy(`extends` = f_))
    def companionExtends: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.companionExtends)((c_, f_) => c_.copy(companionExtends = f_))
    def `type`: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.getType)((c_, f_) => c_.copy(`type` = Option(f_)))
    def optionalType: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[_root_.scala.Predef.String]] = field(_.`type`)((c_, f_) => c_.copy(`type` = f_))
    def baseAnnotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.baseAnnotations)((c_, f_) => c_.copy(baseAnnotations = f_))
    def recognizedAnnotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.recognizedAnnotations)((c_, f_) => c_.copy(recognizedAnnotations = f_))
    def unrecognizedAnnotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.unrecognizedAnnotations)((c_, f_) => c_.copy(unrecognizedAnnotations = f_))
  }
  final val EXTENDS_FIELD_NUMBER = 1
  final val COMPANION_EXTENDS_FIELD_NUMBER = 2
  final val TYPE_FIELD_NUMBER = 3
  final val BASE_ANNOTATIONS_FIELD_NUMBER = 4
  final val RECOGNIZED_ANNOTATIONS_FIELD_NUMBER = 5
  final val UNRECOGNIZED_ANNOTATIONS_FIELD_NUMBER = 6
  def of(
    `extends`: _root_.scala.Seq[_root_.scala.Predef.String],
    companionExtends: _root_.scala.Seq[_root_.scala.Predef.String],
    `type`: _root_.scala.Option[_root_.scala.Predef.String],
    baseAnnotations: _root_.scala.Seq[_root_.scala.Predef.String],
    recognizedAnnotations: _root_.scala.Seq[_root_.scala.Predef.String],
    unrecognizedAnnotations: _root_.scala.Seq[_root_.scala.Predef.String]
  ): _root_.scalapb.options.EnumOptions = _root_.scalapb.options.EnumOptions(
    `extends`,
    companionExtends,
    `type`,
    baseAnnotations,
    recognizedAnnotations,
    unrecognizedAnnotations
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[scalapb.EnumOptions])
}
