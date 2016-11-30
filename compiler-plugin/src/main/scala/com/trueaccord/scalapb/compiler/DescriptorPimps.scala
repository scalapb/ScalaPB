package com.trueaccord.scalapb.compiler

import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, SourceCodeInfo}
import com.google.protobuf.Descriptors._
import com.google.protobuf.WireFormat.FieldType
import com.google.protobuf.descriptor.DescriptorProto
import com.trueaccord.scalapb.Scalapb
import com.trueaccord.scalapb.Scalapb.{FieldOptions, MessageOptions, ScalaPbOptions}

import scala.collection.JavaConverters._
import scala.collection.immutable.IndexedSeq

trait DescriptorPimps {
  def params: GeneratorParams

  val SCALA_RESERVED_WORDS = Set(
    "abstract", "case", "catch", "class", "def",
    "do", "else", "extends", "false", "final",
    "finally", "for", "forSome", "if", "implicit",
    "import", "lazy", "macro", "match", "new", "null",
    "object", "override", "package", "private", "protected",
    "return", "sealed", "super", "this", "throw",
    "trait", "try", "true", "type", "val",
    "var", "while", "with", "yield",
    "ne", "scala")

  implicit class AsSymbolPimp(val s: String) {
    def asSymbol: String = if (SCALA_RESERVED_WORDS.contains(s)) s"`$s`" else s
  }

  protected final def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2) = name.head match {
        case c if c.isLower => (Some(if (capNext) c.toUpper else c), false)
        case c if c.isUpper =>
          // force first letter to lower unless forced to capitalize it.
          (Some(if (index == 0 && !capNext) c.toLower else c), false)
        case c if c.isDigit => (Some(c), true)
        case _ => (None, true)
      }
      r.foreach(b.append)
      inner(name.tail, index + 1, capNext2)
    }
    inner(name, 0, upperInitial)
    b.toString
  }

  protected final def toAllCaps(name: String): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, lastLower: Boolean): Unit = if (name.nonEmpty) {
      val nextLastLower = name.head match {
        case c if c.isLower => b.append(c.toUpper)
          true
        case c if c.isUpper =>
          if (lastLower) { b.append('_') }
          b.append(c)
          false
        case c =>
          b.append(c)
          false
      }
      inner(name.tail, nextLastLower)
    }
    inner(name, false)
    b.toString
  }

  implicit final class MethodDescriptorPimp(self: MethodDescriptor) {
    def scalaOut: String = self.getOutputType.scalaTypeName

    def scalaIn: String = self.getInputType.scalaTypeName

    def isClientStreaming = self.toProto.getClientStreaming

    def isServerStreaming = self.toProto.getServerStreaming

    def streamType: StreamType = {
      (isClientStreaming, isServerStreaming) match {
        case (false, false) => StreamType.Unary
        case (true, false) => StreamType.ClientStreaming
        case (false, true) => StreamType.ServerStreaming
        case (true, true) => StreamType.Bidirectional
      }
    }

    def canBeBlocking = !self.toProto.getClientStreaming

    private def name0: String = snakeCaseToCamelCase(self.getName)

    def name: String = name0.asSymbol

    def descriptorName = s"METHOD_${toAllCaps(self.getName)}"
  }

  implicit final class ServiceDescriptorPimp(self: ServiceDescriptor) {
    def objectName = self.getName + "Grpc"

    def name = self.getName.asSymbol

    def blockingClient = self.getName + "BlockingClient"

    def blockingStub = self.getName + "BlockingStub"

    def stub = self.getName + "Stub"

    def methods = self.getMethods.asScala.toIndexedSeq
  }

  implicit class FieldDescriptorPimp(val fd: FieldDescriptor) {
    def containingOneOf: Option[OneofDescriptor] = Option(fd.getContainingOneof)

    def isInOneof: Boolean = containingOneOf.isDefined

    def scalaName: String = fd.getName match {
      case ("number" | "value") if fd.isInOneof => "_" + fd.getName
      case "serialized_size" => "_serializedSize"
      case "class" => "_class"
      case x => snakeCaseToCamelCase(x)
    }

    def upperScalaName: String = fd.getName match {
      case "serialized_size" => "_SerializedSize"
      case "class" => "_Class"
      case x => snakeCaseToCamelCase(x, true)
    }

    def upperJavaName: String = fd.getName match {
      case "serialized_size" => "SerializedSize_"
      case "class" => "Class_"
      case x => upperScalaName
    }

    def fieldNumberConstantName: String = fd.getName.toUpperCase() + "_FIELD_NUMBER"

    def oneOfTypeName = {
      assert(isInOneof)
      fd.getContainingOneof.scalaTypeName + "." + upperScalaName
    }

    // Is this field boxed inside an Option in Scala. Equivalent, does the Java API
    // support hasX methods for this field.
    def supportsPresence: Boolean =
      fd.isOptional && !fd.isInOneof && (!fd.getFile.isProto3 || fd.isMessage)

    // Is the Scala representation of this field is a singular type.
    def isSingular = fd.isRequired || (
      fd.getFile.isProto3 && !fd.isInOneof && fd.isOptional && !fd.isMessage)

    def isMap = isMessage && fd.isRepeated && fd.getMessageType.isMapEntry

    def mapType: MessageDescriptorPimp#MapType = {
      assert(isMap)
      fd.getMessageType.mapType
    }

    def typeCategory(base: String): String = {
      if (supportsPresence) s"scala.Option[$base]"
      else if (fd.isRepeated) s"${params.collectionType}[$base]"
      else base
    }

    def baseScalaTypeName: String = typeCategory(baseSingleScalaTypeName)

    def scalaTypeName: String = if (fd.isMap)
      fd.mapType.scalaTypeName else
      typeCategory(singleScalaTypeName)

    def fieldOptions: FieldOptions = fd.getOptions.getExtension[FieldOptions](Scalapb.field)

    def customSingleScalaTypeName: Option[String] =
      if (isMap) Some(s"(${mapType.keyType}, ${mapType.valueType})")
      else if (fieldOptions.hasType) Some(fieldOptions.getType)
      else if (isMessage && fd.getFile.scalaOptions.getPrimitiveWrappers) (fd.getMessageType.getFullName match {
        case "google.protobuf.Int32Value" => Some("Int")
        case "google.protobuf.Int64Value" => Some("Long")
        case "google.protobuf.UInt32Value" => Some("Long")
        case "google.protobuf.UInt64Value" => Some("Long")
        case "google.protobuf.DoubleValue" => Some("Double")
        case "google.protobuf.FloatValue" => Some("Float")
        case "google.protobuf.StringValue" => Some("String")
        case "google.protobuf.BoolValue" => Some("Boolean")
        case "google.protobuf.BytesValue" => Some("_root_.com.google.protobuf.ByteString")
        case _ => None
      })
      else None

    def baseSingleScalaTypeName: String = fd.getJavaType match {
      case FieldDescriptor.JavaType.INT => "Int"
      case FieldDescriptor.JavaType.LONG => "Long"
      case FieldDescriptor.JavaType.FLOAT => "Float"
      case FieldDescriptor.JavaType.DOUBLE => "Double"
      case FieldDescriptor.JavaType.BOOLEAN => "Boolean"
      case FieldDescriptor.JavaType.BYTE_STRING => "_root_.com.google.protobuf.ByteString"
      case FieldDescriptor.JavaType.STRING => "String"
      case FieldDescriptor.JavaType.MESSAGE => fd.getMessageType.scalaTypeName
      case FieldDescriptor.JavaType.ENUM => fd.getEnumType.scalaTypeName
    }

    def singleScalaTypeName = customSingleScalaTypeName.getOrElse(baseSingleScalaTypeName)

    def getMethod = "get" + upperScalaName

    def typeMapperValName = "_typemapper_" + scalaName

    def typeMapper = {
      if (!fd.isExtension)
        fd.getContainingType.scalaTypeName + "." + typeMapperValName
      else {
        val c = if (fd.getExtensionScope == null) fd.getFile.fileDescriptorObjectFullName
        else fd.getExtensionScope.scalaTypeName
        c + "." + typeMapperValName
      }
    }

    def isEnum = fd.getType == FieldDescriptor.Type.ENUM

    def isMessage = fd.getType == FieldDescriptor.Type.MESSAGE

    def javaExtensionFieldFullName = {
      require(fd.isExtension)
      val inClass =
        if (fd.getExtensionScope == null) fd.getFile.javaFullOuterClassName
        else fd.getExtensionScope.javaTypeName
      s"$inClass.${fd.scalaName}"
    }

    def sourcePath: Seq[Int] = {
      fd.getContainingType.sourcePath ++ Seq(DescriptorProto.FIELD_FIELD_NUMBER, fd.getIndex)
    }

    def comment: Option[String] = {
      fd.getFile.findLocationByPath(sourcePath)
        .map(t => t.getLeadingComments + t.getTrailingComments)
        .map(Helper.escapeComment)
        .filter(_.nonEmpty)
    }
  }

  implicit class OneofDescriptorPimp(val oneof: OneofDescriptor) {
    def scalaName = snakeCaseToCamelCase(oneof.getName)

    def upperScalaName = snakeCaseToCamelCase(oneof.getName, true)

    def fields: IndexedSeq[FieldDescriptor] = (0 until oneof.getFieldCount).map(oneof.getField).filter(_.getLiteType != FieldType.GROUP)

    def scalaTypeName = oneof.getContainingType.scalaTypeName + "." + upperScalaName

    def empty = scalaTypeName + ".Empty"
  }

  implicit class MessageDescriptorPimp(val message: Descriptor) {
    def fields = message.getFields.asScala.filter(_.getLiteType != FieldType.GROUP)

    def fieldsWithoutOneofs = fields.filterNot(_.isInOneof)

    def parent: Option[Descriptor] = Option(message.getContainingType)

    def scalaName: String = message.getName match {
      case "Option" => "OptionProto"
      case n => n
    }

    lazy val scalaTypeName: String = parent match {
      case Some(p) => p.scalaTypeName + "." + nameSymbol
      case None => message.getFile.scalaPackageName + "." + nameSymbol
    }

    // When the first component of the package name is the same as one of the fields in the
    // current context, we need to disambiguate or we get a compile error.
    def scalaTypeNameWithMaybeRoot(context: Descriptor) = {
      val fullName = scalaTypeName
      val topLevelPackage = fullName.split('.')(0)
      if (context.fields.map(_.scalaName).contains(topLevelPackage))
        s"_root_.$fullName"
      else fullName
    }

    private[compiler] def hasConflictingJavaClassName(className: String): Boolean = (
      (message.getName == className) ||
        (message.getEnumTypes.asScala.exists(_.getName == className)) ||
        (message.nestedTypes.exists(_.hasConflictingJavaClassName(className))))

    def javaTypeName = message.getFile.fullJavaName(message.getFullName)

    def messageOptions: MessageOptions = message.getOptions.getExtension[MessageOptions](Scalapb.message)

    def extendsOption = messageOptions.getExtendsList.asScala.toSeq

    def companionExtendsOption = messageOptions.getCompanionExtendsList.asScala.toSeq

    def nameSymbol = scalaName.asSymbol

    def baseClasses: Seq[String] = {
      val specialMixins = message.getFullName match {
        case "google.protobuf.Any" => Seq("com.trueaccord.scalapb.AnyMethods")
        case _ => Seq()
      }

      Seq("com.trueaccord.scalapb.GeneratedMessage",
        s"com.trueaccord.scalapb.Message[$nameSymbol]",
        s"com.trueaccord.lenses.Updatable[$nameSymbol]") ++ extendsOption ++ specialMixins
    }

    def companionBaseClasses: Seq[String] = {
      val mixins = if (javaConversions)
        Seq(s"com.trueaccord.scalapb.JavaProtoSupport[$scalaTypeName, $javaTypeName]") else Nil

      val specialMixins = message.getFullName match {
        case "google.protobuf.Any" => Seq("com.trueaccord.scalapb.AnyCompanionMethods")
        case _ => Seq()
      }

      Seq(s"com.trueaccord.scalapb.GeneratedMessageCompanion[$scalaTypeName]") ++
        mixins ++
        companionExtendsOption ++
        specialMixins
    }

    def nestedTypes: Seq[Descriptor] = message.getNestedTypes.asScala.toSeq

    def isMapEntry: Boolean = message.getOptions.getMapEntry

    def javaConversions = params.javaConversions && !isMapEntry

    def isTopLevel = message.getContainingType == null

    class MapType {
      def keyField = message.findFieldByName("key")

      def keyType = keyField.singleScalaTypeName

      def valueField = message.findFieldByName("value")

      def valueType = valueField.singleScalaTypeName

      def scalaTypeName = s"scala.collection.immutable.Map[$keyType, $valueType]"

      def pairType = s"($keyType, $valueType)"
    }

    def mapType: MapType = {
      assert(message.isMapEntry)
      new MapType
    }

    def descriptorSource: String = if (message.isTopLevel)
      s"${message.getFile.fileDescriptorObjectName}.descriptor.getMessageTypes.get(${message.getIndex})"
      else s"${message.getContainingType.scalaTypeName}.descriptor.getNestedTypes.get(${message.getIndex})"

    def sourcePath: Seq[Int] = {
      if (message.isTopLevel) Seq(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, message.getIndex)
      else message.getContainingType.sourcePath ++ Seq(DescriptorProto.NESTED_TYPE_FIELD_NUMBER, message.getIndex)
    }

    def comment: Option[String] = {
      message.getFile.findLocationByPath(sourcePath)
        .map(t => t.getLeadingComments + t.getTrailingComments)
        .map(Helper.escapeComment)
        .filter(_.nonEmpty)
    }
  }

  implicit class EnumDescriptorPimp(val enum: EnumDescriptor) {
    def parentMessage: Option[Descriptor] = Option(enum.getContainingType)

    def name: String = enum.getName match {
      case "Option" => "OptionEnum"
      case n => n
    }

    def nameSymbol = name.asSymbol

    lazy val scalaTypeName: String = parentMessage match {
      case Some(p) => p.scalaTypeName + "." + nameSymbol
      case None => enum.getFile.scalaPackageName + "." + nameSymbol
    }

    def isTopLevel = enum.getContainingType == null

    def javaTypeName = enum.getFile.fullJavaName(enum.getFullName)

    def valuesWithNoDuplicates = enum.getValues.asScala.groupBy(_.getNumber)
      .mapValues(_.head).values.toVector.sortBy(_.getNumber)

    def descriptorSource: String = if (enum.isTopLevel)
      s"${enum.getFile.fileDescriptorObjectName}.descriptor.getEnumTypes.get(${enum.getIndex})"
      else s"${enum.getContainingType.scalaTypeName}.descriptor.getEnumTypes.get(${enum.getIndex})"
  }

  implicit class EnumValueDescriptorPimp(val enumValue: EnumValueDescriptor) {
    def isName = {
      if (enumValue.getName.toUpperCase == "UNRECOGNIZED") {
        throw new GeneratorException(
          s"The name '${enumValue.getName}' for an enum value is not allowed due to conflict with the catch-all " +
            "Unrecognized(v: Int) value.")
      }
      Helper.makeUniqueNames(
        enumValue.getType.getValues.asScala.sortBy(v => (v.getNumber, v.getName)).map {
          e => e -> ("is" + allCapsToCamelCase(e.getName, true))
        })(enumValue)
    }
  }

  implicit class FileDescriptorPimp(val file: FileDescriptor) {
    def scalaOptions: ScalaPbOptions = file.getOptions.getExtension[ScalaPbOptions](Scalapb.options)

    def javaPackage: String = {
      if (file.getOptions.hasJavaPackage)
        file.getOptions.getJavaPackage
      else file.getPackage
    }

    def javaPackageAsSymbol: String =
      javaPackage.split('.').map(_.asSymbol).mkString(".")

    private def hasConflictingJavaClassName(className: String) = (
      file.getEnumTypes.asScala.exists(_.getName == className) ||
        file.getServices.asScala.exists(_.getName == className) ||
        file.getMessageTypes.asScala.exists(_.hasConflictingJavaClassName(className)))

    def javaOuterClassName: String =
      if (file.getOptions.hasJavaOuterClassname)
        file.getOptions.getJavaOuterClassname
      else {
        val r = snakeCaseToCamelCase(baseName(file.getName), true)
        if (!hasConflictingJavaClassName(r)) r
        else r + "OuterClass"
      }

    private def isNonFlatDependency = javaPackage == "com.google.protobuf"

    private def scalaPackageParts: Seq[String] = {
      val requestedPackageName: Seq[String] =
        if (scalaOptions.hasPackageName) scalaOptions.getPackageName.split('.')
        else javaPackage.split('.')

      if (scalaOptions.getFlatPackage || (params.flatPackage && !isNonFlatDependency))
        requestedPackageName
      else if (requestedPackageName.nonEmpty) requestedPackageName :+ baseName(file.getName)
      else Seq(baseName(file.getName))
    }

    def scalaPackageName = {
      scalaPackageParts.map(_.asSymbol).mkString(".")
    }

    def scalaDirectory = {
      scalaPackageParts.mkString("/")
    }

    def javaFullOuterClassName = {
      val pkg = javaPackageAsSymbol
      if (pkg.isEmpty) javaOuterClassName
      else pkg + "." + javaOuterClassName
    }

    private def stripPackageName(fullName: String): String =
      if (file.getPackage.isEmpty) fullName
      else {
        assert(fullName.startsWith(file.getPackage + "."))
        fullName.substring(file.getPackage.size + 1)
      }

    def fullJavaName(fullName: String) = {
      val base = if (!file.getOptions.getJavaMultipleFiles)
        (javaFullOuterClassName + ".")
      else {
        val pkg = javaPackageAsSymbol
        if (pkg.isEmpty) "" else (pkg + ".")
      }
      base + stripPackageName(fullName).asSymbol
    }

    def fileDescriptorObjectName = {
      def inner(s: String): String =
        if (!hasConflictingJavaClassName(s)) s else (s + "Companion")

      inner(snakeCaseToCamelCase(baseName(file.getName) + "Proto", upperInitial = true))
    }

    def fileDescriptorObjectFullName = scalaPackageName + "." + fileDescriptorObjectName

    def isProto2 = file.getSyntax == FileDescriptor.Syntax.PROTO2

    def isProto3 = file.getSyntax == FileDescriptor.Syntax.PROTO3

    def findLocationByPath(path: Seq[Int]): Option[SourceCodeInfo.Location] = {
      file.toProto.getSourceCodeInfo.getLocationList.asScala.find(
        _.getPathList.asScala == path)
    }
  }

  private def allCapsToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2) = name.head match {
        case c if c.isUpper =>
          // capitalize according to capNext.
          (Some(if (capNext) c else c.toLower), false)
        case c if c.isLower =>
          // Lower caps never get capitalized, but will force
          // the next letter to be upper case.
          (Some(c), true)
        case c if c.isDigit => (Some(c), true)
        case _ => (None, true)
      }
      r.foreach(b.append)
      inner(name.tail, capNext2)
    }
    inner(name, upperInitial)
    b.toString
  }

  def baseName(fileName: String) =
    fileName.split("/").last.replaceAll(raw"[.]proto$$|[.]protodevel", "")
}

object Helper {
  def makeUniqueNames[T](values: Seq[(T, String)]): Map[T, String] = {
    val newNameMap: Map[String, T] =
      values.foldLeft(Map.empty[String, T]) {
      case (nameMap, (t, name)) =>
        var newName: String = name
        var attempt: Int = 0
        while (nameMap.contains(newName)) {
          attempt += 1
          newName = s"${name}_$attempt"
        }
        nameMap + (newName -> t)
    }
    newNameMap.map(_.swap)
  }

  def escapeComment(s: String): String = {
    s
      .replace("&", "&amp;")
      .replace("/*", "/&#42;")
      .replace("*/", "*&#47;")
      .replace("@", "&#64;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\\", "&92;")
  }
}
