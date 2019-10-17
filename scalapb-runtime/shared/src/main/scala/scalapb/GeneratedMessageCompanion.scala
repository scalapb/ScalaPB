package scalapb

import java.io.{InputStream, OutputStream}

import com.google.protobuf.{ByteString, CodedInputStream, CodedOutputStream}
import com.google.protobuf.{Descriptors => JavaDescriptors}
import scalapb.lenses.{Lens, Updatable}

import _root_.scalapb.descriptors.{PMessage, PValue}
import scala.util.Try

trait GeneratedEnum extends Any with Product with Serializable {
  type EnumType <: GeneratedEnum

  def value: Int

  def index: Int

  def name: String

  override def toString = name

  def companion: GeneratedEnumCompanion[EnumType]

  def isUnrecognized: Boolean = false

  @deprecated("Use javaValueDescriptor", "ScalaPB 0.5.47")
  def valueDescriptor: JavaDescriptors.EnumValueDescriptor = javaValueDescriptor

  def javaValueDescriptor: JavaDescriptors.EnumValueDescriptor =
    companion.javaDescriptor.getValues.get(index)

  def scalaValueDescriptor: _root_.scalapb.descriptors.EnumValueDescriptor =
    companion.scalaDescriptor.values(index)
}

trait UnrecognizedEnum extends GeneratedEnum {
  def value: Int

  def name = "UNRECOGNIZED"

  def index = -1

  override def isUnrecognized: Boolean = true

  override def scalaValueDescriptor: _root_.scalapb.descriptors.EnumValueDescriptor =
    companion.scalaDescriptor.findValueByNumberCreatingIfUnknown(value)
}

trait GeneratedEnumCompanion[A <: GeneratedEnum] {
  type ValueType = A
  def fromValue(value: Int): A
  def fromName(name: String): Option[A] = values.find(_.name == name)
  def values: Seq[A]

  @deprecated(
    "Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.",
    "ScalaPB 0.5.47"
  )
  def descriptor: com.google.protobuf.Descriptors.EnumDescriptor = javaDescriptor
  def javaDescriptor: com.google.protobuf.Descriptors.EnumDescriptor
  def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor
}

trait GeneratedOneof extends Any with Product with Serializable {
  type ValueType
  def number: Int
  def isDefined: Boolean
  def isEmpty: Boolean
  def value: ValueType
  def valueOption: Option[ValueType] = if (isDefined) Some(value) else None
}

trait GeneratedOneofCompanion

trait GeneratedMessage extends Any with Serializable {
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
      CodedOutputStream.computeUInt32SizeNoTag(serialized) + serialized
    )
    val codedOutput: CodedOutputStream =
      CodedOutputStream.newInstance(output, bufferSize)
    codedOutput.writeUInt32NoTag(serialized)
    writeTo(codedOutput)
    codedOutput.flush()
  }

  def getFieldByNumber(fieldNumber: Int): Any

  // Using a Java field descriptor.
  @deprecated("Use getField that accepts a ScalaPB descriptor and returns PValue", "0.6.0")
  def getField(field: com.google.protobuf.Descriptors.FieldDescriptor): Any = {
    require(field.getContainingType eq companion.javaDescriptor)
    getFieldByNumber(field.getNumber)
  }

  // Using a Scala field descriptor.
  def getField(field: _root_.scalapb.descriptors.FieldDescriptor): PValue

  def toPMessage: PMessage =
    PMessage(companion.scalaDescriptor.fields.map {
      case f => (f, getField(f))
    }.toMap)

  def companion: GeneratedMessageCompanion[_]

  @deprecated("Use toPMessage", "0.6.0")
  def getAllFields: Map[JavaDescriptors.FieldDescriptor, Any] = {
    val b = Map.newBuilder[JavaDescriptors.FieldDescriptor, Any]
    b.sizeHint(companion.javaDescriptor.getFields.size)
    val i = companion.javaDescriptor.getFields.iterator
    while (i.hasNext) {
      val f = i.next()
      if (f.getType != JavaDescriptors.FieldDescriptor.Type.GROUP) {
        getField(f) match {
          case null                         => {}
          case bs: ByteString if bs.isEmpty => b += (f -> bs)
          case Nil                          => {}
          case v                            => b += (f -> v)
        }
      }
    }
    b.result()
  }

  def toByteArray: Array[Byte] = {
    val a            = new Array[Byte](serializedSize)
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

  /** Returns a human-readable ASCII format representation of this message.
    *
    * The original message can be decoded from this format by using fromAscii on
    * the companion object.
    *
    * @return human-readable representation of this message.
    */
  def toProtoString: String
}

trait Message[A] extends Any {
  def mergeFrom(input: CodedInputStream): A
}

trait ExtendableMessage[A <: ExtendableMessage[A]] extends Updatable[A] {
  self: A =>
  def unknownFields: _root_.scalapb.UnknownFieldSet

  def extension[T](generatedExtension: GeneratedExtension[A, T]): T = generatedExtension.get(this)

  def withUnknownFields(unknownFields: _root_.scalapb.UnknownFieldSet): A

  def withExtension[T](generatedExtension: GeneratedExtension[A, T])(value: T): A =
    generatedExtension.set(value)(this)
}

object ExtendableMessage {
  def unknownFieldsLen[A <: ExtendableMessage[A]]: Lens[A, _root_.scalapb.UnknownFieldSet] =
    Lens[A, _root_.scalapb.UnknownFieldSet](_.unknownFields)((c, t) => c.withUnknownFields(t))
}

trait JavaProtoSupport[ScalaPB, JavaPB] extends Any {
  def fromJavaProto(javaProto: JavaPB): ScalaPB

  def toJavaProto(scalaProto: ScalaPB): JavaPB
}

trait GeneratedMessageCompanion[A <: GeneratedMessage with Message[A]] {
  type ValueType = A

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

  @deprecated("Use messageReads", "0.6.0")
  def fromFieldsMap(fields: Map[JavaDescriptors.FieldDescriptor, Any]): A

  @deprecated(
    "Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.",
    "ScalaPB 0.5.47"
  )
  def descriptor: com.google.protobuf.Descriptors.Descriptor = javaDescriptor

  def javaDescriptor: com.google.protobuf.Descriptors.Descriptor

  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor

  // Companion object of all nested messages. That is, all messages that are defined directly inside this
  // message.
  def nestedMessagesCompanions: Seq[GeneratedMessageCompanion[_ <: GeneratedMessage]]

  def messageReads: _root_.scalapb.descriptors.Reads[A]

  def messageCompanionForFieldNumber(field: Int): GeneratedMessageCompanion[_]

  def messageCompanionForField(
      field: JavaDescriptors.FieldDescriptor
  ): GeneratedMessageCompanion[_] = {
    require(
      field.getContainingType() == javaDescriptor,
      "FieldDescriptor does not match message type."
    )
    messageCompanionForFieldNumber(field.getNumber)
  }

  def enumCompanionForFieldNumber(field: Int): GeneratedEnumCompanion[_]

  def enumCompanionForField(field: JavaDescriptors.FieldDescriptor): GeneratedEnumCompanion[_] = {
    require(
      field.getContainingType() == javaDescriptor,
      "FieldDescriptor does not match message type."
    )
    enumCompanionForFieldNumber(field.getNumber)
  }

  // The ASCII representation is the representation returned by toProtoString. The following
  // functions allow you to convert the ASCII format back to a protocol buffer.  These
  // functions are compatible with the Java implementation in the sense that anything that
  // was generated by TextFormat.printToString can be parsed by these functions.
  def validateAscii(s: String): Either[TextFormatError, A] = TextFormat.fromAscii(this, s)

  def fromAscii(s: String): A =
    validateAscii(s).fold(t => throw new TextFormatException(t.msg), identity[A])

  def defaultInstance: A
}

case class KeyValue[K, V](key: K, value: V)

abstract class GeneratedFileObject {
  def scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor
  def javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor
  // Other file objects that this file depends on.
  def dependencies: Seq[GeneratedFileObject]

  // Companion object of all top-level messages
  def messagesCompanions: Seq[GeneratedMessageCompanion[_ <: GeneratedMessage]]
}

trait GeneratedSealedOneof extends Any with Product with Serializable {
  type MessageType <: GeneratedMessage with Message[MessageType]
  def isEmpty: Boolean
  def isDefined: Boolean
  def asMessage: MessageType
}
