package scalapb.compiler

import com.google.protobuf.{ExtendableMessageOrBuilder, GeneratedMessage}

/** Works around ambiguous getExtension overload introduced in protobuf 4.35.0.
  *
  * Both ExtendableMessage (class) and ExtendableMessageOrBuilder (interface) define
  * getExtension, causing ambiguity in Scala when the concrete descriptor options type is used.
  * By typing the receiver as ExtendableMessageOrBuilder, only the interface overload is visible.
  */
private[compiler] object ProtobufExtensionHelper {
  def getExtension[ContainerT <: ExtendableMessageOrBuilder[ContainerT], T](
      msg: ExtendableMessageOrBuilder[ContainerT],
      ext: GeneratedMessage.GeneratedExtension[ContainerT, T]
  ): T = msg.getExtension(ext)
}
