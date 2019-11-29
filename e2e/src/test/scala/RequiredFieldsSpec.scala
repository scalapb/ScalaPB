import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.InvalidProtocolBufferException
import com.thesamet.proto.e2e.reqs.OptionalFields
import com.thesamet.proto.e2e.reqs.RequiredFields
import org.scalatest._

import scala.collection.JavaConverters._

class RequiredFieldsSpec extends FlatSpec with MustMatchers {
  "RequiredMessage" should "throw InvalidProtocolBufferException for empty byte array" in {
    intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(Array[Byte]()))
  }
}
