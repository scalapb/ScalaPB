package scalapb.textformat

import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import munit.FunSuite
import scalapb.{TextFormat, UnknownFieldSet}

class UnknownFieldsTextFormatSpec extends FunSuite {
  test("unknown scalar printing is opt-in and preserves unsigned wire values") {
    val message = Int32Value(42).withUnknownFields(
      UnknownFieldSet(
        Map(
          3 -> UnknownFieldSet.Field(
            varint = Seq(0L, -1L),
            fixed32 = Seq(-1),
            fixed64 = Seq(-1L),
            lengthDelimited = Seq(ByteString.copyFrom(Array[Byte](-1)))
          ),
          2 -> UnknownFieldSet.Field(varint = Seq(123))
        )
      )
    )
    assertEquals(TextFormat.printToString(message), "value: 42\n")
    assertEquals(
      TextFormat.printToString(message, true),
      "value: 42\n2: 123\n3: 0\n3: 18446744073709551615\n3: 0xffffffff\n3: 0xffffffffffffffff\n3: \"\\377\"\n"
    )
  }
  test("unknown embedded messages print recursively with a depth bound and byte fallback") {
    def wrap(bytes: ByteString) = Int32Value().withUnknownFields(
      UnknownFieldSet(
        Map(
          2 -> UnknownFieldSet.Field(lengthDelimited = Seq(bytes))
        )
      )
    )
    assertEquals(
      TextFormat.printToString(wrap(ByteString.copyFrom(Array[Byte](8, 111))), true),
      "2 {\n  1: 111\n}\n"
    )
    assertEquals(TextFormat.printToString(wrap(ByteString.EMPTY), true), "2 {\n}\n")
    val nested = (1 to 105).foldLeft(ByteString.EMPTY)((bytes, _) => wrap(bytes).toByteString)
    val text   = TextFormat.printToString(wrap(nested), true)
    assertEquals(text.count(_ == '{'), 100)
    assert(text.contains(": \""))
  }
}
