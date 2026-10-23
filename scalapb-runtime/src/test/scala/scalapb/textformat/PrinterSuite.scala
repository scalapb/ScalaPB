package scalapb.textformat

import com.google.protobuf.ByteString
import munit.FunSuite
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, TextFormat}
import scalapb.printer._

class PrinterSuite extends FunSuite {
  private def print(m: GeneratedMessage): String =
    TextFormat.printToString(m, PrinterOptions.default.withOmitDefaults(true))

  test("proto3 scalars at default") {
    assertEquals(
      print(Scalars()),
      ""
    )
  }

  test("proto3 scalars set") {
    assertEquals(
      print(Scalars(1, "a", ByteString.copyFromUtf8("b"), Color.GREEN)),
      """|int: 1
         |str: "a"
         |bytes: "b"
         |color: GREEN
         |""".stripMargin
    )
  }

  test("proto3 optional and oneof set to default") {
    assertEquals(
      print(Scalars(opt = Some(0), choice = Scalars.Choice.Chosen(0))),
      """|opt: 0
         |chosen: 0
         |""".stripMargin
    )
  }

  test("proto2 optional set to default") {
    assertEquals(
      print(Proto2(Some(0), Some("x"), 0)),
      """|int: 0
         |str: "x"
         |req: 0
         |""".stripMargin
    )
  }

  test("message fields at default") {
    assertEquals(
      print(Messages.defaultInstance),
      """|required {
         |}
         |""".stripMargin
    )
  }

  test("message fields set to default") {
    assertEquals(
      print(Messages.defaultInstance.withBoxed(Inner()).withOptionalShape(Some(Square()))),
      """|boxed {
         |}
         |required {
         |}
         |optional_shape {
         |  square {
         |  }
         |}
         |""".stripMargin
    )
  }

  test("message fields set") {
    assertEquals(
      print(Messages(Some(Inner(1)), Inner(2), Inner(3), Circle(4), Some(Square(5)))),
      """|boxed {
         |  n: 1
         |}
         |unboxed {
         |  n: 2
         |}
         |required {
         |  n: 3
         |}
         |shape {
         |  circle {
         |    radius: 4
         |  }
         |}
         |optional_shape {
         |  square {
         |    side: 5
         |  }
         |}
         |""".stripMargin
    )
  }

  test("repeated and map fields with default elements") {
    assertEquals(
      print(Collections(Seq(0), Map("" -> 0), Seq(Scalars()))),
      """|ints: 0
         |counts {
         |}
         |list {
         |}
         |""".stripMargin
    )
  }

  test("nested message with default fields") {
    assertEquals(
      print(Nested(Some(Scalars(str = "a")))),
      """|scalars {
         |  str: "a"
         |}
         |""".stripMargin
    )
  }

  test("no_box message whose default encodes a field") {
    assertEquals(
      print(NoBox.defaultInstance),
      """|messages {
         |  required {
         |  }
         |}
         |""".stripMargin
    )
  }

  test("round trip through fromAscii") {
    def roundTrip[A <: GeneratedMessage](c: GeneratedMessageCompanion[A], m: A): Unit =
      assertEquals(TextFormat.fromAscii(c, print(m)), Right(m))
    roundTrip(Scalars, Scalars(opt = Some(0)))
    roundTrip(Proto2, Proto2(Some(0), Some("x"), 0))
    roundTrip(Messages, Messages.defaultInstance.withOptionalShape(Some(Square())))
    roundTrip(Collections, Collections(Seq(0), Map("" -> 0), Seq(Scalars())))
    roundTrip(Nested, Nested(Some(Scalars(str = "a"))))
  }

  test("entry points without options") {
    val m = Scalars(str = "é")
    assertEquals(
      TextFormat.printToString(m),
      """|int: 0
         |str: "\303\251"
         |bytes: ""
         |color: RED
         |""".stripMargin
    )
    assertEquals(
      TextFormat.printToUnicodeString(m),
      """|int: 0
         |str: "é"
         |bytes: ""
         |color: RED
         |""".stripMargin
    )
    assertEquals(
      TextFormat.printToSingleLineUnicodeString(m),
      """int: 0 str: "é" bytes: "" color: RED"""
    )
    assertEquals(
      Printer.printToString(m, singleLineMode = true, escapeNonAscii = true),
      """int: 0 str: "\303\251" bytes: "" color: RED"""
    )
  }
}
