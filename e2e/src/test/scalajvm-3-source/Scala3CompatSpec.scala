import com.thesamet.proto.e2e.scala3.issue1576.Foo
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import scalapb.lenses.{MessageLens, ObjectLens}

class Scala3CompatSpec extends AnyFlatSpec with Matchers with OptionValues {
  "message lens" should "extend MessageLens and ObjectLens" in {
    classOf[MessageLens[?, ?]].isAssignableFrom(classOf[Foo.FooLens[?]]) must be(true)
    classOf[ObjectLens[?, ?]].isAssignableFrom(classOf[Foo.FooLens[?]]) must be(true)
  }
}
