
import com.trueaccord.scalapb.compiler
import com.trueaccord.scalapb.compiler.FPrintable

import scala.collection.mutable
import scala.util.Try

object Nodes {
  import GenTypes._

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
      r.foreach(b +=)
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
      case Primitive(name, _) => name
      case MessageReference(id) => fullMessageNameParts(id).mkString(".")
      case EnumReference(id) => fullEnumNameParts(id).mkString(".")
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
      file.javaOuterClass + "$" + (if (file.protoPackage.isDefined) parts.tail else parts).mkString("$")
    }

    def scalaObjectName(m: MessageNode) = {
      val parts = fullMessageNameParts(m.id)
      val file = filesById(m.fileId)
      file.javaOuterClass + "PB$" + (if (file.protoPackage.isDefined) parts.tail else parts).mkString("$")
    }

    lazy val messagesById: Map[Int, MessageNode] = files.flatMap(_.allMessages).map(m => (m.id, m)).toMap
    lazy val enumsById: Map[Int, EnumNode] = files.flatMap(_.allEnums).map(e => (e.id, e)).toMap
    lazy val filesById: Map[Int, FileNode] = files.map(f => (f.fileId, f)).toMap
  }

  case class FileNode(baseFileName: String,
                      protoPackage: Option[String],
                      javaPackage: Option[String],
                      messages: Seq[MessageNode],
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
    }).toSet.map(rootNode.filesById).map(_.baseFileName)

    def print(rootNode: RootNode, printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter = {
      val p0 = printer.add(s"// File id $fileId. messages: $minMessageId-$maxMessageId. Enums: $minEnumId-$maxEnumId")
      val p1 = protoPackage.fold(p0)(pkg => p0.add(s"package $pkg;"))
      val p2 = javaPackage.fold(p1)(pkg => p1.add(s"""option java_package = "$pkg";"""))
      p2.add(fileReferences(rootNode).collect({
          case f if f != baseFileName => s"""import "${f}.proto";"""
      }).toSeq: _*)
        .printAll(enums)
        .print(messages)(_.print(rootNode, _))
    }

    def javaOuterClass = (javaPackage.orElse(protoPackage).toSeq :+ snakeCaseToCamelCase(baseFileName, upperInitial = true)) mkString "."
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

    def print(rootNode: RootNode, printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter = {
      printer
        .add(s"message $name {  // message $id")
        .indent
        .printAll(enums)
        .print(messages)(_.print(rootNode, _))
        .print(fields)(_.print(rootNode, _))
        .outdent
        .add("}\n")
    }
  }

  case class FieldNode(name: String,
                       fieldType: GenTypes.ProtoType,
                       fieldOptions: GenTypes.FieldOptions.Value,
                       tag: Int) {
    def print(rootNode: RootNode, printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter = {
      printer.add(s"$fieldOptions ${rootNode.resolveProtoTypeName(fieldType)} $name = $tag;  // $fieldType")
    }
  }

  case class EnumNode(id: Int, name: String, values: Seq[(String, Int)], parentMessageId: Option[Int], fileId: Int) extends FPrintable {
    override def print(printer: compiler.FunctionalPrinter): compiler.FunctionalPrinter = {
      printer.add(s"enum $name {  // enum $id")
        .indent
        .add(values.map { case (s, v) => s"$s = $v;"}: _*)
        .outdent
        .add("}")
    }
  }
}

