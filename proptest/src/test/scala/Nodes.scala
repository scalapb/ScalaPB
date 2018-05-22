
import scalapb.options.Scalapb.ScalaPbOptions
import scalapb.compiler
import scalapb.compiler.{StreamType, FunctionalPrinter}

import scala.collection.mutable
import scala.util.Try

object Nodes {
  import GenTypes._
  import GraphGen._

  sealed trait ProtoSyntax {
    def syntaxName: String
    def isProto2: Boolean
    def isProto3: Boolean
  }

  case object Proto2 extends ProtoSyntax {
    def syntaxName: String = "proto2"
    def isProto2: Boolean = true
    def isProto3: Boolean = false
  }

  case object Proto3 extends ProtoSyntax {
    def syntaxName: String = "proto3"
    def isProto2: Boolean = false
    def isProto3: Boolean = true
  }

  private def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new mutable.StringBuilder()
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
      r.foreach(b.+=)
      inner(name.tail, index + 1, capNext2)
    }
    inner(name, 0, upperInitial)
    b.toString
  }

  sealed trait Node {
    def allMessages: Stream[MessageNode]
  }

  case class RootNode(files: Seq[FileNode]) {
    def minMessageId = Try(files.flatMap(_.minMessageId).min).toOption

    def maxMessageId = Try(files.flatMap(_.maxMessageId).max).toOption

    def minEnumId = Try(files.flatMap(_.minEnumId).min).toOption

    def maxEnumId = Try(files.flatMap(_.maxEnumId).max).toOption

    def resolveProtoTypeName(t: GenTypes.ProtoType): String = t match {
      case Primitive(name, _, _, _) => name
      case MessageReference(id) => fullMessageNameParts(id).mkString(".")
      case EnumReference(id) => fullEnumNameParts(id).mkString(".")
      case MapType(keyType, valueType) => s"map<${resolveProtoTypeName(keyType)}, ${resolveProtoTypeName(valueType)}>"
    }

    def fullMessageNameParts(id: Int): Seq[String] = {
      val m = messagesById(id)
      m.parentMessageId match {
        case Some(parentId) => fullMessageNameParts(parentId) :+ m.name
        case None => filesById(m.fileId).protoPackage.toSeq :+ m.name
      }
    }

    def fullEnumNameParts(id: Int): Seq[String] = {
      val m = enumsById(id)
      m.parentMessageId match {
        case Some(parentId) => fullMessageNameParts(parentId) :+ m.name
        case None => filesById(m.fileId).protoPackage.toSeq :+ m.name
      }
    }

    def javaClassName(m: MessageNode) = {
      val parts = fullMessageNameParts(m.id)
      val file = filesById(m.fileId)
      val innerClassName = (if (file.protoPackage.isDefined) parts.tail else parts).mkString("$")
      file.javaOuterClassOrPackage match {
        case Right(packageName) =>
          packageName + "." + innerClassName
        case Left(outerClass) =>
          outerClass + "$" + innerClassName
      }
    }

    def scalaObjectName(m: MessageNode) = {
      val file = filesById(m.fileId)
      val parts = fullMessageNameParts(m.id)
      val className = (if (file.protoPackage.isDefined) parts.tail else parts).mkString("$")
      file.scalaPackage + "." + className
    }

    override def toString: String = {
      files.foldLeft(new FunctionalPrinter) { case (fp, f) =>
        fp.add(s"${f.baseFileName}.proto:\n")
          .call(f.print(this, _))
      }
        .result()
    }

    lazy val messagesById: Map[Int, MessageNode] = files.flatMap(_.allMessages).map(m => (m.id, m)).toMap
    lazy val enumsById: Map[Int, EnumNode] = files.flatMap(_.allEnums).map(e => (e.id, e)).toMap
    lazy val filesById: Map[Int, FileNode] = files.map(f => (f.fileId, f)).toMap
  }

  final case class MethodNode(name: String, request: MessageNode, response: MessageNode, streamType: StreamType) {
    def print(printer: FunctionalPrinter): FunctionalPrinter = {
      val method = streamType match {
        case StreamType.Unary =>
          s"rpc $name (${request.name}) returns (${response.name}) {};"
        case StreamType.ClientStreaming =>
          s"rpc $name (stream ${request.name}) returns (${response.name}) {};"
        case StreamType.ServerStreaming =>
          s"rpc $name (${request.name}) returns (stream ${response.name}) {};"
        case StreamType.Bidirectional =>
          s"rpc $name (stream ${request.name}) returns (stream ${response.name}) {};"
      }
      printer.add(method)
    }
  }

  final case class ServiceNode(name: String, methods: Seq[MethodNode]) {
    def print(printer: FunctionalPrinter): FunctionalPrinter =
      printer.add(s"service $name {").indent
       .print(methods)((node, p) => p print node).outdent
       .add("}")
  }

  case class FileNode(baseFileName: String,
                      protoSyntax: ProtoSyntax,
                      protoPackage: Option[String],
                      javaPackage: Option[String],
                      javaMultipleFiles: Boolean,
                      scalaOptions: Option[ScalaPbOptions],
                      messages: Seq[MessageNode],
                      services: Seq[ServiceNode],
                      enums: Seq[EnumNode],
                      fileId: Int) extends Node {
    def allMessages = messages.foldLeft(Stream.empty[MessageNode])(_ ++ _.allMessages)

    def allEnums = messages.foldLeft(enums.toStream)(_ ++ _.allEnums)

    lazy val minMessageId = Try(allMessages.map(_.id).min).toOption
    lazy val maxMessageId = Try(allMessages.map(_.id).max).toOption
    lazy val minEnumId = Try(allEnums.map(_.id).min).toOption
    lazy val maxEnumId = Try(allEnums.map(_.id).max).toOption

    def fileReferences(rootNode: RootNode): Set[String] = (for {
      message <- allMessages
      field <- message.fields
    } yield field.fieldType).collect({
      case MessageReference(id) => rootNode.messagesById(id).fileId
      case EnumReference(id) => rootNode.enumsById(id).fileId
      case MapType(keyType, EnumReference(id)) => rootNode.enumsById(id).fileId
      case MapType(keyType, MessageReference(id)) => rootNode.messagesById(id).fileId
    }).toSet.map(rootNode.filesById).map(_.baseFileName)

    def print(rootNode: RootNode, printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter =
      printer.add(s"// File id $fileId. messages: $minMessageId-$maxMessageId. Enums: $minEnumId-$maxEnumId")
        .add(s"""syntax = "${protoSyntax.syntaxName}";""")
        .print(protoPackage)((p, pkg) => p.add(s"package $pkg;"))
        .print(javaPackage)((p, pkg) => p.add(s"""option java_package = "$pkg";"""))
        .when(javaMultipleFiles)(_.add("option java_multiple_files = true;"))
        .print(scalaOptions)((p, options) =>
        p.add("""import "scalapb/scalapb.proto";""")
          .add("option (scalapb.options) = {")
          .indent
          .when(options.hasPackageName)(_.add(s"""package_name: "${options.getPackageName}""""))
          .add(s"flat_package: ${options.getFlatPackage}")
          .outdent
          .add("};"))
        .add(fileReferences(rootNode).collect({
        case f if f != baseFileName => s"""import "${f}.proto";"""
      }).toSeq: _*)
        .print(enums)((`enum`, p) => p.print(`enum`))
        .print(messages)((message, p) => p.print(rootNode, this, message))
        .print(services)((service, p) => p.print(service))

    /**
     * @return
     * Right(package name) if `java_multiple_files` option is true
     * Left(outer class name) if `java_multiple_files` option is false
     */
    def javaOuterClassOrPackage: Either[String, String] = {
      val pkg = javaPackage.orElse(protoPackage).toSeq
      if(javaMultipleFiles) {
        Right(pkg mkString ".")
      } else {
        Left((pkg :+ snakeCaseToCamelCase(baseFileName, upperInitial = true)) mkString ".")
      }
    }

    def scalaPackage = {
      val scalaPackageName = scalaOptions.flatMap(options =>
        if (options.hasPackageName) Some(options.getPackageName) else None)
      val requestedPackageName = scalaPackageName.orElse(javaPackage).orElse(protoPackage)
      val flatPackage = scalaOptions.exists(_.getFlatPackage)
      if (flatPackage) requestedPackageName.getOrElse("")
      else requestedPackageName.fold(baseFileName)(_ + "." + baseFileName)
   }
  }

  case class MessageNode(id: Int,
                         name: String,
                         messages: Seq[MessageNode],
                         enums: Seq[EnumNode],
                         fields: Seq[FieldNode],
                         parentMessageId: Option[Int],
                         fileId: Int) extends Node {
    def allMessages: Stream[MessageNode] = messages.foldLeft(Stream(this))(_ ++ _.allMessages)

    def allEnums: Stream[EnumNode] = messages.foldLeft(enums.toStream)(_ ++ _.allEnums)

    def print(rootNode: RootNode, fileNode: FileNode, printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter = {
      sealed trait FieldLine
      case class OneofOpener(name: String) extends FieldLine
      case class Field(field: FieldNode) extends FieldLine
      case class OneofCloser(name: String) extends FieldLine

      def makeList(fields: Seq[FieldNode]): Seq[FieldLine] = {
        def makeList0(fields: Seq[FieldNode], prevGroup: Option[String]): Seq[FieldLine] =
          if (fields.isEmpty) Seq.empty
          else ((fields.head.oneOfGroup, prevGroup) match {
            case (OneofContainer(name), Some(otherName)) if (name != otherName) =>
              Seq(OneofCloser(otherName), OneofOpener(name))
            case (OneofContainer(name), None) =>
              Seq(OneofOpener(name))
            case (NotInOneof, Some(otherName)) =>
              Seq(OneofCloser(otherName))
            case _ => Nil
          }) ++ Seq(Field(fields.head)) ++
            makeList0(fields.tail, if (fields.head.oneOfGroup.isOneof) Some(fields.head.oneOfGroup.name) else None)

        val l = makeList0(fields, None)
        fields.lastOption.map(_.oneOfGroup) match {
          case Some(OneofContainer(name)) => l :+ OneofCloser(name)
          case _ => l
        }
      }

      printer
        .add(s"message $name {  // message $id")
        .indent
        .print(enums)((`enum`, p) => p.print(`enum`))
        .print(messages)((message, p) => p.print(rootNode, fileNode, message))
        .print(makeList(fields)) {
        case (printer, OneofOpener(name)) =>
          printer
            .add(s"oneof $name {")
            .indent
        case (printer, OneofCloser(name)) =>
          printer
            .outdent
            .add(s"}  // oneof $name")
        case (printer, Field(field)) =>
          field.print(rootNode, fileNode, printer)
      }
        .outdent
        .add("}\n")
    }
  }

  case class FieldNode(name: String,
                       fieldType: GenTypes.ProtoType,
                       fieldOptions: GenTypes.FieldOptions,
                       oneOfGroup: OneOfGrouping,
                       tag: Int) {
    def print(rootNode: RootNode, fileNode: FileNode, printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter =
      if (!oneOfGroup.isOneof) {
      val packed = if (fieldOptions.isPacked) " [packed = true]" else ""
      val modifier = if (fileNode.protoSyntax.isProto2) {
        if (fieldType.isMap) "" else fieldOptions.modifier + " "
      } else {
        assert(fieldOptions.modifier == FieldModifier.OPTIONAL || fieldOptions.modifier == FieldModifier.REPEATED)
        if (fieldOptions.modifier == FieldModifier.OPTIONAL || fieldType.isMap) ""
        else if (fieldOptions.modifier == FieldModifier.REPEATED) "repeated "
        else throw new RuntimeException("Unexpected modifier")
      }
      printer.add(s"${modifier}${rootNode.resolveProtoTypeName(fieldType)} $name = $tag$packed;  // $fieldType")
    } else {
      printer.add(s"${rootNode.resolveProtoTypeName(fieldType)} $name = $tag;  // $fieldType")
    }
  }

  case class EnumNode(id: Int, name: String, values: Seq[(String, Int)], parentMessageId: Option[Int], fileId: Int) {
    def print(printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter = {
      printer.add(s"enum $name {  // enum $id")
        .indent
        .add(values.map { case (s, v) => s"$s = $v;"}: _*)
        .outdent
        .add("}")
    }
  }
}

