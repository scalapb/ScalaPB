package scalapb.compiler

import com.google.protobuf.Descriptors._
import scala.jdk.CollectionConverters._

class ProtoValidation(implicits: DescriptorImplicits) {
  import implicits._

  def validateFiles(files: Seq[FileDescriptor]) = {
    files.foreach(validateFile)
    FileOptionsCache.buildCache(files)
  }

  def validateFile(fd: FileDescriptor): Unit = {
    fd.getEnumTypes.asScala.foreach(validateEnum)
    fd.getMessageTypes.asScala.foreach(validateMessage)
    if (fd.scalaOptions.hasPrimitiveWrappers && fd.scalaOptions.getNoPrimitiveWrappers) {
      throw new GeneratorException(
        s"${fd.getFullName}: primitive_wrappers and no_primitive_wrappers must not be used at the same time."
      )
    }
    val allSealedOneofCases = for {
      msg   <- fd.getMessageTypes.asScala
      cases <- msg.sealedOneofCases.getOrElse(Seq.empty)
    } yield cases
    allSealedOneofCases
      .groupBy(identity)
      .collect {
        case (d, occurences) if occurences.length > 1 => d
      }
      .foreach { d =>
        throw new GeneratorException(
          s"${d.getFullName}: message may belong to at most one sealed oneof"
        )
      }
  }

  def validateEnum(e: EnumDescriptor): Unit = {
    if (e.getValues.asScala.exists(_.getName.toUpperCase == "UNRECOGNIZED")) {
      throw new GeneratorException(
        s"The enum value 'UNRECOGNIZED' in ${e.getName} is not allowed due to conflict with the catch-all " +
          "Unrecognized(v: Int) value."
      )
    }
  }

  def validateMessage(m: Descriptor): Unit = {
    m.getEnumTypes.asScala.foreach(validateEnum)
    m.getNestedTypes.asScala.foreach(validateMessage)
    m.getFields.asScala.foreach(validateField)
    if (m.isSealedOneofType) {
      val oneof = m.getOneofs.get(0)
      if (m.getContainingType != null) {
        throw new GeneratorException(s"${m.getFullName}: sealed oneofs must be top-level messages")
      }
      if (m.getFields.size() != oneof.getFields.size()) {
        throw new GeneratorException(
          s"${m.getFullName}: sealed oneofs must have all their fields inside a single oneof"
        )
      }
      val fields = oneof.getFields.asScala
      fields.find(!_.isMessage).foreach { field =>
        throw new GeneratorException(
          s"${m.getFullName}.${field.getName}: sealed oneofs must have all their fields be message types"
        )
      }
      fields.find(_.getMessageType.isSealedOneofType).foreach { field =>
        throw new GeneratorException(
          s"${m.getFullName}.${field.getName}: sealed oneofs may not be a case within another sealed oneof"
        )
      }
      fields.find(_.getMessageType.getContainingType != null).foreach { field =>
        throw new GeneratorException(
          s"${m.getFullName}.${field.getName}: all sealed oneof cases must be top-level"
        )
      }
      val distinctTypes = fields.map(_.getMessageType).toSet
      if (distinctTypes.size != fields.size) {
        throw new GeneratorException(
          s"${m.getFullName}: all sealed oneof cases must be of a distinct message type"
        )
      }
      if (!m.getNestedTypes.isEmpty) {
        throw new GeneratorException(
          s"${m.getFullName}: sealed oneofs may not contain nested messages"
        )
      }
      if (!m.getEnumTypes.isEmpty) {
        throw new GeneratorException(
          s"${m.getFullName}: sealed oneofs may not contain nested enums"
        )
      }
    } else if (m.sealedOneOfExtendsCount > 0) {
      throw new GeneratorException(
        s"${m.getFullName}: is not a Sealed oneof and may not contain a sealed_oneof_extends message option. Use extends instead."
      )
    }
  }

  def validateField(fd: FieldDescriptor): Unit = {
    if (ProtoValidation.ForbiddenFieldNames.contains(fd.scalaName))
      throw new GeneratorException(
        s"Field named '${fd.getName}' in message '${fd.getFullName}' is not allowed. See https://scalapb.github.io/customizations.html#custom-names"
      )
    if (!fd.isRepeated && fd.fieldOptions.hasCollectionType)
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} has collection_type set but is not a repeated field."
      )
    if (!fd.isMapField && (fd.fieldOptions.hasKeyType || fd.fieldOptions.hasValueType)) {
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} is not a map but specifies key_type or value_type."
      )
    }
    if (fd.isMapField && fd.fieldOptions.hasCollectionType) {
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} is a map but has collection_type specified. Use map_type instead."
      )
    }
    if (!fd.isMapField && fd.fieldOptions.hasMapType) {
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} has map_type specified, but it is not a map field"
      )
    }
    if (fd.isMapField && fd.fieldOptions.hasType)
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} is a map and has type specified. Use key_type or value_type instead."
      )
    if (!fd.isOptional && fd.fieldOptions.hasNoBox)
      throw new GeneratorException(
        s"${fd.getFullName}: Field ${fd.getName} has no_box set but is not an optional field."
      )
    if (fd.isMessage && fd.getMessageType.isSealedOneofType && fd.fieldOptions.hasType) {
      throw new GeneratorException(
        s"${fd.getFullName}: Sealed oneofs can not be type mapped. Use regular oneofs instead."
      )
    }
  }
}

object ProtoValidation {
  val ForbiddenFieldNames = Set(
    "hashCode",
    "equals",
    "class",
    "clone",
    "copy",
    "finalize",
    "getClass",
    "notify",
    "notifyAll",
    "toString",
    "wait",
    "productArity",
    "productElementName",
    "productElementNames",
    "productIterator",
    "productPrefix"
  )
}
