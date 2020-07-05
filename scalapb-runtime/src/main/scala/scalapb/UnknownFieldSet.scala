package scalapb

import com.google.protobuf.{
  ByteString,
  CodedInputStream,
  CodedOutputStream,
  InvalidProtocolBufferException
}
import scalapb.lenses.Lens

import scala.collection.mutable
import scala.collection.compat._

final case class UnknownFieldSet(
    private[scalapb] val fields: Map[Int, UnknownFieldSet.Field] = Map.empty
) {
  def getField(fieldNumber: Int): Option[UnknownFieldSet.Field] = fields.get(fieldNumber)

  def withField(fieldNumber: Int, value: UnknownFieldSet.Field) =
    new UnknownFieldSet(fields = fields + (fieldNumber -> value))

  def writeTo(output: CodedOutputStream): Unit = {
    fields.foreach {
      case (fieldNumber, field) => field.writeTo(fieldNumber, output)
    }
  }

  def serializedSize: Int = {
    var size: Int = 0
    fields.foreach {
      case (fieldNumber, field) => size += field.serializedSize(fieldNumber)
    }
    size
  }
}

object UnknownFieldSet {
  val empty = UnknownFieldSet()

  implicit class UnknownFieldSetLens[UpperPB](
      lens: _root_.scalapb.lenses.Lens[UpperPB, UnknownFieldSet]
  ) {
    def apply(fieldNumber: Int): Lens[UpperPB, UnknownFieldSet.Field] =
      lens.compose(Lens[UnknownFieldSet, UnknownFieldSet.Field]({ t =>
        t.fields.getOrElse(fieldNumber, UnknownFieldSet.Field())
      })({ (c, t) => c.withField(fieldNumber, t) }))
  }

  case class Field(
      varint: Seq[Long] = Vector.empty,
      fixed64: Seq[Long] = Vector.empty,
      fixed32: Seq[Int] = Vector.empty,
      lengthDelimited: Seq[ByteString] = Vector.empty
  ) {
    def writeTo(fieldNumber: Int, output: CodedOutputStream): Unit = {
      varint.foreach(output.writeUInt64(fieldNumber, _))
      fixed32.foreach(output.writeFixed32(fieldNumber, _))
      fixed64.foreach(output.writeFixed64(fieldNumber, _))
      lengthDelimited.foreach(output.writeBytes(fieldNumber, _))
    }
    def serializedSize(fieldNumber: Int): Int = {
      varint.map(CodedOutputStream.computeUInt64Size(fieldNumber, _)).sum +
        fixed32.map(CodedOutputStream.computeFixed32Size(fieldNumber, _)).sum +
        fixed64.map(CodedOutputStream.computeFixed64Size(fieldNumber, _)).sum +
        lengthDelimited.map(CodedOutputStream.computeBytesSize(fieldNumber, _)).sum
    }
  }

  object Field {
    val varintLens  = Lens[Field, Seq[Long]](_.varint)((c, v) => c.copy(varint = v))
    val fixed64Lens = Lens[Field, Seq[Long]](_.fixed64)((c, v) => c.copy(fixed64 = v))
    val fixed32Lens = Lens[Field, Seq[Int]](_.fixed32)((c, v) => c.copy(fixed32 = v))
    val lengthDelimitedLens =
      Lens[Field, Seq[ByteString]](_.lengthDelimited)((c, v) => c.copy(lengthDelimited = v))

    class Builder {
      private val varint          = Vector.newBuilder[Long]
      private val fixed64         = Vector.newBuilder[Long]
      private val fixed32         = Vector.newBuilder[Int]
      private val lengthDelimited = Vector.newBuilder[ByteString]

      def result() =
        Field(
          varint = varint.result(),
          fixed64 = fixed64.result(),
          fixed32 = fixed32.result(),
          lengthDelimited = lengthDelimited.result()
        )

      def parseField(tag: Int, input: CodedInputStream) = {
        val wireType = WireType.getTagWireType(tag)

        wireType match {
          case WireType.WIRETYPE_VARINT =>
            varint += input.readInt64()
          case WireType.WIRETYPE_FIXED64 =>
            fixed64 += input.readFixed64()
          case WireType.WIRETYPE_LENGTH_DELIMITED =>
            lengthDelimited += input.readBytes()
          case WireType.WIRETYPE_FIXED32 =>
            fixed32 += input.readFixed32()
          case _ =>
            throw new InvalidProtocolBufferException(
              s"Protocol message tag had invalid wire type: ${wireType}"
            )
        }
      }
    }

    object Builder {
      def fromField(f: Field): Field.Builder = {
        val b = new Field.Builder
        b.varint ++= f.varint
        b.fixed32 ++= f.fixed32
        b.fixed64 ++= f.fixed64
        b.lengthDelimited ++= f.lengthDelimited
        b
      }
    }
  }

  class Builder {
    private val fieldBuilders = new mutable.HashMap[Int, Field.Builder]

    def this(base: UnknownFieldSet) = {
      this()
      if (base.fields.nonEmpty) {
        fieldBuilders ++= base.fields.view.mapValues(Field.Builder.fromField)
      }
    }

    def result() =
      if (fieldBuilders.isEmpty) UnknownFieldSet.empty
      else new UnknownFieldSet(fieldBuilders.view.mapValues(_.result()).toMap)

    def parseField(tag: Int, input: CodedInputStream) = {
      val fieldNumber = WireType.getTagFieldNumber(tag)
      fieldBuilders.getOrElseUpdate(fieldNumber, new Field.Builder()).parseField(tag, input)
    }
  }
}

object WireType {
  def getTagWireType(tag: Int) = tag & 7

  def getTagFieldNumber(tag: Int) = tag >>> 3

  val WIRETYPE_VARINT           = 0
  val WIRETYPE_FIXED64          = 1
  val WIRETYPE_LENGTH_DELIMITED = 2
  val WIRETYPE_START_GROUP      = 3
  val WIRETYPE_END_GROUP        = 4
  val WIRETYPE_FIXED32          = 5

  sealed trait WireValue

  case class Fixed64(value: Seq[Long])               extends WireValue
  case class Fixed32(value: Seq[Long])               extends WireValue
  case class Varint(value: Seq[Long])                extends WireValue
  case class LengthDelimited(value: Seq[ByteString]) extends WireValue
}
