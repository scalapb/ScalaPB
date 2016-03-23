package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors._
import com.google.protobuf.WireFormat.FieldType
import com.trueaccord.scalapb.Scalapb
import com.trueaccord.scalapb.Scalapb.{FieldOptions, MessageOptions, ScalaPbOptions}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
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

  protected final def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false, prevDigit: Boolean=false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2, prevDigit2) = name.head match {
        case c if c.isLower => (Some(if (capNext) c.toUpper else c), false, false)
        case c if c.isUpper =>
          // force first letter to lower unless forced to capitalize it.
          (Some(if (index == 0 && !capNext) c.toLower else c), false, false)
        case c if c.isDigit => (Some(c), true, true)
        case '_' if prevDigit => name.tail.headOption match {
          case None => (None, true, prevDigit)
          case Some(c) if c.isDigit => (Some('_'), false, false)
          case _ =>  (None, true, prevDigit)
        }
        case _ => (None, true, false)
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

    def methods = self.getMethods.toIndexedSeq
  }

  implicit class FieldDescriptorPimp(val fd: FieldDescriptor) {
    def containingOneOf: Option[OneofDescriptor] = Option(fd.getContainingOneof)

    def isInOneof: Boolean = containingOneOf.isDefined

    def scalaName: String = snakeCaseToCamelCase(fd.getName)

    def upperScalaName: String = snakeCaseToCamelCase(fd.getName, true)

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
      if (supportsPresence) s"Option[$base]"
      else if (fd.isRepeated) s"Seq[$base]"
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
      else None

    def baseSingleScalaTypeName: String = fd.getJavaType match {
      case FieldDescriptor.JavaType.INT => "Int"
      case FieldDescriptor.JavaType.LONG => "Long"
      case FieldDescriptor.JavaType.FLOAT => "Float"
      case FieldDescriptor.JavaType.DOUBLE => "Double"
      case FieldDescriptor.JavaType.BOOLEAN => "Boolean"
      case FieldDescriptor.JavaType.BYTE_STRING => "com.google.protobuf.ByteString"
      case FieldDescriptor.JavaType.STRING => "String"
      case FieldDescriptor.JavaType.MESSAGE => fd.getMessageType.scalaTypeName
      case FieldDescriptor.JavaType.ENUM => fd.getEnumType.scalaTypeName
    }

    def singleScalaTypeName = customSingleScalaTypeName.getOrElse(baseSingleScalaTypeName)

    def getMethod = "get" + upperScalaName

    def typeMapperValName = "_typemapper_" + scalaName

    def typeMapper = fd.getContainingType.scalaTypeName + "." + typeMapperValName

    def isEnum = fd.getType == FieldDescriptor.Type.ENUM

    def isMessage = fd.getType == FieldDescriptor.Type.MESSAGE
  }

  implicit class OneofDescriptorPimp(val oneof: OneofDescriptor) {
    def scalaName = snakeCaseToCamelCase(oneof.getName)

    def upperScalaName = snakeCaseToCamelCase(oneof.getName, true)

    def fields: IndexedSeq[FieldDescriptor] = (0 until oneof.getFieldCount).map(oneof.getField).filter(_.getLiteType != FieldType.GROUP)

    def scalaTypeName = oneof.getContainingType.scalaTypeName + "." + upperScalaName

    def empty = scalaTypeName + ".Empty"
  }

  implicit class MessageDescriptorPimp(val message: Descriptor) {
    def fields = message.getFields.filter(_.getLiteType != FieldType.GROUP)

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

    private[compiler] def hasConflictingJavaClassName(className: String): Boolean = (
      (message.getName == className) ||
        (message.getEnumTypes.exists(_.getName == className)) ||
        (message.nestedTypes.exists(_.hasConflictingJavaClassName(className))))

    def javaTypeName = message.getFile.fullJavaName(message.getFullName)

    def messageOptions: MessageOptions = message.getOptions.getExtension[MessageOptions](Scalapb.message)

    def extendsOption = messageOptions.getExtendsList.toSeq

    def nameSymbol = scalaName.asSymbol

    def baseClasses: Seq[String] =
      Seq("com.trueaccord.scalapb.GeneratedMessage",
        s"com.trueaccord.scalapb.Message[$nameSymbol]",
        s"com.trueaccord.lenses.Updatable[$nameSymbol]") ++ extendsOption

    def nestedTypes: Seq[Descriptor] = message.getNestedTypes.toSeq

    def isMapEntry: Boolean = message.getOptions.getMapEntry

    def javaConversions = params.javaConversions && !isMapEntry

    def isTopLevel = message.getContainingType == null

    class MapType {
      def keyField = message.findFieldByName("key")

      def keyType = keyField.singleScalaTypeName

      def valueField = message.findFieldByName("value")

      def valueType = valueField.singleScalaTypeName

      def scalaTypeName = s"Map[$keyType, $valueType]"

      def pairType = s"($keyType, $valueType)"
    }

    def mapType: MapType = {
      assert(message.isMapEntry)
      new MapType
    }

    def descriptorSource: String = if (message.isTopLevel)
      s"${message.getFile.fileDescriptorObjectName}.descriptor.getMessageTypes.get(${message.getIndex})"
      else s"${message.getContainingType.scalaTypeName}.descriptor.getNestedTypes.get(${message.getIndex})"
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

    def valuesWithNoDuplicates = enum.getValues.groupBy(_.getNumber)
      .mapValues(_.head).values.toVector.sortBy(_.getNumber)

    def descriptorSource: String = if (enum.isTopLevel)
      s"${enum.getFile.fileDescriptorObjectName}.descriptor.getEnumTypes.get(${enum.getIndex})"
      else s"${enum.getContainingType.scalaTypeName}.descriptor.getEnumTypes.get(${enum.getIndex})"
  }

  implicit class EnumValueDescriptorPimp(val enumValue: EnumValueDescriptor) {
    def objectName = allCapsToCamelCase(enumValue.getName, true)
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
      file.getEnumTypes.exists(_.getName == className) ||
        file.getServices.exists(_.getName == className) ||
        file.getMessageTypes.exists(_.hasConflictingJavaClassName(className)))

    def javaOuterClassName: String =
      if (file.getOptions.hasJavaOuterClassname)
        file.getOptions.getJavaOuterClassname
      else {
        val r = snakeCaseToCamelCase(baseName(file.getName), true)
        if (!hasConflictingJavaClassName(r)) r
        else r + "OuterClass"
      }

    def scalaPackageName = {
      val requestedPackageName =
        if (scalaOptions.hasPackageName) scalaOptions.getPackageName
        else javaPackageAsSymbol

      if (scalaOptions.getFlatPackage || params.flatPackage)
        requestedPackageName
      else if (requestedPackageName.nonEmpty) requestedPackageName + "." + baseName(file.getName).asSymbol
      else baseName(file.getName).asSymbol
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
      @tailrec
      def inner(s: String): String =
        if (!hasConflictingJavaClassName(s)) s else inner("_" + s)

      inner(snakeCaseToCamelCase(file.getName, upperInitial = true))
    }

    def fileDescriptorObjectFullName = scalaPackageName + "." + fileDescriptorObjectName

    def isProto2 = file.getSyntax == FileDescriptor.Syntax.PROTO2

    def isProto3 = file.getSyntax == FileDescriptor.Syntax.PROTO3
  }

  private def allCapsToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, capNext: Boolean, prevDigit: Boolean=false): Unit = if (name.nonEmpty) {
      val (r, capNext2, prevDigit2) = name.head match {
        case c if c.isUpper =>
          // capitalize according to capNext.
          (Some(if (capNext) c else c.toLower), false, false)
        case c if c.isLower =>
          // Lower caps never get capitalized, but will force
          // the next letter to be upper case.
          (Some(c), true, false)
        case c if c.isDigit => (Some(c), true, true)
        case '_' if prevDigit => name.tail.headOption match {
          case None => (None, true, prevDigit)
          case Some(c) if c.isDigit => (Some('_'), false, false)
          case _ =>  (None, true, prevDigit)
        }
        case _ => (None, true, false)
      }
      r.foreach(b.append)
      inner(name.tail, capNext2, prevDigit2)
    }
    inner(name, upperInitial)
    b.toString
  }

  def baseName(fileName: String) =
    fileName.split("/").last.replaceAll(raw"[.]proto$$|[.]protodevel", "")

}
