import com.thesamet.proto.e2e.MessageSet
import com.thesamet.proto.e2e.message_set.{LegacyMessageSet, MessageSetPayload}
import com.google.protobuf.ExtensionRegistry
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class MessageSetJavaSpec extends AnyFlatSpec with Matchers {
  "MessageSet" should "match Java for mixed encodings, reversed fields and unknown extensions" in {
    val registry = ExtensionRegistry.newInstance()
    MessageSet.registerAllExtensions(registry)
    def bytes(values: Int*): Array[Byte] = values.map(_.toByte).toArray
    val ordinary                         = bytes(162, 6, 4, 8, 7, 24, 9)
    val item                             = bytes(11, 16, 100, 26, 3, 18, 1, 120, 12)
    Seq(
      ordinary ++ item,
      item ++ ordinary,
      bytes(11, 26, 3, 18, 1, 120, 16, 100, 12),
      bytes(11, 16, 101, 26, 2, 8, 1, 12)
    ).foreach { wire =>
      val scalaValue = LegacyMessageSet.parseFrom(wire)
      val javaValue  = MessageSet.LegacyMessageSet.parseFrom(wire, registry)
      MessageSet.LegacyMessageSet.parseFrom(scalaValue.toByteArray, registry) must be(javaValue)
      LegacyMessageSet
        .parseFrom(javaValue.toByteArray)
        .extension(MessageSetPayload.payload) must be(
        scalaValue.extension(MessageSetPayload.payload)
      )
      val unknown = MessageSet.LegacyMessageSet.parseFrom(wire)
      scalaValue.toByteArray.toSeq must be(unknown.toByteArray.toSeq)
      scalaValue.serializedSize must be(unknown.getSerializedSize)
    }
  }
}
