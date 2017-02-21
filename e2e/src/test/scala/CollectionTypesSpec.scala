import com.trueaccord.pb.MyVector
import com.trueaccord.proto.e2e.collection_types._
import org.scalatest._

class CollectionTypesSpec extends FlatSpec with MustMatchers {
  "lenses" should "compile" in {
    val cis = CollectionTypesMessage().update(_.repeatedInt32 :++= Seq(11, 9))
    val cv = CollectionTypesVector().update(_.repeatedInt32 :++= Seq(11, 9))
    val cl = CollectionTypesList().update(_.repeatedInt32 :++= Seq(11, 9))
    val cs = CollectionTypesSet().update(_.repeatedInt32 :++= Seq(11, 9))
    cis.repeatedInt32 must be (a[collection.immutable.Seq[_]])
    cv.repeatedInt32 must be (a[Vector[_]])
    cl.repeatedInt32 must be (a[List[_]])
    cs.repeatedInt32 must be (a[Set[_]])
  }

  "custom collection" should "work" in {
    val c = CustomCollection(repeatedInt32 = MyVector(Vector(11, 24, 19)))
    CustomCollection.parseFrom(c.toByteArray) must be(c)
    CustomCollection.fromAscii(c.toString) must be(c)
    CustomCollection.fromJavaProto(CustomCollection.toJavaProto(c)) must be(c)
  }
}
