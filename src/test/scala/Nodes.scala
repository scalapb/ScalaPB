
import scala.util.Try

object Nodes {
  import GenTypes._

  sealed trait Node {
    def allMessages: Stream[MessageNode]
  }

  case class RootNode(files: Seq[FileNode]) {
    def minMessageId = Try(files.flatMap(_.minMessageId).min).toOption

    def maxMessageId = Try(files.flatMap(_.maxMessageId).max).toOption

    def minEnumId = Try(files.flatMap(_.minEnumId).min).toOption

    def maxEnumId = Try(files.flatMap(_.maxEnumId).max).toOption

    def resolveTypeName(t: GenTypes.ProtoType): String = t match {
      case Primitive(name) => name
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

    def fileReferences(rootNode: RootNode): Set[String] = allMessages.flatMap(_.fields.map(_.fieldType).collect {
      case MessageReference(id) => rootNode.messagesById(id).fileId
      case EnumReference(id) => rootNode.enumsById(id).fileId
    }).toSet.map(rootNode.filesById).map(_.baseFileName)

    def print(rootNode: RootNode, printer: FunctionalPrinter): FunctionalPrinter = {
      val p1 = protoPackage.fold(printer)(pkg => printer.add(s"package $pkg;"))
      val p2 = javaPackage.fold(p1)(pkg => p1.add(s"""option java_package = "$pkg";"""))
      p2.add(fileReferences(rootNode).collect({
          case f if f != baseFileName => s"""import "${f}.proto";"""
      }).toSeq: _*)
        .printAll(enums)
        .print(messages)(_.print(rootNode, _))
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

    def print(rootNode: RootNode, printer: FunctionalPrinter): FunctionalPrinter = {
      printer
        .add(s"message $name {  // $id")
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
    def print(rootNode: RootNode, printer: FunctionalPrinter): FunctionalPrinter = {
      printer.add(s"$fieldOptions ${rootNode.resolveTypeName(fieldType)} $name = $tag;")
    }
  }

  case class EnumNode(id: Int, name: String, values: Seq[(String, Int)], parentMessageId: Option[Int], fileId: Int) extends FPrintable {
    override def print(printer: FunctionalPrinter): FunctionalPrinter = {
      printer.add(s"enum $name {")
        .indent
        .add(values.map { case (s, v) => s"$s = $v;"}: _*)
        .outdent
        .add("}")
    }
  }
}

