package scalapb

import com.google.protobuf.CodedInputStream

/** Kept for binary compatibility between 0.11.x and 0.10.x
  *
  * On 0.10.x, the code generator would create a class that extended MessageBuilder
  * for each message.
  */
@deprecated("Kept for binary compatibility", "0.11.x")
abstract class MessageBuilder[A] {
  def merge(input: CodedInputStream): this.type

  /* Builds an instance of `A`. The message may throw an exception of type
     com.google.protobuf.InvalidProtocolBufferException when a required field is missing.
   */
  def result(): A
}

@deprecated("Kept for binary compatibility", "0.11.x")
trait MessageBuilderCompanion[A, Builder] {
  def apply(a: A): Builder
}
