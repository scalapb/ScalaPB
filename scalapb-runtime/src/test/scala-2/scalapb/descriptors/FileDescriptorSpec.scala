package scalapb.descriptors

import munit._
import com.google.protobuf.descriptor.FileDescriptorProto

class FileDescriptorSpec extends FunSuite {
  // intercept tests discard value
  test("nameChains gives chain of names") {
    assertEquals(FileDescriptor.nameChain("foo"), ("" :: "foo" :: Nil))
    assertEquals(
      FileDescriptor.nameChain("foo.bar.baz"),
      ("" :: "foo" :: "foo.bar" :: "foo.bar.baz" :: Nil)
    )
    assertEquals(FileDescriptor.nameChain(""), ("" :: Nil))
  }

  test("nameChains raises exception if name starts or ends with dot") {
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

  test("parentOf returns the right parent") {
    assertEquals(FileDescriptor.parentOf("foo.bar"), ("foo"))
    assertEquals(FileDescriptor.parentOf("foo.bar.baz"), ("foo.bar"))
    assertEquals(FileDescriptor.parentOf("foo"), (""))
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

  test("buildFrom builds basic descriptor") {
    val fd = FileDescriptor.buildFrom(Basic, Nil)

    assertEquals(fd.packageName, ("mypkg"))
    assertEquals(fd.messages.length, 1)
    assertEquals(fd.messages.head.name, ("Msg1"))
    assertEquals(fd.messages.head.fullName, ("mypkg.Msg1"))
    assertEquals(fd.messages.head.fields.head.name, ("my_field"))
    assertEquals(fd.messages.head.fields.head.scalaType, (ScalaType.Int))
  }

  test("buildFrom fails on duplicate message name") {
    interceptMessage[DescriptorValidationException](
      "myfile.proto: Duplicate names found: mypkg.Msg1"
    ) {
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
    }
  }

  test("buildFrom fails on duplicate message name between packages") {
    val fd = FileDescriptor.buildFrom(Basic, Nil)
    interceptMessage[DescriptorValidationException](
      "basic.proto: Name already defined in 'basic.proto': mypkg.Msg1"
    ) {
      FileDescriptor.buildFrom(Basic, Seq(fd))
    }
  }

  test("buildFrom fails when message name conflicts with package name") {
    val myPkgMessage = FileDescriptorProto.fromAscii("""|name: "mypkg.proto"
                                                        |message_type {
                                                        |  name: "mypkg"
                                                        |  field {
                                                        |    name: "my_field"
                                                        |    type: TYPE_UINT32
                                                        |  }
                                                        |}
        """.stripMargin)
    interceptMessage[DescriptorValidationException](
      "mypkg.proto: Name already defined in 'basic.proto': mypkg"
    ) {
      FileDescriptor.buildFrom(myPkgMessage, Seq(FileDescriptor.buildFrom(Basic, Nil)))
    }
    interceptMessage[DescriptorValidationException](
      "basic.proto: Name already defined in 'mypkg.proto': mypkg"
    ) {
      FileDescriptor.buildFrom(Basic, Seq(FileDescriptor.buildFrom(myPkgMessage, Nil)))
    }
  }

  test("buildFrom resolves message names") {
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
    assertEquals(msg.fullName, ("mypkg.Msg1"))
    assertEquals(msg.findFieldByName("field_full").get.scalaType, (ScalaType.Message(msg)))
    assertEquals(msg.findFieldByName("field_ref").get.scalaType, (ScalaType.Message(msg)))
    val msg2 = msg.nestedMessages.find(_.name == "Msg2").get
    assertEquals(
      msg2.findFieldByName("f1").get.scalaType,
      (ScalaType.Message(
        msg.nestedMessages.find(_.name == "Msg1").get
      ))
    )
    assertEquals(msg2.findFieldByName("f2").get.scalaType, (ScalaType.Message(msg2)))
  }

  test("buildFrom fails when reference does not exist") {
    val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
                                              |message_type {
                                              |  name: "Msg1"
                                              |  field {
                                              |    name: "ff"
                                              |    type: TYPE_MESSAGE
                                              |    type_name: "Msg2"
                                              |  }
                                              |}""".stripMargin)
    interceptMessage[DescriptorValidationException](
      "mypkg.Msg1: Could not find type Msg2 for field ff"
    ) {
      FileDescriptor.buildFrom(fdp, Nil)
    }
  }

  test("buildFrom fails when message ref type is not a message") {
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
    interceptMessage[DescriptorValidationException](
      "mypkg.Msg1: Invalid type TheEnum for field ff"
    ) {
      FileDescriptor.buildFrom(fdp, Nil)
    }
  }

  test("buildFrom fails when enum ref type is not an enum") {
    val fdp = FileDescriptorProto.fromAscii("""package: "mypkg"
                                              |message_type {
                                              |  name: "Msg1"
                                              |  field {
                                              |    name: "ff"
                                              |    type: TYPE_ENUM
                                              |    type_name: "Msg1"
                                              |  }
                                              |}""".stripMargin)
    interceptMessage[DescriptorValidationException]("mypkg.Msg1: Invalid type Msg1 for field ff") {
      FileDescriptor.buildFrom(fdp, Nil)
    }
  }

  test("buildFrom recognizes messages when type field is missing") {
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
    assertEquals(msg.fullName, ("mypkg.Msg1"))
    assertEquals(msg.findFieldByName("field_full").get.scalaType, (ScalaType.Message(msg)))
  }

  test("buildFrom recognizes enums when type field is missing") {
    val fdp     = FileDescriptorProto.fromAscii("""package: "mypkg"
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
    val fd      = FileDescriptor.buildFrom(fdp, Nil)
    val msg     = fd.messages(0)
    val enumVal = msg.enums(0)
    assertEquals(msg.fullName, ("mypkg.Msg1"))
    assertEquals(msg.findFieldByName("field_full").get.scalaType, (ScalaType.Enum(enumVal)))
  }
}
