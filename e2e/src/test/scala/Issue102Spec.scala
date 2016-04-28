import com.trueaccord.proto.e2e.issue102.{ProtoPacked, ProtoUnpacked}
import org.scalatest._

class Issue102Spec extends FunSpec {

  it("issue102"){
    val a = Seq(1)
    assert(ProtoUnpacked.parseFrom(ProtoPacked(a).toByteArray).a == a)
    assert(ProtoPacked.parseFrom(ProtoUnpacked(a).toByteArray).a == a)

    val extremeCase = ProtoPacked(Seq(0, 1)).toByteArray ++
      ProtoUnpacked(Seq(2, 3, 4)).toByteArray ++
      ProtoPacked(Seq(5, 6, 7)).toByteArray ++
      ProtoUnpacked(Seq(8)).toByteArray

    val expected = 0 until 9
    assert(ProtoUnpacked.parseFrom(extremeCase).a == expected)
    assert(ProtoPacked.parseFrom(extremeCase).a == expected)
  }

}
