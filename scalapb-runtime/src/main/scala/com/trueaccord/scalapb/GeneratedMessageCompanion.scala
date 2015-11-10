package com.trueaccord.scalapb

import java.io.{InputStream, OutputStream}

import com.google.protobuf.{ByteString, CodedOutputStream, CodedInputStream}
import com.google.protobuf.Descriptors.{EnumValueDescriptor, FieldDescriptor, EnumDescriptor}
import scala.collection.JavaConversions._

import scala.util.Try

trait GeneratedEnum extends Serializable {
  type EnumType <: GeneratedEnum

  def value: Int

  def index: Int

  def name: String

  override def toString = name

  def companion: GeneratedEnumCompanion[EnumType]

  def valueDescriptor: EnumValueDescriptor = companion.descriptor.getValues.get(index)
}

trait GeneratedEnumCompanion[A <: GeneratedEnum] {
  type ValueType = A
  def fromValue(value: Int): A
  def values: Seq[A]
  def descriptor: EnumDescriptor
}

trait GeneratedOneof extends Serializable {
  def number: Int
}

trait GeneratedOneofCompanion

trait GeneratedMessage extends Serializable {
  def writeTo(output: CodedOutputStream): Unit

  def writeTo(output: OutputStream): Unit = {
    val bufferSize =
        LiteParser.preferredCodedOutputStreamBufferSize(serializedSize)
    val codedOutput: CodedOutputStream =
        CodedOutputStream.newInstance(output, bufferSize)
    writeTo(codedOutput)
    codedOutput.flush()
  }

  def writeDelimitedTo(output: OutputStream): Unit = {
    val serialized: Int = serializedSize
    val bufferSize: Int = LiteParser.preferredCodedOutputStreamBufferSize(
        CodedOutputStream.computeRawVarint32Size(serialized) + serialized)
    val codedOutput: CodedOutputStream =
        CodedOutputStream.newInstance(output, bufferSize)
    codedOutput.writeRawVarint32(serialized)
    writeTo(codedOutput)
    codedOutput.flush()
  }

  def getField(field: FieldDescriptor): Any

  def companion: GeneratedMessageCompanion[_]

  def getAllFields: Map[FieldDescriptor, Any] =
    companion.descriptor.getFields.flatMap({
      f =>
        getField(f) match {
          case bs: ByteString if bs.isEmpty => Some(f -> bs)
          case None | Nil => None
          case v => Some(f -> v)
        }
    })(collection.breakOut)

  def toByteArray: Array[Byte] = {
    val a = new Array[Byte](serializedSize)
    val outputStream = CodedOutputStream.newInstance(a)
    writeTo(outputStream)
    outputStream.checkNoSpaceLeft()
    a
  }

  def serializedSize: Int
}

trait Message[A] {
  def mergeFrom(input: CodedInputStream): A
}

trait JavaProtoSupport[ScalaPB, JavaPB] {
  def fromAscii(ascii: String): ScalaPB

  def fromJavaProto(javaProto: JavaPB): ScalaPB

  def toJavaProto(scalaProto: ScalaPB): JavaPB
}

trait GeneratedMessageCompanion[A <: GeneratedMessage with Message[A]] {
  def parseFrom(input: CodedInputStream): A = LiteParser.parseFrom(this, input)

  def parseFrom(input: InputStream): A = parseFrom(CodedInputStream.newInstance(input))

  def parseDelimitedFrom(input: CodedInputStream): Option[A] =
    LiteParser.parseDelimitedFrom(this, input)

  def parseDelimitedFrom(input: InputStream): Option[A] =
    LiteParser.parseDelimitedFrom(this, input)

  // Creates a stream that parses one message at a time from the delimited input stream.
  def streamFromDelimitedInput(input: InputStream): Stream[A] = {
    val codedInputStream = CodedInputStream.newInstance(input)
    Stream.continually(parseDelimitedFrom(codedInputStream)).takeWhile(_.isDefined).map(_.get)
  }

  def parseFrom(s: Array[Byte]): A = parseFrom(CodedInputStream.newInstance(s))

  def validate(s: Array[Byte]): Try[A] = Try(parseFrom(s))

  def toByteArray(a: A): Array[Byte] = a.toByteArray

  def fromFieldsMap(fields: Map[FieldDescriptor, Any]): A

  def descriptor: com.google.protobuf.Descriptors.Descriptor

  def messageCompanionForField(field: FieldDescriptor): GeneratedMessageCompanion[_]

  def enumCompanionForField(field: FieldDescriptor): GeneratedEnumCompanion[_]

  def defaultInstance: A
}

case class KeyValue[K, V](key: K, value: V) {

}
