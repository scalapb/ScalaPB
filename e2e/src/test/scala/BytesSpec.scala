import com.google.protobuf.ByteString
import com.trueaccord.proto.e2e.bytes._
import com.trueaccord.proto.e2e.bytes_proto2.ByteMessage2
import org.scalatest._

class BytesSpec extends FlatSpec with MustMatchers {

  "fromFieldsMap" should "work when empty" in {
    val b = ByteMessage()
    ByteMessage.fromFieldsMap(b.getAllFields) must be(b)
  }

  "fromFieldsMap" should "work when non-empty" in {
    val b = ByteMessage(s = ByteString.copyFrom(Array[Byte](17, 54)))
    ByteMessage.fromFieldsMap(b.getAllFields) must be(b)
  }

  "formFieldsMap" should "work with proto2" in {
    ByteMessage2().getAllFields must be(Map())
    val b = ByteMessage2(s = Some(ByteString.copyFromUtf8("boo")))
    b.getAllFields must be(
        Map(ByteMessage2.javaDescriptor.findFieldByName("s") -> b.getS))
    b.getField(ByteMessage2.scalaDescriptor.findFieldByName("s").get) must be (scalapb.descriptors.PByteString(b.getS))
    ByteMessage2.fromFieldsMap(b.getAllFields) must be(b)
  }

  "default value" should "work in proto2" in {
    val b = ByteMessage2()
    b.getSDef.toStringUtf8() must be("foobar")
  }
}

