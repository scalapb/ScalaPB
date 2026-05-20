package scalapb.textformat

import com.google.protobuf.descriptor.FieldDescriptorProto

import scalapb.descriptors._
import scalapb.descriptors.{FieldDescriptor, PValue}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.util.Try

private[scalapb] object AstUtils {
  case class AstError(position: Position, error: String)

  private def flatten[T](s: Seq[Either[AstError, T]]): Either[AstError, Vector[T]] = {
    s.foldLeft[Either[AstError, Vector[T]]](Right(Vector.empty)) {
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
      case (Right(xs), Right(x)) => Right(xs :+ x)
    }
  }

  def parseMessage[T <: GeneratedMessage](
      v: GeneratedMessageCompanion[T],
      ast: TMessage
  ): Either[AstError, T] = {
    parseUnsafe(v, ast).map(v.messageReads.read)
  }

  def checkBigInt(
      p: TPrimitive,
      isSigned: Boolean = true,
      isLong: Boolean
  ): Either[AstError, BigInt] = p match {
    case TIntLiteral(index, v) =>
      val negative = v.signum == -1
      val maxBits  = if (isLong) {
        if (isSigned) 63 else 64
      } else {
        if (isSigned) 31 else 32
      }
      if (!isSigned && negative)
        Left(AstError(index, s"Number must be positive: $v"))
      else if (v.bitLength > maxBits)
        Left(
          AstError(
            index,
            s"Number out of range for ${if (isLong) 64 else 32}-bit ${if (isSigned) "signed"
              else "unsigned"} integer: $v"
          )
        )
      else Right(v)
    case p => Left(AstError(p.position, s"Invalid input '${p.asString}'"))
  }

  def parseInt32(p: TPrimitive) =
    checkBigInt(p, isSigned = true, isLong = false).map(t => PInt(t.intValue))

  def parseUint32(p: TPrimitive) =
    checkBigInt(p, isSigned = false, isLong = false).map(t => PInt(t.intValue))

  def parseInt64(p: TPrimitive) =
    checkBigInt(p, isSigned = true, isLong = true).map(t => PLong(t.longValue))

  def parseUint64(p: TPrimitive) =
    checkBigInt(p, isSigned = false, isLong = true).map(t => PLong(t.longValue))

  private def parseUnsafe(
      v: GeneratedMessageCompanion[?],
      ast: TMessage
  ): Either[AstError, PMessage] = {
    def parseDouble(p: TPrimitive): Either[AstError, PDouble] = p match {
      case TLiteral(_, value) =>
        val low = value.toLowerCase()
        low match {
          case "inf" | "infinity"   => Right(PDouble(Double.PositiveInfinity))
          case "-inf" | "-infinity" => Right(PDouble(Double.NegativeInfinity))
          case "nan"                => Right(PDouble(Double.NaN))
          case _                    =>
            Try(PDouble(value.toDouble)).toOption
              .toRight(AstError(p.position, s"Invalid value for double: '$value'"))
        }
      case TIntLiteral(_, value) => Right(PDouble(value.toDouble))
      case p => Left(AstError(p.position, s"Invalid input '${p.asString}', expected float"))
    }

    def parseFloat(p: TPrimitive): Either[AstError, PFloat] = p match {
      case TLiteral(_, value) =>
        val low = value.toLowerCase()
        low match {
          case "inf" | "inff" | "infinity" | "infinityf" => Right(PFloat(Float.PositiveInfinity))
          case "-inf" | "-inff" | "-infinity" | "-infinityf" =>
            Right(PFloat(Float.NegativeInfinity))
          case "nan" | "nanf" => Right(PFloat(Float.NaN))
          case _              =>
            Try(PFloat(value.toFloat)).toOption
              .toRight(AstError(p.position, s"Invalid value for float: '$value'"))
        }
      case TIntLiteral(_, value) => Right(PFloat(value.toFloat))
      case p => Left(AstError(p.position, s"Invalid input '${p.asString}', expected float"))
    }

    def parseBoolean(p: TPrimitive): Either[AstError, PBoolean] = {
      def invalidInput(v: String): String = s"Invalid input '$v', expected 'true' or 'false'"

      p match {
        case TIntLiteral(index, v) =>
          if (v == 0) Right(PBoolean(false))
          else if (v == 1) Right(PBoolean(true))
          else Left(AstError(index, invalidInput(v.toString)))
        case TLiteral(index, v) =>
          v.toLowerCase match {
            case "t" | "true"  => Right(PBoolean(true))
            case "f" | "false" => Right(PBoolean(false))
            case _             => Left(AstError(index, invalidInput(v.toString)))
          }
        case TBytes(_, v) => Left(AstError(p.position, invalidInput(v)))
      }
    }

    def parseString(p: TPrimitive): Either[AstError, PString] = p match {
      case TBytes(_, value) =>
        TextFormatUtils.unescapeText(value).map(PString(_)).left.map { error =>
          AstError(p.position, error.msg)
        }
      case p => Left(AstError(p.position, s"Invalid input '${p.asString}', expected string"))
    }

    def parseBytes(p: TPrimitive): Either[AstError, PByteString] = p match {
      case TBytes(_, value) =>
        TextFormatUtils.unescapeBytes(value).map(PByteString(_)).left.map { error =>
          AstError(p.position, error.msg)
        }
      case _ => Left(AstError(p.position, "Unexpected input"))
    }

    def parsePrimitive(field: FieldDescriptor, p: TPrimitive): Either[AstError, PValue] =
      field.protoType match {
        case FieldDescriptorProto.Type.TYPE_DOUBLE   => parseDouble(p)
        case FieldDescriptorProto.Type.TYPE_FLOAT    => parseFloat(p)
        case FieldDescriptorProto.Type.TYPE_INT64    => parseInt64(p)
        case FieldDescriptorProto.Type.TYPE_UINT64   => parseUint64(p)
        case FieldDescriptorProto.Type.TYPE_INT32    => parseInt32(p)
        case FieldDescriptorProto.Type.TYPE_FIXED64  => parseUint64(p)
        case FieldDescriptorProto.Type.TYPE_FIXED32  => parseUint32(p)
        case FieldDescriptorProto.Type.TYPE_BOOL     => parseBoolean(p)
        case FieldDescriptorProto.Type.TYPE_STRING   => parseString(p)
        case FieldDescriptorProto.Type.TYPE_BYTES    => parseBytes(p)
        case FieldDescriptorProto.Type.TYPE_UINT32   => parseUint32(p)
        case FieldDescriptorProto.Type.TYPE_SFIXED32 => parseInt32(p)
        case FieldDescriptorProto.Type.TYPE_SFIXED64 => parseInt64(p)
        case FieldDescriptorProto.Type.TYPE_SINT32   => parseInt32(p)
        case FieldDescriptorProto.Type.TYPE_SINT64   => parseInt64(p)
        case FieldDescriptorProto.Type.TYPE_GROUP    =>
          Left(AstError(p.position, "groups are not supported"))
        case FieldDescriptorProto.Type.TYPE_ENUM => {
          val enumDesc = v.enumCompanionForFieldNumber(field.number).scalaDescriptor
          p match {
            case TIntLiteral(index, num) =>
              enumDesc.values
                .find(_.number == num.toInt)
                .map(PEnum(_))
                .toRight(
                  AstError(
                    index,
                    s"""Expected Enum type "${enumDesc.asProto.getName}" has no value with number ${num.toString}"""
                  )
                )
            case TLiteral(index, name) =>
              enumDesc.values
                .find(_.name == name)
                .map(PEnum(_))
                .toRight(
                  AstError(
                    index,
                    s"""Expected Enum type "${enumDesc.asProto.getName}" has no value named "${name}""""
                  )
                )
            case p =>
              Left(
                AstError(
                  p.position,
                  s"""Invalid value '${p.asString}, expected Enum type "${enumDesc.asProto.getName}""""
                )
              )
          }
        }
        case _ => Left(AstError(p.position, "This should not happen."))
      }

    def pfieldToValue(fd: FieldDescriptor, pfield: TField): Either[AstError, PValue] =
      pfield.value match {
        case arr: TArray =>
          if (!fd.isRepeated)
            Left(
              AstError(
                arr.position,
                s"Invalid input '[', expected ${fd.protoType.toString.toLowerCase}"
              )
            )
          else {
            if (fd.protoType.isTypeMessage) {
              val idx = arr.values.indexWhere(!_.isInstanceOf[TMessage])
              if (idx != -1)
                Left(AstError(arr.position, s"Array contain a non-message value at index ${idx}"))
              else
                flatten(
                  arr.values
                    .map(t =>
                      parseUnsafe(
                        v.messageCompanionForFieldNumber(fd.number),
                        t.asInstanceOf[TMessage]
                      )
                    )
                    .toVector
                ).map(PRepeated(_))
            } else {
              val idx = arr.values.indexWhere(!_.isInstanceOf[TPrimitive])
              if (idx != -1) Left(AstError(arr.position, s"Unexpected value at index $idx"))
              else
                flatten(
                  arr.values.map(t => parsePrimitive(fd, t.asInstanceOf[TPrimitive])).toVector
                ).map(PRepeated(_))
            }
          }
        case p: TPrimitive =>
          if (fd.protoType.isTypeMessage) Left(AstError(p.position, "invalid value for message"))
          else {
            parsePrimitive(fd, p)
          }
        case p: TMessage =>
          if (!fd.protoType.isTypeMessage) Left(AstError(p.position, "invalid value for message"))
          else {
            parseUnsafe(v.messageCompanionForFieldNumber(fd.number), p)
          }
      }

    val fieldMap: Map[String, FieldDescriptor] =
      v.scalaDescriptor.fields.map(f => (f.name, f)).toMap

    val fields: Map[String, Seq[TField]] = ast.fields.groupBy(_.name)

    def fieldGroupToValue(
        name: String,
        group: Seq[TField]
    ): Either[AstError, (FieldDescriptor, PValue)] = {
      val fd: FieldDescriptor                      = fieldMap(name)
      val values: Either[AstError, Vector[PValue]] = flatten(group.map(pfieldToValue(fd, _)))

      values.map { (t: Seq[PValue]) =>
        if (!fd.isRepeated) fd -> t.last
        else
          fd -> PRepeated(t.foldLeft(Vector[PValue]()) {
            case (xs, PRepeated(ys)) => xs ++ ys
            case (xs, t: PValue)     => xs :+ t
          })
      }
    }

    val maybeMap: Either[AstError, Vector[(FieldDescriptor, PValue)]] =
      ast.fields.find(f => !fieldMap.contains(f.name)) match {
        case Some(f) => Left(AstError(f.position, s"Unknown field name '${f.name}'"))
        case None    => flatten(fields.map((fieldGroupToValue _).tupled).toVector)
      }
    maybeMap.map(t => PMessage(t.toMap))
  }
}
