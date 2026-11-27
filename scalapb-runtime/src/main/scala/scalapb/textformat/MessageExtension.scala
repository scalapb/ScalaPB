package scalapb.textformat

import com.google.protobuf.descriptor.{DescriptorProto, FieldDescriptorProto, FeatureSet}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import scalapb.descriptors.Descriptor

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
        scope: String,
        inherited: FeatureSet
    ): Seq[(String, FieldDescriptorProto, FeatureSet)] = {
      val name     = join(scope, message.getName)
      val features =
        FeatureSet.parseFrom(inherited.toByteArray ++ message.getOptions.getFeatures.toByteArray)
      message.extension.map(f => (join(name, f.getName), f, features)) ++
        message.nestedType.flatMap(declarations(_, name, features))
    }
    (containing.file +: companions.map(_.scalaDescriptor.file).toSeq).distinct.toVector.flatMap {
      file =>
        val proto  = file.asProto
        val fields =
          proto.extension.map(f => (join(proto.getPackage, f.getName), f, file.resolvedFeatures)) ++
            proto.messageType.flatMap(declarations(_, proto.getPackage, file.resolvedFeatures))
        fields.flatMap { case (name, field, inherited) =>
          if (
            field.getExtendee.stripPrefix(".") != containing.fullName ||
            !(field.getType.isTypeMessage || field.getType.isTypeGroup)
          ) None
          else
            byName.get(field.getTypeName.stripPrefix(".")).map { companion =>
              val features = FeatureSet.parseFrom(
                inherited.toByteArray ++ field.getOptions.getFeatures.toByteArray
              )
              MessageExtension(
                name,
                field,
                companion,
                field.getType.isTypeGroup || features.getMessageEncoding.isDelimited
              )
            }
        }
    }
  }
}
