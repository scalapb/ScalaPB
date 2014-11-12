package com.trueaccord.scalapb

import com.google.protobuf.{InvalidProtocolBufferException, CodedInputStream}

import collection.mutable
import Descriptors._

import scala.collection.mutable.ArrayBuffer

object LiteParser {
  def parseFrom[A <: GeneratedMessage](companion: GeneratedMessageCompanion[A], input: CodedInputStream): A = {
    val fields = mutable.Map[Int, Any]()
    parseInternal(input, companion.descriptor, fields)
    finalizeFromMap(companion, fields)
  }

  private def parseInternal(input: CodedInputStream, descriptor: MessageDescriptor, fields: mutable.Map[Int, Any]): Unit = {
    var done = false
    while (!done) {
      val tag = input.readTag()
      if (tag == 0) done = true
      else {
        val fieldNumber = tag >> 3
        val wireFormat = tag & 7
        descriptor.fields.find(_.number == fieldNumber) match {
          case Some(field) if !field.isPacked =>
            if (WireType.fromType(field.fieldType.fieldType) != wireFormat)
              throw new InvalidProtocolBufferException("Unexpected wire type.")
            val r: Any = field.fieldType match {
              case MessageType(otherDescriptor) =>
                val submap: mutable.Map[Int, Any] = if (field.label != Repeated) {
                  fields.getOrElse(fieldNumber, mutable.Map[Int, Any]()).asInstanceOf[mutable.Map[Int, Any]]
                } else mutable.Map[Int, Any]()
                val length = input.readRawVarint32()
                val oldLimit = input.pushLimit(length)
                parseInternal(input, otherDescriptor, submap)
                input.popLimit(oldLimit)
                submap
              case EnumType(d) =>
                val v = WireType.read(input, field.fieldType.fieldType)
                d.companion.fromValue(v.asInstanceOf[Integer])
              case _ =>
                WireType.read(input, field.fieldType.fieldType)
            }
            if (field.label == Repeated) {
              fields.getOrElseUpdate(field.number, mutable.ArrayBuffer[Any]()).asInstanceOf[mutable.ArrayBuffer[Any]] += r
            } else {
              fields.update(field.number, r)
            }
          case Some(field) if field.isPacked =>
            if (wireFormat != WireType.WIRETYPE_LENGTH_DELIMITED)
              throw new InvalidProtocolBufferException("Unexpected wire type.")
            val length = input.readRawVarint32()
            val oldLimit = input.pushLimit(length)
            val buf = fields.getOrElseUpdate(fieldNumber, new ArrayBuffer[Any]()).asInstanceOf[ArrayBuffer[Any]]
            while (!input.isAtEnd) {
              buf += WireType.read(input, field.fieldType.fieldType)
            }
            input.popLimit(oldLimit)
          case None =>
            throw new InvalidProtocolBufferException("Unknown field are unsupported.")
        }
      }
    }
  }

  // The map from parseInternal contains messages as maps Int->Any and optional values are
  // not wrapped in Some(). Here we convert all those maps to actual instances, and wrap
  // the optional fields in Some().
  private def finalizeFromMap[A <: GeneratedMessage](companion: GeneratedMessageCompanion[A],
                                                     fields: mutable.Map[Int, Any]): A = {
    fields.foreach {
      case (number, value) =>
        val field = companion.descriptor.fields.find(_.number == number).get
        if (field.label == Repeated) {
          field.fieldType match {
            case MessageType(descriptor) =>
              fields(number) = value.asInstanceOf[ArrayBuffer[mutable.Map[Int, Any]]].map {
                submap =>
                  finalizeFromMap(descriptor.companion, submap)
              }.toSeq
            case _ =>
          }
        } else {
          val convertedValue = field.fieldType match {
            case MessageType(descriptor) =>
              finalizeFromMap(descriptor.companion, value.asInstanceOf[mutable.Map[Int, Any]])
            case _ => fields(number)
          }
          val withOptional = if (field.label == Optional) Some(convertedValue) else convertedValue
          fields(number) = withOptional
        }
    }
    companion.fromFieldsMap(fields.toMap)
  }

}
