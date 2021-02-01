package scalapb

import com.google.protobuf.Descriptors.{Descriptor, FieldDescriptor}
import com.google.protobuf.field_mask.FieldMask

import scala.collection.SortedMap

private[scalapb] case class FieldMaskTree(nodes: SortedMap[String, FieldMaskTree]) {

  def containsField[M <: GeneratedMessage: GeneratedMessageCompanion](fieldNumber: Int): Boolean = {
    val descriptor = implicitly[GeneratedMessageCompanion[M]].javaDescriptor
    Option(descriptor.findFieldByNumber(fieldNumber)) match {
      case Some(field) => nodes.contains(field.getName)
      case None        => false
    }
  }

  def fieldMask: FieldMask = FieldMask(paths)

  private def paths: Vector[String] = {
    nodes.toVector.flatMap {
      case (field, FieldMaskTree.Empty) => Vector(field)
      case (field, subTree) =>
        subTree.paths.map(path => s"$field${FieldMaskTree.FieldSeparator}$path")
    }
  }

  def isValidFor[M <: GeneratedMessage: GeneratedMessageCompanion]: Boolean = {
    val descriptor = implicitly[GeneratedMessageCompanion[M]].javaDescriptor
    isValidFor(descriptor)
  }

  private def isValidFor(descriptor: Descriptor): Boolean = {
    nodes.forall { case (field, tree) =>
      Option(descriptor.findFieldByName(field)) match {
        case None => false
        case Some(field) =>
          if (tree == FieldMaskTree.Empty) {
            true
          } else if (!field.isRepeated && field.getJavaType == FieldDescriptor.JavaType.MESSAGE) {
            tree.isValidFor(field.getMessageType)
          } else {
            false
          }
      }
    }
  }
}

object FieldMaskTree {

  val Empty: FieldMaskTree = FieldMaskTree(SortedMap.empty[String, FieldMaskTree])

  private val FieldSeparator = '.'

  def apply(paths: Seq[String]): FieldMaskTree = {
    paths.map(fromPath).foldLeft[FieldMaskTree](Empty)(union)
  }

  def apply(fieldMask: FieldMask): FieldMaskTree = {
    apply(fieldMask.paths)
  }

  def union(tree1: FieldMaskTree, tree2: FieldMaskTree): FieldMaskTree = (tree1, tree2) match {
    case (Empty, tree) => tree
    case (tree, Empty) => tree
    case (FieldMaskTree(nodes1), FieldMaskTree(nodes2)) =>
      val fields: Seq[String] = (nodes1.keySet ++ nodes2.keySet).toSeq
      val tree = SortedMap(fields.map { field =>
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
