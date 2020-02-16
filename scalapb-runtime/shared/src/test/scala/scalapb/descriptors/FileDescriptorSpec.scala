package scalapb.descriptors

import utest._
import com.google.protobuf.descriptor.FileDescriptorProto

object FileDescriptorSpec extends TestSuite {
  val tests = Tests {
    "nameChains gives chain of names" - {
      FileDescriptor.nameChain("foo") ==> ("" :: "foo" :: Nil)
      FileDescriptor.nameChain("foo.bar.baz") ==> ("" :: "foo" :: "foo.bar" :: "foo.bar.baz" :: Nil)
      FileDescriptor.nameChain("") ==> ("" :: Nil)
    }

    "nameChains raises exception if name starts or ends with dot" - {
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

    "parentOf returns the right parent" - {
      FileDescriptor.parentOf("foo.bar") ==> ("foo")
      FileDescriptor.parentOf("foo.bar.baz") ==> ("foo.bar")
      FileDescriptor.parentOf("foo") ==> ("")
      intercept[IllegalArgumentException] {
        FileDescriptor.parentOf("")
      }
    }

    val Basic = FileDescriptorProto.fromAscii("""package: "mypkg"
                                                |name: "basic.proto"
                                                |message_type {
                                                |  name: "Msg1"
                                                |  field {
                                                |    name: "my_field"
                                                |    type: TYPE_UINT32
                                                |  }
                                                |}
      """.stripMargin)

    "buildFrom builds basic descriptor" - {
      val fd = FileDescriptor.buildFrom(Basic, Nil)

      fd.packageName ==> ("mypkg")
      fd.messages.length ==> 1
      fd.messages.head.name ==> ("Msg1")
      fd.messages.head.fullName ==> ("mypkg.Msg1")
      fd.messages.head.fields.head.name ==> ("my_field")
      fd.messages.head.fields.head.scalaType ==> (ScalaType.Int)
    }

    "buildFrom fails on duplicate message name" - {
      intercept[DescriptorValidationException] {
        FileDescriptor.buildFrom(
          FileDescriptorProto.fromAscii("""package: "mypkg"
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
            """.stripMargin),
          Nil
        )
      }.getMessage ==> ("myfile.proto: Duplicate names found: mypkg.Msg1")
    }

    "buildFrom fails on duplicate message name between packages" - {
      val fd = FileDescriptor.buildFrom(Basic, Nil)
      intercept[DescriptorValidationException] {
        FileDescriptor.buildFrom(Basic, Seq(fd))
      }.getMessage ==> ("basic.proto: Name already defined in 'basic.proto': mypkg.Msg1")
    }

    "buildFrom fails when message name conflicts with package name" - {
      val myPkgMessage = FileDescriptorProto.fromAscii("""|name: "mypkg.proto"
                                                          |message_type {
                                                          |  name: "mypkg"
                                                          |  field {
                                                          |    name: "my_field"
                                                          |    type: TYPE_UINT32
                                                          |  }
                                                          |}
        """.stripMargin)
      intercept[DescriptorValidationException] {
        FileDescriptor.buildFrom(myPkgMessage, Seq(FileDescriptor.buildFrom(Basic, Nil)))
      }.getMessage ==> ("mypkg.proto: Name already defined in 'basic.proto': mypkg")
      intercept[DescriptorValidationException] {
        FileDescriptor.buildFrom(Basic, Seq(FileDescriptor.buildFrom(myPkgMessage, Nil)))
      }.getMessage ==> ("basic.proto: Name already defined in 'mypkg.proto': mypkg")
    }

    "buildFrom resolves message names" - {
      val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
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
      val fd  = FileDescriptor.buildFrom(fdp, Nil)
      val msg = fd.messages(0)
      msg.fullName ==> ("mypkg.Msg1")
      msg.findFieldByName("field_full").get.scalaType ==> (ScalaType.Message(msg))
      msg.findFieldByName("field_ref").get.scalaType ==> (ScalaType.Message(msg))
      val msg2 = msg.nestedMessages.find(_.name == "Msg2").get
      msg2.findFieldByName("f1").get.scalaType ==> (ScalaType.Message(
        msg.nestedMessages.find(_.name == "Msg1").get
      ))
      msg2.findFieldByName("f2").get.scalaType ==> (ScalaType.Message(msg2))
    }

    "buildFrom fails when reference does not exist" - {
      val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
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
      }.getMessage ==> ("mypkg.Msg1: Could not find type Msg2 for field ff")
    }

    "buildFrom fails when message ref type is not a message" - {
      val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
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
      }.getMessage ==> ("mypkg.Msg1: Invalid type TheEnum for field ff")
    }

    "buildFrom fails when enum ref type is not an enum" - {
      val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
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
      }.getMessage ==> ("mypkg.Msg1: Invalid type Msg1 for field ff")
    }

    "buildFrom recognizes messages when type field is missing" - {
      val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
                                                |message_type {
                                                |  name: "Msg1"
                                                |  field {
                                                |    name: "field_full"
                                                |    type_name: ".mypkg.Msg1"
                                                |  }
                                                |}""".stripMargin)
      val fd  = FileDescriptor.buildFrom(fdp, Nil)
      val msg = fd.messages(0)
      msg.fullName ==> ("mypkg.Msg1")
      msg.findFieldByName("field_full").get.scalaType ==> (ScalaType.Message(msg))
    }

    "buildFrom recognizes enums when type field is missing" - {
      val fdp  = FileDescriptorProto.fromAscii("""package: "mypkg"
                                                 |message_type {
                                                 |  name: "Msg1"
                                                 |  enum_type {
                                                 |    name: "TheEnum"
                                                 |  }
                                                 |  field {
                                                 |    name: "field_full"
                                                 |    type_name: "TheEnum"
                                                 |  }
                                                 |}""".stripMargin)
      val fd   = FileDescriptor.buildFrom(fdp, Nil)
      val msg  = fd.messages(0)
      val enum = msg.enums(0)
      msg.fullName ==> ("mypkg.Msg1")
      msg.findFieldByName("field_full").get.scalaType ==> (ScalaType.Enum(enum))
    }
  }
}
