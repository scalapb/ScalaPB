package com.trueaccord.scalapb

import java.io.{InputStream, OutputStream}

import com.google.protobuf.{CodedInputStream, CodedOutputStream}

import scala.util.Try

trait GeneratedEnum {
  def id: Int

  def name: String

  override def toString = name
}

trait GeneratedEnumCompanion[A <: GeneratedEnum] {
  type ValueType = A
  def fromValue(id: Int): A
  def values: Seq[A]
}

trait GeneratedMessage {
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

  def getField(field: Descriptors.FieldDescriptor): Any

  def companion: GeneratedMessageCompanion[_]

  def getAllFields: Seq[(Descriptors.FieldDescriptor, Any)] =
    companion.descriptor.fields.flatMap {
      f =>
        getField(f) match {
          case None => None
          case Nil => None
          case v => Some(f -> v)
        }
    }

  def toByteArray: Array[Byte] = {
    val a = new Array[Byte](serializedSize)
    val outputStream = com.google.protobuf.CodedOutputStream.newInstance(a)
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

  def fromFieldsMap(fields: Map[Int, Any]): A

  def descriptor: Descriptors.MessageDescriptor

  def defaultInstance: A
}

case class KeyValue[K, V](key: K, value: V) {

}
