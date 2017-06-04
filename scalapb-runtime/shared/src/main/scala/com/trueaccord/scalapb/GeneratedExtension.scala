package com.trueaccord.scalapb

import com.google.protobuf.{ByteString, CodedInputStream, InvalidProtocolBufferException}
import com.trueaccord.lenses.{Lens, Mutation}

import _root_.scalapb.UnknownFieldSet

case class GeneratedExtension[C <: ExtendableMessage[C], T](lens: Lens[C, T]) extends Lens[C, T] {
  def get(c: C): T = lens.get(c)

  def set(t: T): Mutation[C] = lens.set(t)
}

object GeneratedExtension {
  /* To be used only be generated code */
  def readMessageFromByteString[T <: GeneratedMessage with Message[T]](cmp: GeneratedMessageCompanion[T])
    (bs: ByteString): T = {
    cmp.parseFrom(bs.newCodedInput())
  }

  def singleUnknownFieldLens[E, T](fromBase: E => T, toBase: T => E, default: T): Lens[Seq[E], T] =
    Lens[Seq[E], T](c => c.lastOption.map(fromBase).getOrElse(default))((c, t) => Vector(toBase(t)))

  def optionalUnknownFieldLens[E, T](fromBase: E => T, toBase: T => E): Lens[Seq[E], Option[T]] =
    Lens[Seq[E], Option[T]](c => c.lastOption.map(fromBase))((c, t) => t.map(toBase).toVector)

  private def unpackLengthDelimited[E, T](bss: Seq[ByteString], fromBase: E => T,
    unpack: CodedInputStream => E): Seq[T] = {
    val v = Vector.newBuilder[T]
    bss.foreach {
      ld =>
        val ci = ld.newCodedInput
        while (ci.getBytesUntilLimit > 0) {
          v += fromBase(unpack(ci))
        }
    }
    v.result()
  }

  def repeatedUnknownFieldLensUnpackable[E, T](fromBase: E => T, toBase: T => E): Lens[Seq[E], Seq[T]] =
    Lens[Seq[E], Seq[T]]({
      es => es.map(fromBase)
    })({
      (es, t) =>
        t.map(toBase)
    })

  def repeatedUnknownFieldLensPackable[E, T](fromBase: E => T, toBase: T => E,
    unpack: CodedInputStream => E): Lens[(Seq[E], Seq[ByteString]), Seq[T]] =
    Lens[(Seq[E], Seq[ByteString]), Seq[T]]({
      case (es, bss) =>
        if (bss.nonEmpty) {
          if (es.nonEmpty) {
            throw new InvalidProtocolBufferException("Mixed packed and unpacked data.")
          }
          unpackLengthDelimited(bss, fromBase, unpack)
        } else {
          es.map(fromBase)
        }
    })(
      { case ((es, bss), t) =>
        val v = Vector.newBuilder[E]
        if (bss.nonEmpty) {
          bss.foreach {
            ld =>
              val ci = ld.newCodedInput
              while (ci.getBytesUntilLimit > 0) {
                v += unpack(ci)
              }
          }
        }
        v ++= t.map(toBase)
        (v.result(), Vector.empty[ByteString])
      }
    )

  def forSingularUnknownField[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int, listLens: Lens[UnknownFieldSet.Field, Seq[E]])
    (fromBase: E => T, toBase: T => E, default: T): GeneratedExtension[C, T] = {
    GeneratedExtension(
      ExtendableMessage
        .unknownFieldsLen(fieldNumber)
        .compose(listLens)
        .compose(singleUnknownFieldLens(fromBase, toBase, default)))
  }

  def forOptionalUnknownField[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int,
    listLens: Lens[UnknownFieldSet.Field, Seq[E]])(fromBase: E => T, toBase: T => E): GeneratedExtension[C, Option[T]] =
    GeneratedExtension(
      ExtendableMessage
        .unknownFieldsLen(fieldNumber)
        .compose(listLens)
        .compose(optionalUnknownFieldLens(fromBase, toBase)))

  def forRepeatedUnknownFieldPackable[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int,
    listLens: Lens[UnknownFieldSet.Field, Seq[E]])(fromBase: E => T, toBase: T => E, unpack: CodedInputStream => E): GeneratedExtension[C, Seq[T]] = {
    GeneratedExtension(
      ExtendableMessage
        .unknownFieldsLen(fieldNumber)
        .compose(listLens zip UnknownFieldSet.Field.lengthDelimitedLens)
        .compose(repeatedUnknownFieldLensPackable(fromBase, toBase, unpack)))
  }

  def forRepeatedUnknownFieldUnpackable[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int,
    listLens: Lens[UnknownFieldSet.Field, Seq[E]])(fromBase: E => T, toBase: T => E): GeneratedExtension[C, Seq[T]] = {
    GeneratedExtension(
      ExtendableMessage
        .unknownFieldsLen(fieldNumber)
        .compose(listLens)
        .compose(repeatedUnknownFieldLensUnpackable(fromBase, toBase)))
  }
}
