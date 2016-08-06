import com.trueaccord.proto.e2e.one_of.OneofTest.SubMessage
import com.trueaccord.proto.e2e.one_of.{OneOfProto, OneofTest}
import scalapb.descriptors._
import org.scalatest._

class ScalaDescriptorSpec extends FlatSpec with MustMatchers with LoneElement with OptionValues {
  "scalaDescriptor" must "contain all messages" in {
    OneOfProto.scalaDescriptor.packageName must be("com.trueaccord.proto.e2e")
    OneOfProto.scalaDescriptor.messages.loneElement must be(OneofTest.scalaDescriptor)
    OneofTest.scalaDescriptor.fullName must be("com.trueaccord.proto.e2e.OneofTest")
    OneofTest.scalaDescriptor.nestedMessages.loneElement must be(SubMessage.scalaDescriptor)
    SubMessage.scalaDescriptor.fullName must be("com.trueaccord.proto.e2e.OneofTest.SubMessage")
    OneofTest.scalaDescriptor.fields must have size (5)

    OneofTest.scalaDescriptor.oneofs must have size(1)
    val oneofDesc: OneofDescriptor = OneofTest.scalaDescriptor.oneofs(0)

    val aField = OneofTest.scalaDescriptor.fields.find(_.name == "a").get
    aField.number must be(1)
    aField.name must be("a")
    aField.scalaType must be(ScalaType.Int)
    aField.isOptional must be(true)
    aField.isRequired must be(false)
    aField.isRepeated must be(false)
    aField.containingOneof must be(None)
    aField.containingMessage must be(OneofTest.scalaDescriptor)
    SubMessage.scalaDescriptor.fields.find(_.name == "name").map(_.scalaType).value must be(ScalaType.String)

    val tempField = OneofTest.scalaDescriptor.fields.find(_.name == "temp_field").get
    tempField.number must be(2)
    tempField.isOptional must be(true)
    tempField.containingOneof.value must be (oneofDesc)
    tempField.containingMessage must be(OneofTest.scalaDescriptor)
    SubMessage.scalaDescriptor.nestedMessages mustBe empty

    oneofDesc.fields must have size(3)
    oneofDesc.name must be ("my_one_of")

    val subField = OneofTest.scalaDescriptor.fields.find(_.name == "sub").get
    subField.containingMessage must be(OneofTest.scalaDescriptor)
    subField.scalaType must be(ScalaType.Message(SubMessage.scalaDescriptor))

    val xyzs = OneofTest.scalaDescriptor.fields.find(_.name == "xyzs").get
    xyzs.isRepeated must be(true)
    xyzs.isOptional must be(false)
    xyzs.isRequired must be(false)
    xyzs.scalaType must be (ScalaType.Enum(OneofTest.XYZ.scalaDescriptor))

    OneofTest.XYZ.scalaDescriptor.fullName must be ("com.trueaccord.proto.e2e.OneofTest.XYZ")
  }
}
