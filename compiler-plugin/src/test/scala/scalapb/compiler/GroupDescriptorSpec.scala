package scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor.Type
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class GroupDescriptorSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  "Group descriptors" should "include proto2 groups as message fields and retain Java capitalization" in {
    val files = generateFileSet(Seq("groups.proto" -> """syntax = "proto2";
      package group_test;
      message Container { optional group CamelCase = 1 { optional int32 value = 2; } }
    """))
    val implicits = new DescriptorImplicits(GeneratorParams(), files, SecondaryOutputProvider.empty)
    import implicits._
    val container = files.head.findMessageTypeByName("Container")
    container.fields.size must be(1)
    val field = container.fields.head
    field.isMessage must be(true)
    field.isGroup must be(true)
    field.upperJavaName must be("CamelCase")
    Types.wireType(field.getType) must be(Types.WIRETYPE_START_GROUP)
  }

  "Editions message encoding" should "resolve inherited groups, overrides, oneofs and map exceptions" in {
    val files = generateFileSet(Seq("edition_groups.proto" -> """edition = "2023";
      package group_test;
      option features.message_encoding = DELIMITED;
      message Payload { int32 value = 1; }
      message Container {
        Payload single = 1;
        repeated Payload repeated = 2;
        oneof choice { Payload selected = 3; int32 number = 4; }
        Payload length_prefixed = 5 [features.message_encoding = LENGTH_PREFIXED];
        map<string, Payload> entries = 6;
      }
    """))
    val implicits = new DescriptorImplicits(GeneratorParams(), files, SecondaryOutputProvider.empty)
    import implicits._
    val container = files.head.findMessageTypeByName("Container")
    container.fields.size must be(6)
    Seq(1, 2, 3).foreach { number =>
      val field = container.findFieldByNumber(number)
      field.isGroup must be(true)
      field.isMessage must be(true)
      field.getType must be(Type.GROUP)
    }
    container.getOneofs.get(0).fields.size must be(2)
    container.findFieldByNumber(3).upperJavaName must be("Selected")
    container.findFieldByNumber(5).getType must be(Type.MESSAGE)
    val map = container.findFieldByNumber(6)
    map.isMapField must be(true)
    map.getType must be(Type.MESSAGE)
    map.getMessageType.findFieldByNumber(2).getType must be(Type.MESSAGE)
  }
}
