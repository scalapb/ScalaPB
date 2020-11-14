package scalapb

import com.google.protobuf.CodedInputStream

/** Allows building an instance of a message A
  *
  * The code generator will create a class that extends MessageBuilder for each message.
  * It generally contains a `var` for each optional and required field, and a
  * [scala.collection.mutable.Builder] for each repeated field.
  */
abstract class MessageBuilder[A] {
  def merge(input: CodedInputStream): this.type

  /* Builds an instance of `A`. The message may throw an exception of type
     com.google.protobuf.InvalidProtocolBufferException when a required field is missing.
   */
  def result(): A
}

trait MessageBuilderCompanion[A, Builder] {
  def apply(a: A): Builder
}
