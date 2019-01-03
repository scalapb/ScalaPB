
import org.scalatest.FreeSpec
import com.trueaccord.proto.e2e.sealed_oneof_proto2._

class SealedOneofProto2Spec extends FreeSpec {
  "sealed trait for proto2" - {
    "when sealed trait field is missing case 1" in {
      val bytes = Page0(id=Some("123")).toByteArray
      assert(Page0(id=Some("123")) === Page0.parseFrom(bytes))
      assert(Page1(id="123") === Page1.parseFrom(bytes))
      assert(Page2(id="123", tpe=Seq.empty) === Page2.parseFrom(bytes))
      assert(Page3(id="123", tpe=None) === Page3.parseFrom(bytes))
      val e = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page4.parseFrom(bytes)
      }
      assert(e.getMessage === "Message missing required fields.")
      assert(Page5(id="123", tpe=Map.empty) === Page5.parseFrom(bytes))
    }

    "when sealed trait field is missing case 2" in {
      val bytes = Page1(id="123").toByteArray
      assert(Page0(id=Some("123")) === Page0.parseFrom(bytes))
      assert(Page1(id="123") === Page1.parseFrom(bytes))
      assert(Page2(id="123", tpe=Seq.empty) === Page2.parseFrom(bytes))
      assert(Page3(id="123", tpe=None) === Page3.parseFrom(bytes))
      val e = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page4.parseFrom(bytes)
      }
      assert(e.getMessage === "Message missing required fields.")
      assert(Page5(id="123", tpe=Map.empty) === Page5.parseFrom(bytes))
    }

    "when sealed trait field is repeatable" in {
      val bytes = Page2(id="123", List(SimplePage(), RedirectPage(url="/test"))).toByteArray
      assert(Page0(id=Some("123")) === Page0.parseFrom(bytes))
      assert(Page1(id="123") === Page1.parseFrom(bytes))
      assert(Page2(id="123", tpe=Seq(SimplePage(), RedirectPage(url="/test"))) === Page2.parseFrom(bytes))
      assert(Page3(id="123", tpe=Some(RedirectPage(url="/test"))) === Page3.parseFrom(bytes))
      assert(Page4(id="123", tpe=RedirectPage(url="/test")) === Page4.parseFrom(bytes))
      val e = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page5.parseFrom(bytes)
      }
      assert(e.getMessage === "Protocol message tag had invalid wire type.")
    }

    "when sealed trait field is optional" in {
      val bytes = Page3(id="123", Some(RedirectPage(url="/test"))).toByteArray
      assert(Page0(id=Some("123")) === Page0.parseFrom(bytes))
      assert(Page1(id="123") === Page1.parseFrom(bytes))
      assert(Page2(id="123", tpe=Seq(RedirectPage(url="/test"))) === Page2.parseFrom(bytes))
      assert(Page3(id="123", tpe=Some(RedirectPage(url="/test"))) === Page3.parseFrom(bytes))
      assert(Page4(id="123", tpe=RedirectPage(url="/test")) === Page4.parseFrom(bytes))
      val e = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page5.parseFrom(bytes)
      }
      assert(e.getMessage === "Protocol message tag had invalid wire type.")
    }

    "when sealed trait field is required" in {
      val bytes = Page4(id="123", RedirectPage(url="/test")).toByteArray
      assert(Page0(id=Some("123")) === Page0.parseFrom(bytes))
      assert(Page1(id="123") === Page1.parseFrom(bytes))
      assert(Page2(id="123", tpe=Seq(RedirectPage(url="/test"))) === Page2.parseFrom(bytes))
      assert(Page3(id="123", tpe=Some(RedirectPage(url="/test"))) === Page3.parseFrom(bytes))
      assert(Page4(id="123", tpe=RedirectPage(url="/test")) === Page4.parseFrom(bytes))
      val e = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page5.parseFrom(bytes)
      }
      assert(e.getMessage === "Protocol message tag had invalid wire type.")
    }

    "when sealed trait field is map" in {
      val bytes = Page5(id="123", Map("test1" -> RedirectPage(url="/test1"), "test2" -> RedirectPage(url="/test2"))).toByteArray
      assert(Page0(id=Some("123")) === Page0.parseFrom(bytes))
      assert(Page1(id="123") === Page1.parseFrom(bytes))
      val e2 = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page2.parseFrom(bytes)
      }
      assert(e2.getMessage === "While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either that the input has been truncated or that an embedded message misreported its own length.")
      val e3 = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page3.parseFrom(bytes)
      }
      assert(e3.getMessage === "While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either that the input has been truncated or that an embedded message misreported its own length.")
      val e4 = intercept[com.google.protobuf.InvalidProtocolBufferException] {
        Page4.parseFrom(bytes)
      }
      assert(e4.getMessage === "While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either that the input has been truncated or that an embedded message misreported its own length.")
      assert(Page5(id="123", Map("test1" -> RedirectPage(url="/test1"), "test2" -> RedirectPage(url="/test2"))) === Page5.parseFrom(bytes))
    }

    "when sealed trait field is oneof empty" in {
      val bytes = PageTypeContainer(PageTypeContainer.PageType.Empty).toByteArray
      assert(PageTypeContainer(PageTypeContainer.PageType.Empty) === PageTypeContainer.parseFrom(bytes))
    }

    "when sealed trait field is oneof is not empty case 1" in {
      val bytes1 = PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe1(SimplePage()))).toByteArray
      val bytes2 = PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe1(RedirectPage(url="/test")))).toByteArray
      assert(PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe1(SimplePage()))) === PageWithTypeContainer.parseFrom(bytes1))
      assert(PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe1(RedirectPage(url="/test")))) === PageWithTypeContainer.parseFrom(bytes2))
    }

    "when sealed trait field is oneof is not empty case 2" in {
      val bytes1 = PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe2(SimplePage()))).toByteArray
      val bytes2 = PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe2(RedirectPage(url="/test")))).toByteArray
      assert(PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe2(SimplePage()))) === PageWithTypeContainer.parseFrom(bytes1))
      assert(PageWithTypeContainer(id="123", container=PageTypeContainer(PageTypeContainer.PageType.Tpe2(RedirectPage(url="/test")))) === PageWithTypeContainer.parseFrom(bytes2))
    }
  }
}
