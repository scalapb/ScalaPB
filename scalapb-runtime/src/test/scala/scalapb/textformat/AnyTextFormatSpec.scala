package scalapb.textformat

import com.google.protobuf.any.Any
import com.google.protobuf.wrappers.Int32Value
import munit.FunSuite
import scalapb.TextFormat

class AnyTextFormatSpec extends FunSuite {
  private val registry = TypeRegistry.empty.addMessage(Int32Value).addMessage(Any)
  private val expanded = "[type.googleapis.com/google.protobuf.Int32Value] { value: 42 }"

  test("expanded Any packs the registered type and preserves the type URL") {
    assertEquals(TextFormat.fromAscii(Any, expanded, registry), Right(Any.pack(Int32Value(42))))
    val alternate = expanded.replace("type.googleapis.com", "example.com/types")
    assertEquals(
      TextFormat.fromAscii(Any, alternate, registry).toOption.get.typeUrl,
      "example.com/types/google.protobuf.Int32Value"
    )
    assert(TextFormat.fromAscii(Any, expanded).isLeft)
    assert(TextFormat.fromAscii(Int32Value, expanded, registry).isLeft)
    assertEquals(
      TextFormat.fromAscii(Any, "type_url: 'x' value: 'y'", registry),
      Right(Any("x", com.google.protobuf.ByteString.copyFromUtf8("y")))
    )
  }

  test("expanded Any rejects mixed, duplicate, missing, and malformed values") {
    for (
      input <- Seq(
        expanded + expanded,
        "type_url: 'x' " + expanded,
        "[type.googleapis.com/google.protobuf.Int32Value] 42",
        "[type.googleapis.com/google.protobuf.Int32Value { value: 42 }",
        "[type.googleapis.com/Unknown] {}"
      )
    ) {
      assert(TextFormat.fromAscii(Any, input, registry).isLeft, input)
    }
  }

  test("expanded Any obeys the same recursion limit as ordinary messages") {
    val nested = "[type.googleapis.com/google.protobuf.Any] { " + expanded + " }"
    assert(TextFormat.fromAscii(Any, nested, registry, 2).isRight)
    assert(TextFormat.fromAscii(Any, nested, registry, 1).isLeft)
  }
}
