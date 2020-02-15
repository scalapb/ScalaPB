import foo.issue320.{Foo, Issue320Proto}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ScalapbExtensionSpec extends AnyWordSpec with Matchers {
  "Foo.toByteArray" must {
    "not blow up" in {
      Foo().toByteArray should have length 0
      val someMessage = Some("baz")
      Foo().withExtension(Issue320Proto.bar)(someMessage).toByteArray should have length 7
    }
  }
}
