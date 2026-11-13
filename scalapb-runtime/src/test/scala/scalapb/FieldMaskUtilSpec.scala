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

  test("lowerSnakeCaseToCamelCase handles empty string") {
    val value = FieldMaskUtil.lowerSnakeCaseToCamelCase("")
    assertEquals(value, "")
  }

  test("lowerSnakeCaseToCamelCase handles '_'") {
    val value = FieldMaskUtil.lowerSnakeCaseToCamelCase("_")
    assertEquals(value, "")
  }

  test("lowerSnakeCaseToCamelCase handles '__'") {
    val value = FieldMaskUtil.lowerSnakeCaseToCamelCase("__")
    assertEquals(value, "")
  }
  test("JSON field masks reject underscores and preserve initial capitals as leading underscores") {
    intercept[IllegalArgumentException](FieldMaskUtil.fromJsonString("foo,bar_bar"))
    assertEquals(
      FieldMaskUtil.fromJsonString("Foo,fooBar.baz"),
      FieldMask(Seq("_foo", "foo_bar.baz"))
    )
  }
  test("JSON field mask serialization rejects paths that cannot round trip") {
    for (path <- Seq("fooBar", "foo_3_bar", "foo__bar", "foo_", "foo._3")) {
      intercept[IllegalArgumentException](FieldMaskUtil.toJsonString(FieldMask(Seq(path))))
    }
    val mask = FieldMask(Seq("foo_bar", "_foo", "foo._bar", "foo3_bar"))
    assertEquals(FieldMaskUtil.fromJsonString(FieldMaskUtil.toJsonString(mask)), mask)
  }
}
