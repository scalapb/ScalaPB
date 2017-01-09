package com.trueaccord.scalapb

import java.io.{InputStream, OutputStream}

import com.google.protobuf.{ByteString, CodedInputStream, CodedOutputStream}
import com.google.protobuf.Descriptors.{EnumDescriptor, EnumValueDescriptor, FieldDescriptor}

import scala.util.Try

trait GeneratedEnum extends Product with Serializable {
  type EnumType <: GeneratedEnum

  def value: Int

  def index: Int

  def name: String

  override def toString = name

  def companion: GeneratedEnumCompanion[EnumType]

  @deprecated("Use javaValueDescriptor", "ScalaPB 0.5.47")
  def valueDescriptor: EnumValueDescriptor = javaValueDescriptor

  def javaValueDescriptor: EnumValueDescriptor = companion.javaDescriptor.getValues.get(index)
}

trait GeneratedEnumCompanion[A <: GeneratedEnum] {
  type ValueType = A
  def fromValue(value: Int): A
  def fromName(name: String): Option[A] = values.find(_.name == name)
  def values: Seq[A]
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.EnumDescriptor = javaDescriptor
  def javaDescriptor: com.google.protobuf.Descriptors.EnumDescriptor
}

trait GeneratedOneof extends Product with Serializable {
  def number: Int
  def isDefined: Boolean
  def isEmpty: Boolean
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
        CodedOutputStream.computeUInt32SizeNoTag(serialized) + serialized)
    val codedOutput: CodedOutputStream =
        CodedOutputStream.newInstance(output, bufferSize)
    codedOutput.writeUInt32NoTag(serialized)
    writeTo(codedOutput)
    codedOutput.flush()
  }

  def getField(field: FieldDescriptor): Any

  def companion: GeneratedMessageCompanion[_]

  def getAllFields: Map[FieldDescriptor, Any] = {
    val b = Map.newBuilder[FieldDescriptor, Any]
    b.sizeHint(companion.javaDescriptor.getFields.size)
    val i = companion.javaDescriptor.getFields.iterator
    while (i.hasNext) {
      val f = i.next()
      if (f.getType != FieldDescriptor.Type.GROUP) {
        getField(f) match {
          case null => {}
          case bs: ByteString if bs.isEmpty => b += (f -> bs)
          case Nil => {}
          case v => b += (f -> v)
        }
      }
    }
    b.result()
  }

  def toByteArray: Array[Byte] = {
    val a = new Array[Byte](serializedSize)
    val outputStream = CodedOutputStream.newInstance(a)
    writeTo(outputStream)
    outputStream.checkNoSpaceLeft()
    a
  }

  def toByteString: ByteString = {
    val output = ByteString.newOutput(serializedSize)
    writeTo(output)
    output.close()
    output.toByteString
  }

  def serializedSize: Int
}

trait Message[A] {
  def mergeFrom(input: CodedInputStream): A
}

trait JavaProtoSupport[ScalaPB, JavaPB] {
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

  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.Descriptor = javaDescriptor

  def javaDescriptor: com.google.protobuf.Descriptors.Descriptor

  def messageCompanionForField(field: FieldDescriptor): GeneratedMessageCompanion[_]

  def enumCompanionForField(field: FieldDescriptor): GeneratedEnumCompanion[_]

  // The ASCII representation is the representation returned by toString. The following
  // functions allow you to convert the ASCII format back to a protocol buffer.  These
  // functions are compatible with the Java implementation in the sense that anything that
  // was generated by TextFormat.printToString can be parsed by these functions.
  def validateAscii(s: String): Either[TextFormatError, A] = TextFormat.fromAscii(this, s)

  def fromAscii(s: String): A = validateAscii(s).fold(
    t => throw new TextFormatException(t.msg), identity[A])

  def defaultInstance: A
}

case class KeyValue[K, V](key: K, value: V)

