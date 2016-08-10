
import com.google.protobuf.InvalidProtocolBufferException
import com.trueaccord.scalapb.grpc.ProtoUtils
import com.trueaccord.scalapb.TestUtil
import com.trueaccord.proto.well_known._
import org.scalatest.{FlatSpec, MustMatchers}


class ProtoUtilsSpec extends FlatSpec with MustMatchers{
  "marshaller from metadataMarshaller" should "make a un/marshall roundtrip" in {
    val marshaller = ProtoUtils.metadataMarshaller(TestWrappers.defaultInstance)
    val proto = TestWrappers()

    marshaller.parseBytes(marshaller.toBytes(proto)) must be (proto)
  }

  "marshaller from metadataMarshaller" should "throws for invalid input" in {
    val marshaller = ProtoUtils.metadataMarshaller(TestWrappers.defaultInstance)

    an [IllegalArgumentException] must be thrownBy marshaller.parseBytes(Array[Byte](-127))
  }

  "keyForProto" should "returns a key with name \"type\"-bin" in {
    val key = ProtoUtils.keyForProto(TestWrappers.defaultInstance)

    key.originalName() must be ("com.trueaccord.proto.TestWrappers-bin")
  }
}
