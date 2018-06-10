import com.trueaccord.proto.e2e.one_of.OneofTest
import com.trueaccord.proto.e2e.one_of.OneofTest.SubMessage
import org.scalatest.{FlatSpec, MustMatchers}

class OneofTypeClasses extends FlatSpec with MustMatchers {
  trait TC[T] {
    def apply(a: T): Int
  }
  object TC {
    implicit val int: TC[Int] = new TC[Int] {
      def apply(a: Int) = a
    }
    implicit val str: TC[String] = new TC[String] {
      def apply(a: String) = a.length
    }
    implicit val sub: TC[SubMessage] = new TC[SubMessage] {
      def apply(a: SubMessage) = a.getSubField
    }
  }

  def f(et: OneofTest.MyOneOf)(implicit tc: TC[et.ValueType]) = tc(et.value)

  "f" should "work correctly" in {
    f(OneofTest.MyOneOf.TempField(5)) must be(5)
    f(OneofTest.MyOneOf.OtherField("yo")) must be(2)
    f(OneofTest.MyOneOf.Sub(SubMessage().withSubField(17))) must be(17)
  }
}
