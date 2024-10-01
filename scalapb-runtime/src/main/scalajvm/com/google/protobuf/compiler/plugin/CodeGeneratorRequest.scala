// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!

package com.google.protobuf.compiler.plugin
import _root_.scalapb.internal.compat.JavaConverters._

/** An encoded CodeGeneratorRequest is written to the plugin's stdin.
  *
  * @param fileToGenerate
  *   The .proto files that were explicitly listed on the command-line.  The
  *   code generator should generate code only for these files.  Each file's
  *   descriptor will be included in proto_file, below.
  * @param parameter
  *   The generator parameter passed on the command-line.
  * @param protoFile
  *   FileDescriptorProtos for all files in files_to_generate and everything
  *   they import.  The files will appear in topological order, so each file
  *   appears before any file that imports it.
  *  
  *   Note: the files listed in files_to_generate will include runtime-retention
  *   options only, but all other files will include source-retention options.
  *   The source_file_descriptors field below is available in case you need
  *   source-retention options for files_to_generate.
  *  
  *   protoc guarantees that all proto_files will be written after
  *   the fields above, even though this is not technically guaranteed by the
  *   protobuf wire format.  This theoretically could allow a plugin to stream
  *   in the FileDescriptorProtos and handle them one by one rather than read
  *   the entire set into memory at once.  However, as of this writing, this
  *   is not similarly optimized on protoc's end -- it will store all fields in
  *   memory at once before sending them to the plugin.
  *  
  *   Type names of fields and extensions in the FileDescriptorProto are always
  *   fully qualified.
  * @param sourceFileDescriptors
  *   File descriptors with all options, including source-retention options.
  *   These descriptors are only provided for the files listed in
  *   files_to_generate.
  * @param compilerVersion
  *   The version number of protocol compiler.
  */
@SerialVersionUID(0L)
final case class CodeGeneratorRequest(
    fileToGenerate: _root_.scala.Seq[_root_.scala.Predef.String] = _root_.scala.Seq.empty,
    parameter: _root_.scala.Option[_root_.scala.Predef.String] = _root_.scala.None,
    protoFile: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto] = _root_.scala.Seq.empty,
    sourceFileDescriptors: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto] = _root_.scala.Seq.empty,
    compilerVersion: _root_.scala.Option[com.google.protobuf.compiler.plugin.Version] = _root_.scala.None,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[CodeGeneratorRequest] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      fileToGenerate.foreach { __item =>
        val __value = __item
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(1, __value)
      }
      if (parameter.isDefined) {
        val __value = parameter.get
        __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
      };
      protoFile.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      sourceFileDescriptors.foreach { __item =>
        val __value = __item
        __size += 2 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
      if (compilerVersion.isDefined) {
        val __value = compilerVersion.get
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      };
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
      fileToGenerate.foreach { __v =>
        val __m = __v
        _output__.writeString(1, __m)
      };
      parameter.foreach { __v =>
        val __m = __v
        _output__.writeString(2, __m)
      };
      compilerVersion.foreach { __v =>
        val __m = __v
        _output__.writeTag(3, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      protoFile.foreach { __v =>
        val __m = __v
        _output__.writeTag(15, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      sourceFileDescriptors.foreach { __v =>
        val __m = __v
        _output__.writeTag(17, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      unknownFields.writeTo(_output__)
    }
    def clearFileToGenerate = copy(fileToGenerate = _root_.scala.Seq.empty)
    def addFileToGenerate(__vs: _root_.scala.Predef.String *): CodeGeneratorRequest = addAllFileToGenerate(__vs)
    def addAllFileToGenerate(__vs: Iterable[_root_.scala.Predef.String]): CodeGeneratorRequest = copy(fileToGenerate = fileToGenerate ++ __vs)
    def withFileToGenerate(__v: _root_.scala.Seq[_root_.scala.Predef.String]): CodeGeneratorRequest = copy(fileToGenerate = __v)
    def getParameter: _root_.scala.Predef.String = parameter.getOrElse("")
    def clearParameter: CodeGeneratorRequest = copy(parameter = _root_.scala.None)
    def withParameter(__v: _root_.scala.Predef.String): CodeGeneratorRequest = copy(parameter = _root_.scala.Option(__v))
    def clearProtoFile = copy(protoFile = _root_.scala.Seq.empty)
    def addProtoFile(__vs: com.google.protobuf.descriptor.FileDescriptorProto *): CodeGeneratorRequest = addAllProtoFile(__vs)
    def addAllProtoFile(__vs: Iterable[com.google.protobuf.descriptor.FileDescriptorProto]): CodeGeneratorRequest = copy(protoFile = protoFile ++ __vs)
    def withProtoFile(__v: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]): CodeGeneratorRequest = copy(protoFile = __v)
    def clearSourceFileDescriptors = copy(sourceFileDescriptors = _root_.scala.Seq.empty)
    def addSourceFileDescriptors(__vs: com.google.protobuf.descriptor.FileDescriptorProto *): CodeGeneratorRequest = addAllSourceFileDescriptors(__vs)
    def addAllSourceFileDescriptors(__vs: Iterable[com.google.protobuf.descriptor.FileDescriptorProto]): CodeGeneratorRequest = copy(sourceFileDescriptors = sourceFileDescriptors ++ __vs)
    def withSourceFileDescriptors(__v: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]): CodeGeneratorRequest = copy(sourceFileDescriptors = __v)
    def getCompilerVersion: com.google.protobuf.compiler.plugin.Version = compilerVersion.getOrElse(com.google.protobuf.compiler.plugin.Version.defaultInstance)
    def clearCompilerVersion: CodeGeneratorRequest = copy(compilerVersion = _root_.scala.None)
    def withCompilerVersion(__v: com.google.protobuf.compiler.plugin.Version): CodeGeneratorRequest = copy(compilerVersion = _root_.scala.Option(__v))
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => fileToGenerate
        case 2 => parameter.orNull
        case 15 => protoFile
        case 17 => sourceFileDescriptors
        case 3 => compilerVersion.orNull
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(fileToGenerate.iterator.map(_root_.scalapb.descriptors.PString(_)).toVector)
        case 2 => parameter.map(_root_.scalapb.descriptors.PString(_)).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 15 => _root_.scalapb.descriptors.PRepeated(protoFile.iterator.map(_.toPMessage).toVector)
        case 17 => _root_.scalapb.descriptors.PRepeated(sourceFileDescriptors.iterator.map(_.toPMessage).toVector)
        case 3 => compilerVersion.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion: com.google.protobuf.compiler.plugin.CodeGeneratorRequest.type = com.google.protobuf.compiler.plugin.CodeGeneratorRequest
    // @@protoc_insertion_point(GeneratedMessage[google.protobuf.compiler.CodeGeneratorRequest])
}

object CodeGeneratorRequest extends scalapb.GeneratedMessageCompanion[com.google.protobuf.compiler.plugin.CodeGeneratorRequest] with scalapb.JavaProtoSupport[com.google.protobuf.compiler.plugin.CodeGeneratorRequest, com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.google.protobuf.compiler.plugin.CodeGeneratorRequest] with scalapb.JavaProtoSupport[com.google.protobuf.compiler.plugin.CodeGeneratorRequest, com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest] = this
  def toJavaProto(scalaPbSource: com.google.protobuf.compiler.plugin.CodeGeneratorRequest): com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest = {
    val javaPbOut = com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest.newBuilder
    javaPbOut.addAllFileToGenerate(scalaPbSource.fileToGenerate.asJava)
    scalaPbSource.parameter.foreach(javaPbOut.setParameter)
    javaPbOut.addAllProtoFile(_root_.scalapb.internal.compat.toIterable(scalaPbSource.protoFile.iterator.map(com.google.protobuf.descriptor.FileDescriptorProto.toJavaProto(_))).asJava)
    javaPbOut.addAllSourceFileDescriptors(_root_.scalapb.internal.compat.toIterable(scalaPbSource.sourceFileDescriptors.iterator.map(com.google.protobuf.descriptor.FileDescriptorProto.toJavaProto(_))).asJava)
    scalaPbSource.compilerVersion.map(com.google.protobuf.compiler.plugin.Version.toJavaProto(_)).foreach(javaPbOut.setCompilerVersion)
    javaPbOut.build
  }
  def fromJavaProto(javaPbSource: com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest): com.google.protobuf.compiler.plugin.CodeGeneratorRequest = com.google.protobuf.compiler.plugin.CodeGeneratorRequest(
    fileToGenerate = javaPbSource.getFileToGenerateList.asScala.iterator.map(_root_.scala.Predef.identity).toSeq,
    parameter = if (javaPbSource.hasParameter) Some(javaPbSource.getParameter) else _root_.scala.None,
    protoFile = javaPbSource.getProtoFileList.asScala.iterator.map(com.google.protobuf.descriptor.FileDescriptorProto.fromJavaProto(_)).toSeq,
    sourceFileDescriptors = javaPbSource.getSourceFileDescriptorsList.asScala.iterator.map(com.google.protobuf.descriptor.FileDescriptorProto.fromJavaProto(_)).toSeq,
    compilerVersion = if (javaPbSource.hasCompilerVersion) Some(com.google.protobuf.compiler.plugin.Version.fromJavaProto(javaPbSource.getCompilerVersion)) else _root_.scala.None
  )
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.google.protobuf.compiler.plugin.CodeGeneratorRequest = {
    val __fileToGenerate: _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String] = new _root_.scala.collection.immutable.VectorBuilder[_root_.scala.Predef.String]
    var __parameter: _root_.scala.Option[_root_.scala.Predef.String] = _root_.scala.None
    val __protoFile: _root_.scala.collection.immutable.VectorBuilder[com.google.protobuf.descriptor.FileDescriptorProto] = new _root_.scala.collection.immutable.VectorBuilder[com.google.protobuf.descriptor.FileDescriptorProto]
    val __sourceFileDescriptors: _root_.scala.collection.immutable.VectorBuilder[com.google.protobuf.descriptor.FileDescriptorProto] = new _root_.scala.collection.immutable.VectorBuilder[com.google.protobuf.descriptor.FileDescriptorProto]
    var __compilerVersion: _root_.scala.Option[com.google.protobuf.compiler.plugin.Version] = _root_.scala.None
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 10 =>
          __fileToGenerate += _input__.readStringRequireUtf8()
        case 18 =>
          __parameter = _root_.scala.Option(_input__.readStringRequireUtf8())
        case 122 =>
          __protoFile += _root_.scalapb.LiteParser.readMessage[com.google.protobuf.descriptor.FileDescriptorProto](_input__)
        case 138 =>
          __sourceFileDescriptors += _root_.scalapb.LiteParser.readMessage[com.google.protobuf.descriptor.FileDescriptorProto](_input__)
        case 26 =>
          __compilerVersion = _root_.scala.Option(__compilerVersion.fold(_root_.scalapb.LiteParser.readMessage[com.google.protobuf.compiler.plugin.Version](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    com.google.protobuf.compiler.plugin.CodeGeneratorRequest(
        fileToGenerate = __fileToGenerate.result(),
        parameter = __parameter,
        protoFile = __protoFile.result(),
        sourceFileDescriptors = __sourceFileDescriptors.result(),
        compilerVersion = __compilerVersion,
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.google.protobuf.compiler.plugin.CodeGeneratorRequest] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      com.google.protobuf.compiler.plugin.CodeGeneratorRequest(
        fileToGenerate = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[_root_.scala.Predef.String]]).getOrElse(_root_.scala.Seq.empty),
        parameter = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).flatMap(_.as[_root_.scala.Option[_root_.scala.Predef.String]]),
        protoFile = __fieldsMap.get(scalaDescriptor.findFieldByNumber(15).get).map(_.as[_root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]]).getOrElse(_root_.scala.Seq.empty),
        sourceFileDescriptors = __fieldsMap.get(scalaDescriptor.findFieldByNumber(17).get).map(_.as[_root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]]).getOrElse(_root_.scala.Seq.empty),
        compilerVersion = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).flatMap(_.as[_root_.scala.Option[com.google.protobuf.compiler.plugin.Version]])
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = com.google.protobuf.compiler.plugin.PluginProto.javaDescriptor.getMessageTypes().get(1)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = com.google.protobuf.compiler.plugin.PluginProto.scalaDescriptor.messages(1)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 15 => __out = com.google.protobuf.descriptor.FileDescriptorProto
      case 17 => __out = com.google.protobuf.descriptor.FileDescriptorProto
      case 3 => __out = com.google.protobuf.compiler.plugin.Version
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.google.protobuf.compiler.plugin.CodeGeneratorRequest(
    fileToGenerate = _root_.scala.Seq.empty,
    parameter = _root_.scala.None,
    protoFile = _root_.scala.Seq.empty,
    sourceFileDescriptors = _root_.scala.Seq.empty,
    compilerVersion = _root_.scala.None
  )
  implicit class CodeGeneratorRequestLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.compiler.plugin.CodeGeneratorRequest]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.google.protobuf.compiler.plugin.CodeGeneratorRequest](_l) {
    def fileToGenerate: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Predef.String]] = field(_.fileToGenerate)((c_, f_) => c_.copy(fileToGenerate = f_))
    def parameter: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.getParameter)((c_, f_) => c_.copy(parameter = _root_.scala.Option(f_)))
    def optionalParameter: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[_root_.scala.Predef.String]] = field(_.parameter)((c_, f_) => c_.copy(parameter = f_))
    def protoFile: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]] = field(_.protoFile)((c_, f_) => c_.copy(protoFile = f_))
    def sourceFileDescriptors: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto]] = field(_.sourceFileDescriptors)((c_, f_) => c_.copy(sourceFileDescriptors = f_))
    def compilerVersion: _root_.scalapb.lenses.Lens[UpperPB, com.google.protobuf.compiler.plugin.Version] = field(_.getCompilerVersion)((c_, f_) => c_.copy(compilerVersion = _root_.scala.Option(f_)))
    def optionalCompilerVersion: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[com.google.protobuf.compiler.plugin.Version]] = field(_.compilerVersion)((c_, f_) => c_.copy(compilerVersion = f_))
  }
  final val FILE_TO_GENERATE_FIELD_NUMBER = 1
  final val PARAMETER_FIELD_NUMBER = 2
  final val PROTO_FILE_FIELD_NUMBER = 15
  final val SOURCE_FILE_DESCRIPTORS_FIELD_NUMBER = 17
  final val COMPILER_VERSION_FIELD_NUMBER = 3
  def of(
    fileToGenerate: _root_.scala.Seq[_root_.scala.Predef.String],
    parameter: _root_.scala.Option[_root_.scala.Predef.String],
    protoFile: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto],
    sourceFileDescriptors: _root_.scala.Seq[com.google.protobuf.descriptor.FileDescriptorProto],
    compilerVersion: _root_.scala.Option[com.google.protobuf.compiler.plugin.Version]
  ): _root_.com.google.protobuf.compiler.plugin.CodeGeneratorRequest = _root_.com.google.protobuf.compiler.plugin.CodeGeneratorRequest(
    fileToGenerate,
    parameter,
    protoFile,
    sourceFileDescriptors,
    compilerVersion
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[google.protobuf.compiler.CodeGeneratorRequest])
}
