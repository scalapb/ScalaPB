package scalapb.textformat

import scalapb.descriptors._
import scalapb.{GeneratedMessage, textformat}
import scala.annotation.nowarn

object Printer {
  def print(t: GeneratedMessage, out: TextGenerator): Unit = print(t, out, false)

  @nowarn("cat=deprecation")
  private def print(t: GeneratedMessage, out: TextGenerator, printUnknownFields: Boolean): Unit = {
    t.companion.scalaDescriptor.fields.sortBy(_.number).foreach { fd =>
      fd.scalaType match {
        case ScalaType.Message(messageType) =>
          // Unlike PMessage, the original message retains unknown fields and its serialized size.
          // Only use the legacy accessor for messages: it drops implicit scalar defaults itself,
          // including negative floating-point zero, which TextFormat must preserve.
          val value = t.getFieldByNumber(fd.number)
          if (fd.isRepeated)
            value
              .asInstanceOf[Seq[GeneratedMessage]]
              .foreach(printMessageField(fd, _, out, printUnknownFields))
          else {
            val omitEmpty = omitEmptyMessage(fd, messageType)
            // The legacy accessor returns null for an unboxed default instance, even when
            // required fields inside that instance give it a nonzero serialized size.
            val message =
              if (value == null && (omitEmpty || fd.containingMessage.getOptions.getMapEntry))
                t.companion
                  .messageCompanionForFieldNumber(fd.number)
                  .defaultInstance
                  .asInstanceOf[GeneratedMessage]
              else value.asInstanceOf[GeneratedMessage]
            if (message != null && (!omitEmpty || message.serializedSize != 0))
              printMessageField(fd, message, out, printUnknownFields)
          }
        case _ => printField(fd, t.getField(fd), out)
      }
    }
    if (printUnknownFields) printUnknown(t.unknownFields, out)
  }

  private def printUnknown(
      fields: scalapb.UnknownFieldSet,
      out: TextGenerator,
      depth: Int = 0
  ): Unit =
    fields.asMap.toSeq.sortBy(_._1).foreach { case (number, field) =>
      def scalar(value: String): Unit = {
        out.add(number.toString).add(": ").addNewLine(value)
        ()
      }
      field.varint.foreach(v => scalar(TextFormatUtils.unsignedToString(v)))
      field.fixed32
        .foreach(v => scalar("0x" + ("00000000" + java.lang.Integer.toHexString(v)).takeRight(8)))
      field.fixed64.foreach(v =>
        scalar("0x" + ("0000000000000000" + java.lang.Long.toHexString(v)).takeRight(16))
      )
      field.lengthDelimited.foreach { bytes =>
        // Prefer a message representation for complete wire messages, as Java TextFormat does.
        // Bound speculative recursive decoding of unknown data.
        val nested =
          if (depth >= 100) None
          else
            scala.util.Try {
              val input   = bytes.newCodedInput()
              val builder = new scalapb.UnknownFieldSet.Builder
              var tag     = input.readTag()
              while (tag != 0) {
                builder.parseField(tag, input)
                tag = input.readTag()
              }
              input.checkLastTagWas(0)
              builder.result()
            }.toOption
        nested match {
          case Some(message) =>
            out.add(number.toString).addNewLine(" {").indent()
            printUnknown(message, out, depth + 1)
            out.outdent().addNewLine("}")
          case None => scalar("\"" + TextFormatUtils.escapeBytes(bytes) + "\"")
        }
      }
    }

  private def omitEmptyMessage(fd: FieldDescriptor, messageType: Descriptor): Boolean = {
    val options = fd.getOptions
      .extension(scalapb.options.ScalapbProto.field)
      .getOrElse(scalapb.options.FieldOptions.defaultInstance)
    val noBox =
      if (options.noBox.isDefined || options.required.isDefined)
        options.getNoBox || options.getRequired
      else
        messageType.getOptions
          .extension(scalapb.options.ScalapbProto.message)
          .exists(_.getNoBox)
    val sealedOneof =
      messageType.oneofs.exists(o => o.name == "sealed_value" || o.name == "sealed_value_optional")
    !fd.isRequired && !options.getRequired && fd.containingOneof.isEmpty &&
    !fd.containingMessage.getOptions.getMapEntry && (noBox || sealedOneof)
  }

  private def printMessageField(
      fd: FieldDescriptor,
      message: GeneratedMessage,
      out: TextGenerator,
      printUnknownFields: Boolean
  ): Unit = {
    out.add(fd.name).addNewLine(" {").indent()
    print(message, out, printUnknownFields)
    out.outdent().addNewLine("}")
    ()
  }

  private def isImplicitDefault(fd: FieldDescriptor, value: PValue): Boolean =
    !fd.hasPresence && !fd.containingMessage.getOptions.getMapEntry && (value match {
      case PInt(v)        => v == 0
      case PLong(v)       => v == 0L
      case PFloat(v)      => java.lang.Float.floatToRawIntBits(v) == 0
      case PDouble(v)     => java.lang.Double.doubleToRawLongBits(v) == 0L
      case PBoolean(v)    => !v
      case PString(v)     => v.isEmpty
      case PByteString(v) => v.isEmpty
      case PEnum(v)       => v.number == 0
      case _              => false
    })

  def print(p: scalapb.descriptors.PMessage, out: TextGenerator): Unit = {
    p.value.toSeq.sortBy(_._1.number).foreach { case (fd, value) =>
      printField(fd, value, out)
    }
  }

  def printToString(
      t: GeneratedMessage,
      singleLineMode: Boolean,
      escapeNonAscii: Boolean
  ): String = {
    printToString(t, singleLineMode, escapeNonAscii, false)
  }

  def printToString(
      t: GeneratedMessage,
      singleLineMode: Boolean,
      escapeNonAscii: Boolean,
      printUnknownFields: Boolean
  ): String = {
    val out = new TextGenerator(singleLine = singleLineMode, escapeNonAscii = escapeNonAscii)
    print(t, out, printUnknownFields)
    out.result()
  }

  def printField(fd: FieldDescriptor, value: scalapb.descriptors.PValue, out: TextGenerator): Unit =
    value match {
      case PRepeated(values) =>
        values.foreach(v => printSingleField(fd, v, out))
      case PEmpty                            =>
      case _ if isImplicitDefault(fd, value) =>
      case _                                 =>
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
        out.add(
          if (java.lang.Float.floatToRawIntBits(v) == Int.MinValue) "-0.0"
          else NumberUtils.floatToString(v)
        )
      case scalapb.descriptors.PDouble(v) =>
        out.add(
          if (java.lang.Double.doubleToRawLongBits(v) == Long.MinValue) "-0.0"
          else NumberUtils.doubleToString(v)
        )
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
