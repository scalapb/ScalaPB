package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors._
import scala.collection.JavaConverters._

class ProtoValidation(val params: GeneratorParams) extends DescriptorPimps {
  val ForbiddenFieldNames = Set(
    "hashCode", "equals", "clone", "finalize", "getClass", "notify", "notifyAll", "toString", "wait")

  def validateFile(fd: FileDescriptor): Unit = {
    fd.getEnumTypes.asScala.foreach(validateEnum)
    fd.getMessageTypes.asScala.foreach(validateMessage)
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
  }
}
