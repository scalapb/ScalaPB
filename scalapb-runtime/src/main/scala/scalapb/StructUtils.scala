package scalapb

import java.util.Base64

import com.google.protobuf.ByteString
import com.google.protobuf.struct.Value.Kind
import com.google.protobuf.struct.{ListValue, Struct, Value}
import scalapb.descriptors._

import scala.util.{Failure, Success, Try}

object StructUtils {
  case class StructParsingError(error: String)
  def toStruct(generatedMessage: GeneratedMessage): Struct =
    toStruct(generatedMessage.toPMessage.value)

  def fromStruct[T <: GeneratedMessage](
      struct: Struct
  )(implicit companion: GeneratedMessageCompanion[T]): Either[StructParsingError, T] = {
    val fieldDescriptorToPValue = structMapToFDMap(struct.fields)
    fieldDescriptorToPValue.map(companion.messageReads.read)
  }

  private def structMapToFDMap(
      structFields: Map[String, Value]
  )(implicit companion: GeneratedMessageCompanion[?]): Either[StructParsingError, PMessage] = {
    val fieldDescriptorToPValue = companion.scalaDescriptor.fields.map { fd =>
      structFields
        .get(fd.name)
        .map(fromValue(fd))
        .getOrElse(Right(defaultFor(fd)))
        .map(value => fd -> value)
    }
    flatten(fieldDescriptorToPValue).map(_.toMap).map(PMessage(_))
  }

  private def fromValue(fd: FieldDescriptor)(value: Value)(implicit
      companion: GeneratedMessageCompanion[?]
  ): Either[StructParsingError, PValue] = (value.kind, fd.scalaType) match {
    case (Kind.NumberValue(v), ScalaType.Int) if v.isValidInt =>
      Right(PInt(v.toInt))
    case (Kind.StringValue(v), ScalaType.Long) =>
      Try {
        PLong(v.toLong)
      } match {
        case Success(pLong) => Right(pLong)
        case Failure(_)     =>
          Left(
            StructParsingError(
              s"""Field "${fd.fullName}" is of type long but received invalid long value "$v""""
            )
          )
      }
    case (Kind.NumberValue(v), ScalaType.Double)     => Right(PDouble(v))
    case (Kind.NumberValue(v), ScalaType.Float)      => Right(PFloat(v.toFloat))
    case (Kind.StringValue(v), ScalaType.ByteString) =>
      Right(PByteString(ByteString.copyFrom(Base64.getDecoder.decode(v.getBytes))))
    case (Kind.StringValue(v), en @ ScalaType.Enum(_)) =>
      en.descriptor.values
        .find(_.name == v)
        .map(PEnum(_))
        .toRight(
          StructParsingError(
            s"""Field "${fd.fullName}" is of type enum "${en.descriptor.fullName}" but received invalid enum value "$v""""
          )
        )
    case (Kind.StringValue(v), ScalaType.String)   => Right(PString(v))
    case (Kind.BoolValue(v), ScalaType.Boolean)    => Right(PBoolean(v))
    case (Kind.ListValue(v), _) if (fd.isRepeated) =>
      flatten(v.values.map(fromValue(fd))).map(PRepeated(_))
    case (Kind.StructValue(v), _: ScalaType.Message) =>
      structMapToFDMap(v.fields)(companion.messageCompanionForFieldNumber(fd.number))
    case (Kind.Empty, _)                    => Right(PEmpty)
    case (kind: Kind, scalaType: ScalaType) =>
      Left(
        StructParsingError(
          s"""Field "${fd.fullName}" is of type "${scalaType}" but received "$kind""""
        )
      )
  }

  private def defaultFor(fd: FieldDescriptor): PValue =
    if (fd.isRepeated)
      PRepeated(Vector.empty)
    else
      PEmpty

  private def toStruct(pMessageMap: Map[FieldDescriptor, PValue]): Struct = {
    val pMessageWithNonEmptyFields = pMessageMap.filter { case (_, value) =>
      value != PEmpty && value != PRepeated(Vector.empty)
    }
    Struct(pMessageWithNonEmptyFields.map(pair => pair._1.name -> toValue(pair._2)))
  }

  private def toValue(pValue: PValue): Value =
    Value(pValue match {
      case PInt(value)        => Value.Kind.NumberValue(value.toDouble)
      case PLong(value)       => Value.Kind.StringValue(value.toString)
      case PDouble(value)     => Value.Kind.NumberValue(value)
      case PFloat(value)      => Value.Kind.NumberValue(value.toDouble)
      case PString(value)     => Value.Kind.StringValue(value)
      case PByteString(value) =>
        Value.Kind.StringValue(
          new String(Base64.getEncoder.encode(value.toByteArray()))
        )
      case PBoolean(value)  => Value.Kind.BoolValue(value)
      case PEnum(value)     => Value.Kind.StringValue(value.name)
      case PMessage(value)  => Value.Kind.StructValue(toStruct(value))
      case PRepeated(value) => Value.Kind.ListValue(ListValue(value.map(toValue)))
      // added for completeness of match case but we should never get here because we filter empty fields before
      case PEmpty => Value.Kind.Empty
    })

  private def flatten[T](
      s: Seq[Either[StructParsingError, T]]
  ): Either[StructParsingError, Vector[T]] = {
    s.foldLeft[Either[StructParsingError, Vector[T]]](Right(Vector.empty)) {
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
      case (Right(xs), Right(x)) => Right(xs :+ x)
    }
  }

}
