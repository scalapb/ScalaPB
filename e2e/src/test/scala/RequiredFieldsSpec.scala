import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.InvalidProtocolBufferException
import com.trueaccord.proto.e2e.reqs.OptionalFields
import com.trueaccord.proto.e2e.reqs.RequiredFields
import org.scalatest._

import scala.collection.JavaConverters._

class RequiredFieldsSpec extends FlatSpec with MustMatchers {

  val FullFieldMap: Map[FieldDescriptor, Any] = OptionalFields.javaDescriptor.getFields.asScala.map {
    fd => fd -> 1
  }.toMap

  "RequiredMessage" should "throw InvalidProtocolBufferException for empty byte array" in {
    intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(Array[Byte]()))
  }

  "RequiredMessage" should "parse when all fields are set" in {
    val b = OptionalFields.fromFieldsMap(FullFieldMap).toByteArray
    RequiredFields.parseFrom(b)
  }

  "RequiredMessage" should "throw when all but one of the fields is set" in {
    for { fd <- OptionalFields.javaDescriptor.getFields.asScala } {
      val b = OptionalFields.fromFieldsMap(FullFieldMap - fd).toByteArray
      intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(b))
    }
  }
}
