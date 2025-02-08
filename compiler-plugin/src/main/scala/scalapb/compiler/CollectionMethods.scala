package scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor
import DescriptorImplicits._

class CollectionMethods(fd: FieldDescriptor, val implicits: DescriptorImplicits) {
  import implicits._

  def newBuilder: String = {
    val t = if (fd.collectionType == ScalaSeq) ScalaVector else fd.collectionType

    if (!fd.isMapField) {
      adapter match {
        case None =>
          // Use VectorBuilder directly rather than mutable.Builder since
          // working directly with the concrete class has been shown to be
          // about 5% faster.
          if (t == ScalaVector)
            s"new _root_.scala.collection.immutable.VectorBuilder[${fd.singleScalaTypeName}]"
          else s"$t.newBuilder[${fd.singleScalaTypeName}]"
        case Some(tc) => s"${tc.fullName}.newBuilder"
      }
    } else {
      adapter match {
        case None     => s"$t.newBuilder[${fd.mapType.keyType}, ${fd.mapType.valueType}]"
        case Some(tc) => s"${tc.fullName}.newBuilder"
      }
    }
  }

  def builderType: String = {
    if (adapter.isDefined)
      s"${adapter.get.fullName}.Builder"
    else if (fd.collectionType == ScalaSeq || fd.collectionType == ScalaVector)
      s"_root_.scala.collection.immutable.VectorBuilder[${fd.singleScalaTypeName}]"
    else
      s"_root_.scala.collection.mutable.Builder[${fd.singleScalaTypeName}, ${fd.scalaTypeName}]"
  }

  def empty: String = adapter match {
    case None =>
      if (fd.collectionType == ScalaSeq)
        s"${ScalaVector}.empty"
      else
        s"${fd.collectionType}.empty"
    case Some(tc) => s"${tc.fullName}.empty"
  }

  def foreach = adapter match {
    case None     => fd.scalaName.asSymbol + ".foreach"
    case Some(tc) => s"${tc.fullName}.foreach(${fd.scalaName.asSymbol})"
  }

  def concat(left: String, right: String) = adapter match {
    case None     => s"$left ++ $right"
    case Some(tc) => s"${tc.fullName}.concat($left, $right)"
  }

  def nonEmptyType = fd.fieldOptions.getCollection.getNonEmpty

  def nonEmptyCheck(expr: String) = if (nonEmptyType) "true" else s"$expr.nonEmpty"

  def adapter: Option[ScalaName] =
    if (adapterClass.isDefined)
      Some(fd.getContainingType.scalaType / s"_adapter_${fd.scalaName}")
    else None

  def adapterClass: Option[String] = {
    if (fd.fieldOptions.getCollection.hasAdapter())
      Some(fd.fieldOptions.getCollection.getAdapter())
    else None
  }

  def size: Expression = adapter match {
    case None     => MethodApplication("size")
    case Some(tc) => FunctionApplication(s"${tc.fullName}.size")
  }

  def iterator: Expression = adapter match {
    case None     => MethodApplication("iterator")
    case Some(tc) => FunctionApplication(s"${tc.fullName}.toIterator")
  }
}
