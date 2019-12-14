package scalapb.scoped

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class PackageOptionsSpec extends AnyFlatSpec with Matchers {
  "package options" should "affect files in that package" in {
    assert(scalapb.changed.scoped.Foo.defaultInstance.rep.isInstanceOf[Set[String]])
  }
}
