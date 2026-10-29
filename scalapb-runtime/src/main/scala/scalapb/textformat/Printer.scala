package scalapb.textformat

import scalapb.descriptors.{FieldDescriptor, PEmpty, PRepeated, ScalaType}
import scalapb.options.ScalapbProto
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, textformat}

object Printer {
  def print(t: GeneratedMessage, out: TextGenerator): Unit = {
    print(t.toPMessage, out)
  }

  def print(p: scalapb.descriptors.PMessage, out: TextGenerator): Unit = print(p)(out)

  private def print(
      p: scalapb.descriptors.PMessage,
      companion: Companion = Companion(null), // companion of `p` if omitting default values
  )(implicit out: TextGenerator): Unit = {
    val defaults = companion.instance
    p.value.toSeq.sortBy(_._1.number).foreach { case (fd, value) =>
      if ((defaults eq null) || defaults.getField(fd) != value || companion.encoded(fd))
        printField(fd, value, companion)
    }
  }

  def printToString(t: GeneratedMessage, singleLineMode: Boolean, escapeNonAscii: Boolean): String =
    printToString(
      t,
      PrinterOptions.default.withSingleLineMode(singleLineMode).withEscapeNonAscii(escapeNonAscii)
    )

  private[scalapb] def printToString(t: GeneratedMessage, options: PrinterOptions): String = {
    implicit val out: TextGenerator =
      new TextGenerator(options.singleLineMode, options.escapeNonAscii)
    print(t.toPMessage, if (options.omitDefaults) t.companion else null)
    out.result()
  }

  def printField(fd: FieldDescriptor, value: scalapb.descriptors.PValue, out: TextGenerator): Unit =
    printField(fd, value)(out)

  private def printField(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      companion: Companion = Companion(null),
  )(implicit out: TextGenerator): Unit =
    value match {
      case PRepeated(values) =>
        values.foreach(printSingleField(fd, _, companion))
      case PEmpty =>
      case _      =>
        printSingleField(fd, value, companion); ()
    }

  def printSingleField(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      out: TextGenerator
  ): TextGenerator = printSingleField(fd, value)(out)

  private def printSingleField(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      companion: Companion = Companion(null),
  )(implicit out: TextGenerator): TextGenerator = {
    out.add(fd.name)
    value match {
      case scalapb.descriptors.PMessage(_) =>
        out.addNewLine(" {").indent()
        printFieldValue(fd, value, companion)
        out.outdent().addNewLine("}")
      case _ =>
        out.add(": ")
        printFieldValue(fd, value, companion)
        out.addNewLine("")
    }
  }

  def printFieldValue(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      out: TextGenerator
  ): Unit = printFieldValue(fd, value)(out)

  private def printFieldValue(
      fd: FieldDescriptor,
      value: scalapb.descriptors.PValue,
      companion: Companion = Companion(null),
  )(implicit out: TextGenerator): Unit = {
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
        print(e, companion.forField(fd))
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

  private implicit class Companion(private val self: GeneratedMessageCompanion[?]) extends AnyVal {
    def instance: GeneratedMessage =
      if (null eq self) null
      else self.defaultInstance.asInstanceOf[GeneratedMessage]

    def forField(fd: FieldDescriptor): GeneratedMessageCompanion[?] =
      if (null eq self) null
      else self.messageCompanionForFieldNumber(fd.number)

    // The binary encoding writes these fields even at their default value
    def encoded(fd: FieldDescriptor): Boolean =
      fd.isRequired || fd.getOptions.extension(ScalapbProto.field).exists(_.getRequired) ||
        fd.scalaType.isInstanceOf[ScalaType.Message] && forField(fd).instance.serializedSize != 0

  }

}
