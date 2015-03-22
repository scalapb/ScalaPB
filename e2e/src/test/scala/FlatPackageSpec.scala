import com.trueaccord.proto.e2e.one_of._
import com.trueaccord.pb.flat.Priority
import com.trueaccord.pb.flat.FlatTest
import org.scalatest._

class elatPackageSpec extends FlatSpec with MustMatchers {

  "FlatTest" should "serialize and parse" in {
    val ft = FlatTest(b=Some(4), priority=Some(Priority.MEDIUM),
                      oneOfMsg=Some(OneofTest(a=Some(4))))
    FlatTest.parseFrom(ft.toByteArray) must be(ft)
  }
}
