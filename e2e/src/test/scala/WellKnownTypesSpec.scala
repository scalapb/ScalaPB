import com.trueaccord.proto.well_known._
import org.scalatest._
import org.scalatest.prop._

class WellKnownTypesSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers {
  "new TestWrapper" should "have Nones" in {
    val t = TestWrappers()
    t.myDouble must be (None)
    t.myFloat must be (None)
    t.myInt32 must be (None)
    t.myInt64 must be (None)
    t.myUint32 must be (None)
    t.myUint64 must be (None)
    t.myBool must be (None)
    t.myString must be (None)
    t.myBytes must be (None)
  }

  "TestWrapper" should "be updatable with primitives" in {
    // We just test that it compiles and we don't go through the wrapper.
    val o = TestWrappers().update(
      _.myDouble := 34.1,
      _.myFloat := 14.5f,
      _.myInt32 := 35,
      _.myInt64 := 35,
      _.myUint32 := 35,
      _.myUint64 := 17,
      _.myBool := true,
      _.myString := "foo",
      _.myBytes := com.google.protobuf.ByteString.copyFromUtf8("foo")
    )
  }
}
