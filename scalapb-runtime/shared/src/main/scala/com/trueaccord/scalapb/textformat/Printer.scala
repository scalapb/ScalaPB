package com.trueaccord.scalapb.textformat

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.EnumValueDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.trueaccord.scalapb.{textformat, GeneratedMessage}

object Printer {
  def print(t: GeneratedMessage, out: TextGenerator): Unit = {
    t.getAllFields.toSeq.sortBy(_._1.getNumber).foreach {
      case (fd, value) =>
        printField(fd, value, out)
    }
  }

  def printToString(t: GeneratedMessage, singleLineMode: Boolean, escapeNonAscii: Boolean) = {
    val out = new TextGenerator(singleLine = singleLineMode, escapeNonAscii = escapeNonAscii)
    print(t, out)
    out.result()
  }

  def printField(fd: FieldDescriptor, value: Any, out: TextGenerator) = {
    if (fd.isRepeated) value.asInstanceOf[Seq[Any]].foreach {
      v: Any => printSingleField(fd, v, out)
    } else {
      printSingleField(fd, value, out)
    }
  }

  def printSingleField(fd: FieldDescriptor, value: Any, out: TextGenerator) = {
    out.add(fd.getName)
    if (fd.getType == Type.MESSAGE) {
      out.addNewLine(" {").indent()
    } else {
      out.add(": ")
    }
    printFieldValue(fd, value, out)
    if (fd.getType == Type.MESSAGE) {
      out.outdent().addNewLine("}")
    } else {
      out.addNewLine("")
    }
  }

  def printFieldValue(fd: FieldDescriptor, value: Any, out: TextGenerator): Unit =
    fd.getType match {
      case Type.INT32 | Type.SINT32 | Type.SFIXED32 =>
        out.add(value.asInstanceOf[Int].toString)
      case Type.INT64 | Type.SINT64 | Type.SFIXED64 =>
        out.add(value.asInstanceOf[Long].toString)
      case Type.BOOL =>
        out.add(value.asInstanceOf[Boolean].toString)
      case Type.FLOAT =>
        out.add(value.asInstanceOf[Float].toString)
      case Type.DOUBLE =>
        out.add(value.asInstanceOf[Double].toString)
      case Type.UINT32 | Type.FIXED32 =>
        out.add(TextFormatUtils.unsignedToString(value.asInstanceOf[Int]))
      case Type.UINT64 | Type.FIXED64 =>
        out.add(TextFormatUtils.unsignedToString(value.asInstanceOf[Long]))
      case Type.STRING =>
        out
          .add("\"")
          .addMaybeEscape(value.asInstanceOf[String])
          .add("\"")

      case Type.BYTES =>
        out
          .add("\"")
          .add(textformat.TextFormatUtils.escapeBytes(value.asInstanceOf[ByteString]))
          .add("\"")

      case Type.ENUM =>
        out.add(value.asInstanceOf[EnumValueDescriptor].getName)

      case Type.GROUP => ???

      case Type.MESSAGE =>
        print(value.asInstanceOf[GeneratedMessage], out)
    }
}
