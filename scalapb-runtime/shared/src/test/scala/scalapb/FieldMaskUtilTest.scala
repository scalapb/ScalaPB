package scalapb

import com.google.protobuf.field_mask.FieldMask
import utest.{TestSuite, Tests}

object FieldMaskUtilTest extends TestSuite {
  override def tests = Tests {
    // https://github.com/google/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/JsonFormatTest.java#L761-L770
    val x      = FieldMask(Seq("foo.bar", "baz", "foo_bar.baz"))
    val expect = "foo.bar,baz,fooBar.baz"
    val json   = FieldMaskUtil.toJsonString(x)
    assert(json == expect)
  }
}
