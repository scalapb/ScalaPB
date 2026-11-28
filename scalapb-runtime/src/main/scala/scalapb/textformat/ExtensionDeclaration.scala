package scalapb.textformat

import com.google.protobuf.descriptor.{DescriptorProto, FieldDescriptorProto, FeatureSet}
import scalapb.descriptors.{Descriptor, FileDescriptor}

/** Internal declaration metadata; generated extension lenses and public field descriptors are
  * unchanged.
  */
private[textformat] case class ExtensionDeclaration(
    name: String,
    field: FieldDescriptorProto,
    features: FeatureSet,
    file: FileDescriptor
)

private[textformat] object ExtensionDeclaration {
  def find(
      containing: Descriptor,
      files: Iterable[FileDescriptor]
  ): Vector[ExtensionDeclaration] = {
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
    (containing.file +: files.toSeq).distinct.toVector.flatMap { file =>
      val proto  = file.asProto
      val fields =
        proto.extension.map(f => (join(proto.getPackage, f.getName), f, file.resolvedFeatures)) ++
          proto.messageType.flatMap(declarations(_, proto.getPackage, file.resolvedFeatures))
      fields.collect {
        case (name, field, inherited)
            if field.getExtendee.stripPrefix(".") == containing.fullName =>
          ExtensionDeclaration(
            name,
            field,
            FeatureSet.parseFrom(inherited.toByteArray ++ field.getOptions.getFeatures.toByteArray),
            file
          )
      }
    }
  }
}
