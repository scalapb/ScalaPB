import com.google.protobuf.descriptor.FileDescriptorProto
import com.thesamet.proto.e2e.custom_options.CustomOptionsProto.{messageC, optName}
import com.thesamet.proto.e2e.custom_options_use.FooMessage
import com.thesamet.proto.e2e.extensions._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.descriptors.FileDescriptor

class ExtensionsSpec extends AnyFlatSpec with Matchers with OptionValues {
  "BaseMessage.parseFrom" should "parse unknown fields" in {
    val helper   = Helper(optInt = Some(37), optString = Some("foo"))
    val extended = BaseMessage.parseFrom(helper.toByteArray)
    extended.extension(Extension.optInt) must be(Some(37))
    extended.extension(Extension.optString) must be(Some("foo"))
  }

  "BaseMessage.parseFrom" should "parse unknown fields with duplication" in {
    val repeatedHelper = RepeatedHelper(optInt = Seq(37, 12), optString = Seq("foo", "bar"))
    val extended       = BaseMessage.parseFrom(repeatedHelper.toByteArray)
    extended.extension(Extension.optInt) must be(Some(12))
    extended.extension(Extension.optString) must be(Some("bar"))
  }

  "BaseMessage.parseFrom" should "parse extensions after full serialization cycle" in {
    //** Serializing side **//
    val fooFile   = FooMessage.scalaDescriptor.file
    val serialized = com.google.protobuf.descriptor.FileDescriptorProto.toByteArray(fooFile.asProto)

    //** De-serializing side **/
    val javaFileDescriptorProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(serialized)

    // Prior to PR #1963 the scalaFileDescriptorProto did not contain the options/extensions...
    val javaFileDescriptor = FileDescriptorProto.fromJavaProto(javaFileDescriptorProto)

    // ...and subsequently, the resulting fileDescriptor was also missing the options/extensions
    val fileDescriptor = FileDescriptor.buildFrom(javaFileDescriptor, Nil)

    val revivedFieldDescriptor = fileDescriptor.messages.find(_.name == "FooMessage").get.fields.find(_.name == "myField1").get

    val messageCOption = revivedFieldDescriptor.asProto.getOptions.extension(messageC).get
    val optNameOption = revivedFieldDescriptor.asProto.getOptions.extension(optName).get
    messageCOption.c must be (Some("CCC"))
    optNameOption.firstName must be ("John")
  }
}
