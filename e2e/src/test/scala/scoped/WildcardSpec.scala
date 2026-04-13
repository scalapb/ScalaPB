package scalapb.e2e.scoped

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.changed.scoped._

class WildcardSpec extends AnyFlatSpec with Matchers {
  assert(Wild1().isInstanceOf[WildcardTrait])
  assert(Wild2().isInstanceOf[WildcardTrait])
  assert(Wild2().isInstanceOf[SomeTrait])
}
