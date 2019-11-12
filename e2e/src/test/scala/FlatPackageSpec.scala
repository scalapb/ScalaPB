import com.thesamet.pb.flat.{FlatTest, Priority}
import com.thesamet.proto.e2e.one_of._
import org.scalatest._

class elatPackageSpec extends FlatSpec with MustMatchers {

  "FlatTest" should "serialize and parse" in {
    val ft = FlatTest(
      b = Some(4),
      priority = Some(Priority.MEDIUM),
      oneOfMsg = Some(OneofTest(a = Some(4)))
    )
    FlatTest.parseFrom(ft.toByteArray) must be(ft)
  }
}
