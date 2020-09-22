package scalapb.textformat

import munit.FunSuite

class ParserSpec extends FunSuite with ParserSuite {

  def keyValue(p: Parser): Option[TField] = p.parseKeyValueList(None, Nil).headOption

  def message(p: Parser): Option[TMessage] = Some(p.parseMessage)

  def P(x: Int, y: Int) = Position(x, y)

  test("KeyValue") {
    check2(keyValue(_), "foo: 17", TField(P(0, 0), "foo", TIntLiteral(P(0, 5), 17)))

    check2(keyValue(_), "foo:    0x13   ", TField(P(0, 0), "foo", TIntLiteral(P(0, 8), 0x13)))
    check2(keyValue(_), "bar: true", TField(P(0, 0), "bar", TLiteral(P(0, 5), "true")))
    check2(keyValue(_), "bar:        true   ", TField(P(0, 0), "bar", TLiteral(P(0, 12), "true")))
    check2(keyValue(_), "bar  :        true   ", TField(P(0, 0), "bar", TLiteral(P(0, 14), "true")))
    check2(keyValue(_), "barr:        true   ", TField(P(0, 0), "barr", TLiteral(P(0, 13), "true")))
    check2(
      keyValue(_),
      "barr:        1e-17   ",
      TField(P(0, 0), "barr", TLiteral(P(0, 13), "1e-17"))
    )

    check2(
      keyValue(_),
      "foo { x: 3 }",
      TField(
        P(0, 0),
        "foo",
        TMessage(
          P(0, 4),
          Seq(
            TField(P(0, 6), "x", TIntLiteral(P(0, 9), 3))
          )
        )
      )
    )

    check2(
      keyValue(_),
      "foo { x: 3 y: 4}",
      TField(
        P(0, 0),
        "foo",
        TMessage(
          P(0, 4),
          Seq(
            TField(P(0, 6), "x", TIntLiteral(P(0, 9), 3)),
            TField(P(0, 11), "y", TIntLiteral(P(0, 14), 4))
          )
        )
      )
    )

    check2(
      keyValue(_),
      """foo {
        |
        |     x: 3 y: 4
        |   }""".stripMargin,
      TField(
        P(0, 0),
        "foo",
        TMessage(
          P(0, 4),
          Seq(
            TField(P(2, 5), "x", TIntLiteral(P(2, 8), 3)),
            TField(P(2, 10), "y", TIntLiteral(P(2, 13), 4))
          )
        )
      )
    )

    check2(
      keyValue(_),
      """foo {
        |     # comment
        |     x: 3 # comment 2
        |     # comment
        |     y: 4
        |     z: # comment
        |     17
        |   }""".stripMargin,
      TField(
        P(0, 0),
        "foo",
        TMessage(
          P(0, 4),
          Seq(
            TField(P(2, 5), "x", TIntLiteral(P(2, 8), 3)),
            TField(P(4, 5), "y", TIntLiteral(P(4, 8), 4)),
            TField(P(5, 5), "z", TIntLiteral(P(6, 5), 17))
          )
        )
      )
    )

    check2(
      keyValue(_),
      """foo <
        |     # comment
        |     x: 3 # comment 2
        |     # comment
        |     y: 4
        |     z: # comment
        |     17
        |   >""".stripMargin,
      TField(
        P(0, 0),
        "foo",
        TMessage(
          P(0, 4),
          Seq(
            TField(P(2, 5), "x", TIntLiteral(P(2, 8), 3)),
            TField(P(4, 5), "y", TIntLiteral(P(4, 8), 4)),
            TField(P(5, 5), "z", TIntLiteral(P(6, 5), 17))
          )
        )
      )
    )

    check2(
      keyValue(_),
      "foo [{bar: 4}, {t: 17}]",
      TField(
        P(0, 0),
        "foo",
        TArray(
          P(0, 4),
          Seq(
            TMessage(P(0, 5), Seq(TField(P(0, 6), "bar", TIntLiteral(P(0, 11), 4)))),
            TMessage(P(0, 15), Seq(TField(P(0, 16), "t", TIntLiteral(P(0, 19), 17))))
          )
        )
      )
    )

    check2(
      keyValue(_),
      "foo: [0, 2]",
      TField(P(0, 0), "foo", TArray(P(0, 5), Seq(TIntLiteral(P(0, 6), 0), TIntLiteral(P(0, 9), 2))))
    )

    check2(keyValue(_), "foo: []", TField(P(0, 0), "foo", TArray(P(0, 5), Seq())))

    check2(keyValue(_), "foo []", TField(P(0, 0), "foo", TArray(P(0, 4), Seq())))

    check2(keyValue(_), "foo {  }", TField(P(0, 0), "foo", TMessage(P(0, 4), Seq())))

    check2(keyValue(_), "foo: {  }", TField(P(0, 0), "foo", TMessage(P(0, 5), Seq())))

    check2(
      keyValue(_),
      "foo: \"\u5d8b\u2367\u633d\"",
      TField(P(0, 0), "foo", TBytes(P(0, 5), "\u5d8b\u2367\u633d"))
    )

    check2(
      keyValue(_),
      "foo: [{bar: 4}]",
      TField(
        P(0, 0),
        "foo",
        TArray(
          P(0, 5),
          Seq(TMessage(P(0, 6), Seq(TField(P(0, 7), "bar", TIntLiteral(P(0, 12), 4)))))
        )
      )
    )

    checkFail2(keyValue(_), "foo 17")
    checkFail2(keyValue(_), "foo: [[17, 5]]")
    checkFail2(keyValue(_), "foo { x: 3")
    checkFail2(keyValue(_), "foo [{bar: 4}, 17]")
    checkFail2(keyValue(_), "foo [,]")
  }

  test("Message") {
    check2(
      message,
      """foo: 4
        |baz: true
        |bal: [3, 4, 5]
        |gamba: 1.0f
        |mar {
        |}
        |tang [{foo: 3 kar: 9}]
        | """.stripMargin,
      TMessage(
        P(0, 0),
        Seq(
          TField(P(0, 0), "foo", TIntLiteral(P(0, 5), 4)),
          TField(P(1, 0), "baz", TLiteral(P(1, 5), "true")),
          TField(
            P(2, 0),
            "bal",
            TArray(
              P(2, 5),
              Seq(TIntLiteral(P(2, 6), 3), TIntLiteral(P(2, 9), 4), TIntLiteral(P(2, 12), 5))
            )
          ),
          TField(P(3, 0), "gamba", TLiteral(P(3, 7), "1.0f")),
          TField(P(4, 0), "mar", TMessage(P(4, 4), Seq())),
          TField(
            P(6, 0),
            "tang",
            TArray(
              P(6, 5),
              Seq(
                TMessage(
                  P(6, 6),
                  Seq(
                    TField(P(6, 7), "foo", TIntLiteral(P(6, 12), 3)),
                    TField(P(6, 14), "kar", TIntLiteral(P(6, 19), 9))
                  )
                )
              )
            )
          )
        )
      )
    )
    check2(
      message,
      """wa {
        |  e5 {
        |    brr {
        |      eee {
        |      }
        |    }
        |  }
        |  brr {
        |  }
        |}
        |wa {
        |  njp: -3.90173722E9
        |}
        | """.stripMargin,
      TMessage(
        P(0, 0),
        Seq(
          TField(
            P(0, 0),
            "wa",
            TMessage(
              P(0, 3),
              Seq(
                TField(
                  P(1, 2),
                  "e5",
                  TMessage(
                    P(1, 5),
                    Seq(
                      TField(
                        P(2, 4),
                        "brr",
                        TMessage(P(2, 8), Seq(TField(P(3, 6), "eee", TMessage(P(3, 10), Seq()))))
                      )
                    )
                  )
                ),
                TField(P(7, 2), "brr", TMessage(P(7, 6), Seq()))
              )
            )
          ),
          TField(
            P(10, 0),
            "wa",
            TMessage(P(10, 3), Seq(TField(P(11, 2), "njp", TLiteral(P(11, 7), "-3.90173722E9"))))
          )
        )
      )
    )
  }
}
