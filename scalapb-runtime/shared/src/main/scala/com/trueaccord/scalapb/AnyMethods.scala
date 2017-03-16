package com.trueaccord.scalapb

import com.google.protobuf.ByteString

trait AnyMethods {
  def typeUrl: String
  def value: ByteString

  def is[A <: GeneratedMessage with Message[A]](implicit cmp: GeneratedMessageCompanion[A]) = {
    AnyMethods.typeNameFromTypeUrl(typeUrl) == cmp.scalaDescriptor.fullName
  }

  def unpack[A <: GeneratedMessage with Message[A]](implicit cmp: GeneratedMessageCompanion[A]) = {
    require(is[A], s"Type of the Any message does not match the given class.")
    cmp.parseFrom(value.newCodedInput())
  }
}

object AnyMethods {
  private def typeNameFromTypeUrl(typeUrl: String): String = {
    typeUrl.split("/").lastOption.getOrElse(typeUrl)
  }
}


trait AnyCompanionMethods {
  def pack[A <: GeneratedMessage with Message[A]](generatedMessage: A): com.google.protobuf.any.Any =
    pack(generatedMessage, "type.googleapis.com/")

  def pack[A <: GeneratedMessage with Message[A]](generatedMessage: A, urlPrefix: String): com.google.protobuf.any.Any =
    com.google.protobuf.any.Any(
      typeUrl = if (urlPrefix.endsWith("/"))
        urlPrefix + generatedMessage.companion.scalaDescriptor.fullName else
        urlPrefix + "/" + generatedMessage.companion.scalaDescriptor.fullName,
      value = generatedMessage.toByteString)
}
