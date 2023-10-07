import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import scalapb.derives.{Show, TC}
import com.thesamet.proto.e2e.derives.cases

class DerivesSpec extends AnyFlatSpec with Matchers with OptionValues:
    "Show typeclass" should "be summonable for derives.foo" in:
        val s = summon[Show[cases.Foo]]
        s.show(cases.Foo(3,"xyz")) must be(
            """a: 3
              |b: "xyz"
              |""".stripMargin)

    "TC typeclass" should "be summonable for derives.foo and be null" in:
        val s = summon[TC[cases.Foo]]
        s must be(null)

    "Show" should "return sealed for Expr" in:
        val s = summon[Show[cases.Expr]]
        s.show(cases.M1()) must be("Sealed!")