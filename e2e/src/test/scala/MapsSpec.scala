import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.google.protobuf.CodedInputStream
import com.trueaccord.proto.e2e.maps.MapsTest
import com.trueaccord.proto.e2e.maps2.MapsTest2
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest.Nested
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatest.prop._

class MapsSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers {

  val nestedGen =
    Arbitrary.arbitrary[Option[Int]].map(s => Nested(nestedField = s))

  val boolColorPair = for {
    b <- Gen.oneOf(true, false)
    c <- Gen.oneOf(MapsTest.Color.BLUE, MapsTest.Color.GREEN, MapsTest.Color.NOCOLOR)
  } yield (b, c)

  val boolColorPair2 = for {
    b <- Gen.oneOf(true, false)
    c <- Gen.oneOf(MapsTest2.Color.BLUE, MapsTest2.Color.GREEN, MapsTest2.Color.NOCOLOR)
  } yield (b, c)

  val mapsGen = for {
    strToStr <- Gen.listOf(Arbitrary.arbitrary[(String, String)]).map(_.toMap)
    strToInt <- Gen.listOf(Arbitrary.arbitrary[(String, Int)]).map(_.toMap)
    intToStr <- Gen.listOf(Arbitrary.arbitrary[(Int, String)]).map(_.toMap)
    boolToColor <- Gen.listOf(boolColorPair).map(_.toMap)
  } yield MapsTest(strToStr = strToStr, strToInt32 = strToInt, int32ToString = intToStr,
      boolToColor = boolToColor)

  val mapsGen2 = for {
    strToStr <- Gen.listOf(Arbitrary.arbitrary[(String, String)]).map(_.toMap)
    strToInt <- Gen.listOf(Arbitrary.arbitrary[(String, Int)]).map(_.toMap)
    intToStr <- Gen.listOf(Arbitrary.arbitrary[(Int, String)]).map(_.toMap)
    boolToColor <- Gen.listOf(boolColorPair2).map(_.toMap)
  } yield MapsTest2(strToStr = strToStr, strToInt32 = strToInt, int32ToString = intToStr,
      boolToColor = boolToColor)

  def mergeMaps(x: MapsTest, y: MapsTest) = MapsTest(
    strToStr = x.strToStr ++ y.strToStr,
    strToInt32 = x.strToInt32 ++ y.strToInt32,
    int32ToString = x.int32ToString ++ y.int32ToString,
    boolToColor = x.boolToColor ++ y.boolToColor)

  def mergeMaps2(x: MapsTest2, y: MapsTest2) = MapsTest2(
    strToStr = x.strToStr ++ y.strToStr,
    strToInt32 = x.strToInt32 ++ y.strToInt32,
    int32ToString = x.int32ToString ++ y.int32ToString,
    boolToColor = x.boolToColor ++ y.boolToColor)

  "clear" should "empty the map" in {
    forAll(mapsGen) {
      map =>
        map.clearStrToStr must be(map.copy(strToStr = Map.empty))
        map.clearStrToInt32 must be(map.copy(strToInt32 = Map.empty))
        map.clearInt32ToString must be(map.copy(int32ToString = Map.empty))
        map.clearBoolToColor must be(map.copy(boolToColor = Map.empty))
    }
  }

  "addAll" should "merge the maps" in {
    forAll(mapsGen, mapsGen) {
      (map, other) =>
        map
          .addAllStrToStr(other.strToStr)
          .addAllStrToInt32(other.strToInt32)
          .addAllInt32ToString(other.int32ToString)
          .addAllBoolToColor(other.boolToColor) must be(mergeMaps(map, other))
    }
  }

  "with" should "set the entire map" in {
    forAll(mapsGen, mapsGen) {
      (map, other) =>
        map
          .withStrToStr(other.strToStr)
          .withStrToInt32(other.strToInt32)
          .withInt32ToString(other.int32ToString)
          .withBoolToColor(other.boolToColor) must be(other)
    }
  }

  "updates" should "allow adding a key by assignment" in {
    forAll(mapsGen) {
      map =>
        map.update(_.int32ToString(-17) := "foo").int32ToString must be(map.int32ToString.updated(-17, "foo"))
    }
  }

  "updates" should "allow adding a key-value" in {
    forAll(mapsGen) {
      map =>
        map.update(_.int32ToString :+= (12 -> "foo")).int32ToString must be(map.int32ToString.updated(12, "foo"))
    }
  }

  "parse" should "be the inverse of toByteArray" in {
    forAll(mapsGen) {
      map =>
        MapsTest.parseFrom(map.toByteArray) must be(map)
    }
  }

  "parse" should "be the inverse of toByteArray for proto2" in {
    forAll(mapsGen2) {
      map =>
        MapsTest2.parseFrom(map.toByteArray) must be(map)
    }
  }

  "concatenate message" should "result in merged maps" in {
    forAll(mapsGen, mapsGen) {
      (map1, map2) =>
        MapsTest.parseFrom(map1.toByteArray ++ map2.toByteArray) must be(
          mergeMaps(map1, map2))
    }
  }

  "concatenate message" should "result in merged maps for proto2" in {
    forAll(mapsGen2, mapsGen2) {
      (map1, map2) =>
        MapsTest2.parseFrom(map1.toByteArray ++ map2.toByteArray) must be(
          mergeMaps2(map1, map2))
    }
  }
}
