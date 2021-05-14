import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.e2e.no_getters._
import scalapb.e2e.no_getters_or_lenses._
import scalapb.e2e.no_lenses._

class NoGettersSpec extends AnyFlatSpec with Matchers {
  val noGetters = NoGetters(a = Some(NoGettersA(12)))

  "getters" should "not be available but lenses should function" in {
    noGetters.a mustBe defined
    assertDoesNotCompile("noGetters.getA")
    noGetters.update(_.a := NoGettersA(12))
  }

  val noGettersOrLenses = NoGettersOrLenses(a = Some(NoGettersOrLensesA(12)))

  it should "not exist at all if lenses do not exist either" in {
    noGettersOrLenses.a mustBe defined

    assertDoesNotCompile("noGettersOrLenses.update(_.a := NoGettersOrLensesA(11))")
    assertDoesNotCompile("noGettersOrLenses.getA")
  }

  val noLenses = NoLenses(a = Some(NoLensesA(12)), b = 12)

  it should "still exist even if lenses are disabled" in {
    noLenses.a mustBe defined
    noLenses.getA.a mustBe 12
    assertDoesNotCompile("noLenses.update(_.a := NoLensesA(11))")
  }
}
