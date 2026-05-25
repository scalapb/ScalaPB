package scalapb.textformat

import scalapb.descriptors.{FieldDescriptor, PEmpty, PRepeated}
import scalapb.{GeneratedMessage, textformat}

object Printer {
  def print(t: GeneratedMessage, out: TextGenerator): Unit = {
    print(t.toPMessage, out)
  }

  def print(p: scalapb.descriptors.PMessage, out: TextGenerator): Unit = {
    p.value.toSeq.sortBy(_._1.number).foreach { case (fd, value) =>
      printField(fd, value, out)
    }
  }

  def printToString(t: GeneratedMessage, singleLineMode: Boolean, escapeNonAscii: Boolean) = {
    val out = new TextGenerator(singleLine = singleLineMode, escapeNonAscii = escapeNonAscii)
    print(t, out)
    out.result()
  }

  def printField(fd: FieldDescriptor, value: scalapb.descriptors.PValue, out: TextGenerator): Unit =
    value match {
      case PRepeated(values) =>
        values.foreach(v => printSingleField(fd, v, out))
      case PEmpty =>
      case _      =>
        printSingleField(fd, value, out); ()
    }

  def printSingleField(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      out: TextGenerator
  ): TextGenerator = {
    out.add(fd.name)
    value match {
      case scalapb.descriptors.PMessage(_) =>
        out.addNewLine(" {").indent()
        printFieldValue(fd, value, out)
        out.outdent().addNewLine("}")
      case _ =>
        out.add(": ")
        printFieldValue(fd, value, out)
        out.addNewLine("")
    }
  }

  def printFieldValue(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      out: TextGenerator
  ): Unit = {
    value match {
      case scalapb.descriptors.PInt(v) =>
        if (fd.protoType.isTypeUint32 || fd.protoType.isTypeFixed32)
          out.add(TextFormatUtils.unsignedToString(v))
        else
          out.add(v.toString)
      case scalapb.descriptors.PLong(v) =>
        if (fd.protoType.isTypeUint64 || fd.protoType.isTypeFixed64)
          out.add(TextFormatUtils.unsignedToString(v))
        else
          out.add(v.toString)
      case scalapb.descriptors.PBoolean(v) =>
        out.add(v.toString)
      case scalapb.descriptors.PFloat(v) =>
        out.add(NumberUtils.floatToString(v))
      case scalapb.descriptors.PDouble(v) =>
        out.add(NumberUtils.doubleToString(v))
      case scalapb.descriptors.PEnum(v) =>
        if (!v.isUnrecognized)
          out.add(v.name)
        else
          out.add(v.number.toString)
      case e: scalapb.descriptors.PMessage =>
        print(e, out)
      case scalapb.descriptors.PString(v) =>
        out
          .add("\"")
          .addMaybeEscape(v)
          .add("\"")
      case scalapb.descriptors.PByteString(v) =>
        out
          .add("\"")
          .add(textformat.TextFormatUtils.escapeBytes(v))
          .add("\"")
      case scalapb.descriptors.PRepeated(_) =>
        throw new RuntimeException("Should not happen.")
      case scalapb.descriptors.PEmpty =>
        throw new RuntimeException("Should not happen.")
    }
    ()
  }
}
