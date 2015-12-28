package com.trueaccord.scalapb.textformat

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.trueaccord.scalapb.{GeneratedMessageCompanion, Message, GeneratedMessage}

import scala.util.Try

private[scalapb] object AstUtils {
  case class AstOneError(index: Int, s: String)

  object AstOneError {
    implicit def toAstErrors(b: AstOneError) = AstErrors(Seq(b))
  }

  case class AstErrors(errors: Seq[AstOneError]) {
    def ++(other: AstErrors) = AstErrors(errors ++ other.errors)
  }

  object AstErrors {
    def flatten[T](e: Seq[Either[AstErrors, Seq[T]]]): Either[AstErrors, Seq[T]] =
      e.foldLeft(Right(Seq.empty): Either[AstErrors, Seq[T]])({
        case (Right(s), Right(b)) => Right(s ++ b)
        case (Right(s), Left(b)) => Left(b)
        case (l@Left(s), Right(b)) => l
        case (Left(s), Left(b)) => Left(s ++ b)
      })

    def seq1[T](e: Seq[Either[AstOneError, T]]): Either[AstErrors, Seq[T]] =
      e.foldLeft(Right(Seq.empty): Either[AstErrors, Seq[T]])({
        case (Right(s), Right(b)) => Right(s :+ b)
        case (Right(s), Left(b)) => Left(b)
        case (l@Left(s), Right(b)) => l
        case (Left(s), Left(b)) => Left(s ++ b)
      })

    def seq[T](e: Seq[Either[AstErrors, T]]): Either[AstErrors, Seq[T]] =
      e.foldLeft(Right(Seq.empty): Either[AstErrors, Seq[T]])({
        case (Right(s), Right(b)) => Right(s :+ b)
        case (Right(s), Left(b)) => Left(b)
        case (l@Left(s), Right(b)) => l
        case (Left(s), Left(b)) => Left(s ++ b)
      })
  }

  def parseMessage[T <: GeneratedMessage with Message[T]](v: GeneratedMessageCompanion[T], ast: PMessage): Either[AstErrors, T] = {
    parseUnsafe(v, ast).asInstanceOf[Either[AstErrors, T]]
  }

  private def parseUnsafe(v: GeneratedMessageCompanion[_], ast: PMessage): Either[AstErrors, Any] = {
    import scala.collection.JavaConversions._
    val fieldMap: Map[String, FieldDescriptor] = v.descriptor.getFields.map(f => (f.getName, f)).toMap

    val fields: Map[String, Seq[PField]] = ast.fields.groupBy(_.name)

    def parseDouble(p: PPrimitive): Either[AstOneError, Double] = p match {
      case PLiteral(_, value) =>
        val low = value.toLowerCase()
        low match {
          case "inf" | "infinity" => Right(Double.PositiveInfinity)
          case "-inf" | "-infinity" => Right(Double.NegativeInfinity)
          case "nan" => Right(Double.NaN)
          case _ => Try(value.toDouble).toOption.toRight(AstOneError(p.index, s"Invalid value for double: '$value'"))
        }
      case PIntLiteral(_, value) => Right(value.toDouble)
      case _ => Left(AstOneError(p.index, "Unexpected input"))
    }

    def parseFloat(p: PPrimitive): Either[AstOneError, Float] = p match {
      case PLiteral(_, value) =>
        val low = value.toLowerCase()
        low match {
          case "inf" | "inff" | "infinity" | "infinityf" => Right(Float.PositiveInfinity)
          case "-inf" | "-inff" | "-infinity" | "-infinityf" => Right(Float.NegativeInfinity)
          case "nan" => Right(Float.NaN)
          case _ => Try(value.toFloat).toOption.toRight(AstOneError(p.index, s"Invalid value for float: '$value'"))
        }
      case PIntLiteral(_, value) => Right(value.toFloat)
      case _ => Left(AstOneError(p.index, "Unexpected input"))
    }

    def checkBigInt(p: PPrimitive, isSigned: Boolean = true, isLong: Boolean): Either[AstOneError, BigInt] = p match {
      case PIntLiteral(index, v) =>
        val negative = v.signum == -1
        val maxBits = if (isLong) {
          if (isSigned) 63 else 64
        } else {
          if (isSigned) 31 else 32
        }
        if ((isSigned || !negative) && // if unsigned, must be non-negative
          v.bitLength <= maxBits) Right(v)
        else Left(AstOneError(index, "integer not in allowed range"))
      case p => Left(AstOneError(p.index, "Unexpected input"))
    }

    def parseInt32(p: PPrimitive) = checkBigInt(p, isSigned = true, isLong = false).right.map(_.intValue)

    def parseUint32(p: PPrimitive) = checkBigInt(p, isSigned = false, isLong = false).right.map(_.intValue)

    def parseInt64(p: PPrimitive) = checkBigInt(p, isSigned = true, isLong = true).right.map(_.longValue)

    def parseUint64(p: PPrimitive) = checkBigInt(p, isSigned = false, isLong = true).right.map(_.longValue)

    def parseBoolean(p: PPrimitive) = p match {
      case PIntLiteral(index, v) =>
        if (v == 0) Right(false)
        else if (v == 1) Right(true)
        else Left(AstOneError(index, s"Unexpected value '$v'"))
      case PLiteral(index, v) =>
        v.toLowerCase match {
          case "t" | "true" => Right(true)
          case "f" | "false" => Right(false)
          case _ => Left(AstOneError(index, s"Unexpected value '$v'"))
        }
      case p => Left(AstOneError(p.index, "Unexpected input"))
    }

    def parseString(p: PPrimitive) = p match {
      case PBytes(_, value) => Right(value)
      case _ => Left(AstOneError(p.index, "Unexpected input"))
    }

    def parseBytes(p: PPrimitive) = p match {
      case PBytes(_, value) => Right(ByteString.copyFromUtf8(value))
      case _ => Left(AstOneError(p.index, "Unexpected input"))
    }

    def parsePrimitive(t: Type, p: PPrimitive): Either[AstOneError, Any] = t match {
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
      case Type.GROUP => Right(AstOneError(p.index, "groups are not supported"))
      case Type.MESSAGE | Type.ENUM => Right(AstOneError(p.index, "This should not happen."))
    }

    def pfieldToValue(fd: FieldDescriptor, pfield: PField): Either[AstErrors, Seq[Any]] = pfield.value match {
      case arr: PPrimitiveArray =>
        if (!fd.isRepeated) Left(AstOneError(arr.index, "unexpected array for non-repeated field."))
        else if (fd.getType == Type.MESSAGE) Left(AstOneError(arr.index, "expected array of messages"))
        else AstErrors.seq1(arr.values.map(parsePrimitive(fd.getType, _)))
      case p: PPrimitive =>
        if (fd.getType == Type.MESSAGE) Left(AstOneError(p.index, "invalid value for message"))
        else {
          parsePrimitive(fd.getType, p).fold(l => Left(AstOneError.toAstErrors(l)), r => Right(Seq(r)))
        }
      case arr: PMessageArray =>
        if (!fd.isRepeated) Left(AstOneError(arr.index, "unexpected array for non-repeated field."))
        else if (fd.getType != Type.MESSAGE) Left(AstOneError(arr.index, "expected array of primitives"))
        else AstErrors.seq(arr.values.map(parseUnsafe(v.messageCompanionForField(fd), _)))
      case p: PMessage =>
        if (fd.getType == Type.MESSAGE) Left(AstOneError(p.index, "invalid value for message"))
        else {
          parseUnsafe(v.messageCompanionForField(fd), p).fold(l => Left(l), r => Right(Seq(r)))
        }
    }

    def fieldGroupToValue(name: String, group: Seq[PField]): Either[AstErrors, (FieldDescriptor, Any)] = {
      val fd: FieldDescriptor = fieldMap(name)
      AstErrors.flatten(group.map(pfieldToValue(fd, _))).right.map {
        v =>
          if (fd.isOptional) (fd -> v.lastOption)
          else if (fd.isRequired) (fd -> v.last)
          else (fd -> v)
      }
    }

    val unknownNames = ast.fields.collect {
      case f if !fieldMap.contains(f.name) => AstOneError(f.index, s"unknown field name '${f.name}'")
    }
    if (unknownNames.nonEmpty) Left(AstErrors(unknownNames))
    else {
      AstErrors.seq(fields.map((fieldGroupToValue _).tupled).toSeq).right.map(z => v.fromFieldsMap(z.toMap))
    }
  }

}
