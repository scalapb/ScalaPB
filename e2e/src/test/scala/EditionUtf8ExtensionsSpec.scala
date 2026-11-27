import com.google.protobuf.{ByteString, InvalidProtocolBufferException}
import com.thesamet.proto.e2e.edition_utf8_extensions.{
  EditionUtf8Extensions,
  EditionUtf8ExtensionsProto
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.UnknownFieldSet

class EditionUtf8ExtensionsSpec extends AnyFlatSpec with Matchers {
  "Edition 2023 string extensions" should "honor VERIFY and NONE when interpreted" in {
    val invalid = ByteString.copyFrom(Array(0x80.toByte))
    val fields  = Seq(100, 101, 102).foldLeft(UnknownFieldSet.empty) { (unknown, number) =>
      unknown.withField(number, UnknownFieldSet.Field(lengthDelimited = Seq(invalid)))
    }
    val message = EditionUtf8Extensions(unknownFields = fields)
    intercept[InvalidProtocolBufferException](
      message.extension(EditionUtf8ExtensionsProto.verified)
    )
    intercept[InvalidProtocolBufferException](
      message.extension(EditionUtf8ExtensionsProto.repeatedVerified)
    )
    message.extension(EditionUtf8ExtensionsProto.relaxed) must be(Some("\ufffd"))
    val valid =
      EditionUtf8Extensions().withExtension(EditionUtf8ExtensionsProto.verified)(Some("é"))
    EditionUtf8Extensions
      .parseFrom(valid.toByteArray)
      .extension(EditionUtf8ExtensionsProto.verified) must be(Some("é"))
  }
}
