// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO2

package com.google.protobuf.descriptor

/** @param javaPackage
  *   Sets the Java package where classes generated from this .proto will be
  *   placed.  By default, the proto package is used, but this is often
  *   inappropriate because proto packages do not normally start with backwards
  *   domain names.
  * @param javaOuterClassname
  *   If set, all the classes from the .proto file are wrapped in a single
  *   outer class with the given name.  This applies to both Proto1
  *   (equivalent to the old "--one_java_file" option) and Proto2 (where
  *   a .proto always translates to a single class, but you may want to
  *   explicitly choose the class name).
  * @param javaMultipleFiles
  *   If set true, then the Java code generator will generate a separate .java
  *   file for each top-level message, enum, and service defined in the .proto
  *   file.  Thus, these types will *not* be nested inside the outer class
  *   named by java_outer_classname.  However, the outer class will still be
  *   generated to contain the file's getDescriptor() method as well as any
  *   top-level extensions defined in the file.
  * @param javaGenerateEqualsAndHash
  *   This option does nothing.
  * @param javaStringCheckUtf8
  *   If set true, then the Java2 code generator will generate code that
  *   throws an exception whenever an attempt is made to assign a non-UTF-8
  *   byte sequence to a string field.
  *   Message reflection will do the same.
  *   However, an extension field still accepts non-UTF-8 byte sequences.
  *   This option has no effect on when used with the lite runtime.
  * @param goPackage
  *   Sets the Go package where structs generated from this .proto will be
  *   placed. If omitted, the Go package will be derived from the following:
  *     - The basename of the package import path, if provided.
  *     - Otherwise, the package statement in the .proto file, if present.
  *     - Otherwise, the basename of the .proto file, without extension.
  * @param ccGenericServices
  *   Should generic services be generated in each language?  "Generic" services
  *   are not specific to any particular RPC system.  They are generated by the
  *   main code generators in each language (without additional plugins).
  *   Generic services were the only kind of service generation supported by
  *   early versions of google.protobuf.
  *  
  *   Generic services are now considered deprecated in favor of using plugins
  *   that generate code specific to your particular RPC system.  Therefore,
  *   these default to false.  Old code which depends on generic services should
  *   explicitly set them to true.
  * @param deprecated
  *   Is this file deprecated?
  *   Depending on the target platform, this can emit Deprecated annotations
  *   for everything in the file, or it will be completely ignored; in the very
  *   least, this is a formalization for deprecating files.
  * @param ccEnableArenas
  *   Enables the use of arenas for the proto messages in this file. This applies
  *   only to generated classes for C++.
  * @param objcClassPrefix
  *   Sets the objective c class prefix which is prepended to all objective c
  *   generated classes from this .proto. There is no default.
  * @param csharpNamespace
  *   Namespace for generated classes; defaults to the package.
  * @param swiftPrefix
  *   By default Swift generators will take the proto package and CamelCase it
  *   replacing '.' with underscore and use that to prefix the types/symbols
  *   defined. When this options is provided, they will use this value instead
  *   to prefix the types/symbols defined.
  * @param phpClassPrefix
  *   Sets the php class prefix which is prepended to all php generated classes
  *   from this .proto. Default is empty.
  * @param uninterpretedOption
  *   The parser stores options it doesn't recognize here. See above.
  */
@SerialVersionUID(0L)
final case class FileOptions(
    javaPackage: scala.Option[String] = None,
    javaOuterClassname: scala.Option[String] = None,
    javaMultipleFiles: scala.Option[Boolean] = None,
    @scala.deprecated(message="Marked as deprecated in proto file", "") javaGenerateEqualsAndHash: scala.Option[Boolean] = None,
    javaStringCheckUtf8: scala.Option[Boolean] = None,
    optimizeFor: scala.Option[com.google.protobuf.descriptor.FileOptions.OptimizeMode] = None,
    goPackage: scala.Option[String] = None,
    ccGenericServices: scala.Option[Boolean] = None,
    javaGenericServices: scala.Option[Boolean] = None,
    pyGenericServices: scala.Option[Boolean] = None,
    deprecated: scala.Option[Boolean] = None,
    ccEnableArenas: scala.Option[Boolean] = None,
    objcClassPrefix: scala.Option[String] = None,
    csharpNamespace: scala.Option[String] = None,
    swiftPrefix: scala.Option[String] = None,
    phpClassPrefix: scala.Option[String] = None,
    uninterpretedOption: _root_.scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption] = _root_.scala.collection.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet()
    ) extends scalapb.GeneratedMessage with scalapb.Message[FileOptions] with scalapb.lenses.Updatable[FileOptions] with _root_.scalapb.ExtendableMessage[FileOptions] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (javaPackage.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, javaPackage.get) }
      if (javaOuterClassname.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(8, javaOuterClassname.get) }
      if (javaMultipleFiles.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(10, javaMultipleFiles.get) }
      if (javaGenerateEqualsAndHash.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(20, javaGenerateEqualsAndHash.get) }
      if (javaStringCheckUtf8.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(27, javaStringCheckUtf8.get) }
      if (optimizeFor.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(9, optimizeFor.get.value) }
      if (goPackage.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(11, goPackage.get) }
      if (ccGenericServices.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(16, ccGenericServices.get) }
      if (javaGenericServices.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(17, javaGenericServices.get) }
      if (pyGenericServices.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(18, pyGenericServices.get) }
      if (deprecated.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(23, deprecated.get) }
      if (ccEnableArenas.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(31, ccEnableArenas.get) }
      if (objcClassPrefix.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(36, objcClassPrefix.get) }
      if (csharpNamespace.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(37, csharpNamespace.get) }
      if (swiftPrefix.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(39, swiftPrefix.get) }
      if (phpClassPrefix.isDefined) { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(40, phpClassPrefix.get) }
      uninterpretedOption.foreach(uninterpretedOption => __size += 2 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(uninterpretedOption.serializedSize) + uninterpretedOption.serializedSize)
      __size += unknownFields.serializedSize
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
      javaPackage.foreach { __v =>
        _output__.writeString(1, __v)
      };
      javaOuterClassname.foreach { __v =>
        _output__.writeString(8, __v)
      };
      optimizeFor.foreach { __v =>
        _output__.writeEnum(9, __v.value)
      };
      javaMultipleFiles.foreach { __v =>
        _output__.writeBool(10, __v)
      };
      goPackage.foreach { __v =>
        _output__.writeString(11, __v)
      };
      ccGenericServices.foreach { __v =>
        _output__.writeBool(16, __v)
      };
      javaGenericServices.foreach { __v =>
        _output__.writeBool(17, __v)
      };
      pyGenericServices.foreach { __v =>
        _output__.writeBool(18, __v)
      };
      javaGenerateEqualsAndHash.foreach { __v =>
        _output__.writeBool(20, __v)
      };
      deprecated.foreach { __v =>
        _output__.writeBool(23, __v)
      };
      javaStringCheckUtf8.foreach { __v =>
        _output__.writeBool(27, __v)
      };
      ccEnableArenas.foreach { __v =>
        _output__.writeBool(31, __v)
      };
      objcClassPrefix.foreach { __v =>
        _output__.writeString(36, __v)
      };
      csharpNamespace.foreach { __v =>
        _output__.writeString(37, __v)
      };
      swiftPrefix.foreach { __v =>
        _output__.writeString(39, __v)
      };
      phpClassPrefix.foreach { __v =>
        _output__.writeString(40, __v)
      };
      uninterpretedOption.foreach { __v =>
        _output__.writeTag(999, 2)
        _output__.writeUInt32NoTag(__v.serializedSize)
        __v.writeTo(_output__)
      };
      unknownFields.writeTo(_output__)
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.descriptor.FileOptions = {
      var __javaPackage = this.javaPackage
      var __javaOuterClassname = this.javaOuterClassname
      var __javaMultipleFiles = this.javaMultipleFiles
      var __javaGenerateEqualsAndHash = this.javaGenerateEqualsAndHash
      var __javaStringCheckUtf8 = this.javaStringCheckUtf8
      var __optimizeFor = this.optimizeFor
      var __goPackage = this.goPackage
      var __ccGenericServices = this.ccGenericServices
      var __javaGenericServices = this.javaGenericServices
      var __pyGenericServices = this.pyGenericServices
      var __deprecated = this.deprecated
      var __ccEnableArenas = this.ccEnableArenas
      var __objcClassPrefix = this.objcClassPrefix
      var __csharpNamespace = this.csharpNamespace
      var __swiftPrefix = this.swiftPrefix
      var __phpClassPrefix = this.phpClassPrefix
      val __uninterpretedOption = (_root_.scala.collection.immutable.Vector.newBuilder[com.google.protobuf.descriptor.UninterpretedOption] ++= this.uninterpretedOption)
      val _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder(this.unknownFields)
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __javaPackage = Some(_input__.readString())
          case 66 =>
            __javaOuterClassname = Some(_input__.readString())
          case 80 =>
            __javaMultipleFiles = Some(_input__.readBool())
          case 160 =>
            __javaGenerateEqualsAndHash = Some(_input__.readBool())
          case 216 =>
            __javaStringCheckUtf8 = Some(_input__.readBool())
          case 72 =>
            __optimizeFor = Some(com.google.protobuf.descriptor.FileOptions.OptimizeMode.fromValue(_input__.readEnum()))
          case 90 =>
            __goPackage = Some(_input__.readString())
          case 128 =>
            __ccGenericServices = Some(_input__.readBool())
          case 136 =>
            __javaGenericServices = Some(_input__.readBool())
          case 144 =>
            __pyGenericServices = Some(_input__.readBool())
          case 184 =>
            __deprecated = Some(_input__.readBool())
          case 248 =>
            __ccEnableArenas = Some(_input__.readBool())
          case 290 =>
            __objcClassPrefix = Some(_input__.readString())
          case 298 =>
            __csharpNamespace = Some(_input__.readString())
          case 314 =>
            __swiftPrefix = Some(_input__.readString())
          case 322 =>
            __phpClassPrefix = Some(_input__.readString())
          case 7994 =>
            __uninterpretedOption += _root_.scalapb.LiteParser.readMessage(_input__, com.google.protobuf.descriptor.UninterpretedOption.defaultInstance)
          case tag => _unknownFields__.parseField(tag, _input__)
        }
      }
      com.google.protobuf.descriptor.FileOptions(
          javaPackage = __javaPackage,
          javaOuterClassname = __javaOuterClassname,
          javaMultipleFiles = __javaMultipleFiles,
          javaGenerateEqualsAndHash = __javaGenerateEqualsAndHash,
          javaStringCheckUtf8 = __javaStringCheckUtf8,
          optimizeFor = __optimizeFor,
          goPackage = __goPackage,
          ccGenericServices = __ccGenericServices,
          javaGenericServices = __javaGenericServices,
          pyGenericServices = __pyGenericServices,
          deprecated = __deprecated,
          ccEnableArenas = __ccEnableArenas,
          objcClassPrefix = __objcClassPrefix,
          csharpNamespace = __csharpNamespace,
          swiftPrefix = __swiftPrefix,
          phpClassPrefix = __phpClassPrefix,
          uninterpretedOption = __uninterpretedOption.result(),
          unknownFields = _unknownFields__.result()
      )
    }
    def getJavaPackage: String = javaPackage.getOrElse("")
    def clearJavaPackage: FileOptions = copy(javaPackage = None)
    def withJavaPackage(__v: String): FileOptions = copy(javaPackage = Some(__v))
    def getJavaOuterClassname: String = javaOuterClassname.getOrElse("")
    def clearJavaOuterClassname: FileOptions = copy(javaOuterClassname = None)
    def withJavaOuterClassname(__v: String): FileOptions = copy(javaOuterClassname = Some(__v))
    def getJavaMultipleFiles: Boolean = javaMultipleFiles.getOrElse(false)
    def clearJavaMultipleFiles: FileOptions = copy(javaMultipleFiles = None)
    def withJavaMultipleFiles(__v: Boolean): FileOptions = copy(javaMultipleFiles = Some(__v))
    def getJavaGenerateEqualsAndHash: Boolean = javaGenerateEqualsAndHash.getOrElse(false)
    def clearJavaGenerateEqualsAndHash: FileOptions = copy(javaGenerateEqualsAndHash = None)
    def withJavaGenerateEqualsAndHash(__v: Boolean): FileOptions = copy(javaGenerateEqualsAndHash = Some(__v))
    def getJavaStringCheckUtf8: Boolean = javaStringCheckUtf8.getOrElse(false)
    def clearJavaStringCheckUtf8: FileOptions = copy(javaStringCheckUtf8 = None)
    def withJavaStringCheckUtf8(__v: Boolean): FileOptions = copy(javaStringCheckUtf8 = Some(__v))
    def getOptimizeFor: com.google.protobuf.descriptor.FileOptions.OptimizeMode = optimizeFor.getOrElse(com.google.protobuf.descriptor.FileOptions.OptimizeMode.SPEED)
    def clearOptimizeFor: FileOptions = copy(optimizeFor = None)
    def withOptimizeFor(__v: com.google.protobuf.descriptor.FileOptions.OptimizeMode): FileOptions = copy(optimizeFor = Some(__v))
    def getGoPackage: String = goPackage.getOrElse("")
    def clearGoPackage: FileOptions = copy(goPackage = None)
    def withGoPackage(__v: String): FileOptions = copy(goPackage = Some(__v))
    def getCcGenericServices: Boolean = ccGenericServices.getOrElse(false)
    def clearCcGenericServices: FileOptions = copy(ccGenericServices = None)
    def withCcGenericServices(__v: Boolean): FileOptions = copy(ccGenericServices = Some(__v))
    def getJavaGenericServices: Boolean = javaGenericServices.getOrElse(false)
    def clearJavaGenericServices: FileOptions = copy(javaGenericServices = None)
    def withJavaGenericServices(__v: Boolean): FileOptions = copy(javaGenericServices = Some(__v))
    def getPyGenericServices: Boolean = pyGenericServices.getOrElse(false)
    def clearPyGenericServices: FileOptions = copy(pyGenericServices = None)
    def withPyGenericServices(__v: Boolean): FileOptions = copy(pyGenericServices = Some(__v))
    def getDeprecated: Boolean = deprecated.getOrElse(false)
    def clearDeprecated: FileOptions = copy(deprecated = None)
    def withDeprecated(__v: Boolean): FileOptions = copy(deprecated = Some(__v))
    def getCcEnableArenas: Boolean = ccEnableArenas.getOrElse(false)
    def clearCcEnableArenas: FileOptions = copy(ccEnableArenas = None)
    def withCcEnableArenas(__v: Boolean): FileOptions = copy(ccEnableArenas = Some(__v))
    def getObjcClassPrefix: String = objcClassPrefix.getOrElse("")
    def clearObjcClassPrefix: FileOptions = copy(objcClassPrefix = None)
    def withObjcClassPrefix(__v: String): FileOptions = copy(objcClassPrefix = Some(__v))
    def getCsharpNamespace: String = csharpNamespace.getOrElse("")
    def clearCsharpNamespace: FileOptions = copy(csharpNamespace = None)
    def withCsharpNamespace(__v: String): FileOptions = copy(csharpNamespace = Some(__v))
    def getSwiftPrefix: String = swiftPrefix.getOrElse("")
    def clearSwiftPrefix: FileOptions = copy(swiftPrefix = None)
    def withSwiftPrefix(__v: String): FileOptions = copy(swiftPrefix = Some(__v))
    def getPhpClassPrefix: String = phpClassPrefix.getOrElse("")
    def clearPhpClassPrefix: FileOptions = copy(phpClassPrefix = None)
    def withPhpClassPrefix(__v: String): FileOptions = copy(phpClassPrefix = Some(__v))
    def clearUninterpretedOption = copy(uninterpretedOption = _root_.scala.collection.Seq.empty)
    def addUninterpretedOption(__vs: com.google.protobuf.descriptor.UninterpretedOption*): FileOptions = addAllUninterpretedOption(__vs)
    def addAllUninterpretedOption(__vs: TraversableOnce[com.google.protobuf.descriptor.UninterpretedOption]): FileOptions = copy(uninterpretedOption = uninterpretedOption ++ __vs)
    def withUninterpretedOption(__v: _root_.scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]): FileOptions = copy(uninterpretedOption = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => javaPackage.orNull
        case 8 => javaOuterClassname.orNull
        case 10 => javaMultipleFiles.orNull
        case 20 => javaGenerateEqualsAndHash.orNull
        case 27 => javaStringCheckUtf8.orNull
        case 9 => optimizeFor.map(_.javaValueDescriptor).orNull
        case 11 => goPackage.orNull
        case 16 => ccGenericServices.orNull
        case 17 => javaGenericServices.orNull
        case 18 => pyGenericServices.orNull
        case 23 => deprecated.orNull
        case 31 => ccEnableArenas.orNull
        case 36 => objcClassPrefix.orNull
        case 37 => csharpNamespace.orNull
        case 39 => swiftPrefix.orNull
        case 40 => phpClassPrefix.orNull
        case 999 => uninterpretedOption
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => javaPackage.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 8 => javaOuterClassname.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 10 => javaMultipleFiles.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 20 => javaGenerateEqualsAndHash.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 27 => javaStringCheckUtf8.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 9 => optimizeFor.map(__e => _root_.scalapb.descriptors.PEnum(__e.scalaValueDescriptor)).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 11 => goPackage.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 16 => ccGenericServices.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 17 => javaGenericServices.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 18 => pyGenericServices.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 23 => deprecated.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 31 => ccEnableArenas.map(_root_.scalapb.descriptors.PBoolean).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 36 => objcClassPrefix.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 37 => csharpNamespace.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 39 => swiftPrefix.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 40 => phpClassPrefix.map(_root_.scalapb.descriptors.PString).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 999 => _root_.scalapb.descriptors.PRepeated(uninterpretedOption.map(_.toPMessage)(_root_.scala.collection.breakOut))
      }
    }
    def toProtoString: String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.google.protobuf.descriptor.FileOptions
}

object FileOptions extends scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.FileOptions] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.google.protobuf.descriptor.FileOptions] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.google.protobuf.descriptor.FileOptions = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.google.protobuf.descriptor.FileOptions(
      __fieldsMap.get(__fields.get(0)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(1)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(2)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(3)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(4)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(5)).asInstanceOf[scala.Option[_root_.com.google.protobuf.Descriptors.EnumValueDescriptor]].map(__e => com.google.protobuf.descriptor.FileOptions.OptimizeMode.fromValue(__e.getNumber)),
      __fieldsMap.get(__fields.get(6)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(7)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(8)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(9)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(10)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(11)).asInstanceOf[scala.Option[Boolean]],
      __fieldsMap.get(__fields.get(12)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(13)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(14)).asInstanceOf[scala.Option[String]],
      __fieldsMap.get(__fields.get(15)).asInstanceOf[scala.Option[String]],
      __fieldsMap.getOrElse(__fields.get(16), Nil).asInstanceOf[_root_.scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.descriptor.FileOptions] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.google.protobuf.descriptor.FileOptions(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(8).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(10).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(20).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(27).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(9).get).flatMap(_.as[scala.Option[_root_.scalapb.descriptors.EnumValueDescriptor]]).map(__e => com.google.protobuf.descriptor.FileOptions.OptimizeMode.fromValue(__e.number)),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(11).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(16).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(17).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(18).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(23).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(31).get).flatMap(_.as[scala.Option[Boolean]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(36).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(37).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(39).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(40).get).flatMap(_.as[scala.Option[String]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(999).get).map(_.as[_root_.scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]]).getOrElse(_root_.scala.collection.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = DescriptorProtoCompanion.javaDescriptor.getMessageTypes.get(9)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = DescriptorProtoCompanion.scalaDescriptor.messages(9)
  def messageCompanionForFieldNumber(__number: Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 999 => __out = com.google.protobuf.descriptor.UninterpretedOption
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 9 => com.google.protobuf.descriptor.FileOptions.OptimizeMode
    }
  }
  lazy val defaultInstance = com.google.protobuf.descriptor.FileOptions(
  )
  sealed trait OptimizeMode extends _root_.scalapb.GeneratedEnum {
    type EnumType = OptimizeMode
    def isSpeed: Boolean = false
    def isCodeSize: Boolean = false
    def isLiteRuntime: Boolean = false
    def companion: _root_.scalapb.GeneratedEnumCompanion[OptimizeMode] = com.google.protobuf.descriptor.FileOptions.OptimizeMode
  }
  
  object OptimizeMode extends _root_.scalapb.GeneratedEnumCompanion[OptimizeMode] {
    implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[OptimizeMode] = this
    @SerialVersionUID(0L)
    case object SPEED extends OptimizeMode {
      val value = 1
      val index = 0
      val name = "SPEED"
      override def isSpeed: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object CODE_SIZE extends OptimizeMode {
      val value = 2
      val index = 1
      val name = "CODE_SIZE"
      override def isCodeSize: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object LITE_RUNTIME extends OptimizeMode {
      val value = 3
      val index = 2
      val name = "LITE_RUNTIME"
      override def isLiteRuntime: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case class Unrecognized(value: Int) extends OptimizeMode with _root_.scalapb.UnrecognizedEnum
    
    lazy val values = scala.collection.Seq(SPEED, CODE_SIZE, LITE_RUNTIME)
    def fromValue(value: Int): OptimizeMode = value match {
      case 1 => SPEED
      case 2 => CODE_SIZE
      case 3 => LITE_RUNTIME
      case __other => Unrecognized(__other)
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = com.google.protobuf.descriptor.FileOptions.javaDescriptor.getEnumTypes.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = com.google.protobuf.descriptor.FileOptions.scalaDescriptor.enums(0)
  }
  implicit class FileOptionsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.descriptor.FileOptions]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.google.protobuf.descriptor.FileOptions](_l) {
    def javaPackage: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getJavaPackage)((c_, f_) => c_.copy(javaPackage = Some(f_)))
    def optionalJavaPackage: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.javaPackage)((c_, f_) => c_.copy(javaPackage = f_))
    def javaOuterClassname: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getJavaOuterClassname)((c_, f_) => c_.copy(javaOuterClassname = Some(f_)))
    def optionalJavaOuterClassname: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.javaOuterClassname)((c_, f_) => c_.copy(javaOuterClassname = f_))
    def javaMultipleFiles: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getJavaMultipleFiles)((c_, f_) => c_.copy(javaMultipleFiles = Some(f_)))
    def optionalJavaMultipleFiles: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.javaMultipleFiles)((c_, f_) => c_.copy(javaMultipleFiles = f_))
    def javaGenerateEqualsAndHash: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getJavaGenerateEqualsAndHash)((c_, f_) => c_.copy(javaGenerateEqualsAndHash = Some(f_)))
    def optionalJavaGenerateEqualsAndHash: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.javaGenerateEqualsAndHash)((c_, f_) => c_.copy(javaGenerateEqualsAndHash = f_))
    def javaStringCheckUtf8: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getJavaStringCheckUtf8)((c_, f_) => c_.copy(javaStringCheckUtf8 = Some(f_)))
    def optionalJavaStringCheckUtf8: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.javaStringCheckUtf8)((c_, f_) => c_.copy(javaStringCheckUtf8 = f_))
    def optimizeFor: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.descriptor.FileOptions.OptimizeMode] = field(_.getOptimizeFor)((c_, f_) => c_.copy(optimizeFor = Some(f_)))
    def optionalOptimizeFor: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[com.google.protobuf.descriptor.FileOptions.OptimizeMode]] = field(_.optimizeFor)((c_, f_) => c_.copy(optimizeFor = f_))
    def goPackage: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getGoPackage)((c_, f_) => c_.copy(goPackage = Some(f_)))
    def optionalGoPackage: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.goPackage)((c_, f_) => c_.copy(goPackage = f_))
    def ccGenericServices: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getCcGenericServices)((c_, f_) => c_.copy(ccGenericServices = Some(f_)))
    def optionalCcGenericServices: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.ccGenericServices)((c_, f_) => c_.copy(ccGenericServices = f_))
    def javaGenericServices: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getJavaGenericServices)((c_, f_) => c_.copy(javaGenericServices = Some(f_)))
    def optionalJavaGenericServices: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.javaGenericServices)((c_, f_) => c_.copy(javaGenericServices = f_))
    def pyGenericServices: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getPyGenericServices)((c_, f_) => c_.copy(pyGenericServices = Some(f_)))
    def optionalPyGenericServices: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.pyGenericServices)((c_, f_) => c_.copy(pyGenericServices = f_))
    def deprecated: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getDeprecated)((c_, f_) => c_.copy(deprecated = Some(f_)))
    def optionalDeprecated: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.deprecated)((c_, f_) => c_.copy(deprecated = f_))
    def ccEnableArenas: _root_.scalapb.lenses.Lens[UpperPB, Boolean] = field(_.getCcEnableArenas)((c_, f_) => c_.copy(ccEnableArenas = Some(f_)))
    def optionalCcEnableArenas: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[Boolean]] = field(_.ccEnableArenas)((c_, f_) => c_.copy(ccEnableArenas = f_))
    def objcClassPrefix: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getObjcClassPrefix)((c_, f_) => c_.copy(objcClassPrefix = Some(f_)))
    def optionalObjcClassPrefix: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.objcClassPrefix)((c_, f_) => c_.copy(objcClassPrefix = f_))
    def csharpNamespace: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getCsharpNamespace)((c_, f_) => c_.copy(csharpNamespace = Some(f_)))
    def optionalCsharpNamespace: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.csharpNamespace)((c_, f_) => c_.copy(csharpNamespace = f_))
    def swiftPrefix: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getSwiftPrefix)((c_, f_) => c_.copy(swiftPrefix = Some(f_)))
    def optionalSwiftPrefix: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.swiftPrefix)((c_, f_) => c_.copy(swiftPrefix = f_))
    def phpClassPrefix: _root_.scalapb.lenses.Lens[UpperPB, String] = field(_.getPhpClassPrefix)((c_, f_) => c_.copy(phpClassPrefix = Some(f_)))
    def optionalPhpClassPrefix: _root_.scalapb.lenses.Lens[UpperPB, scala.Option[String]] = field(_.phpClassPrefix)((c_, f_) => c_.copy(phpClassPrefix = f_))
    def uninterpretedOption: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.collection.Seq[com.google.protobuf.descriptor.UninterpretedOption]] = field(_.uninterpretedOption)((c_, f_) => c_.copy(uninterpretedOption = f_))
  }
  final val JAVA_PACKAGE_FIELD_NUMBER = 1
  final val JAVA_OUTER_CLASSNAME_FIELD_NUMBER = 8
  final val JAVA_MULTIPLE_FILES_FIELD_NUMBER = 10
  final val JAVA_GENERATE_EQUALS_AND_HASH_FIELD_NUMBER = 20
  final val JAVA_STRING_CHECK_UTF8_FIELD_NUMBER = 27
  final val OPTIMIZE_FOR_FIELD_NUMBER = 9
  final val GO_PACKAGE_FIELD_NUMBER = 11
  final val CC_GENERIC_SERVICES_FIELD_NUMBER = 16
  final val JAVA_GENERIC_SERVICES_FIELD_NUMBER = 17
  final val PY_GENERIC_SERVICES_FIELD_NUMBER = 18
  final val DEPRECATED_FIELD_NUMBER = 23
  final val CC_ENABLE_ARENAS_FIELD_NUMBER = 31
  final val OBJC_CLASS_PREFIX_FIELD_NUMBER = 36
  final val CSHARP_NAMESPACE_FIELD_NUMBER = 37
  final val SWIFT_PREFIX_FIELD_NUMBER = 39
  final val PHP_CLASS_PREFIX_FIELD_NUMBER = 40
  final val UNINTERPRETED_OPTION_FIELD_NUMBER = 999
}
