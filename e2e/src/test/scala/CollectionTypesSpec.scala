import com.thesamet.pb.{MyMap, MyVector}
import com.thesamet.proto.e2e.collection_types._

import scala.collection.mutable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CollectionTypesSpec extends AnyFlatSpec with Matchers {
  "lenses" should "compile" in {
    val cis = CollectionTypesMessage().update(_.repeatedInt32 :++= Seq(11, 9))
    val cv  = CollectionTypesVector().update(_.repeatedInt32 :++= Seq(11, 9))
    val cl  = CollectionTypesList().update(_.repeatedInt32 :++= Seq(11, 9))
    val cs  = CollectionTypesSet().update(_.repeatedInt32 :++= Seq(11, 9))
    cis.repeatedInt32 must be(a[collection.immutable.Seq[?]])
    cv.repeatedInt32 must be(a[Vector[?]])
    cl.repeatedInt32 must be(a[List[?]])
    cs.repeatedInt32 must be(a[Set[?]])
  }

  "custom collection" should "work" in {
    val c = CustomCollection(repeatedInt32 = MyVector(Vector(11, 24, 19)))
    CustomCollection.parseFrom(c.toByteArray) must be(c)
    CustomCollection.fromAscii(c.toProtoString) must be(c)
  }

  // See https://github.com/scalapb/ScalaPB/issues/274
  "packed sets serialization" should "work" in {
    val m = CollectionTypesPackedSet(repeatedUint32 = Set(1, 2, 3, 4, 5))
    CollectionTypesPackedSet.parseFrom(m.toByteArray) must be(m)
  }

  "custom maps" should "have expected types" in {
    val m = CollectionTypesMap()
    m.mapInt32Bool must be(a[mutable.Map[?, ?]])
    m.mapInt32Enum must be(a[mutable.Map[?, ?]])
    m.mymapInt32Bool must be(a[MyMap[?, ?]])
  }

  "custom maps" should "serialize and deserialize" in {
    val m = CollectionTypesMap(mymapInt32Bool = MyMap(Map(3 -> true, 4 -> false)))
    CollectionTypesMap.parseFrom(m.toByteArray) must be(m)
    m.mapInt32Bool += (3 -> true)
  }
}
