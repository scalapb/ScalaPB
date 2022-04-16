import com.thesamet.proto.e2e.issue1354.issue1354._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class Issue1354Spec extends AnyFunSpec with Matchers {

  it("issue1354") {
    val hr = HelloRequest(name=Some(Name("name")), nickname = Name("name"))
    // nickname is a proto3 optional, but is much to have no_box applied to
    // it.
    hr.nickname mustBe(a[Name])
  }

}
