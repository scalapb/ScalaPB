import com.thesamet.proto.e2e.maps.CustomMaps
import com.thesamet.proto.e2e.maps2.CustomMaps2
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class MapsJavaSpec
    extends AnyFlatSpec
    with Matchers {

  "custom map types" should "work in Java" in {
    import MapSpec.{c1,c2}
    CustomMaps.fromJavaProto(CustomMaps.toJavaProto(c1)) must be(c1)
    CustomMaps2.fromJavaProto(CustomMaps2.toJavaProto(c2)) must be(c2)
  }
}
