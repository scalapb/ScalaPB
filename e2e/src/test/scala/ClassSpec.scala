import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import protobuf_unittest.unittest.TestAllTypes
import protobuf_unittest.unittest.TestRequired
import com.thesamet.proto.e2e.one_of.OneofTest

class ClassSpec extends AnyFlatSpec with Matchers {
  val message = TestAllTypes(
    optionalInt32 = Some(17),
    optionalString = Some("boo")
  )

  val req = TestRequired(a = 17, b = 35, c = 19)

  val oot = OneofTest(
    a = Some(17),
    myOneOf = OneofTest.MyOneOf.Sub(OneofTest.SubMessage(name = Some("sub"))),
    xyzs = Seq(OneofTest.XYZ.X, OneofTest.XYZ.Y)
  )

  "hashCode" should "be sensitive to small adjustments" in {
    message.hashCode() must equal(message.hashCode())
    message.copy(optionalInt32 = None).hashCode() must not equal (message.hashCode())
    req.copy(a = req.a + 1).hashCode() must not equal (req.hashCode())
    req.copy(a = req.a + 1, b = req.b - 1).hashCode() must not equal (req.hashCode())
  }

  "toString" should "work" in {
    oot.toString() must be(
      "OneofTest(Some(17), Sub(SubMessage(None, Some(sub), UnknownFieldSet(Map()))), List(X, Y), UnknownFieldSet(Map()))"
    )
  }
}
