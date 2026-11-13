package scalapb.textformat

import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

/** Explicitly registered message types accepted in expanded text-format Any values. */
final class TypeRegistry private (
    private val types: Map[String, GeneratedMessageCompanion[?]]
) {
  def addMessage[T <: GeneratedMessage](companion: GeneratedMessageCompanion[T]): TypeRegistry =
    new TypeRegistry(types.updated(companion.scalaDescriptor.fullName, companion))

  private[textformat] def find(typeUrl: String): Option[GeneratedMessageCompanion[?]] = {
    val slash = typeUrl.lastIndexOf('/')
    if (slash <= 0 || slash == typeUrl.length - 1) None
    else types.get(typeUrl.substring(slash + 1))
  }
}

object TypeRegistry {
  val empty: TypeRegistry = new TypeRegistry(Map.empty)
}
