import foo.issue320.{Foo, Issue320Proto}
import org.scalatest.{Matchers, WordSpec}

class ScalapbExtensionSpec extends WordSpec with Matchers {
  "Foo.toByteArray" must {
    "not blow up" in {
      Foo().toByteArray should have length 0
      val someMessage = Some("baz")
      Foo().withExtension(Issue320Proto.bar)(someMessage).toByteArray should have length 7
    }
  }
}
