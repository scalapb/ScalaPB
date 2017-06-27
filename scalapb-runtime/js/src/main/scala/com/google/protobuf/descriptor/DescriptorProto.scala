// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package com.google.protobuf.descriptor



/** Describes a message type.
  *
  * @param reservedName
  *   Reserved field names, which may not be used by fields in the same message.
  *   A given name may only be reserved once.
  */
@SerialVersionUID(0L)
final case class DescriptorProto(
    name: scala.Option[String] = None,
    field: _root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto] = _root_.scala.collection.Seq.empty,
    extension: _root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto] = _root_.scala.collection.Seq.empty,
    nestedType: _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto] = _root_.scala.collection.Seq.empty,
    enumType: _root_.scala.collection.Seq[com.google.protobuf.descriptor.EnumDescriptorProto] = _root_.scala.collection.Seq.empty,
    extensionRange: _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange] = _root_.scala.collection.Seq.empty,
    oneofDecl: _root_.scala.collection.Seq[com.google.protobuf.descriptor.OneofDescriptorProto] = _root_.scala.collection.Seq.empty,
    options: scala.Option[com.google.protobuf.descriptor.MessageOptions] = None,
    reservedRange: _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ReservedRange] = _root_.scala.collection.Seq.empty,
    reservedName: _root_.scala.collection.Seq[String] = _root_.scala.collection.Seq.empty
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[DescriptorProto] with com.trueaccord.lenses.Updatable[DescriptorProto] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (name.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, name.get) }
      field.foreach(field => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(field.serializedSize) + field.serializedSize)
      extension.foreach(extension => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(extension.serializedSize) + extension.serializedSize)
      nestedType.foreach(nestedType => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(nestedType.serializedSize) + nestedType.serializedSize)
      enumType.foreach(enumType => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(enumType.serializedSize) + enumType.serializedSize)
      extensionRange.foreach(extensionRange => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(extensionRange.serializedSize) + extensionRange.serializedSize)
      oneofDecl.foreach(oneofDecl => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(oneofDecl.serializedSize) + oneofDecl.serializedSize)
      if (options.isDefined) { __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(options.get.serializedSize) + options.get.serializedSize }
      reservedRange.foreach(reservedRange => __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(reservedRange.serializedSize) + reservedRange.serializedSize)
      reservedName.foreach(reservedName => __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(10, reservedName))
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
      name.foreach { __v =>
        _output__.writeString(1, __v)
      };
      field.foreach { __v =>
        _output__.writeTag(2, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      nestedType.foreach { __v =>
        _output__.writeTag(3, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      enumType.foreach { __v =>
        _output__.writeTag(4, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      extensionRange.foreach { __v =>
        _output__.writeTag(5, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      extension.foreach { __v =>
        _output__.writeTag(6, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      options.foreach { __v =>
        _output__.writeTag(7, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      oneofDecl.foreach { __v =>
        _output__.writeTag(8, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      reservedRange.foreach { __v =>
        _output__.writeTag(9, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      reservedName.foreach { __v =>
        _output__.writeString(10, __v)
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.descriptor.DescriptorProto = {
      var __name = this.name
      val __field = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.FieldDescriptorProto] ++= this.field)
      val __extension = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.FieldDescriptorProto] ++= this.extension)
      val __nestedType = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.DescriptorProto] ++= this.nestedType)
      val __enumType = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.EnumDescriptorProto] ++= this.enumType)
      val __extensionRange = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange] ++= this.extensionRange)
      val __oneofDecl = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.OneofDescriptorProto] ++= this.oneofDecl)
      var __options = this.options
      val __reservedRange = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.DescriptorProto.ReservedRange] ++= this.reservedRange)
      val __reservedName = (_root_.scala.collection.immutable.Vector.newBuilder[String] ++= this.reservedName)
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __name = Some(_input__.readString())
          case 18 =>
            __field += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.FieldDescriptorProto.defaultInstance)
          case 50 =>
            __extension += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.FieldDescriptorProto.defaultInstance)
          case 26 =>
            __nestedType += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.DescriptorProto.defaultInstance)
          case 34 =>
            __enumType += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.EnumDescriptorProto.defaultInstance)
          case 42 =>
            __extensionRange += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.DescriptorProto.ExtensionRange.defaultInstance)
          case 66 =>
            __oneofDecl += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.OneofDescriptorProto.defaultInstance)
          case 58 =>
            __options = Some(_root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, __options.getOrElse(com.google.protobuf.descriptor.MessageOptions.defaultInstance)))
          case 74 =>
            __reservedRange += _root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.DescriptorProto.ReservedRange.defaultInstance)
          case 82 =>
            __reservedName += _input__.readString()
          case tag => _input__.skipField(tag)
        }
      }
      com.google.protobuf.descriptor.DescriptorProto(
          name = __name,
          field = __field.result(),
          extension = __extension.result(),
          nestedType = __nestedType.result(),
          enumType = __enumType.result(),
          extensionRange = __extensionRange.result(),
          oneofDecl = __oneofDecl.result(),
          options = __options,
          reservedRange = __reservedRange.result(),
          reservedName = __reservedName.result()
      )
    }
    def getName: String = name.getOrElse("")
    def clearName: DescriptorProto = copy(name = None)
    def withName(__v: String): DescriptorProto = copy(name = Some(__v))
    def clearField = copy(field = _root_.scala.collection.Seq.empty)
    def addField(__vs: com.google.protobuf.descriptor.FieldDescriptorProto*): DescriptorProto = addAllField(__vs)
    def addAllField(__vs: TraversableOnce[com.google.protobuf.descriptor.FieldDescriptorProto]): DescriptorProto = copy(field = field ++ __vs)
    def withField(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]): DescriptorProto = copy(field = __v)
    def clearExtension = copy(extension = _root_.scala.collection.Seq.empty)
    def addExtension(__vs: com.google.protobuf.descriptor.FieldDescriptorProto*): DescriptorProto = addAllExtension(__vs)
    def addAllExtension(__vs: TraversableOnce[com.google.protobuf.descriptor.FieldDescriptorProto]): DescriptorProto = copy(extension = extension ++ __vs)
    def withExtension(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]): DescriptorProto = copy(extension = __v)
    def clearNestedType = copy(nestedType = _root_.scala.collection.Seq.empty)
    def addNestedType(__vs: com.google.protobuf.descriptor.DescriptorProto*): DescriptorProto = addAllNestedType(__vs)
    def addAllNestedType(__vs: TraversableOnce[com.google.protobuf.descriptor.DescriptorProto]): DescriptorProto = copy(nestedType = nestedType ++ __vs)
    def withNestedType(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto]): DescriptorProto = copy(nestedType = __v)
    def clearEnumType = copy(enumType = _root_.scala.collection.Seq.empty)
    def addEnumType(__vs: com.google.protobuf.descriptor.EnumDescriptorProto*): DescriptorProto = addAllEnumType(__vs)
    def addAllEnumType(__vs: TraversableOnce[com.google.protobuf.descriptor.EnumDescriptorProto]): DescriptorProto = copy(enumType = enumType ++ __vs)
    def withEnumType(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.EnumDescriptorProto]): DescriptorProto = copy(enumType = __v)
    def clearExtensionRange = copy(extensionRange = _root_.scala.collection.Seq.empty)
    def addExtensionRange(__vs: com.google.protobuf.descriptor.DescriptorProto.ExtensionRange*): DescriptorProto = addAllExtensionRange(__vs)
    def addAllExtensionRange(__vs: TraversableOnce[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange]): DescriptorProto = copy(extensionRange = extensionRange ++ __vs)
    def withExtensionRange(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange]): DescriptorProto = copy(extensionRange = __v)
    def clearOneofDecl = copy(oneofDecl = _root_.scala.collection.Seq.empty)
    def addOneofDecl(__vs: com.google.protobuf.descriptor.OneofDescriptorProto*): DescriptorProto = addAllOneofDecl(__vs)
    def addAllOneofDecl(__vs: TraversableOnce[com.google.protobuf.descriptor.OneofDescriptorProto]): DescriptorProto = copy(oneofDecl = oneofDecl ++ __vs)
    def withOneofDecl(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.OneofDescriptorProto]): DescriptorProto = copy(oneofDecl = __v)
    def getOptions: com.google.protobuf.descriptor.MessageOptions = options.getOrElse(com.google.protobuf.descriptor.MessageOptions.defaultInstance)
    def clearOptions: DescriptorProto = copy(options = None)
    def withOptions(__v: com.google.protobuf.descriptor.MessageOptions): DescriptorProto = copy(options = Some(__v))
    def clearReservedRange = copy(reservedRange = _root_.scala.collection.Seq.empty)
    def addReservedRange(__vs: com.google.protobuf.descriptor.DescriptorProto.ReservedRange*): DescriptorProto = addAllReservedRange(__vs)
    def addAllReservedRange(__vs: TraversableOnce[com.google.protobuf.descriptor.DescriptorProto.ReservedRange]): DescriptorProto = copy(reservedRange = reservedRange ++ __vs)
    def withReservedRange(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ReservedRange]): DescriptorProto = copy(reservedRange = __v)
    def clearReservedName = copy(reservedName = _root_.scala.collection.Seq.empty)
    def addReservedName(__vs: String*): DescriptorProto = addAllReservedName(__vs)
    def addAllReservedName(__vs: TraversableOnce[String]): DescriptorProto = copy(reservedName = reservedName ++ __vs)
    def withReservedName(__v: _root_.scala.collection.Seq[String]): DescriptorProto = copy(reservedName = __v)
    def getFieldByNumber(__fieldNumber: Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => name.orNull
        case 2 => field
        case 6 => extension
        case 3 => nestedType
        case 4 => enumType
        case 5 => extensionRange
        case 8 => oneofDecl
        case 7 => options.orNull
        case 9 => reservedRange
        case 10 => reservedName
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => name.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 2 => _root_.scalapb.descriptors.PRepeated(field.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 6 => _root_.scalapb.descriptors.PRepeated(extension.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 3 => _root_.scalapb.descriptors.PRepeated(nestedType.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 4 => _root_.scalapb.descriptors.PRepeated(enumType.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 5 => _root_.scalapb.descriptors.PRepeated(extensionRange.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 8 => _root_.scalapb.descriptors.PRepeated(oneofDecl.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 7 => options.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 9 => _root_.scalapb.descriptors.PRepeated(reservedRange.map(_.toPMessage)(_root_.scala.collection.breakOut))
        case 10 => _root_.scalapb.descriptors.PRepeated(reservedName.map(_root_.scalapb.descriptors.PString)(_root_.scala.collection.breakOut))
      }
    }
    override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.descriptor.DescriptorProto
}

object DescriptorProto extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.DescriptorProto] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.DescriptorProto] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.descriptor.DescriptorProto = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.google.protobuf.descriptor.DescriptorProto(
      __fieldsMap.get(__fields.get(0)).asInstanceOf[scala.Option[String]],
      __fieldsMap.getOrElse(__fields.get(1), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]],
      __fieldsMap.getOrElse(__fields.get(2), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]],
      __fieldsMap.getOrElse(__fields.get(3), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto]],
      __fieldsMap.getOrElse(__fields.get(4), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.EnumDescriptorProto]],
      __fieldsMap.getOrElse(__fields.get(5), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange]],
      __fieldsMap.getOrElse(__fields.get(6), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.OneofDescriptorProto]],
      __fieldsMap.get(__fields.get(7)).asInstanceOf[scala.Option[com.google.protobuf.descriptor.MessageOptions]],
      __fieldsMap.getOrElse(__fields.get(8), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ReservedRange]],
      __fieldsMap.getOrElse(__fields.get(9), Nil).asInstanceOf[_root_.scala.collection.Seq[String]]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.descriptor.DescriptorProto] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.google.protobuf.descriptor.DescriptorProto(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(6).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.EnumDescriptorProto]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(8).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.OneofDescriptorProto]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(7).get).flatMap(_.as[scala.Option[com.google.protobuf.descriptor.MessageOptions]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(9).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ReservedRange]]).getOrElse(_root_.scala.collection.Seq.empty),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(10).get).map(_.as[_root_.scala.collection.Seq[String]]).getOrElse(_root_.scala.collection.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = DescriptorProtoCompanion.javaDescriptor.getMessageTypes.get(2)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = DescriptorProtoCompanion.scalaDescriptor.messages(2)
  def messageCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = null
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 2 => __out = com.google.protobuf.descriptor.FieldDescriptorProto
      case 6 => __out = com.google.protobuf.descriptor.FieldDescriptorProto
      case 3 => __out = com.google.protobuf.descriptor.DescriptorProto
      case 4 => __out = com.google.protobuf.descriptor.EnumDescriptorProto
      case 5 => __out = com.google.protobuf.descriptor.DescriptorProto.ExtensionRange
      case 8 => __out = com.google.protobuf.descriptor.OneofDescriptorProto
      case 7 => __out = com.google.protobuf.descriptor.MessageOptions
      case 9 => __out = com.google.protobuf.descriptor.DescriptorProto.ReservedRange
    }
    __out
  }
  def enumCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.google.protobuf.descriptor.DescriptorProto(
  )
  @SerialVersionUID(0L)
  final case class ExtensionRange(
      start: scala.Option[Int] = None,
      end: scala.Option[Int] = None
      ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[ExtensionRange] with com.trueaccord.lenses.Updatable[ExtensionRange] {
      @transient
      private[this] var __serializedSizeCachedValue: Int = 0
      private[this] def __computeSerializedValue(): Int = {
        var __size = 0
        if (start.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(1, start.get) }
        if (end.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(2, end.get) }
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
        start.foreach { __v =>
          _output__.writeInt32(1, __v)
        };
        end.foreach { __v =>
          _output__.writeInt32(2, __v)
        };
      }
      def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.descriptor.DescriptorProto.ExtensionRange = {
        var __start = this.start
        var __end = this.end
        var _done__ = false
        while (!_done__) {
          val _tag__ = _input__.readTag()
          _tag__ match {
            case 0 => _done__ = true
            case 8 =>
              __start = Some(_input__.readInt32())
            case 16 =>
              __end = Some(_input__.readInt32())
            case tag => _input__.skipField(tag)
          }
        }
        com.google.protobuf.descriptor.DescriptorProto.ExtensionRange(
            start = __start,
            end = __end
        )
      }
      def getStart: Int = start.getOrElse(0)
      def clearStart: ExtensionRange = copy(start = None)
      def withStart(__v: Int): ExtensionRange = copy(start = Some(__v))
      def getEnd: Int = end.getOrElse(0)
      def clearEnd: ExtensionRange = copy(end = None)
      def withEnd(__v: Int): ExtensionRange = copy(end = Some(__v))
      def getFieldByNumber(__fieldNumber: Int): scala.Any = {
        (__fieldNumber: @_root_.scala.unchecked) match {
          case 1 => start.orNull
          case 2 => end.orNull
        }
      }
      def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
        require(__field.containingMessage eq companion.scalaDescriptor)
        (__field.number: @_root_.scala.unchecked) match {
          case 1 => start.map(_root_.scalapb.descriptors.PInt).getOrElse(_root_.scalapb.descriptors.PEmpty)
          case 2 => end.map(_root_.scalapb.descriptors.PInt).getOrElse(_root_.scalapb.descriptors.PEmpty)
        }
      }
      override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
      def companion = com.google.protobuf.descriptor.DescriptorProto.ExtensionRange
  }
  
  object ExtensionRange extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange] {
    implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange] = this
    def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.descriptor.DescriptorProto.ExtensionRange = {
      require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
      val __fields = javaDescriptor.getFields
      com.google.protobuf.descriptor.DescriptorProto.ExtensionRange(
        __fieldsMap.get(__fields.get(0)).asInstanceOf[scala.Option[Int]],
        __fieldsMap.get(__fields.get(1)).asInstanceOf[scala.Option[Int]]
      )
    }
    implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange] = _root_.scalapb.descriptors.Reads{
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
        com.google.protobuf.descriptor.DescriptorProto.ExtensionRange(
          __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[scala.Option[Int]]),
          __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[scala.Option[Int]])
        )
      case _ => throw new RuntimeException("Expected PMessage")
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = com.google.protobuf.descriptor.DescriptorProto.javaDescriptor.getNestedTypes.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = com.google.protobuf.descriptor.DescriptorProto.scalaDescriptor.nestedMessages(0)
    def messageCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__fieldNumber)
    def enumCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
    lazy val defaultInstance = com.google.protobuf.descriptor.DescriptorProto.ExtensionRange(
    )
    implicit class ExtensionRangeLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.descriptor.DescriptorProto.ExtensionRange]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.descriptor.DescriptorProto.ExtensionRange](_l) {
      def start: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.getStart)((c_, f_) => c_.copy(start = Some(f_)))
      def optionalStart: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[Int]] = field(_.start)((c_, f_) => c_.copy(start = f_))
      def end: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.getEnd)((c_, f_) => c_.copy(end = Some(f_)))
      def optionalEnd: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[Int]] = field(_.end)((c_, f_) => c_.copy(end = f_))
    }
    final val START_FIELD_NUMBER = 1
    final val END_FIELD_NUMBER = 2
  }
  
  /** Range of reserved tag numbers. Reserved tag numbers may not be used by
    * fields or extension ranges in the same message. Reserved ranges may
    * not overlap.
    *
    * @param start
    *   Inclusive.
    * @param end
    *   Exclusive.
    */
  @SerialVersionUID(0L)
  final case class ReservedRange(
      start: scala.Option[Int] = None,
      end: scala.Option[Int] = None
      ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[ReservedRange] with com.trueaccord.lenses.Updatable[ReservedRange] {
      @transient
      private[this] var __serializedSizeCachedValue: Int = 0
      private[this] def __computeSerializedValue(): Int = {
        var __size = 0
        if (start.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(1, start.get) }
        if (end.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(2, end.get) }
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
        start.foreach { __v =>
          _output__.writeInt32(1, __v)
        };
        end.foreach { __v =>
          _output__.writeInt32(2, __v)
        };
      }
      def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.descriptor.DescriptorProto.ReservedRange = {
        var __start = this.start
        var __end = this.end
        var _done__ = false
        while (!_done__) {
          val _tag__ = _input__.readTag()
          _tag__ match {
            case 0 => _done__ = true
            case 8 =>
              __start = Some(_input__.readInt32())
            case 16 =>
              __end = Some(_input__.readInt32())
            case tag => _input__.skipField(tag)
          }
        }
        com.google.protobuf.descriptor.DescriptorProto.ReservedRange(
            start = __start,
            end = __end
        )
      }
      def getStart: Int = start.getOrElse(0)
      def clearStart: ReservedRange = copy(start = None)
      def withStart(__v: Int): ReservedRange = copy(start = Some(__v))
      def getEnd: Int = end.getOrElse(0)
      def clearEnd: ReservedRange = copy(end = None)
      def withEnd(__v: Int): ReservedRange = copy(end = Some(__v))
      def getFieldByNumber(__fieldNumber: Int): scala.Any = {
        (__fieldNumber: @_root_.scala.unchecked) match {
          case 1 => start.orNull
          case 2 => end.orNull
        }
      }
      def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
        require(__field.containingMessage eq companion.scalaDescriptor)
        (__field.number: @_root_.scala.unchecked) match {
          case 1 => start.map(_root_.scalapb.descriptors.PInt).getOrElse(_root_.scalapb.descriptors.PEmpty)
          case 2 => end.map(_root_.scalapb.descriptors.PInt).getOrElse(_root_.scalapb.descriptors.PEmpty)
        }
      }
      override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
      def companion = com.google.protobuf.descriptor.DescriptorProto.ReservedRange
  }
  
  object ReservedRange extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.DescriptorProto.ReservedRange] {
    implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.DescriptorProto.ReservedRange] = this
    def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.descriptor.DescriptorProto.ReservedRange = {
      require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
      val __fields = javaDescriptor.getFields
      com.google.protobuf.descriptor.DescriptorProto.ReservedRange(
        __fieldsMap.get(__fields.get(0)).asInstanceOf[scala.Option[Int]],
        __fieldsMap.get(__fields.get(1)).asInstanceOf[scala.Option[Int]]
      )
    }
    implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.descriptor.DescriptorProto.ReservedRange] = _root_.scalapb.descriptors.Reads{
      case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
        require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
        com.google.protobuf.descriptor.DescriptorProto.ReservedRange(
          __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[scala.Option[Int]]),
          __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[scala.Option[Int]])
        )
      case _ => throw new RuntimeException("Expected PMessage")
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = com.google.protobuf.descriptor.DescriptorProto.javaDescriptor.getNestedTypes.get(1)
    def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = com.google.protobuf.descriptor.DescriptorProto.scalaDescriptor.nestedMessages(1)
    def messageCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__fieldNumber)
    def enumCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
    lazy val defaultInstance = com.google.protobuf.descriptor.DescriptorProto.ReservedRange(
    )
    implicit class ReservedRangeLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.descriptor.DescriptorProto.ReservedRange]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.descriptor.DescriptorProto.ReservedRange](_l) {
      def start: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.getStart)((c_, f_) => c_.copy(start = Some(f_)))
      def optionalStart: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[Int]] = field(_.start)((c_, f_) => c_.copy(start = f_))
      def end: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.getEnd)((c_, f_) => c_.copy(end = Some(f_)))
      def optionalEnd: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[Int]] = field(_.end)((c_, f_) => c_.copy(end = f_))
    }
    final val START_FIELD_NUMBER = 1
    final val END_FIELD_NUMBER = 2
  }
  
  implicit class DescriptorProtoLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.descriptor.DescriptorProto]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.google.protobuf.descriptor.DescriptorProto](_l) {
    def name: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.getName)((c_, f_) => c_.copy(name = Some(f_)))
    def optionalName: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[String]] = field(_.name)((c_, f_) => c_.copy(name = f_))
    def field: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]] = field(_.field)((c_, f_) => c_.copy(field = f_))
    def extension: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.FieldDescriptorProto]] = field(_.extension)((c_, f_) => c_.copy(extension = f_))
    def nestedType: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto]] = field(_.nestedType)((c_, f_) => c_.copy(nestedType = f_))
    def enumType: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.EnumDescriptorProto]] = field(_.enumType)((c_, f_) => c_.copy(enumType = f_))
    def extensionRange: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ExtensionRange]] = field(_.extensionRange)((c_, f_) => c_.copy(extensionRange = f_))
    def oneofDecl: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.OneofDescriptorProto]] = field(_.oneofDecl)((c_, f_) => c_.copy(oneofDecl = f_))
    def options: _root_.com.trueaccord.lenses.Lens[UpperPB, com.google.protobuf.descriptor.MessageOptions] = field(_.getOptions)((c_, f_) => c_.copy(options = Some(f_)))
    def optionalOptions: _root_.com.trueaccord.lenses.Lens[UpperPB, scala.Option[com.google.protobuf.descriptor.MessageOptions]] = field(_.options)((c_, f_) => c_.copy(options = f_))
    def reservedRange: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.DescriptorProto.ReservedRange]] = field(_.reservedRange)((c_, f_) => c_.copy(reservedRange = f_))
    def reservedName: _root_.com.trueaccord.lenses.Lens[UpperPB, _root_.scala.collection.Seq[String]] = field(_.reservedName)((c_, f_) => c_.copy(reservedName = f_))
  }
  final val NAME_FIELD_NUMBER = 1
  final val FIELD_FIELD_NUMBER = 2
  final val EXTENSION_FIELD_NUMBER = 6
  final val NESTED_TYPE_FIELD_NUMBER = 3
  final val ENUM_TYPE_FIELD_NUMBER = 4
  final val EXTENSION_RANGE_FIELD_NUMBER = 5
  final val ONEOF_DECL_FIELD_NUMBER = 8
  final val OPTIONS_FIELD_NUMBER = 7
  final val RESERVED_RANGE_FIELD_NUMBER = 9
  final val RESERVED_NAME_FIELD_NUMBER = 10
}
