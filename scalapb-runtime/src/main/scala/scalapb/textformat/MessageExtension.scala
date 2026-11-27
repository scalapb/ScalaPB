package scalapb.textformat

import com.google.protobuf.descriptor.{DescriptorProto, FieldDescriptorProto}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import scalapb.descriptors.Descriptor

/** Message-valued extensions whose payload companions were explicitly registered. */
private[textformat] case class MessageExtension(
    name: String,
    field: FieldDescriptorProto,
    companion: GeneratedMessageCompanion[GeneratedMessage]
)

private[textformat] object MessageExtension {
  def find(
      containing: Descriptor,
      companions: Iterable[GeneratedMessageCompanion[?]]
  ): Vector[MessageExtension] = {
    val byName = companions
      .map(c =>
        c.scalaDescriptor.fullName -> c.asInstanceOf[GeneratedMessageCompanion[GeneratedMessage]]
      )
      .toMap
    def join(scope: String, name: String) = if (scope.isEmpty) name else scope + "." + name
    def declarations(
        message: DescriptorProto,
        scope: String
    ): Seq[(String, FieldDescriptorProto)] = {
      val name = join(scope, message.getName)
      message.extension.map(f => join(name, f.getName) -> f) ++
        message.nestedType.flatMap(declarations(_, name))
    }
    (containing.file +: companions.map(_.scalaDescriptor.file).toSeq).distinct.toVector.flatMap {
      file =>
        val proto  = file.asProto
        val fields = proto.extension.map(f => join(proto.getPackage, f.getName) -> f) ++
          proto.messageType.flatMap(declarations(_, proto.getPackage))
        fields.flatMap { case (name, field) =>
          if (
            field.getExtendee.stripPrefix(".") != containing.fullName ||
            !(field.getType.isTypeMessage || field.getType.isTypeGroup)
          ) None
          else byName.get(field.getTypeName.stripPrefix(".")).map(MessageExtension(name, field, _))
        }
    }
  }
}
