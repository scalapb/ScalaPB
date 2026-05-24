package scalapb

import com.google.protobuf.GeneratedMessage

package object compiler {

  /** Works around ambiguous getExtension overload introduced in protobuf 4.35.0.
    *
    * Both ExtendableMessage (class) and ExtendableMessageOrBuilder (interface) define getExtension,
    * causing ambiguity in Scala when the concrete descriptor options type is used. By typing the
    * receiver as ExtendableMessageOrBuilder, only the interface overload is visible.
    */
  private[compiler] implicit class ExtendableMessageOps[
      C <: GeneratedMessage.ExtendableMessage[C]
  ](
      val msg: GeneratedMessage.ExtendableMessageOrBuilder[C]
  ) extends AnyVal {
    def extension[T](ext: GeneratedMessage.GeneratedExtension[C, T]): T = msg.getExtension(ext)
  }
}
