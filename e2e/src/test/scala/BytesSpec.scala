import com.google.protobuf.ByteString
import com.thesamet.proto.e2e.bytes._
import com.thesamet.proto.e2e.bytes_proto2.ByteMessage2
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class BytesSpec extends AnyFlatSpec with Matchers {
  "getField" should "work with proto2" in {
    val b = ByteMessage2(s = Some(ByteString.copyFromUtf8("boo")))
    b.getField(ByteMessage2.scalaDescriptor.findFieldByName("s").get) must be(
      scalapb.descriptors.PByteString(b.getS)
    )
  }

  "default value" should "work in proto2" in {
    val b = ByteMessage2()
    b.getSDef.toStringUtf8() must be("foobar")
  }
}
