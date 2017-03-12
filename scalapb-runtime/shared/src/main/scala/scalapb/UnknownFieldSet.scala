package scalapb

import com.google.protobuf.{ByteString, CodedInputStream, InvalidProtocolBufferException}

import scala.collection.mutable

case class UnknownFieldSet(private val fields: Map[Int, UnknownFieldSet.Field] = Map.empty) {
  def getField(fieldNumber: Int): Option[UnknownFieldSet.Field] = fields.get(fieldNumber)
}

object UnknownFieldSet {
  case class Field(
    varint: Vector[Long] = Vector.empty,
    fixed64: Vector[Long] = Vector.empty,
    fixed32: Vector[Int] = Vector.empty,
    lengthDelimited: Vector[ByteString] = Vector.empty)


  object Field {
    class Builder {
      private val varint = Vector.newBuilder[Long]
      private val fixed64 = Vector.newBuilder[Long]
      private val fixed32 = Vector.newBuilder[Int]
      private val lengthDelimited = Vector.newBuilder[ByteString]

      def result() = Field(
        varint = varint.result(),
        fixed64 = fixed64.result(),
        fixed32 = fixed32.result(),
        lengthDelimited = lengthDelimited.result())

      def parseField(tag: Int, input: CodedInputStream) = {
        val wireType = WireType.getTagWireType(tag)

        wireType match {
          case WireType.WIRETYPE_VARINT =>
            varint += input.readInt64
          case WireType.WIRETYPE_FIXED64 =>
            fixed64 += input.readFixed64
          case WireType.WIRETYPE_LENGTH_DELIMITED =>
            lengthDelimited += input.readBytes
          case WireType.WIRETYPE_FIXED32 =>
            fixed32 += input.readFixed32
          case _ => throw new InvalidProtocolBufferException(s"Protocol message tag had invalid wire type: ${wireType}")
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
      fieldBuilders ++= base.fields.mapValues(Field.Builder.fromField)
    }

    def result() = UnknownFieldSet(fieldBuilders.mapValues(_.result()).toMap)

    def parseField(tag: Int, input: CodedInputStream) = {
      val fieldNumber = WireType.getTagFieldNumber(tag)
      fieldBuilders.getOrElseUpdate(fieldNumber, new Field.Builder()).parseField(tag, input)
    }
  }
}

object WireType {
  def getTagWireType(tag: Int) = tag & 7

  def getTagFieldNumber(tag: Int) = tag >>> 3

  val WIRETYPE_VARINT = 0
  val WIRETYPE_FIXED64 = 1
  val WIRETYPE_LENGTH_DELIMITED = 2
  val WIRETYPE_START_GROUP = 3
  val WIRETYPE_END_GROUP = 4
  val WIRETYPE_FIXED32 = 5

  sealed trait WireValue

  case class Fixed64(value: Seq[Long]) extends WireValue
  case class Fixed32(value: Seq[Long]) extends WireValue
  case class Varint(value: Seq[Long]) extends WireValue
  case class LengthDelimited(value: Seq[ByteString]) extends WireValue
}
