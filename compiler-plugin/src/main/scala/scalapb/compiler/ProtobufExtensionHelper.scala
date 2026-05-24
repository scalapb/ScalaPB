package scalapb.compiler

import com.google.protobuf.GeneratedMessage

/** Works around ambiguous getExtension overload introduced in protobuf 4.35.0.
  *
  * Both ExtendableMessage (class) and ExtendableMessageOrBuilder (interface) define
  * getExtension, causing ambiguity in Scala when the concrete descriptor options type is used.
  * By typing the receiver as ExtendableMessageOrBuilder, only the interface overload is visible.
  */
private[compiler] object ProtobufExtensionHelper {
  def getExtension[ContainerT <: GeneratedMessage.ExtendableMessage[ContainerT], T](
      msg: GeneratedMessage.ExtendableMessageOrBuilder[ContainerT],
      ext: GeneratedMessage.GeneratedExtension[ContainerT, T]
  ): T = msg.getExtension(ext)
}
