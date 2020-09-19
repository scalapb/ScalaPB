package scalapb.e2e.scoped

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scalapb.changed.scoped.Foo

class PackageOptionsSpec extends AnyFlatSpec with Matchers {
  "package options" should "affect files in that package" in {
    assert(Foo.defaultInstance.rep.isInstanceOf[Set[String]])
  }

  "aux options" should "impact target messages in that package" in {
    assert(Foo.defaultInstance.isInstanceOf[SomeTrait])
    assert(Foo.defaultInstance.impChanged.isInstanceOf[SomeTrait])
    assert(Foo.defaultInstance.b.isInstanceOf[Array[Byte]])
  }

  "aux enum value options" should "impact target enum values in that package" in {
    assert(Foo.Vals.BB.name == "B")
    assertCompiles("val t: SomeTrait = Foo.Vals.BB")
  }
}
