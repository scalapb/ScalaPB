import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import com.thesamet.proto.e2e.issue1664.NestedExampleEvent

class Issue1664Spec extends AnyFunSpec with Matchers {

  it("issue1664:public_constructor_parameters") {
    // Without public_constructor_parameters, this is inaccessible
    NestedExampleEvent._typemapper_action.toBase("Allow") mustBe NestedExampleEvent.Action.Allow
  }

}
