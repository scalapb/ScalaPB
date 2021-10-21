import com.thesamet.proto.e2e.type_level.UsesMessageLevelType
import com.thesamet.pb.{Point2D, Dir2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class TypeLevelSpec extends AnyFlatSpec with Matchers {
  "message level type" should "generate correct types" in {
    val t = UsesMessageLevelType().update(
      _.optPoint := Point2D(3, 17),
      _.repPoint := Seq(Point2D(15, 19), Point2D(24, 35)),
      _.mapPoint := Map(3 -> Point2D(3, 17), 25 -> Point2D(14, 19)),
      _.optDir   := Dir2D(2),
      _.repDir   := Seq(Dir2D(2), Dir2D(1)),
      _.mapDir   := Map(4 -> Dir2D(1))
    )
    UsesMessageLevelType.parseFrom(t.toByteArray) must be(t)
  }

}
