package scalapb

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import com.google.protobuf.{CodedInputStream, InvalidProtocolBufferException}
import com.google.protobuf.wrappers.Int32Value
import munit.FunSuite

class LiteParserSpec extends FunSuite {
  private def bytes(values: Int*): Array[Byte] = values.map(_.toByte).toArray

  private def framed(messages: Int32Value*): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    messages.foreach(_.writeDelimitedTo(output))
    output.toByteArray
  }

  // Exercise stream-backed decoders as well as the array decoder: physical EOF can occur
  // before a pushed limit only in the stream-backed case on the JVM.
  private val modes = Seq("InputStream", "coded array", "coded stream")
  private def parser(data: Array[Byte], mode: String): Int => Option[Int32Value] = {
    if (mode == "InputStream") {
      val input: InputStream = new ByteArrayInputStream(data)
      max => Int32Value.parseDelimitedFrom(input, max)
    } else {
      val input =
        if (mode == "coded array") CodedInputStream.newInstance(data)
        else CodedInputStream.newInstance(new ByteArrayInputStream(data))
      max => Int32Value.parseDelimitedFrom(input, max)
    }
  }

  modes.foreach { mode =>
    test(s"$mode: clean EOF, empty frames and consecutive messages") {
      val parse = parser(framed(Int32Value(), Int32Value(150), Int32Value()), mode)
      assertEquals(parse(0), Some(Int32Value()))
      assertEquals(parse(3), Some(Int32Value(150)))
      assertEquals(parse(0), Some(Int32Value()))
      assertEquals(parse(0), None)
    }

    test(s"$mode: reject negative frame lengths") {
      Seq(bytes(0x80, 0x80, 0x80, 0x80, 0x08), bytes(0xff, 0xff, 0xff, 0xff, 0x0f))
        .foreach { prefix =>
          intercept[InvalidProtocolBufferException] {
            parser(prefix ++ bytes(8, 1), mode)(Int.MaxValue)
          }
        }
    }

    test(s"$mode: reject malformed and truncated length varints") {
      Seq(bytes(0x80), Array.fill[Byte](10)(0x80.toByte)).foreach { data =>
        intercept[InvalidProtocolBufferException] {
          parser(data, mode)(Int.MaxValue)
        }
      }
    }

    test(s"$mode: reject truncated frames, including EOF between fields") {
      Seq(bytes(1), bytes(3, 8, 1), bytes(2, 8), bytes(2, 8, 0x80)).foreach { data =>
        intercept[InvalidProtocolBufferException] {
          parser(data, mode)(Int.MaxValue)
        }
      }
    }

    test(s"$mode: reject end-group tags and unconsumed frame bytes") {
      Seq(bytes(1, 12), bytes(3, 12, 8, 1)).foreach { data =>
        intercept[InvalidProtocolBufferException] {
          parser(data, mode)(Int.MaxValue)
        }
      }
    }

    test(s"$mode: frame size cap accepts boundary and rejects larger frames") {
      assertEquals(parser(framed(Int32Value(1)), mode)(2), Some(Int32Value(1)))
      intercept[InvalidProtocolBufferException] {
        parser(framed(Int32Value(1)), mode)(1)
      }
      intercept[InvalidProtocolBufferException] {
        parser(bytes(0xff, 0xff, 0xff, 0xff, 0x07), mode)(1024)
      }
      intercept[IllegalArgumentException] {
        parser(Array.emptyByteArray, mode)(-1)
      }
    }
  }

  test("existing overloads preserve valid consecutive frames") {
    val data                = framed(Int32Value(1), Int32Value())
    val stream: InputStream = new ByteArrayInputStream(data)
    val coded               = CodedInputStream.newInstance(data)
    Seq(Some(Int32Value(1)), Some(Int32Value()), None).foreach { expected =>
      assertEquals(Int32Value.parseDelimitedFrom(stream), expected)
      assertEquals(Int32Value.parseDelimitedFrom(coded), expected)
    }
  }

  test("InputStream frame size rejection does not read the body") {
    val input = new ByteArrayInputStream(framed(Int32Value(1)))
    intercept[InvalidProtocolBufferException] {
      Int32Value.parseDelimitedFrom(input, 1)
    }
    assertEquals(input.read(), 8)
    assertEquals(input.read(), 1)
  }

  test("LimitedInputStream rejects negative limits") {
    intercept[IllegalArgumentException] {
      new LimitedInputStream(new ByteArrayInputStream(Array.emptyByteArray), -1)
    }
  }

  for {
    merge <- Seq(false, true)
    data  <- Seq(bytes(1, 0, 0), bytes(1, 12, 0), bytes(3, 8, 1))
  } test(s"readMessage(merge=$merge) restores enclosing limit on failure: ${data.toSeq}") {
    val input = CodedInputStream.newInstance(new ByteArrayInputStream(data))
    input.pushLimit(20)
    intercept[InvalidProtocolBufferException] {
      if (merge) LiteParser.readMessage(input, Int32Value(42))(Int32Value)
      else LiteParser.readMessage(input)(Int32Value)
    }
    assertEquals(input.getBytesUntilLimit, 20 - input.getTotalBytesRead())
  }

  test("readMessage merge succeeds and restores its enclosing limit") {
    val input = CodedInputStream.newInstance(framed(Int32Value(1), Int32Value(2)))
    input.pushLimit(6)
    assertEquals(LiteParser.readMessage(input, Int32Value(42))(Int32Value), Int32Value(1))
    assertEquals(input.getBytesUntilLimit, 3)
    assertEquals(Int32Value.parseDelimitedFrom(input), Some(Int32Value(2)))
  }

  private val earlyReturningCompanion = new GeneratedMessageCompanion[Int32Value] {
    def parseFrom(input: CodedInputStream): Int32Value = Int32Value()
    def javaDescriptor                                 = Int32Value.javaDescriptor
    def scalaDescriptor                                = Int32Value.scalaDescriptor
    def nestedMessagesCompanions                       = Int32Value.nestedMessagesCompanions
    def messageReads                                   = Int32Value.messageReads
    def messageCompanionForFieldNumber(field: Int)     =
      Int32Value.messageCompanionForFieldNumber(field)
    def enumCompanionForFieldNumber(field: Int) = Int32Value.enumCompanionForFieldNumber(field)
    def defaultInstance                         = Int32Value.defaultInstance
  }

  test("a custom parser must consume all frame bytes even with last tag zero") {
    val data = framed(Int32Value(1))
    intercept[InvalidProtocolBufferException] {
      LiteParser.parseDelimitedFrom(new ByteArrayInputStream(data))(earlyReturningCompanion)
    }
    val coded         = CodedInputStream.newInstance(data)
    val previousLimit = coded.getBytesUntilLimit
    intercept[InvalidProtocolBufferException] {
      LiteParser.parseDelimitedFrom(coded)(earlyReturningCompanion)
    }
    assertEquals(coded.getBytesUntilLimit, previousLimit - coded.getTotalBytesRead())
  }
}
