package scalapb

import java.nio.charset.Charset

import com.google.protobuf.ByteString
import com.google.protobuf.struct.{ListValue, Struct, Value}
import com.google.protobuf.struct.Value.Kind
import scalapb.descriptors.{FieldDescriptor, PBoolean, PByteString, PDouble, PEmpty, PEnum, PFloat, PInt, PLong, PMessage, PRepeated, PString, PValue, ScalaType}

import scala.util.{Failure, Success, Try}

object StructUtils {
  case class StructDeserError(index: Int, error: String)
  def toStruct(generatedMessage: GeneratedMessage): Struct =
  //TODO(@thesamet)- Can we ignore the case where generatedMessage is null?
    toStruct(generatedMessage.toPMessage.value)

  def fromStruct[T <: GeneratedMessage](
                                         companion: GeneratedMessageCompanion[T],
                                         struct: Struct
                                       ): Either[StructDeserError, T] = {
    //TODO(@thesamet)- do we want to fail (return Either#Left) if a structField arrives that we don't know about? I assume we don't want to fail on that for forward compatibility but want to double check
    val fieldDescriptorToPValue = structMapToFDMap(companion, struct.fields)
    fieldDescriptorToPValue.right.map(PMessage).map(companion.messageReads.read)
  }

  private def structMapToFDMap(companion: GeneratedMessageCompanion[_], structFields: Map[String, Value]): Either[StructDeserError, Map[FieldDescriptor, PValue]] = {
    val fieldDescriptorToPValue = companion.scalaDescriptor.fields.map { fd =>
      structFields.get(fd.name).map(fromValue(fd, companion)).getOrElse(Right(defaultFor(fd))).right.map(value => fd -> value)
    }
    flatten(fieldDescriptorToPValue).right.map(_.toMap)
  }

  private def fromValue(fd: FieldDescriptor, companion: GeneratedMessageCompanion[_])(value: Value): Either[StructDeserError, PValue] = value.kind match {
    case Kind.NumberValue(v) if fd.scalaType == ScalaType.Int && v.isValidInt => Right(PInt(v.toInt))
    case Kind.StringValue(v) if fd.scalaType == ScalaType.Long =>
      Try {
        PLong(v.toLong)
      } match {
        case Success(pLong) => Right(pLong)
        case Failure(_) => Left(StructDeserError(fd.number, s"Invalid value for long: '$v'"))
      }
    case Kind.NumberValue(v) if fd.scalaType == ScalaType.Double => Right(PDouble(v))
    case Kind.NumberValue(v) if fd.scalaType == ScalaType.Float => Right(PFloat(v.toFloat))
    case Kind.StringValue(v) if fd.scalaType == ScalaType.ByteString => Right(PByteString(ByteString.copyFrom(v, Charset.forName("UTF-8"))))
    case Kind.StringValue(v) if fd.scalaType.isInstanceOf[ScalaType.Enum] =>
      val enumDesc = fd.scalaType.asInstanceOf[ScalaType.Enum].descriptor
      enumDesc.values
        .find(_.name == v)
        .map(PEnum)
        .toRight(StructDeserError(fd.number, s"""Expected Enum type "${enumDesc.fullName}" has no value named "$v""""))
    case Kind.StringValue(v) if (fd.scalaType == ScalaType.String) => Right(PString(v))
    case Kind.BoolValue(v) if (fd.scalaType == ScalaType.Boolean) => Right(PBoolean(v))
    case Kind.ListValue(v) if (fd.isRepeated.asInstanceOf[Boolean]) =>
      flatten(v.values.map(fromValue(fd, companion)))
        .right.map(PRepeated)
    case Kind.StructValue(v) if (fd.scalaType.isInstanceOf[ScalaType.Message]) =>
      structMapToFDMap(companion.messageCompanionForFieldNumber(fd.number), v.fields).right.map(PMessage)
    case Kind.Empty => Right(PEmpty)
    case kind: Kind => Left(StructDeserError(fd.number, s"Field `${fd.fullName}` is of type '${fd.scalaType}' but received '$kind'"))
  }

  private def defaultFor(fd: FieldDescriptor): PValue =
    if (fd.isRepeated.asInstanceOf[Boolean])   //TODO @thesmaet- do we have a better alternative?
      PRepeated(Vector.empty)
    else
      PEmpty

  private def toStruct(pMessageMap: Map[FieldDescriptor, PValue]): Struct = {
    val pMessageWithNonEmptyFields = pMessageMap.filter { case (_, value) =>
      value != PEmpty && value != PRepeated(Vector.empty)
    }
    Struct(pMessageWithNonEmptyFields.map(pair => pair._1.name -> toValue(pair._2)))
  }

  private def toValue(pValue: PValue): Value = Value(pValue match {
    case PInt(value) => Value.Kind.NumberValue(value.toDouble)
    case PLong(value) => Value.Kind.StringValue(value.toString)
    case PDouble(value) => Value.Kind.NumberValue(value)
    case PFloat(value) => Value.Kind.NumberValue(value.toDouble)
    case PString(value) => Value.Kind.StringValue(value)
    case PByteString(value) => Value.Kind.StringValue(value.toStringUtf8) //TODO(@thesamet)- not sure if we have a better option here. WDYT?
    case PBoolean(value) => Value.Kind.BoolValue(value)
    case PEnum(value) => Value.Kind.StringValue(value.name.toString) //TODO(@thesamet)- value.name returns Any. Is toString ok?
    case PMessage(value) => Value.Kind.StructValue(toStruct(value))
    case PRepeated(value) => Value.Kind.ListValue(ListValue(value.map(toValue)))
    //added for completeness of match case but we should never get here because we filter empty fields before
    case PEmpty => Value.Kind.Empty
  })

  private def flatten[T](s: Seq[Either[StructDeserError, T]]): Either[StructDeserError, Vector[T]] = {
    s.foldLeft[Either[StructDeserError, Vector[T]]](Right(Vector.empty)) {
      case (Left(l), _)          => Left(l)
      case (_, Left(l))          => Left(l)
      case (Right(xs), Right(x)) => Right(xs :+ x)
    }
  }

}
