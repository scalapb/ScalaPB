package scalapb.scoped

import org.scalatest._

class PackageOptionsSpec extends FlatSpec with MustMatchers {
  "package options" should "affect files in that package" in {
    assert(scalapb.changed.scoped.Foo.defaultInstance.rep.isInstanceOf[Set[String]])
  }
}
