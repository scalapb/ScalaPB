package scalapb

import java.util.Base64

import com.google.protobuf.ByteString
import com.google.protobuf.struct.Value.Kind
import com.google.protobuf.struct.{ListValue, Struct, Value}
import scalapb.descriptors._

import scala.util.{Failure, Success, Try}

object StructUtils {
  case class StructDeserError(index: Int, error: String)
  def toStruct(generatedMessage: GeneratedMessage): Struct =
    toStruct(generatedMessage.toPMessage.value)

  def fromStruct[T <: GeneratedMessage](
      struct: Struct
  )(implicit companion: GeneratedMessageCompanion[T]): Either[StructDeserError, T] = {
    val fieldDescriptorToPValue = structMapToFDMap(struct.fields)
    fieldDescriptorToPValue.right.map(companion.messageReads.read)
  }

  private def structMapToFDMap(
      structFields: Map[String, Value]
  )(implicit companion: GeneratedMessageCompanion[_]): Either[StructDeserError, PMessage] = {
    val fieldDescriptorToPValue = companion.scalaDescriptor.fields.map { fd =>
      structFields
        .get(fd.name)
        .map(fromValue(fd))
        .getOrElse(Right(defaultFor(fd)))
        .right
        .map(value => fd -> value)
    }
    flatten(fieldDescriptorToPValue).right.map(_.toMap).map(PMessage)
  }

  private def fromValue(fd: FieldDescriptor)(value: Value)(
      implicit companion: GeneratedMessageCompanion[_]
  ): Either[StructDeserError, PValue] = value.kind match {
    case Kind.NumberValue(v) if fd.scalaType == ScalaType.Int && v.isValidInt =>
      Right(PInt(v.toInt))
    case Kind.StringValue(v) if fd.scalaType == ScalaType.Long =>
      Try {
        PLong(v.toLong)
      } match {
        case Success(pLong) => Right(pLong)
        case Failure(_)     => Left(StructDeserError(fd.number, s"Invalid value for long: '$v'"))
      }
    case Kind.NumberValue(v) if fd.scalaType == ScalaType.Double => Right(PDouble(v))
    case Kind.NumberValue(v) if fd.scalaType == ScalaType.Float  => Right(PFloat(v.toFloat))
    case Kind.StringValue(v) if fd.scalaType == ScalaType.ByteString =>
      Right(PByteString(ByteString.copyFrom(Base64.getDecoder.decode(v.getBytes))))
    case Kind.StringValue(v) if fd.scalaType.isInstanceOf[ScalaType.Enum] =>
      val enumDesc = fd.scalaType.asInstanceOf[ScalaType.Enum].descriptor
      enumDesc.values
        .find(_.name == v)
        .map(PEnum)
        .toRight(
          StructDeserError(
            fd.number,
            s"""Expected Enum type "${enumDesc.fullName}" has no value named "$v""""
          )
        )
    case Kind.StringValue(v) if (fd.scalaType == ScalaType.String) => Right(PString(v))
    case Kind.BoolValue(v) if (fd.scalaType == ScalaType.Boolean)  => Right(PBoolean(v))
    case Kind.ListValue(v) if (fd.isRepeated.asInstanceOf[Boolean]) =>
      flatten(v.values.map(fromValue(fd))).right.map(PRepeated)
    case Kind.StructValue(v) if (fd.scalaType.isInstanceOf[ScalaType.Message]) =>
      structMapToFDMap(v.fields)(companion.messageCompanionForFieldNumber(fd.number))
    case Kind.Empty => Right(PEmpty)
    case kind: Kind =>
      Left(
        StructDeserError(
          fd.number,
          s"Field `${fd.fullName}` is of type '${fd.scalaType}' but received '$kind'"
        )
      )
  }

  private def defaultFor(fd: FieldDescriptor): PValue =
    if (fd.isRepeated)
      PRepeated(Vector.empty)
    else
      PEmpty

  private def toStruct(pMessageMap: Map[FieldDescriptor, PValue]): Struct = {
    val pMessageWithNonEmptyFields = pMessageMap.filter {
      case (_, value) =>
        value != PEmpty && value != PRepeated(Vector.empty)
    }
    Struct(pMessageWithNonEmptyFields.map(pair => pair._1.name -> toValue(pair._2)))
  }

  private def toValue(pValue: PValue): Value =
    Value(pValue match {
      case PInt(value)    => Value.Kind.NumberValue(value.toDouble)
      case PLong(value)   => Value.Kind.StringValue(value.toString)
      case PDouble(value) => Value.Kind.NumberValue(value)
      case PFloat(value)  => Value.Kind.NumberValue(value.toDouble)
      case PString(value) => Value.Kind.StringValue(value)
      case PByteString(value) =>
        Value.Kind.StringValue(
          new String(Base64.getEncoder.encode(value.toByteArray))
        )
      case PBoolean(value)  => Value.Kind.BoolValue(value)
      case PEnum(value)     => Value.Kind.StringValue(value.name)
      case PMessage(value)  => Value.Kind.StructValue(toStruct(value))
      case PRepeated(value) => Value.Kind.ListValue(ListValue(value.map(toValue)))
      //added for completeness of match case but we should never get here because we filter empty fields before
      case PEmpty => Value.Kind.Empty
    })

  private def flatten[T](
      s: Seq[Either[StructDeserError, T]]
  ): Either[StructDeserError, Vector[T]] = {
    s.foldLeft[Either[StructDeserError, Vector[T]]](Right(Vector.empty)) {
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
      case (Right(xs), Right(x)) => Right(xs :+ x)
    }
  }

}
