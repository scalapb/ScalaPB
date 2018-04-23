// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package scalapb.options

/** @param extends
  *   Additional classes and traits to mix in to the case class.
  * @param companionExtends
  *   Additional classes and traits to mix in to the companion object.
  * @param annotations
  *   Custom annotations to add to the generated case class.
  * @param type
  *   All instances of this message will be converted to this type. An implicit TypeMapper
  *   must be present.
  * @param companionAnnotations
  *   Custom annotations to add to the companion object of the generated class.
  */
@SerialVersionUID(0L)
final case class MessageOptions(
    `extends`: _root_.scala.collection.Seq[_root_.scala.Predef.String] = _root_.scala.collection.Seq.empty,
    companionExtends: _root_.scala.collection.Seq[_root_.scala.Predef.String] = _root_.scala.collection.Seq.empty,
    annotations: _root_.scala.collection.Seq[_root_.scala.Predef.String] = _root_.scala.collection.Seq.empty,
    `type`: scala.Option[_root_.scala.Predef.String] = None,
    companionAnnotations: _root_.scala.collection.Seq[_root_.scala.Predef.String] = _root_.scala.collection.Seq.empty
    ) extends scalapb.GeneratedMessage with scalapb.Message[MessageOptions] with scalapb.lenses.Updatable[MessageOptions] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      `extends`.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
      }
      companionExtends.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
      }
      annotations.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(3, __value)
      }
      if (`type`.isDefined) {
        val __value = `type`.get
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(4, __value)
      };
      companionAnnotations.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(5, __value)
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
      `extends`.foreach { __v =>
        val __m = __v
        _output__.writeString(1, __m)
      };
      companionExtends.foreach { __v =>
        val __m = __v
        _output__.writeString(2, __m)
      };
      annotations.foreach { __v =>
        val __m = __v
        _output__.writeString(3, __m)
      };
      `type`.foreach { __v =>
        val __m = __v
        _output__.writeString(4, __m)
      };
      companionAnnotations.foreach { __v =>
        val __m = __v
        _output__.writeString(5, __m)
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): scalapb.options.MessageOptions = {
      val __extends = (_root_.scala.collection.immutable.Vector.newBuilder[_root_.scala.Predef.String] ++= this.`extends`)
      val __companionExtends = (_root_.scala.collection.immutable.Vector.newBuilder[_root_.scala.Predef.String] ++= this.companionExtends)
      val __annotations = (_root_.scala.collection.immutable.Vector.newBuilder[_root_.scala.Predef.String] ++= this.annotations)
      var __type = this.`type`
      val __companionAnnotations = (_root_.scala.collection.immutable.Vector.newBuilder[_root_.scala.Predef.String] ++= this.companionAnnotations)
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __extends += _input__.readString()
          case 18 =>
            __companionExtends += _input__.readString()
          case 26 =>
            __annotations += _input__.readString()
          case 34 =>
            __type = Option(_input__.readString())
          case 42 =>
            __companionAnnotations += _input__.readString()
          case tag => _input__.skipField(tag)
        }
      }
      scalapb.options.MessageOptions(
          `extends` = __extends.result(),
          companionExtends = __companionExtends.result(),
          annotations = __annotations.result(),
          `type` = __type,
          companionAnnotations = __companionAnnotations.result()
      )
    }
    def clearExtends = copy(`extends` = _root_.scala.collection.Seq.empty)
    def addExtends(__vs: _root_.scala.Predef.String*): MessageOptions = addAllExtends(__vs)
    def addAllExtends(__vs: TraversableOnce[_root_.scala.Predef.String]): MessageOptions = copy(`extends` = `extends` ++ __vs)
    def withExtends(__v: _root_.scala.collection.Seq[_root_.scala.Predef.String]): MessageOptions = copy(`extends` = __v)
    def clearCompanionExtends = copy(companionExtends = _root_.scala.collection.Seq.empty)
    def addCompanionExtends(__vs: _root_.scala.Predef.String*): MessageOptions = addAllCompanionExtends(__vs)
    def addAllCompanionExtends(__vs: TraversableOnce[_root_.scala.Predef.String]): MessageOptions = copy(companionExtends = companionExtends ++ __vs)
    def withCompanionExtends(__v: _root_.scala.collection.Seq[_root_.scala.Predef.String]): MessageOptions = copy(companionExtends = __v)
    def clearAnnotations = copy(annotations = _root_.scala.collection.Seq.empty)
    def addAnnotations(__vs: _root_.scala.Predef.String*): MessageOptions = addAllAnnotations(__vs)
    def addAllAnnotations(__vs: TraversableOnce[_root_.scala.Predef.String]): MessageOptions = copy(annotations = annotations ++ __vs)
    def withAnnotations(__v: _root_.scala.collection.Seq[_root_.scala.Predef.String]): MessageOptions = copy(annotations = __v)
    def getType: _root_.scala.Predef.String = `type`.getOrElse("")
    def clearType: MessageOptions = copy(`type` = None)
    def withType(__v: _root_.scala.Predef.String): MessageOptions = copy(`type` = Option(__v))
    def clearCompanionAnnotations = copy(companionAnnotations = _root_.scala.collection.Seq.empty)
    def addCompanionAnnotations(__vs: _root_.scala.Predef.String*): MessageOptions = addAllCompanionAnnotations(__vs)
    def addAllCompanionAnnotations(__vs: TraversableOnce[_root_.scala.Predef.String]): MessageOptions = copy(companionAnnotations = companionAnnotations ++ __vs)
    def withCompanionAnnotations(__v: _root_.scala.collection.Seq[_root_.scala.Predef.String]): MessageOptions = copy(companionAnnotations = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => `extends`
        case 2 => companionExtends
        case 3 => annotations
        case 4 => `type`.orNull
        case 5 => companionAnnotations
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(`extends`.map(_root_.scalapb.descriptors.PString)(_root_.scala.collection.breakOut))
        case 2 => _root_.scalapb.descriptors.PRepeated(companionExtends.map(_root_.scalapb.descriptors.PString)(_root_.scala.collection.breakOut))
        case 3 => _root_.scalapb.descriptors.PRepeated(annotations.map(_root_.scalapb.descriptors.PString)(_root_.scala.collection.breakOut))
        case 4 => `type`.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 5 => _root_.scalapb.descriptors.PRepeated(companionAnnotations.map(_root_.scalapb.descriptors.PString)(_root_.scala.collection.breakOut))
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = scalapb.options.MessageOptions
}

object MessageOptions extends scalapb.GeneratedMessageCompanion[scalapb.options.MessageOptions] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[scalapb.options.MessageOptions] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): scalapb.options.MessageOptions = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    scalapb.options.MessageOptions(
      __fieldsMap.getOrElse(__fields.get(0), Nil).asInstanceOf[_root_.scala.collection.Seq[_root_.scala.Predef.String]],
      __fieldsMap.getOrElse(__fields.get(1), Nil).asInstanceOf[_root_.scala.collection.Seq[_root_.scala.Predef.String]],
      __fieldsMap.getOrElse(__fields.get(2), Nil).asInstanceOf[_root_.scala.collection.Seq[_root_.scala.Predef.String]],
      __fieldsMap.get(__fields.get(3)).asInstanceOf[scala.Option[_root_.scala.Predef.String]],
      __fieldsMap.getOrElse(__fields.get(4), Nil).asInstanceOf[_root_.scala.collection.Seq[_root_.scala.Predef.String]]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[scalapb.options.MessageOptions] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      scalapb.options.MessageOptions(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.collection.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.collection.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.collection.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).flatMap(_.as[scala.Option[_root_.scala.Predef.String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.collection.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.collection.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ScalapbProto.javaDescriptor.getMessageTypes.get(1)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ScalapbProto.scalaDescriptor.messages(1)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = scalapb.options.MessageOptions(
  )
  implicit class MessageOptionsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, scalapb.options.MessageOptions]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, scalapb.options.MessageOptions](_l) {
    def `extends`: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[_root_.scala.Predef.String]] = field(_.`extends`)((c_, f_) => c_.copy(`extends` = f_))
    def companionExtends: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[_root_.scala.Predef.String]] = field(_.companionExtends)((c_, f_) => c_.copy(companionExtends = f_))
    def annotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[_root_.scala.Predef.String]] = field(_.annotations)((c_, f_) => c_.copy(annotations = f_))
    def `type`: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.getType)((c_, f_) => c_.copy(`type` = Option(f_)))
    def optionalType: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[_root_.scala.Predef.String]] = field(_.`type`)((c_, f_) => c_.copy(`type` = f_))
    def companionAnnotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[_root_.scala.Predef.String]] = field(_.companionAnnotations)((c_, f_) => c_.copy(companionAnnotations = f_))
  }
  final val EXTENDS_FIELD_NUMBER = 1
  final val COMPANION_EXTENDS_FIELD_NUMBER = 2
  final val ANNOTATIONS_FIELD_NUMBER = 3
  final val TYPE_FIELD_NUMBER = 4
  final val COMPANION_ANNOTATIONS_FIELD_NUMBER = 5
}
