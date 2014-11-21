package com.trueaccord.scalapb

import com.google.protobuf.CodedInputStream

object LiteParser {
  def parseFrom[A <: GeneratedMessage with Message[A]](companion: GeneratedMessageCompanion[A], input: CodedInputStream): A = {
    companion.defaultInstance.mergeFrom(input)
  }

  def readMessage[A](input: CodedInputStream, message: Message[A]): A = {
    val length = input.readRawVarint32()
    val oldLimit = input.pushLimit(length)
    val result: A = message.mergeFrom(input)
    input.checkLastTagWas(0)
    input.popLimit(oldLimit)
    result
  }
}
