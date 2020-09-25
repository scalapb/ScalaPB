import com.thesamet.pb.{MyMap, MyVector}
import com.thesamet.proto.e2e.collection_types._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CollectionTypesJavaSpec extends AnyFlatSpec with Matchers {
  "custom collection" should "work" in {
    val c = CustomCollection(repeatedInt32 = MyVector(Vector(11, 24, 19)))
    CustomCollection.fromJavaProto(CustomCollection.toJavaProto(c)) must be(c)
  }

  "custom maps" should "convertible to and from Java" in {
    val m = CollectionTypesMap(mymapInt32Bool = MyMap(Map(3 -> true, 4 -> false)))
    CollectionTypesMap.fromJavaProto(CollectionTypesMap.toJavaProto(m)) must be(m)
  }
}
