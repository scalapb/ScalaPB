package scalapb

import com.google.protobuf.{ByteString, CodedOutputStream}
import scala.collection.immutable.ListMap

class UnknownFieldSetSerializationSpec extends munit.FunSuite {
  test("deterministic writes sort field numbers but preserve each wire-type value sequence") {
    val a = UnknownFieldSet(
      ListMap(
        20 -> UnknownFieldSet.Field(varint = Seq(8L, 2L), fixed64 = Seq(6L, 1L)),
        3  -> UnknownFieldSet.Field(fixed32 = Seq(9, 1), lengthDelimited = Seq(ByteString.EMPTY))
      )
    )
    val b = UnknownFieldSet(ListMap(a.asMap.toSeq.reverse: _*))
    def bytes(fields: UnknownFieldSet, sorted: Boolean): Vector[Byte] = {
      val buffer = new Array[Byte](fields.serializedSize)
      val output = CodedOutputStream.newInstance(buffer)
      if (sorted) fields.writeToDeterministic(output) else fields.writeTo(output)
      output.checkNoSpaceLeft()
      buffer.toVector
    }
    assertNotEquals(bytes(a, false), bytes(b, false))
    assertEquals(bytes(a, true), bytes(b, false))
    assertEquals(bytes(a, true), bytes(b, true))
    assertEquals(bytes(UnknownFieldSet.empty, true), Vector.empty[Byte])
  }
}
