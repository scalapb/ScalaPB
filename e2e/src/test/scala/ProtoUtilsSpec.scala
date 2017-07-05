
import com.trueaccord.scalapb.grpc.ProtoUtils
import com.trueaccord.proto.well_known._
import org.scalatest.{FlatSpec, MustMatchers}


class ProtoUtilsSpec extends FlatSpec with MustMatchers{
  "marshaller from metadataMarshaller" should "make a un/marshall roundtrip" in {
    val marshaller = ProtoUtils.metadataMarshaller[TestWrappers]
    val proto = TestWrappers()

    marshaller.parseBytes(marshaller.toBytes(proto)) must be (proto)
  }

  "marshaller from metadataMarshaller" should "make a un/marshall roundtrip with marshaller from io.grpc.protobuf.lite.ProtoLiteUtils" in {
    val marshaller = ProtoUtils.metadataMarshaller[TestWrappers]
    val proto = TestWrappers()

    val javaProto = com.trueaccord.proto.WellKnown.TestWrappers.newBuilder().build()
    val javaMarshaller = io.grpc.protobuf.lite.ProtoLiteUtils.metadataMarshaller(javaProto)

    marshaller.parseBytes(javaMarshaller.toBytes(javaProto)) must be (proto)
    javaMarshaller.parseBytes(marshaller.toBytes(proto)) must be (javaProto)
  }

  "marshaller from metadataMarshaller" should "throws for invalid input" in {
    val marshaller = ProtoUtils.metadataMarshaller[TestWrappers]

    an [IllegalArgumentException] must be thrownBy marshaller.parseBytes(Array[Byte](-127))
  }

  "keyForProto" should "returns a key with name \"type\"-bin" in {
    val key = ProtoUtils.keyForProto[TestWrappers]

    key.originalName() must be ("com.trueaccord.proto.TestWrappers-bin")
  }
}
