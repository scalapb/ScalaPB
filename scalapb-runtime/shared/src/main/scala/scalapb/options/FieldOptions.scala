// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package scalapb.options

/** @param collectionType
  *   Can be specified only if this field is repeated. If unspecified,
  *   it falls back to the file option named `collection_type`, which defaults
  *   to `scala.collection.Seq`.
  * @param keyType
  *   If the field is a map, you can specify custom Scala types for the key
  *   or value.
  * @param annotations
  *   Custom annotations to add to the field.
  * @param noBox
  *   Do not box this value in Option[T]
  */
@SerialVersionUID(0L)
final case class FieldOptions(
    `type`: scala.Option[String] = None,
    scalaName: scala.Option[String] = None,
    collectionType: scala.Option[String] = None,
    keyType: scala.Option[String] = None,
    valueType: scala.Option[String] = None,
    annotations: _root_.scala.collection.Seq[String] = _root_.scala.collection.Seq.empty,
    noBox: scala.Option[Boolean] = None
    ) extends scalapb.GeneratedMessage with scalapb.Message[FieldOptions] with scalapb.lenses.Updatable[FieldOptions] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (`type`.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, `type`.get) }
      if (scalaName.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, scalaName.get) }
      if (collectionType.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(3, collectionType.get) }
      if (keyType.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(4, keyType.get) }
      if (valueType.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(5, valueType.get) }
      annotations.foreach(annotations => __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(6, annotations))
      if (noBox.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(30, noBox.get) }
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
      `type`.foreach { __v =>
        _output__.writeString(1, __v)
      };
      scalaName.foreach { __v =>
        _output__.writeString(2, __v)
      };
      collectionType.foreach { __v =>
        _output__.writeString(3, __v)
      };
      keyType.foreach { __v =>
        _output__.writeString(4, __v)
      };
      valueType.foreach { __v =>
        _output__.writeString(5, __v)
      };
      annotations.foreach { __v =>
        _output__.writeString(6, __v)
      };
      noBox.foreach { __v =>
        _output__.writeBool(30, __v)
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): scalapb.options.FieldOptions = {
      var __type = this.`type`
      var __scalaName = this.scalaName
      var __collectionType = this.collectionType
      var __keyType = this.keyType
      var __valueType = this.valueType
      val __annotations = (_root_.scala.collection.immutable.Vector.newBuilder[String] ++= this.annotations)
      var __noBox = this.noBox
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __type = Option(_input__.readString())
          case 18 =>
            __scalaName = Option(_input__.readString())
          case 26 =>
            __collectionType = Option(_input__.readString())
          case 34 =>
            __keyType = Option(_input__.readString())
          case 42 =>
            __valueType = Option(_input__.readString())
          case 50 =>
            __annotations += _input__.readString()
          case 240 =>
            __noBox = Option(_input__.readBool())
          case tag => _input__.skipField(tag)
        }
      }
      scalapb.options.FieldOptions(
          `type` = __type,
          scalaName = __scalaName,
          collectionType = __collectionType,
          keyType = __keyType,
          valueType = __valueType,
          annotations = __annotations.result(),
          noBox = __noBox
      )
    }
    def getType: String = `type`.getOrElse("")
    def clearType: FieldOptions = copy(`type` = None)
    def withType(__v: String): FieldOptions = copy(`type` = Option(__v))
    def getScalaName: String = scalaName.getOrElse("")
    def clearScalaName: FieldOptions = copy(scalaName = None)
    def withScalaName(__v: String): FieldOptions = copy(scalaName = Option(__v))
    def getCollectionType: String = collectionType.getOrElse("")
    def clearCollectionType: FieldOptions = copy(collectionType = None)
    def withCollectionType(__v: String): FieldOptions = copy(collectionType = Option(__v))
    def getKeyType: String = keyType.getOrElse("")
    def clearKeyType: FieldOptions = copy(keyType = None)
    def withKeyType(__v: String): FieldOptions = copy(keyType = Option(__v))
    def getValueType: String = valueType.getOrElse("")
    def clearValueType: FieldOptions = copy(valueType = None)
    def withValueType(__v: String): FieldOptions = copy(valueType = Option(__v))
    def clearAnnotations = copy(annotations = _root_.scala.collection.Seq.empty)
    def addAnnotations(__vs: String*): FieldOptions = addAllAnnotations(__vs)
    def addAllAnnotations(__vs: TraversableOnce[String]): FieldOptions = copy(annotations = annotations ++ __vs)
    def withAnnotations(__v: _root_.scala.collection.Seq[String]): FieldOptions = copy(annotations = __v)
    def getNoBox: Boolean = noBox.getOrElse(false)
    def clearNoBox: FieldOptions = copy(noBox = None)
    def withNoBox(__v: Boolean): FieldOptions = copy(noBox = Option(__v))
    def getFieldByNumber(__fieldNumber: Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => `type`.orNull
        case 2 => scalaName.orNull
        case 3 => collectionType.orNull
        case 4 => keyType.orNull
        case 5 => valueType.orNull
        case 6 => annotations
        case 30 => noBox.orNull
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => `type`.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 2 => scalaName.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 3 => collectionType.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 4 => keyType.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 5 => valueType.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 6 => _root_.scalapb.descriptors.PRepeated(annotations.map(_root_.scalapb.descriptors.PString)(_root_.scala.collection.breakOut))
        case 30 => noBox.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
      }
    }
    def toProtoString: String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = scalapb.options.FieldOptions
}

object FieldOptions extends scalapb.GeneratedMessageCompanion[scalapb.options.FieldOptions] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[scalapb.options.FieldOptions] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): scalapb.options.FieldOptions = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    scalapb.options.FieldOptions(
      __fieldsMap.get(__fields.get(0)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(1)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(2)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(3)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(4)).asInstanceOf[scala.Option[String]],
      __fieldsMap.getOrElse(__fields.get(5), Nil).asInstanceOf[_root_.scala.collection.Seq[String]],
      __fieldsMap.get(__fields.get(6)).asInstanceOf[scala.Option[Boolean]]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[scalapb.options.FieldOptions] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      scalapb.options.FieldOptions(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(6).get).map(_.as[_root_.scala.collection.Seq[String]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(30).get).flatMap(_.as[scala.Option[Boolean]])
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ScalapbProto.javaDescriptor.getMessageTypes.get(2)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ScalapbProto.scalaDescriptor.messages(2)
  def messageCompanionForFieldNumber(__number: Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = scalapb.options.FieldOptions(
  )
  implicit class FieldOptionsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, scalapb.options.FieldOptions]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, scalapb.options.FieldOptions](_l) {
    def `type`: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getType)((c_, f_) => c_.copy(`type` = Option(f_)))
    def optionalType: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.`type`)((c_, f_) => c_.copy(`type` = f_))
    def scalaName: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getScalaName)((c_, f_) => c_.copy(scalaName = Option(f_)))
    def optionalScalaName: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.scalaName)((c_, f_) => c_.copy(scalaName = f_))
    def collectionType: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getCollectionType)((c_, f_) => c_.copy(collectionType = Option(f_)))
    def optionalCollectionType: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.collectionType)((c_, f_) => c_.copy(collectionType = f_))
    def keyType: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getKeyType)((c_, f_) => c_.copy(keyType = Option(f_)))
    def optionalKeyType: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.keyType)((c_, f_) => c_.copy(keyType = f_))
    def valueType: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getValueType)((c_, f_) => c_.copy(valueType = Option(f_)))
    def optionalValueType: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.valueType)((c_, f_) => c_.copy(valueType = f_))
    def annotations: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[String]] = field(_.annotations)((c_, f_) => c_.copy(annotations = f_))
    def noBox: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getNoBox)((c_, f_) => c_.copy(noBox = Option(f_)))
    def optionalNoBox: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.noBox)((c_, f_) => c_.copy(noBox = f_))
  }
  final val TYPE_FIELD_NUMBER = 1
  final val SCALA_NAME_FIELD_NUMBER = 2
  final val COLLECTION_TYPE_FIELD_NUMBER = 3
  final val KEY_TYPE_FIELD_NUMBER = 4
  final val VALUE_TYPE_FIELD_NUMBER = 5
  final val ANNOTATIONS_FIELD_NUMBER = 6
  final val NO_BOX_FIELD_NUMBER = 30
}
