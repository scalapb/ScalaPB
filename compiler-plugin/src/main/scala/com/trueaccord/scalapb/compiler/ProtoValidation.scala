package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors._
import scala.collection.JavaConverters._

class ProtoValidation(val params: GeneratorParams) extends DescriptorPimps {
  val ForbiddenFieldNames = Set(
    "hashCode", "equals", "clone", "copy", "finalize", "getClass", "notify", "notifyAll", "toString", "wait",
    "productArity", "productIterator", "productPrefix")

  def validateFile(fd: FileDescriptor): Unit = {
    fd.getEnumTypes.asScala.foreach(validateEnum)
    fd.getMessageTypes.asScala.foreach(validateMessage)
    if (fd.scalaOptions.hasPrimitiveWrappers && fd.scalaOptions.getNoPrimitiveWrappers) {
      throw new GeneratorException(
        s"primitive_wrappers and no_primitive_wrappers must not be used at the same time.")
    }
  }

  def validateEnum(e: EnumDescriptor): Unit = {
    if (e.getValues.asScala.exists(_.getName.toUpperCase == "UNRECOGNIZED")) {
      throw new GeneratorException(
        s"The enum value 'UNRECOGNIZED' in ${e.getName} is not allowed due to conflict with the catch-all " +
          "Unrecognized(v: Int) value.")
    }
  }

  def validateMessage(m: Descriptor): Unit = {
    m.getEnumTypes.asScala.foreach(validateEnum)
    m.getNestedTypes.asScala.foreach(validateMessage)
    m.getFields.asScala.foreach(validateField)
  }

  def validateField(fd: FieldDescriptor): Unit = {
    if (ForbiddenFieldNames.contains(fd.scalaName))
      throw new GeneratorException(
        s"Field named '${fd.getName}' in message '${fd.getFullName}' is not allowed. See https://scalapb.github.io/customizations.html#custom-names")
    if (!fd.isRepeated && fd.fieldOptions.hasCollectionType)
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} has collection_type set but is not a repeated field.")
    if (!fd.isMapField && (fd.fieldOptions.hasKeyType || fd.fieldOptions.hasValueType)) {
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} is not a map but specifies key_type or value_type.")
    }
    if (fd.isMapField && fd.fieldOptions.hasCollectionType)
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} is a map but has collection_type specified.")
    if (fd.isMapField && fd.fieldOptions.hasType)
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} is a map and has type specified. Use key_type or value_type instead.")
  }
}
