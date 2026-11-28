package scalapb.textformat

import munit.FunSuite
import scalapb.TextFormat
import scalapb.textformat.fixtures.ReservedFields

class ReservedFieldsSpec extends FunSuite {
  test("reserved names discard syntactically valid scalar, message and list values") {
    for (
      value <- Seq(
        "true",
        "-123",
        "0.123",
        "ENUM_VALUE",
        "\"hello\"",
        "{ unknown: 123 }",
        "< unknown: 123 >",
        "[-123, 456]",
        "[0.123, 1e-10]",
        "[\"hello\", \"world\"]",
        "[{ a: 1 }, { b: 2 }]"
      )
    ) {
      assertEquals(
        TextFormat.fromAscii(ReservedFields, s"removed: $value value: 7"),
        Right(ReservedFields(value = Some(7)))
      )
      assertEquals(
        TextFormat.fromAscii(ReservedFields, s"child { removed: $value value: 8 }"),
        Right(ReservedFields(child = Some(ReservedFields(value = Some(8)))))
      )
    }
  }

  test("only names reserved in the current message are ignored") {
    assert(TextFormat.fromAscii(ReservedFields, "unknown: 1").isLeft)
    assert(TextFormat.fromAscii(ReservedFields, "child { unknown: 1 }").isLeft)
    assert(TextFormat.fromAscii(ReservedFields, "[removed]: 1").isLeft)
    assertEquals(
      TextFormat.fromAscii(ReservedFields, "removed: 1 removed: 2"),
      Right(ReservedFields())
    )
  }

  test("reserved fields still require valid syntax and obey parser limits") {
    for (value <- Seq("{", "[1,]", "'unterminated", "{ a: }")) {
      assert(TextFormat.fromAscii(ReservedFields, s"removed: $value").isLeft)
    }
    assert(TextFormat.fromAscii(ReservedFields, "removed { x {} }", 1).isLeft)
    assert(TextFormat.fromAscii(ReservedFields, "removed: " + "1" * 1025).isLeft)
  }
}
