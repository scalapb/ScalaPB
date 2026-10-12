package scalapb

import com.google.protobuf.{CodedOutputStream, ByteString}
import scalapb.descriptors.{FieldDescriptor, PValue}
import scala.collection.immutable.ListMap

class SerializationModeSpec extends munit.FunSuite {
  // Models the write API implemented by classes generated before SerializationMode existed.
  private case class LegacyMessage() extends GeneratedMessage {
    def writeTo(output: CodedOutputStream): Unit = output.writeInt32(1, 42)
    def serializedSize: Int                      = 2
    def companion: GeneratedMessageCompanion[?]  = throw new UnsupportedOperationException
    def getFieldByNumber(number: Int): Any       = throw new UnsupportedOperationException
    def getField(field: FieldDescriptor): PValue = throw new UnsupportedOperationException
    def toProtoString: String                    = "value: 42"
  }

  test("older messages keep working in default mode and explicitly reject deterministic mode") {
    val legacy = LegacyMessage()
    assertEquals(legacy.toByteArray.toVector, Vector[Byte](8, 42))
    assertEquals(legacy.toByteArray(0), 8.toByte)
    assertEquals(legacy.toByteArray(SerializationMode.Default).toVector, Vector[Byte](8, 42))
    assertEquals(legacy.toByteString, ByteString.copyFrom(Array[Byte](8, 42)))
    val error = intercept[UnsupportedOperationException] {
      legacy.toByteArray(SerializationMode.Deterministic)
    }
    assert(error.getMessage.contains("regenerating"))
  }

  test("unknown field serialization sorts numbers but preserves each wire-type value sequence") {
    val a = UnknownFieldSet(
      ListMap(
        20 -> UnknownFieldSet.Field(varint = Seq(8L, 2L), fixed64 = Seq(6L, 1L)),
        3  -> UnknownFieldSet.Field(fixed32 = Seq(9, 1), lengthDelimited = Seq(ByteString.EMPTY))
      )
    )
    val b = UnknownFieldSet(ListMap(a.asMap.toSeq.reverse: _*))
    def bytes(fields: UnknownFieldSet, mode: SerializationMode): Vector[Byte] = {
      val buffer = new Array[Byte](fields.serializedSize)
      val output = CodedOutputStream.newInstance(buffer)
      fields.writeTo(output, mode)
      output.checkNoSpaceLeft()
      buffer.toVector
    }
    assertNotEquals(bytes(a, SerializationMode.Default), bytes(b, SerializationMode.Default))
    assertEquals(bytes(a, SerializationMode.Deterministic), bytes(b, SerializationMode.Default))
  }

  test("deterministic well-known messages sort nested maps on every runtime platform") {
    import com.google.protobuf.struct.{Struct, Value}
    def nested(reverse: Boolean): Struct = {
      val entries =
        Seq("z" -> Value().withStringValue("last"), "a" -> Value().withStringValue("first"))
      val inner = Struct(fields = ListMap((if (reverse) entries.reverse else entries): _*))
      Struct(fields = Map("child" -> Value().withStructValue(inner)))
    }
    val a = nested(false)
    val b = nested(true)
    assertEquals(a, b)
    assertNotEquals(a.toByteArray.toVector, b.toByteArray.toVector)
    assertEquals(
      a.toByteArray(SerializationMode.Deterministic).toVector,
      b.toByteArray(SerializationMode.Deterministic).toVector
    )
    assertEquals(Struct.parseFrom(a.toByteArray(SerializationMode.Deterministic)), a)
  }

}
