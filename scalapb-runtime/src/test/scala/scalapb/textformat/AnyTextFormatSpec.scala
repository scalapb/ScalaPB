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

  test("expanded Any accepts URL prefix punctuation and preserves percent escapes") {
    for (prefix <- Seq("non.default.domain/sub-path_0/~!$&()*+,;=", "example.com/%2F/%aB")) {
      val url    = prefix + "/google.protobuf.Int32Value"
      val parsed = TextFormat.fromAscii(Any, s"[$url] { value: 42 }", registry)
      assertEquals(parsed, Right(Any(url, Int32Value(42).toByteString)))
      val text = TextFormat.printToString(parsed.toOption.get)
      assertEquals(TextFormat.fromAscii(Any, text, registry), parsed)
    }
    val split = "[example.com/%2 F/ google.proto # comment\n buf.Int32Value] { value: 42 }"
    assertEquals(
      TextFormat.fromAscii(Any, split, registry),
      Right(Any("example.com/%2F/google.protobuf.Int32Value", Int32Value(42).toByteString))
    )
  }

  test("URL punctuation does not relax percent escapes or extension and type names") {
    for (
      prefix <- Seq(
        "example.com/%",
        "example.com/%2",
        "example.com/%ZZ",
        "example.com/%2G",
        "example.com/:",
        "example.com/?",
        "example.com/@"
      )
    ) {
      assert(TextFormat.fromAscii(Any, s"[$prefix/google.protobuf.Int32Value] {}", registry).isLeft)
    }
    for (
      name <- Seq(
        "example.com/",
        "/google.protobuf.Int32Value",
        "example.com/google.protobuf.Int32Value%20",
        "extension-name",
        "extension%20name"
      )
    ) {
      intercept[scalapb.TextFormatException](new Parser(s"[$name] {}").parseMessage)
    }
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
