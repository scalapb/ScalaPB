package com.trueaccord.scalapb

import com.google.protobuf.{ByteString, CodedInputStream, InvalidProtocolBufferException}

import _root_.scalapb.UnknownFieldSet

case class GeneratedExtension[C <: ExtendableMessage[C], +T](get: C => T)

object GeneratedExtension {
  /* To be used only be generated code */
  def readMessageFromByteString[T <: GeneratedMessage with Message[T]](cmp: GeneratedMessageCompanion[T])(bs: ByteString): T = {
    cmp.parseFrom(bs.newCodedInput())
  }

  def forSingularUnknownField[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int, listGetter: UnknownFieldSet.Field => Vector[E])(convert: E => T, default: T): GeneratedExtension[C, T] =
    GeneratedExtension {
      extendee =>
        (for {
          field <- extendee.unknownFields.getField(fieldNumber)
          elem <- listGetter(field).lastOption
        } yield convert(elem)).getOrElse(default)
    }

  def forOptionalUnknownField[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int, listGetter: UnknownFieldSet.Field => Vector[E])(convert: E => T): GeneratedExtension[C, Option[T]] =
    GeneratedExtension {
      extendee =>
        extendee.unknownFields.getField(fieldNumber) match {
          case None => None
          case Some(field) => listGetter(field).lastOption.map(convert)
        }
    }


  def forRepeatedUnknownField[C <: ExtendableMessage[C], E, T](
    fieldNumber: Int, listGetter: UnknownFieldSet.Field => Vector[E], unpack: CodedInputStream => E)(convert: E => T): GeneratedExtension[C, Vector[T]] = {
    GeneratedExtension {
      extendee: C =>
        extendee.unknownFields.getField(fieldNumber) match {
          case None => Vector.empty
          case Some(field) =>
            extendee.unknownFields.getField(fieldNumber) match {
              case None => Vector.empty
              case Some(field) =>
                if (field.lengthDelimited.nonEmpty && unpack != null) {
                  if (listGetter(field).nonEmpty) {
                    throw new InvalidProtocolBufferException("Mixed packed and unpacked data.")
                  }
                  val v = Vector.newBuilder[T]
                  field.lengthDelimited.foreach {
                    ld =>
                      val ci = ld.newCodedInput
                      while (ci.getBytesUntilLimit > 0) {
                        v += convert(unpack(ci))
                      }
                  }
                  v.result()
                } else {
                  listGetter(field).map(convert)
                }
            }
        }
    }
  }
}
