package scalapb.textformat

import com.google.protobuf.descriptor.FieldDescriptorProto

import scalapb.descriptors._
import scalapb.descriptors.{FieldDescriptor, PValue}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.util.Try
import ParsedValue.{Value => Parsed, Message => ParsedMessage, Repeated => ParsedRepeated, Scalar}

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
      ast: TMessage,
      typeRegistry: TypeRegistry = TypeRegistry.empty
  ): Either[AstError, T] = {
    parseUnsafe(v, ast, typeRegistry).map { parsed =>
      if (parsed.hasExtensions) v.parseFrom(parsed.toByteArray)
      else v.messageReads.read(parsed.toPValue)
    }
  }

  def checkBigInt(
      p: TPrimitive,
      isSigned: Boolean = true,
      isLong: Boolean
  ): Either[AstError, BigInt] = p match {
    case TLiteral(position, value) =>
      new Parser("").tryBigInt(value) match {
        case Some(number) => checkBigInt(TIntLiteral(position, number), isSigned, isLong)
        case None         => Left(AstError(position, s"Invalid input '$value'"))
      }
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

  private def parseDouble(p: TPrimitive): Either[AstError, PDouble] = p match {
    case TLiteral(_, value) =>
      val low = value.toLowerCase()
      if (low.matches("[+-]?0[0-9]+") || low.stripPrefix("-").stripPrefix("+").startsWith("0x"))
        return Left(
          AstError(p.position, "Hexadecimal and octal floating-point literals are not allowed")
        )
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

  private def parseFloat(p: TPrimitive): Either[AstError, PFloat] = p match {
    case TLiteral(_, value) =>
      val low = value.toLowerCase()
      if (low.matches("[+-]?0[0-9]+") || low.stripPrefix("-").stripPrefix("+").startsWith("0x"))
        return Left(
          AstError(p.position, "Hexadecimal and octal floating-point literals are not allowed")
        )
      low match {
        case "inf" | "inff" | "infinity" | "infinityf"     => Right(PFloat(Float.PositiveInfinity))
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

  private def parseBoolean(p: TPrimitive): Either[AstError, PBoolean] = {
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

  private def parseString(p: TPrimitive): Either[AstError, PString] = p match {
    case TBytes(_, value) =>
      TextFormatUtils.unescapeText(value).map(PString(_)).left.map { error =>
        AstError(p.position, error.msg)
      }
    case p => Left(AstError(p.position, s"Invalid input '${p.asString}', expected string"))
  }

  private def parseBytes(p: TPrimitive): Either[AstError, PByteString] = p match {
    case TBytes(_, value) =>
      TextFormatUtils.unescapeBytes(value).map(PByteString(_)).left.map { error =>
        AstError(p.position, error.msg)
      }
    case _ => Left(AstError(p.position, "Unexpected input"))
  }

  private[textformat] def parsePrimitive(
      protoType: FieldDescriptorProto.Type,
      enumDescriptor: Option[EnumDescriptor],
      input: TPrimitive
  ): Either[AstError, PValue] = {
    val p = input match {
      case TLiteral(position, value)
          if protoType != FieldDescriptorProto.Type.TYPE_FLOAT &&
            protoType != FieldDescriptorProto.Type.TYPE_DOUBLE &&
            (value == "-0" || value.matches("[+-]?0[0-9a-fA-Fx]+")) =>
        new Parser("").tryBigInt(value).map(TIntLiteral(position, _)).getOrElse(input)
      case _ => input
    }
    protoType match {
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
        Left(AstError(p.position, "Expected group body"))
      case FieldDescriptorProto.Type.TYPE_ENUM => {
        val enumDesc = enumDescriptor.get
        p match {
          case TIntLiteral(index, num) =>
            (if (!num.isValidInt) None
             else if (enumDesc.isClosed) enumDesc.findValueByNumber(num.toInt)
             else Some(enumDesc.findValueByNumberCreatingIfUnknown(num.toInt)))
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

  }

  private def parseUnsafe(
      v: GeneratedMessageCompanion[?],
      ast: TMessage,
      typeRegistry: TypeRegistry
  ): Either[AstError, ParsedMessage] = {
    if (
      v.scalaDescriptor.fullName == "google.protobuf.Any" && ast.fields.exists(
        _.name.startsWith("[")
      )
    ) {
      return ast.fields match {
        case Seq(TField(position, name, body: TMessage)) if name.startsWith("[") =>
          val url = name.substring(1, name.length - 1)
          typeRegistry.find(url) match {
            case None            => Left(AstError(position, s"Unknown Any type URL '$url'"))
            case Some(companion) =>
              parseUnsafe(companion, body, typeRegistry).map { parsed =>
                val message =
                  (if (parsed.hasExtensions) companion.parseFrom(parsed.toByteArray)
                   else companion.messageReads.read(parsed.toPValue)).asInstanceOf[GeneratedMessage]
                ParsedMessage(
                  Map(
                    v.scalaDescriptor.findFieldByNumber(1).get -> Scalar(PString(url)),
                    v.scalaDescriptor.findFieldByNumber(2).get -> Scalar(
                      PByteString(message.toByteString)
                    )
                  )
                )
              }
          }
        case _ =>
          Left(AstError(ast.position, "An expanded Any must contain exactly one message value"))
      }
    }
    def parsePrimitiveField(fd: FieldDescriptor, input: TPrimitive): Either[AstError, PValue] =
      parsePrimitive(
        fd.protoType,
        fd.scalaType match {
          case ScalaType.Enum(descriptor) => Some(descriptor)
          case _                          => None
        },
        input
      )

    def pfieldToValue(fd: FieldDescriptor, pfield: TField): Either[AstError, Parsed] =
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
            if ((fd.protoType.isTypeMessage || fd.protoType.isTypeGroup)) {
              val idx = arr.values.indexWhere(!_.isInstanceOf[TMessage])
              if (idx != -1)
                Left(AstError(arr.position, s"Array contain a non-message value at index ${idx}"))
              else
                flatten(
                  arr.values
                    .map(t =>
                      parseUnsafe(
                        v.messageCompanionForFieldNumber(fd.number),
                        t.asInstanceOf[TMessage],
                        typeRegistry
                      )
                    )
                    .toVector
                ).map(ParsedRepeated(_))
            } else {
              val idx = arr.values.indexWhere(!_.isInstanceOf[TPrimitive])
              if (idx != -1) Left(AstError(arr.position, s"Unexpected value at index $idx"))
              else
                flatten(
                  arr.values
                    .map(t => parsePrimitiveField(fd, t.asInstanceOf[TPrimitive]).map(Scalar(_)))
                    .toVector
                ).map(ParsedRepeated(_))
            }
          }
        case p: TPrimitive =>
          if ((fd.protoType.isTypeMessage || fd.protoType.isTypeGroup))
            Left(AstError(p.position, "invalid value for message"))
          else {
            parsePrimitiveField(fd, p).map(Scalar(_))
          }
        case p: TMessage =>
          if (!(fd.protoType.isTypeMessage || fd.protoType.isTypeGroup))
            Left(AstError(p.position, "invalid value for message"))
          else {
            parseUnsafe(v.messageCompanionForFieldNumber(fd.number), p, typeRegistry)
          }
      }

    val fieldMap: Map[String, FieldDescriptor] =
      v.scalaDescriptor.fields.flatMap { f =>
        val name = f.textFormatName
        Seq(name -> f, f.name -> f)
      }.toMap

    val (extensions, ordinary) = ast.fields.partition(_.name.startsWith("["))

    def fieldGroupToValue(
        fd: FieldDescriptor,
        group: Seq[TField]
    ): Either[AstError, (FieldDescriptor, Parsed)] = {
      val values: Either[AstError, Vector[Parsed]] = flatten(group.map(pfieldToValue(fd, _)))

      values.map { (t: Seq[Parsed]) =>
        if (!fd.isRepeated) fd -> t.last
        else
          fd -> ParsedRepeated(t.foldLeft(Vector[Parsed]()) {
            case (xs, ParsedRepeated(ys)) => xs ++ ys
            case (xs, t: Parsed)          => xs :+ t
          })
      }
    }

    val maybeMap: Either[AstError, Vector[(FieldDescriptor, Parsed)]] =
      ordinary.find(f => !fieldMap.contains(f.name)) match {
        case Some(f) => Left(AstError(f.position, s"Unknown field name '${f.name}'"))
        case None    =>
          flatten(
            ordinary.groupBy(f => fieldMap(f.name)).map((fieldGroupToValue _).tupled).toVector
          )
      }
    maybeMap.flatMap { t =>
      val parsed = ParsedMessage(t.toMap)
      if (extensions.isEmpty) Right(parsed)
      else {
        val registered =
          typeRegistry.messageExtensions(v.scalaDescriptor).map(e => "[" + e.name + "]" -> e).toMap
        val scalarExtensions =
          typeRegistry.scalarExtensions(v.scalaDescriptor).map(e => "[" + e.name + "]" -> e).toMap
        flatten(extensions.map { f =>
          registered.get(f.name) match {
            case None =>
              scalarExtensions.get(f.name) match {
                case Some(extension) => extension.parse(f).map(bytes => Vector(bytes))
                case None            =>
                  Left(
                    AstError(
                      f.position,
                      s"Unknown extension '${f.name}'; register its declaration file and any message payload companion"
                    )
                  )
              }
            case Some(extension) =>
              val values = f.value match {
                case TArray(_, values) if extension.field.getLabel.isLabelRepeated => values
                case value                                                         => Seq(value)
              }
              flatten(values.map {
                case body: TMessage =>
                  parseMessage(extension.companion, body, typeRegistry).map { message =>
                    val bytes  = new java.io.ByteArrayOutputStream()
                    val output = com.google.protobuf.CodedOutputStream.newInstance(bytes)
                    if (extension.isDelimited) {
                      output.writeTag(
                        extension.field.getNumber,
                        scalapb.WireType.WIRETYPE_START_GROUP
                      )
                      message.writeTo(output)
                      output.writeTag(
                        extension.field.getNumber,
                        scalapb.WireType.WIRETYPE_END_GROUP
                      )
                    } else output.writeBytes(extension.field.getNumber, message.toByteString)
                    output.flush()
                    bytes.toByteArray
                  }
                case value => Left(AstError(value.position, "Expected message extension body"))
              })
          }
        }).map { chunks =>
          parsed.copy(extensions = chunks.flatten)
        }
      }
    }
  }
}
