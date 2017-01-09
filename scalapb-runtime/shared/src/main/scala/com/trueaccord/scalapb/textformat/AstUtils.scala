package com.trueaccord.scalapb.textformat

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.trueaccord.scalapb.{GeneratedMessageCompanion, Message, GeneratedMessage}

import scala.util.Try

private[scalapb] object AstUtils {
  case class AstError(index: Int, error: String)

  private def flatten[T](s: Seq[Either[AstError, T]]): Either[AstError, Seq[T]] = {
    s.find(_.isLeft) match {
      case Some(Left(e)) => Left(e)
      case _ => Right(s.map(_.right.get))
    }
  }

  private def flatten2[T](s: Seq[Either[AstError, Seq[T]]]): Either[AstError, Seq[T]] = {
    s.find(_.isLeft) match {
      case Some(Left(e)) => Left(e)
      case _ => Right(s.flatMap(_.right.get))
    }
  }

  def parseMessage[T <: GeneratedMessage with Message[T]](v: GeneratedMessageCompanion[T], ast: PMessage): Either[AstError, T] = {
    parseUnsafe(v, ast).asInstanceOf[Either[AstError, T]]
  }

  def checkBigInt(p: PPrimitive, isSigned: Boolean = true, isLong: Boolean): Either[AstError, BigInt] = p match {
    case PIntLiteral(index, v) =>
      val negative = v.signum == -1
      val maxBits = if (isLong) {
        if (isSigned) 63 else 64
      } else {
        if (isSigned) 31 else 32
      }
      if (!isSigned && negative)
        Left(AstError(index, s"Number must be positive: $v"))
      else if (v.bitLength > maxBits)
        Left(AstError(index, s"Number out of range for ${if (isLong) 64 else 32}-bit ${if (isSigned) "signed" else "unsigned"} integer: $v"))
      else Right(v)
    case p => Left(AstError(p.index, s"Invalid input '${p.asString}'"))
  }

  def parseInt32(p: PPrimitive) = checkBigInt(p, isSigned = true, isLong = false).right.map(_.intValue)

  def parseUint32(p: PPrimitive) = checkBigInt(p, isSigned = false, isLong = false).right.map(_.intValue)

  def parseInt64(p: PPrimitive) = checkBigInt(p, isSigned = true, isLong = true).right.map(_.longValue)

  def parseUint64(p: PPrimitive) = checkBigInt(p, isSigned = false, isLong = true).right.map(_.longValue)

  private def parseUnsafe(v: GeneratedMessageCompanion[_], ast: PMessage): Either[AstError, Any] = {
    import scala.collection.JavaConversions._

    def parseDouble(p: PPrimitive): Either[AstError, Double] = p match {
      case PLiteral(_, value) =>
        val low = value.toLowerCase()
        low match {
          case "inf" | "infinity" => Right(Double.PositiveInfinity)
          case "-inf" | "-infinity" => Right(Double.NegativeInfinity)
          case "nan" => Right(Double.NaN)
          case _ => Try(value.toDouble).toOption.toRight(AstError(p.index, s"Invalid value for double: '$value'"))
        }
      case PIntLiteral(_, value) => Right(value.toDouble)
      case p => Left(AstError(p.index, s"Invalid input '${p.asString}', expected float"))
    }

    def parseFloat(p: PPrimitive): Either[AstError, Float] = p match {
      case PLiteral(_, value) =>
        val low = value.toLowerCase()
        low match {
          case "inf" | "inff" | "infinity" | "infinityf" => Right(Float.PositiveInfinity)
          case "-inf" | "-inff" | "-infinity" | "-infinityf" => Right(Float.NegativeInfinity)
          case "nan" | "nanf" => Right(Float.NaN)
          case _ => Try(value.toFloat).toOption.toRight(AstError(p.index, s"Invalid value for float: '$value'"))
        }
      case PIntLiteral(_, value) => Right(value.toFloat)
      case p => Left(AstError(p.index, s"Invalid input '${p.asString}', expected float"))
    }

    def parseBoolean(p: PPrimitive) = {
      def invalidInput(v: String): String = s"Invalid input '$v', expected 'true' or 'false'"

      p match {
        case PIntLiteral(index, v) =>
          if (v == 0) Right(false)
          else if (v == 1) Right(true)
          else Left(AstError(index, invalidInput(v.toString)))
        case PLiteral(index, v) =>
          v.toLowerCase match {
            case "t" | "true" => Right(true)
            case "f" | "false" => Right(false)
            case _ => Left(AstError(index, invalidInput(v.toString)))
          }
        case PBytes(index, v) => Left(AstError(p.index, invalidInput(v)))
      }
    }

    def parseString(p: PPrimitive): Either[AstError, String] = p match {
      case PBytes(_, value) =>
        TextFormatUtils.unescapeText(value).left.map {
          error => AstError(p.index, error.msg)
        }
      case p => Left(AstError(p.index, s"Invalid input '${p.asString}', expected string"))
    }

    def parseBytes(p: PPrimitive): Either[AstError, ByteString] = p match {
      case PBytes(_, value) =>
        TextFormatUtils.unescapeBytes(value).left.map {
          error => AstError(p.index, error.msg)
        }
      case _ => Left(AstError(p.index, "Unexpected input"))
    }

    def parsePrimitive(field: FieldDescriptor, p: PPrimitive): Either[AstError, Any] = field.getType match {
      case Type.DOUBLE => parseDouble(p)
      case Type.FLOAT => parseFloat(p)
      case Type.INT64 => parseInt64(p)
      case Type.UINT64 => parseUint64(p)
      case Type.INT32 => parseInt32(p)
      case Type.FIXED64 => parseUint64(p)
      case Type.FIXED32 => parseUint32(p)
      case Type.BOOL => parseBoolean(p)
      case Type.STRING => parseString(p)
      case Type.BYTES => parseBytes(p)
      case Type.UINT32 => parseUint32(p)
      case Type.SFIXED32 => parseInt32(p)
      case Type.SFIXED64 => parseInt64(p)
      case Type.SINT32 => parseInt32(p)
      case Type.SINT64 => parseInt64(p)
      case Type.GROUP => Right(AstError(p.index, "groups are not supported"))
      case Type.ENUM => {
        val enumDesc = v.enumCompanionForField(field).javaDescriptor
        p match {
          case PIntLiteral(index, num) =>
            Option(enumDesc.findValueByNumber(num.toInt))
              .toRight(AstError(index,
                s"""Expected Enum type "${enumDesc.getName}" has no value with number ${num.toString}"""))
          case PLiteral(index, name) =>
            Option(enumDesc.findValueByName(name))
              .toRight(AstError(index,
                s"""Expected Enum type "${enumDesc.getName}" has no value named "${name}""""))
          case p => Left(AstError(p.index, s"""Invalid value '${p.asString}, expected Enum type "${enumDesc.getName}""""))
        }
      }
      case Type.MESSAGE => Right(AstError(p.index, "This should not happen."))
    }

    def pfieldToValue(fd: FieldDescriptor, pfield: PField): Either[AstError, Seq[Any]] = pfield.value match {
      case arr: PArray =>
        if (!fd.isRepeated) Left(AstError(arr.index, s"Invalid input '[', expected ${fd.getType.toString.toLowerCase}"))
        else {
          if (fd.getType == Type.MESSAGE) {
            val idx = arr.values.indexWhere(!_.isInstanceOf[PMessage])
            if (idx != -1) Left(AstError(arr.index, s"Array contain a non-message value at index ${idx}"))
            else flatten(arr.values.map(t => parseUnsafe(v.messageCompanionForField(fd), t.asInstanceOf[PMessage])))
          } else {
            val idx = arr.values.indexWhere(!_.isInstanceOf[PPrimitive])
            if (idx != -1) Left(AstError(arr.index, s"Unexpected value at index $idx"))
            else flatten(arr.values.map(t => parsePrimitive(fd, t.asInstanceOf[PPrimitive])))
          }
        }
      case p: PPrimitive =>
        if (fd.getType == Type.MESSAGE) Left(AstError(p.index, "invalid value for message"))
        else {
          parsePrimitive(fd, p).right.map(Seq(_))
        }
      case p: PMessage =>
        if (fd.getType != Type.MESSAGE) Left(AstError(p.index, "invalid value for message"))
        else {
          parseUnsafe(v.messageCompanionForField(fd), p).right.map(Seq(_))
        }
    }

    val fieldMap: Map[String, FieldDescriptor] = v.javaDescriptor.getFields.map(f => (f.getName, f)).toMap

    val fields: Map[String, Seq[PField]] = ast.fields.groupBy(_.name)

    def fieldGroupToValue(name: String, group: Seq[PField]): Either[AstError, (FieldDescriptor, Any)] = {
      val fd: FieldDescriptor = fieldMap(name)
      flatten2(group.map(pfieldToValue(fd, _))).right.map {
        v =>
          if (fd.isRepeated) (fd -> v)
          else (fd -> v.last)
      }
    }

    val maybeMap = ast.fields.find(f => !fieldMap.contains(f.name)) match {
      case Some(f) => Left(AstError(f.index, s"Unknown field name '${f.name}'"))
      case None => flatten(fields.map((fieldGroupToValue _).tupled).toSeq)
    }
    maybeMap.right.map(t => v.fromFieldsMap(t.toMap))
  }

}
