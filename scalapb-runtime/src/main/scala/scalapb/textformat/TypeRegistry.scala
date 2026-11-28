package scalapb.textformat

import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import scalapb.descriptors.FileDescriptor

/** Registered payload types and extension declaration files for text format. */
final class TypeRegistry private (
    private val types: Map[String, GeneratedMessageCompanion[?]],
    private val files: Vector[FileDescriptor]
) {
  private def this(types: Map[String, GeneratedMessageCompanion[?]]) = this(types, Vector.empty)

  def addMessage[T <: GeneratedMessage](companion: GeneratedMessageCompanion[T]): TypeRegistry =
    new TypeRegistry(types.updated(companion.scalaDescriptor.fullName, companion), files)

  /** Makes extensions declared in this file available to text parsing and printing. Message-valued
    * extensions additionally require their payload companion via addMessage. The containing
    * message's file and files of registered messages are included automatically.
    */
  def addFile(file: FileDescriptor): TypeRegistry =
    new TypeRegistry(types, (files :+ file).distinct)

  private def declarationFiles: Iterable[FileDescriptor] =
    files ++ types.values.map(_.scalaDescriptor.file)

  private[textformat] def scalarExtensions(
      containing: scalapb.descriptors.Descriptor
  ): Vector[ScalarExtension] =
    ExtensionDeclaration.find(containing, declarationFiles).flatMap(ScalarExtension.fromDeclaration)

  private[textformat] def messageExtensions(
      containing: scalapb.descriptors.Descriptor
  ): Vector[MessageExtension] =
    MessageExtension.find(containing, types.values, declarationFiles)

  private[textformat] def find(typeUrl: String): Option[GeneratedMessageCompanion[?]] = {
    val slash = typeUrl.lastIndexOf('/')
    if (slash <= 0 || slash == typeUrl.length - 1) None
    else types.get(typeUrl.substring(slash + 1))
  }
}

object TypeRegistry {
  val empty: TypeRegistry = new TypeRegistry(Map.empty)
}
