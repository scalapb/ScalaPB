package scalapb

import com.google.protobuf.{ByteString, CodedInputStream, InvalidProtocolBufferException}
import scalapb.lenses.{Lens, Mutation}

case class GeneratedExtension[C <: ExtendableMessage[C], T](lens: Lens[C, T]) extends Lens[C, T] {
  def get(c: C): T = lens.get(c)

  def set(t: T): Mutation[C] = lens.set(t)

  def withLens[B](other: Lens[T, B]) = GeneratedExtension(lens.compose(other))
}

object GeneratedExtension {
  /* To be used only be generated code */
  def readMessageFromByteString[T <: GeneratedMessage](
      cmp: GeneratedMessageCompanion[T]
  )(bs: ByteString): T = {
    cmp.parseFrom(bs.newCodedInput())
  }

  def singleUnknownFieldLens[E, T](fromBase: E => T, toBase: T => E, default: T): Lens[Seq[E], T] =
    Lens[Seq[E], T](c => c.lastOption.map(fromBase).getOrElse(default))((_, t) => Vector(toBase(t)))

  def optionalUnknownFieldLens[E, T](fromBase: E => T, toBase: T => E): Lens[Seq[E], Option[T]] =
    Lens[Seq[E], Option[T]](c => c.lastOption.map(fromBase))((_, t) => t.map(toBase).toVector)

  // For messages we concatenate all ByteStrings (https://github.com/scalapb/ScalaPB/issues/574)
  def singleUnknownMessageFieldLens[T](
      fromBase: ByteString => T,
      toBase: T => ByteString,
      default: T
  ): Lens[Seq[ByteString], T] =
    Lens[Seq[ByteString], T](c =>
      c.reduceOption((bs1, bs2) => bs1.concat(bs2)).lastOption.map(fromBase).getOrElse(default)
    )((_, t) => Vector(toBase(t)))

  def optionalUnknownMessageFieldLens[T](
      fromBase: ByteString => T,
      toBase: T => ByteString
  ): Lens[Seq[ByteString], Option[T]] =
    Lens[Seq[ByteString], Option[T]](c =>
      c.reduceOption((bs1, bs2) => bs1.concat(bs2)).map(fromBase)
    )((_, t) => t.map(toBase).toVector)

  private def unpackLengthDelimited[E, T, Coll](
      bss: Seq[ByteString],
      fromBase: E => T,
      unpack: CodedInputStream => E
  )(implicit ca: CollectionAdapter[T, Coll]): Coll = {
    val v = ca.newBuilder
    bss.foreach { ld =>
      val ci = ld.newCodedInput()
      while (ci.getBytesUntilLimit > 0) {
        v += fromBase(unpack(ci))
      }
    }
    v.result().toOption.get
  }

  @deprecated("Use repeatedLensUnpackable", "0.11.14")
  def repeatedUnknownFieldLensUnpackable[E, T, Coll](
      fromBase: E => T,
      toBase: T => E
  ): Lens[Seq[E], Seq[T]] =
    Lens[Seq[E], Seq[T]]({ es => es.map(fromBase) })({ (_, t) => t.map(toBase) })

  def repeatedLensUnpackable[E, T, Coll](
      fromBase: E => T,
      toBase: T => E
  )(implicit ca: CollectionAdapter[T, Coll]): Lens[Seq[E], Coll] =
    Lens[Seq[E], Coll]({ es =>
      val b = ca.newBuilder
      es.foreach { e =>
        b += fromBase(e)
      }
      b.result().toOption.get
    })({ (_, t) =>
      ca.toIterator(t).map(toBase).toSeq
    })

  @deprecated("Use repeatedLensPackable", "0.11.14")
  def repeatedUnknownFieldLensPackable[E, T](
      fromBase: E => T,
      toBase: T => E,
      unpack: CodedInputStream => E
  ): Lens[(Seq[E], Seq[ByteString]), Seq[T]] =
    repeatedLensPackable[E, T, Seq[T]](fromBase, toBase, unpack)

  def repeatedLensPackable[E, T, Coll](
      fromBase: E => T,
      toBase: T => E,
      unpack: CodedInputStream => E
  )(implicit ca: CollectionAdapter[T, Coll]): Lens[(Seq[E], Seq[ByteString]), Coll] =
    Lens[(Seq[E], Seq[ByteString]), Coll]({ case (es, bss) =>
      if (bss.nonEmpty) {
        if (es.nonEmpty) {
          throw new InvalidProtocolBufferException("Mixed packed and unpacked data.")
        }
        unpackLengthDelimited(bss, fromBase, unpack)
      } else {
        ca.fromIteratorUnsafe(es.iterator.map(fromBase))
      }
    })(
      { case ((_, bss), t) =>
        val v = Vector.newBuilder[E]
        if (bss.nonEmpty) {
          bss.foreach { ld =>
            val ci = ld.newCodedInput()
            while (ci.getBytesUntilLimit > 0) {
              v += unpack(ci)
            }
          }
        }
        v ++= ca.toIterator(t).map(toBase)
        (v.result(), Vector.empty[ByteString])
      }
    )

  def forUnknownField[C <: ExtendableMessage[C], E](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  ): GeneratedExtension[C, Seq[E]] = {
    GeneratedExtension[C, Seq[E]](
      ExtendableMessage.unknownFieldsLen(fieldNumber).compose(listLens)
    )
  }

  def forSingularUnknownField[C <: ExtendableMessage[C], E, T](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  )(fromBase: E => T, toBase: T => E, default: T): GeneratedExtension[C, T] =
    forUnknownField(fieldNumber, listLens)
      .withLens(singleUnknownFieldLens(fromBase, toBase, default))

  def forOptionalUnknownField[C <: ExtendableMessage[C], E, T](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  )(fromBase: E => T, toBase: T => E): GeneratedExtension[C, Option[T]] =
    forUnknownField(fieldNumber, listLens)
      .withLens(optionalUnknownFieldLens(fromBase, toBase))

  def forSingularUnknownMessageField[C <: ExtendableMessage[C], T](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[ByteString]]
  )(fromBase: ByteString => T, toBase: T => ByteString, default: T): GeneratedExtension[C, T] =
    forUnknownField(fieldNumber, listLens)
      .withLens(singleUnknownMessageFieldLens(fromBase, toBase, default))

  def forOptionalUnknownMessageField[C <: ExtendableMessage[C], T](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[ByteString]]
  )(fromBase: ByteString => T, toBase: T => ByteString): GeneratedExtension[C, Option[T]] =
    forUnknownField(fieldNumber, listLens)
      .withLens(optionalUnknownMessageFieldLens(fromBase, toBase))

  @deprecated("Use forRepeatedPackable", "0.11.14")
  def forRepeatedUnknownFieldPackable[C <: ExtendableMessage[C], E, T](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  )(
      fromBase: E => T,
      toBase: T => E,
      unpack: CodedInputStream => E
  ): GeneratedExtension[C, Seq[T]] =
    forRepeatedPackable[C, E, T, Seq[T]](fieldNumber, listLens)(fromBase, toBase, unpack)

  def forRepeatedPackable[C <: ExtendableMessage[C], E, T, Coll](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  )(
      fromBase: E => T,
      toBase: T => E,
      unpack: CodedInputStream => E
  )(implicit ca: CollectionAdapter[T, Coll]): GeneratedExtension[C, Coll] = {
    GeneratedExtension(
      ExtendableMessage
        .unknownFieldsLen(fieldNumber)
        .compose(listLens zip UnknownFieldSet.Field.lengthDelimitedLens)
        .compose(
          repeatedLensPackable(
            fromBase,
            toBase,
            unpack
          )
        )
    )
  }

  @deprecated("Use forRepeatedUnpackable", "0.11.14")
  def forRepeatedUnknownFieldUnpackable[C <: ExtendableMessage[C], E, T](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  )(fromBase: E => T, toBase: T => E): GeneratedExtension[C, Seq[T]] =
    forRepeatedUnpackable[C, E, T, Seq[T]](fieldNumber, listLens)(fromBase, toBase)

  def forRepeatedUnpackable[C <: ExtendableMessage[C], E, T, Coll](
      fieldNumber: Int,
      listLens: Lens[UnknownFieldSet.Field, Seq[E]]
  )(fromBase: E => T, toBase: T => E)(implicit
      ca: CollectionAdapter[T, Coll]
  ): GeneratedExtension[C, Coll] = {
    GeneratedExtension(
      ExtendableMessage
        .unknownFieldsLen(fieldNumber)
        .compose(listLens)
        .compose(repeatedLensUnpackable(fromBase, toBase))
    )
  }

  object Internal {
    def bool2Long(b: Boolean) = if (b) 1L else 0L
  }
}
