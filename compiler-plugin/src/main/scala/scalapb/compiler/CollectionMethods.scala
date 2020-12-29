package scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor
import DescriptorImplicits._

class CollectionMethods(fd: FieldDescriptor, implicits: DescriptorImplicits) {
  import implicits._

  def newBuilder: String = {
    val t = if (fd.collectionType == ScalaSeq) ScalaVector else fd.collectionType

    if (!fd.isMapField) {
      adapter match {
        case None     => s"$t.newBuilder[${fd.singleScalaTypeName}]"
        case Some(tc) => s"$tc.newBuilder[${fd.singleScalaTypeName}]"
      }
    } else {
      adapter match {
        case None     => s"$t.newBuilder[${fd.mapType.keyType}, ${fd.mapType.valueType}]"
        case Some(tc) => s"$tc.newBuilder[${fd.mapType.keyType}, ${fd.mapType.valueType}]"
      }
    }
  }

  def empty: String = adapter match {
    case None     => s"${fd.collectionType}.empty"
    case Some(tc) => s"$tc.empty"
  }

  def foreach = adapter match {
    case None     => fd.scalaName.asSymbol + ".foreach"
    case Some(tc) => s"$tc.foreach(${fd.scalaName.asSymbol})"
  }

  def concat(left: String, right: String) = adapter match {
    case None     => s"$left ++ $right"
    case Some(tc) => s"$tc.concat($left, $right)"
  }

  def nonEmptyType = fd.fieldOptions.getCollection.getNonEmpty

  def nonEmptyCheck(expr: String) = if (nonEmptyType) "true" else s"$expr.nonEmpty"

  def adapter: Option[String] = {
    if (fd.fieldOptions.getCollection.hasAdapter())
      Some(fd.fieldOptions.getCollection.getAdapter())
    else None
  }

  def size: Expression = adapter match {
    case None     => MethodApplication("size")
    case Some(tc) => FunctionApplication(s"$tc.size")
  }

  def iterator: Expression = adapter match {
    case None     => MethodApplication("iterator")
    case Some(tc) => FunctionApplication(s"$tc.toIterator")
  }
}
