package scalapb

import com.google.protobuf.field_mask.FieldMask
import scalapb.FieldMaskTree.Empty
import scalapb.descriptors.{Descriptor, PMessage, ScalaType}

import scala.collection.SortedMap

private[scalapb] case class FieldMaskTree(nodes: SortedMap[String, FieldMaskTree]) {

  def applyToMessage[M <: GeneratedMessage: GeneratedMessageCompanion](message: M): M = {
    val companion        = implicitly[GeneratedMessageCompanion[M]]
    val filteredPMessage = applyToMessage(message.toPMessage)
    companion.messageReads.read(filteredPMessage)
  }

  def containsField[M <: GeneratedMessage: GeneratedMessageCompanion](fieldNumber: Int): Boolean = {
    val descriptor = implicitly[GeneratedMessageCompanion[M]].scalaDescriptor
    descriptor.findFieldByNumber(fieldNumber) match {
      case Some(field) => nodes.contains(field.name)
      case None        => false
    }
  }

  def fieldMask: FieldMask = FieldMask(paths)

  def isValidFor[M <: GeneratedMessage: GeneratedMessageCompanion]: Boolean = {
    val descriptor = implicitly[GeneratedMessageCompanion[M]].scalaDescriptor
    isValidFor(descriptor)
  }

  private def applyToMessage(message: PMessage): PMessage = {
    val fieldValues = message.value.keys.flatMap { fieldDescriptor =>
      val value = (message.value.apply(fieldDescriptor), nodes.get(fieldDescriptor.name)) match {
        case (pValue, Some(Empty))                 => Some(pValue)
        case (subMessage: PMessage, Some(subTree)) => Some(subTree.applyToMessage(subMessage))
        case _                                     => None
      }
      value.map(fieldDescriptor -> _)
    }.toMap
    PMessage(fieldValues)
  }

  private def isValidFor(descriptor: Descriptor): Boolean = {
    nodes.forall { case (field, tree) =>
      descriptor.findFieldByName(field) match {
        case None        => false
        case Some(field) =>
          if (tree == FieldMaskTree.Empty) {
            true
          } else {
            field.scalaType match {
              case ScalaType.Message(m) if !field.isRepeated => tree.isValidFor(m)
              case _                                         => false
            }
          }
      }
    }
  }

  private def paths: Vector[String] = {
    nodes.toVector.flatMap {
      case (field, FieldMaskTree.Empty) => Vector(field)
      case (field, subTree)             =>
        subTree.paths.map(path => s"$field${FieldMaskTree.FieldSeparator}$path")
    }
  }
}

private[scalapb] object FieldMaskTree {

  val Empty: FieldMaskTree = FieldMaskTree(SortedMap.empty[String, FieldMaskTree])

  private val FieldSeparator = '.'

  def apply(paths: Seq[String]): FieldMaskTree = {
    paths.map(fromPath).foldLeft[FieldMaskTree](Empty)(union)
  }

  def apply(fieldMask: FieldMask): FieldMaskTree = {
    apply(fieldMask.paths)
  }

  def union(tree1: FieldMaskTree, tree2: FieldMaskTree): FieldMaskTree = (tree1, tree2) match {
    case (Empty, tree)                                  => tree
    case (tree, Empty)                                  => tree
    case (FieldMaskTree(nodes1), FieldMaskTree(nodes2)) =>
      val fields: Seq[String] = (nodes1.keySet ++ nodes2.keySet).toSeq
      val tree                = SortedMap(fields.map { field =>
        val subTree = (nodes1.get(field), nodes2.get(field)) match {
          case (Some(Empty), _) => Empty
          case (_, Some(Empty)) => Empty
          case (mask1, mask2)   => union(mask1.getOrElse(Empty), mask2.getOrElse(Empty))
        }
        field -> subTree
      }: _*)
      FieldMaskTree(tree)
  }

  private def fromPath(path: String): FieldMaskTree = {
    path
      .split(FieldSeparator)
      .filter(_.nonEmpty)
      .foldRight[FieldMaskTree](Empty) { case (field, tree) =>
        FieldMaskTree(SortedMap(field -> tree))
      }
  }
}
