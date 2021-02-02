package scalapb

import com.google.protobuf.field_mask.FieldMask
import munit.FunSuite

class FieldMaskUtilSpec extends FunSuite {
  test("toJsonString") {
    // https://github.com/protocolbuffers/protobuf/blob/v3.14.0/java/util/src/test/java/com/google/protobuf/util/JsonFormatTest.java#L804-L813
    val x      = FieldMask(Seq("foo.bar", "baz", "foo_bar.baz"))
    val expect = "foo.bar,baz,fooBar.baz"
    val json   = FieldMaskUtil.toJsonString(x)
    assertEquals(json, expect)
  }

  test("union") {
    // https://github.com/protocolbuffers/protobuf/blob/v3.14.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskUtilTest.java#L209-L216
    val mask1  = FieldMask(Seq("foo", "bar.baz", "bar.quz"))
    val mask2  = FieldMask(Seq("foo.bar", "bar"))
    val expect = FieldMask(Seq("bar", "foo"))
    val union  = FieldMaskUtil.union(mask1, mask2)
    assertEquals(union, expect)
  }

  test("union using var args") {
    // https://github.com/protocolbuffers/protobuf/blob/v3.14.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskUtilTest.java#L218-L225
    val mask1  = FieldMask(Seq("foo"))
    val mask2  = FieldMask(Seq("foo.bar", "bar.quz"))
    val mask3  = FieldMask(Seq("bar.quz"))
    val mask4  = FieldMask(Seq("bar"))
    val expect = FieldMask(Seq("bar", "foo"))
    val union  = FieldMaskUtil.union(mask1, mask2, mask3, mask4)
    assertEquals(union, expect)
  }
}
