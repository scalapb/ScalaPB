import com.thesamet.proto.e2e.message_set.{LegacyMessageSet, MessageSetPayload}
import com.google.protobuf.InvalidProtocolBufferException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class MessageSetSpec extends AnyFlatSpec with Matchers {
  private def bytes(values: Int*): Array[Byte] = values.map(_.toByte).toArray
  private val ordinary                         = bytes(162, 6, 4, 8, 7, 24, 9)
  private val item                             = bytes(11, 16, 100, 26, 3, 18, 1, 120, 12)

  "MessageSet" should "preserve encounter order across both extension encodings" in {
    val parsed  = LegacyMessageSet.parseFrom(ordinary ++ item)
    val payload = parsed.extension(MessageSetPayload.payload).get
    payload.choice must be(MessageSetPayload.Choice.Second("x"))
    payload.extra must be(Some(9))
    LegacyMessageSet
      .parseFrom(item ++ ordinary)
      .extension(MessageSetPayload.payload)
      .get
      .choice must be(MessageSetPayload.Choice.First(7))
    LegacyMessageSet.parseFrom(parsed.toByteArray) must be(parsed)
    parsed.serializedSize must be(parsed.toByteArray.length)
    parsed.toByteArray.head must be(11.toByte)
    val constructed = LegacyMessageSet().withExtension(MessageSetPayload.payload)(Some(payload))
    LegacyMessageSet.parseFrom(constructed.toByteArray) must be(constructed)
  }

  it should "accept reversed item fields and ignore incomplete items" in {
    LegacyMessageSet.parseFrom(bytes(11, 26, 3, 18, 1, 120, 16, 100, 12)) must be(
      LegacyMessageSet.parseFrom(item)
    )
    Seq(bytes(11, 12), bytes(11, 16, 100, 12), bytes(11, 26, 0, 12)).foreach { wire =>
      LegacyMessageSet.parseFrom(wire) must be(LegacyMessageSet())
    }
    Seq(bytes(11), bytes(11, 16, 100), bytes(11, 20), bytes(11, 0, 12)).foreach { wire =>
      intercept[InvalidProtocolBufferException](LegacyMessageSet.parseFrom(wire))
    }
  }
}
