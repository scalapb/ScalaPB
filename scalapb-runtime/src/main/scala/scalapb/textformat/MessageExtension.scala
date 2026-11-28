package scalapb.textformat

import com.google.protobuf.descriptor.FieldDescriptorProto
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import scalapb.descriptors.{Descriptor, FileDescriptor}

/** Message-valued extensions whose payload companions were explicitly registered. */
private[textformat] case class MessageExtension(
    name: String,
    field: FieldDescriptorProto,
    companion: GeneratedMessageCompanion[GeneratedMessage],
    isDelimited: Boolean
)

private[textformat] object MessageExtension {
  def find(
      containing: Descriptor,
      companions: Iterable[GeneratedMessageCompanion[?]],
      files: Iterable[FileDescriptor]
  ): Vector[MessageExtension] = {
    val byName = companions
      .map(c =>
        c.scalaDescriptor.fullName -> c.asInstanceOf[GeneratedMessageCompanion[GeneratedMessage]]
      )
      .toMap
    ExtensionDeclaration.find(containing, files).flatMap { declaration =>
      val field = declaration.field
      if (!(field.getType.isTypeMessage || field.getType.isTypeGroup)) None
      else
        byName.get(field.getTypeName.stripPrefix(".")).map { companion =>
          MessageExtension(
            declaration.name,
            field,
            companion,
            field.getType.isTypeGroup || declaration.features.getMessageEncoding.isDelimited
          )
        }
    }
  }
}
