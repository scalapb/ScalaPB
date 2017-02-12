import com.trueaccord.proto.e2e.collection_types.CollectionTypesMessage
import org.scalatest._

class CollectionTypesSpec extends FlatSpec with MustMatchers {
  "lenses" should "compile" in {
    CollectionTypesMessage().update(_.repeatedInt32 ++= Seq())
  }
}
