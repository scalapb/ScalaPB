package scalapb

import com.google.protobuf.field_mask.FieldMask
import munit.FunSuite

class FieldMaskTreeSpec extends FunSuite {
  test("testAddFieldPath") {
    // https://github.com/protocolbuffers/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskTreeTest.java#L39-L62
    val tree1 = FieldMaskTree.Empty
    assertEquals(tree1.fieldMask, FieldMask())
    val tree2 = FieldMaskTree.union(tree1, FieldMaskTree(Seq("")))
    assertEquals(tree2.fieldMask, FieldMask())
    val tree3 = FieldMaskTree.union(tree2, FieldMaskTree(Seq("foo")))
    assertEquals(tree3.fieldMask, FieldMask(Seq("foo")))
    val tree4 = FieldMaskTree.union(tree3, FieldMaskTree(Seq("foo")))
    assertEquals(tree4.fieldMask, FieldMask(Seq("foo")))
    val tree5 = FieldMaskTree.union(tree4, FieldMaskTree(Seq("bar.baz")))
    assertEquals(tree5.fieldMask, FieldMask(Seq("bar.baz", "foo")))
    val tree6 = FieldMaskTree.union(tree5, FieldMaskTree(Seq("foo.bar")))
    assertEquals(tree6.fieldMask, FieldMask(Seq("bar.baz", "foo")))
    val tree7 = FieldMaskTree.union(tree6, FieldMaskTree(Seq("bar.quz")))
    assertEquals(tree7.fieldMask, FieldMask(Seq("bar.baz", "bar.quz", "foo")))
    val tree8 = FieldMaskTree.union(tree7, FieldMaskTree(Seq("bar")))
    assertEquals(tree8.fieldMask, FieldMask(Seq("bar", "foo")))
  }

  test("testMergeFromFieldMask") {
    // https://github.com/protocolbuffers/protobuf/blob/v3.6.0/java/util/src/test/java/com/google/protobuf/util/FieldMaskTreeTest.java#L64-L69
    val tree1 = FieldMaskTree(Seq("foo", "bar.baz", "bar.quz"))
    assertEquals(tree1.fieldMask, FieldMask(Seq("bar.baz", "bar.quz", "foo")))
    val tree2 = FieldMaskTree(Seq("foo.bar", "bar"))
    assertEquals(tree2.fieldMask, FieldMask(Seq("bar", "foo")))
  }
}
