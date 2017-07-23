package scalapb.descriptors

import com.google.protobuf.descriptor.FileDescriptorProto
import org.scalatest._

class FileDescriptorSpec extends FlatSpec with MustMatchers with OptionValues {
  "nameChains" should "give chain of names" in {
    FileDescriptor.nameChain("foo") must be ("" :: "foo" :: Nil)
    FileDescriptor.nameChain("foo.bar.baz") must be ("" :: "foo" :: "foo.bar" :: "foo.bar.baz" :: Nil)
    FileDescriptor.nameChain("") must be ("" :: Nil)
  }

  "nameChains" should "raise exception if name starts or ends with dot" in {
    intercept[IllegalArgumentException] {
      FileDescriptor.nameChain("foo.")
    }
    intercept[IllegalArgumentException] {
      FileDescriptor.nameChain(".foo")
    }
    intercept[IllegalArgumentException] {
      FileDescriptor.nameChain(".")
    }
    intercept[IllegalArgumentException] {
      FileDescriptor.nameChain(".foo.bar")
    }
  }

  "parentOf" should "return the right parent" in {
    FileDescriptor.parentOf("foo.bar") must be ("foo")
    FileDescriptor.parentOf("foo.bar.baz") must be ("foo.bar")
    FileDescriptor.parentOf("foo") must be ("")
    intercept[IllegalArgumentException] {
      FileDescriptor.parentOf("")
    }
  }

  val Basic = FileDescriptorProto.fromAscii(
    """package: "mypkg"
      |name: "basic.proto"
      |message_type {
      |  name: "Msg1"
      |  field {
      |    name: "my_field"
      |    type: TYPE_UINT32
      |  }
      |}
    """.stripMargin)

  "buildFrom" should "build basic descriptor" in {
    val fd = FileDescriptor.buildFrom(Basic, Nil)

    fd.packageName must be ("mypkg")
    fd.messages must have size(1)
    fd.messages.head.name must be ("Msg1")
    fd.messages.head.fullName must be ("mypkg.Msg1")
    fd.messages.head.fields.head.name must be ("my_field")
    fd.messages.head.fields.head.scalaType must be (ScalaType.Int)
  }

  "buildFrom" should "fail on duplicate message name" in {
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(FileDescriptorProto.fromAscii(
        """package: "mypkg"
          |name: "myfile.proto"
          |message_type {
          |  name: "Msg1"
          |  field {
          |    name: "my_field"
          |    type: TYPE_UINT32
          |  }
          |}
          |message_type {
          |  name: "Msg1"
          |  field {
          |    name: "other_field"
          |    type: TYPE_UINT64
          |  }
          |}
        """.stripMargin), Nil)
    }.getMessage must be ("myfile.proto: Duplicate names found: mypkg.Msg1")
  }

  "buildFrom" should "fail on duplicate message name between packages" in {
    val fd = FileDescriptor.buildFrom(Basic, Nil)
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(Basic, Seq(fd))
    }.getMessage must be ("basic.proto: Name already defined in 'basic.proto': mypkg.Msg1")
  }

  "buildFrom" should "fail when message name conflicts with package name" in {
    val myPkgMessage = FileDescriptorProto.fromAscii(
        """|name: "mypkg.proto"
           |message_type {
           |  name: "mypkg"
           |  field {
           |    name: "my_field"
           |    type: TYPE_UINT32
           |  }
           |}
        """.stripMargin)
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(
        myPkgMessage, Seq(FileDescriptor.buildFrom(Basic, Nil)))
    }.getMessage must be ("mypkg.proto: Name already defined in 'basic.proto': mypkg")
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(
        Basic, Seq(FileDescriptor.buildFrom(myPkgMessage, Nil)))
    }.getMessage must be ("basic.proto: Name already defined in 'mypkg.proto': mypkg")
  }

  "buildFrom" should "resolve message names" in {
    val fdp = FileDescriptorProto.fromAscii(
      """package: "mypkg"
        |message_type {
        |  name: "Msg1"
        |  field {
        |    name: "field_full"
        |    type: TYPE_MESSAGE
        |    type_name: ".mypkg.Msg1"
        |  }
        |  field {
        |    name: "field_ref"
        |    type: TYPE_MESSAGE
        |    type_name: "mypkg.Msg1"
        |  }
        |  nested_type {
        |    name: "Msg1"
        |  }
        |  nested_type {
        |    name: "Msg2"
        |    field: {
        |      name: "f1"
        |      type: TYPE_MESSAGE
        |      type_name: "Msg1"
        |    }
        |    field: {
        |      name: "f2"
        |      type: TYPE_MESSAGE
        |      type_name: "Msg2"
        |    }
        |  }
        |}
      """.stripMargin)
    val fd = FileDescriptor.buildFrom(fdp, Nil)
    val msg = fd.messages(0)
    msg.fullName must be ("mypkg.Msg1")
    msg.findFieldByName("field_full").value.scalaType must be (ScalaType.Message(msg))
    msg.findFieldByName("field_ref").value.scalaType must be (ScalaType.Message(msg))
    val msg2 = msg.nestedMessages.find(_.name == "Msg2").value
    msg2.findFieldByName("f1").value.scalaType must be (
      ScalaType.Message(msg.nestedMessages.find(_.name == "Msg1").value))
    msg2.findFieldByName("f2").value.scalaType must be(ScalaType.Message(msg2))
  }

  "buildFrom" should "fail when reference does not exist" in {
    val fdp = FileDescriptorProto.fromAscii(
      """package: "mypkg"
        |message_type {
        |  name: "Msg1"
        |  field {
        |    name: "ff"
        |    type: TYPE_MESSAGE
        |    type_name: "Msg2"
        |  }
        |}""".stripMargin)
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(fdp, Nil)
    }.getMessage must be ("mypkg.Msg1: Could not find message Msg2 for field ff")
  }

  "buildFrom" should "fail when message ref type is not a message" in {
    val fdp = FileDescriptorProto.fromAscii(
      """package: "mypkg"
        |message_type {
        |  name: "Msg1"
        |  enum_type {
        |    name: "TheEnum"
        |  }
        |  field {
        |    name: "ff"
        |    type: TYPE_MESSAGE
        |    type_name: "TheEnum"
        |  }
        |}""".stripMargin)
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(fdp, Nil)
    }.getMessage must be ("mypkg.Msg1: Invalid type TheEnum for field ff")
  }

  "buildFrom" should "fail when enum ref type is not an enum" in {
    val fdp = FileDescriptorProto.fromAscii(
      """package: "mypkg"
        |message_type {
        |  name: "Msg1"
        |  field {
        |    name: "ff"
        |    type: TYPE_ENUM
        |    type_name: "Msg1"
        |  }
        |}""".stripMargin)
    intercept[DescriptorValidationException] {
      FileDescriptor.buildFrom(fdp, Nil)
    }.getMessage must be ("mypkg.Msg1: Invalid type Msg1 for field ff")
  }
}
