package com.trueaccord.scalapb.textformat

import utest._

object ParserSpec extends TestSuite with ParserSuite {

  import ProtoAsciiParser._

  val tests = TestSuite {
    'KeyValue {
      check(KeyValue, "foo: 17", TField(0, "foo", TIntLiteral(5, 17)))
      check(KeyValue, "foo:    0x13   ", TField(0, "foo", TIntLiteral(8, 0x13)))
      check(KeyValue, "bar: true", TField(0, "bar", TLiteral(5, "true")))
      check(KeyValue, "bar:        true   ", TField(0, "bar", TLiteral(12, "true")))
      check(KeyValue, "bar  :        true   ", TField(0, "bar", TLiteral(14, "true")))
      check(KeyValue, "barr:        true   ", TField(0, "barr", TLiteral(13, "true")))
      //      check(KeyValue, "barr:        1e-17   ", PField(0, "barr", PLiteral(13, "1e-17")))

      check(KeyValue, "foo { x: 3 }", TField(0, "foo", TMessage(4,
        Seq(
          TField(6, "x", TIntLiteral(9, 3))
        )
      )))

      check(KeyValue, "foo { x: 3 y: 4}", TField(0, "foo", TMessage(4,
        Seq(
          TField(6, "x", TIntLiteral(9, 3)),
          TField(11, "y", TIntLiteral(14, 4))
        )
      )))

      check(KeyValue,
        """foo {
          |
          |     x: 3 y: 4
          |   }""".stripMargin, TField(0, "foo", TMessage(4,
          Seq(
            TField(12, "x", TIntLiteral(15, 3)),
            TField(17, "y", TIntLiteral(20, 4))
          )
        )))

      check(KeyValue,
        """foo {
          |     # comment
          |     x: 3 # comment 2
          |     # comment
          |     y: 4
          |     z: # comment
          |     17
          |   }""".stripMargin, TField(0, "foo", TMessage(4,
          Seq(
            TField(26, "x", TIntLiteral(29, 3)),
            TField(63, "y", TIntLiteral(66, 4)),
            TField(73, "z", TIntLiteral(91, 17))
          )
        )))

      check(KeyValue,
        """foo <
          |     # comment
          |     x: 3 # comment 2
          |     # comment
          |     y: 4
          |     z: # comment
          |     17
          |   >""".stripMargin, TField(0, "foo", TMessage(4,
          Seq(
            TField(26, "x", TIntLiteral(29, 3)),
            TField(63, "y", TIntLiteral(66, 4)),
            TField(73, "z", TIntLiteral(91, 17))
          )
        )))

      check(KeyValue,
        "foo [{bar: 4}, {t: 17}]",
        TField(0, "foo", TArray(4, Seq(
          TMessage(5, Seq(TField(6, "bar", TIntLiteral(11, 4)))),
          TMessage(15, Seq(TField(16, "t", TIntLiteral(19, 17))))))))

      check(KeyValue,
        "foo: [0, 2]",
        TField(0, "foo", TArray(5, Seq(TIntLiteral(6, 0), TIntLiteral(9, 2)))))

      check(KeyValue,
        "foo: []",
        TField(0, "foo", TArray(5, Seq())))

      check(KeyValue,
        "foo []",
        TField(0, "foo", TArray(4, Seq())))

      check(KeyValue,
        "foo {  }",
        TField(0, "foo", TMessage(4, Seq())))

      check(KeyValue,
        "foo: {  }",
        TField(0, "foo", TMessage(5, Seq())))

      check(KeyValue,
        "foo: \"\u5d8b\u2367\u633d\"",
        TField(0, "foo", TBytes(5, "\u5d8b\u2367\u633d")))

      check(KeyValue, "foo: [{bar: 4}]",
        TField(0, "foo", TArray(5, Seq(
          TMessage(6, Seq(
            TField(7, "bar", TIntLiteral(12, 4))))))))

      checkFail(KeyValue, "foo 17")
      checkFail(KeyValue, "foo: [[17, 5]]")
      checkFail(KeyValue, "foo { x: 3")
      checkFail(KeyValue, "foo [{bar: 4}, 17]")
      checkFail(KeyValue, "foo [,]")
    }

    'Message {
      check(Message,
        """foo: 4
          |baz: true
          |bal: [3, 4, 5]
          |gamba: 1.0f
          |mar {
          |}
          |tang [{foo: 3 kar: 9}]
          | """.stripMargin,
        TMessage(0, Seq(
          TField(0, "foo", TIntLiteral(5, 4)),
          TField(7, "baz", TLiteral(12, "true")),
          TField(17, "bal", TArray(22, Seq(TIntLiteral(23, 3), TIntLiteral(26, 4), TIntLiteral(29, 5)))),
          TField(32, "gamba", TLiteral(39, "1.0f")),
          TField(44, "mar", TMessage(48, Seq())),
          TField(52, "tang", TArray(57, Seq(
            TMessage(58, Seq(
              TField(59, "foo", TIntLiteral(64, 3)),
              TField(66, "kar", TIntLiteral(71, 9))))))))))
      check(Message,
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
          | """.stripMargin, TMessage(0, Seq(
          TField(0, "wa", TMessage(3, Seq(
            TField(7, "e5", TMessage(10, Seq(
              TField(16, "brr", TMessage(20, Seq(
                TField(28, "eee", TMessage(32, Seq())))))))),
            TField(54, "brr", TMessage(58, Seq()))))),
          TField(66, "wa", TMessage(69, Seq(
            TField(73, "njp", TLiteral(78, "-3.90173722E9"))))))))
    }
  }

}
