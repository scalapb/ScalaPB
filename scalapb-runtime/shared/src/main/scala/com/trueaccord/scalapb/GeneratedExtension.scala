package com.trueaccord.scalapb

import com.google.protobuf.GeneratedMessageV3.ExtendableMessage
import com.google.protobuf.{ByteString, UnknownFieldSet}
import com.trueaccord.scalapb.GeneratedExtension.JavaExtendee

case class GeneratedExtension[J <: JavaExtendee[J], +T](get: J => T)

object GeneratedExtension {
  type JavaExtendee[S <: ExtendableMessage[S]] = com.google.protobuf.Message with com.google.protobuf.GeneratedMessageV3.ExtendableMessage[S]

  import scala.collection.JavaConverters._

  /* To be used only be generated code */
  def readMessageFromByteString[T <: GeneratedMessage with Message[T]](cmp: GeneratedMessageCompanion[T])(bs: ByteString): T = {
    cmp.parseFrom(bs.newCodedInput())
  }

  def forSingularUnknownField[J <: JavaExtendee[J], E, T](
    fieldNumber: Int, listGetter: UnknownFieldSet.Field => java.util.List[E])(convert: E => T, default: T): GeneratedExtension[J, T] =
    GeneratedExtension {
      extendee =>
        val list = listGetter(extendee.getUnknownFields.getField(fieldNumber))
        if (list.isEmpty) default
        else convert(list.get(0))
    }

  def forOptionalUnknownField[J <: JavaExtendee[J], E, T](
    fieldNumber: Int, listGetter: UnknownFieldSet.Field => java.util.List[E])(convert: E => T): GeneratedExtension[J, Option[T]] =
    GeneratedExtension {
      extendee =>
        val list = listGetter(extendee.getUnknownFields.getField(fieldNumber))
        if (list.isEmpty) None
        else Some(convert(list.get(0)))
    }


  def forRepeatedUnknownField[J <: JavaExtendee[J], E, T](
    fieldNumber: Int, listGetter: UnknownFieldSet.Field => java.util.List[E])(convert: E => T): GeneratedExtension[J, Vector[T]] = {
    GeneratedExtension {
      extendee: J =>
        listGetter(extendee.getUnknownFields.getField(fieldNumber)).asScala.map(convert)(scala.collection.breakOut)
    }
  }

  def forExtension[J <: JavaExtendee[J], T](convert: J => T) = {
    GeneratedExtension(convert)
  }
}

object Implicits {
  implicit class JavaMessageExt[J <: JavaExtendee[J]](val javaExtendee: J) extends AnyVal {
    def extension[T](ext: GeneratedExtension[J, T]): T = ext.get(javaExtendee)
  }
}
