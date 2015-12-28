package com.trueaccord.scalapb.textformat

import utest._

object ParserSpec extends TestSuite with ParserSuite {

  import ProtoAsciiParser._

  val tests = TestSuite {
    'KeyValue {
      check(KeyValue, "foo: 17", PField(0, "foo", PIntLiteral(5, 17)))
      check(KeyValue, "foo:    0x13   ", PField(0, "foo", PIntLiteral(8, 0x13)))
      check(KeyValue, "bar: true", PField(0, "bar", PLiteral(5, "true")))
      check(KeyValue, "bar:        true   ", PField(0, "bar", PLiteral(12, "true")))
      check(KeyValue, "bar  :        true   ", PField(0, "bar", PLiteral(14, "true")))
      check(KeyValue, "barr:        true   ", PField(0, "barr", PLiteral(13, "true")))
//      check(KeyValue, "barr:        1e-17   ", PField(0, "barr", PLiteral(13, "1e-17")))

      check(KeyValue, "foo { x: 3 }", PField(0, "foo", PMessage(4,
        Seq(
          PField(6, "x", PIntLiteral(9, 3))
        )
      )))

      check(KeyValue, "foo { x: 3 y: 4}", PField(0, "foo", PMessage(4,
        Seq(
          PField(6, "x", PIntLiteral(9, 3)),
          PField(11, "y", PIntLiteral(14, 4))
        )
      )))

      check(KeyValue,
        """foo {
          |
          |     x: 3 y: 4
          |   }""".stripMargin, PField(0, "foo", PMessage(4,
          Seq(
            PField(12, "x", PIntLiteral(15, 3)),
            PField(17, "y", PIntLiteral(20, 4))
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
          |   }""".stripMargin, PField(0, "foo", PMessage(4,
          Seq(
            PField(26, "x", PIntLiteral(29, 3)),
            PField(63, "y", PIntLiteral(66, 4)),
            PField(73, "z", PIntLiteral(91, 17))
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
          |   >""".stripMargin, PField(0, "foo", PMessage(4,
          Seq(
            PField(26, "x", PIntLiteral(29, 3)),
            PField(63, "y", PIntLiteral(66, 4)),
            PField(73, "z", PIntLiteral(91, 17))
          )
        )))

      check(KeyValue,
        "foo [{bar: 4}, {t: 17}]",
        PField(0, "foo", PMessageArray(4, Seq(
          PMessage(5, Seq(PField(6, "bar", PIntLiteral(11, 4)))),
          PMessage(15, Seq(PField(16, "t", PIntLiteral(19, 17))))))))

      check(KeyValue,
        "foo: [0, 2]",
        PField(0, "foo", PPrimitiveArray(5, Seq(PIntLiteral(6, 0), PIntLiteral(9, 2)))))

      check(KeyValue,
        "foo: []",
        PField(0, "foo", PPrimitiveArray(5, Seq())))

      check(KeyValue,
        "foo []",
        PField(0, "foo", PMessageArray(4, Seq())))


      checkFail(KeyValue, "foo 17")
      checkFail(KeyValue, "foo { x: 3", """KeyValue:1:1 / Value:1:5 / MessageValue:1:5 / (identifier | "}"):1:10 ...""""")
      checkFail(KeyValue, "foo [{bar: 4}, 17]")
      checkFail(KeyValue, "foo: [{bar: 4}]")
      checkFail(KeyValue, "foo [,]")
    }

    'Message {
      check(Message,
        """foo: 4
          |baz: true
          |bal: [3, 4, 5]
          |mar {
          |}
          |tang [{foo: 3 kar: 9}]
          | """.stripMargin,
        PMessage(0, Seq(
          PField(0, "foo", PIntLiteral(5, 4)),
          PField(7, "baz", PLiteral(12, "true")),
          PField(17, "bal", PPrimitiveArray(22, Seq(PIntLiteral(23, 3), PIntLiteral(26, 4), PIntLiteral(29, 5)))),
          PField(32, "mar", PMessage(36, Seq())),
          PField(40, "tang", PMessageArray(45, Seq(
            PMessage(46, Seq(
              PField(47, "foo", PIntLiteral(52, 3)),
              PField(54, "kar", PIntLiteral(59, 9))))))))))

    }
  }

}
