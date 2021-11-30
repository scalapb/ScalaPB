import com.thesamet.proto.e2e.maps.{CustomMaps, MapsTest}
import com.thesamet.proto.e2e.maps2.{MapsTest2, CustomMaps2}
import com.thesamet.proto.e2e.repeatables.RepeatablesTest.Nested
import com.thesamet.pb.{PersonId, Years}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatestplus.scalacheck._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class MapsSpec
    extends AnyFlatSpec
    with ScalaCheckDrivenPropertyChecks
    with Matchers
    with OptionValues {

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
    strToStr    <- Gen.listOf(Arbitrary.arbitrary[(String, String)]).map(_.toMap)
    strToInt    <- Gen.listOf(Arbitrary.arbitrary[(String, Int)]).map(_.toMap)
    intToStr    <- Gen.listOf(Arbitrary.arbitrary[(Int, String)]).map(_.toMap)
    boolToColor <- Gen.listOf(boolColorPair).map(_.toMap)
  } yield MapsTest(
    strToStr = strToStr,
    stringToInt32 = strToInt,
    int32ToString = intToStr,
    boolToColor = boolToColor
  )

  val mapsGen2 = for {
    strToStr    <- Gen.listOf(Arbitrary.arbitrary[(String, String)]).map(_.toMap)
    strToInt    <- Gen.listOf(Arbitrary.arbitrary[(String, Int)]).map(_.toMap)
    intToStr    <- Gen.listOf(Arbitrary.arbitrary[(Int, String)]).map(_.toMap)
    boolToColor <- Gen.listOf(boolColorPair2).map(_.toMap)
  } yield MapsTest2(
    strToStr = strToStr,
    stringToInt32 = strToInt,
    int32ToString = intToStr,
    boolToColor = boolToColor
  )

  def mergeMaps(x: MapsTest, y: MapsTest) =
    MapsTest(
      strToStr = x.strToStr ++ y.strToStr,
      stringToInt32 = x.stringToInt32 ++ y.stringToInt32,
      int32ToString = x.int32ToString ++ y.int32ToString,
      boolToColor = x.boolToColor ++ y.boolToColor
    )

  def mergeMaps2(x: MapsTest2, y: MapsTest2) =
    MapsTest2(
      strToStr = x.strToStr ++ y.strToStr,
      stringToInt32 = x.stringToInt32 ++ y.stringToInt32,
      int32ToString = x.int32ToString ++ y.int32ToString,
      boolToColor = x.boolToColor ++ y.boolToColor
    )

  "descriptor.isMapEntry" should "be true" in {
    MapsTest.scalaDescriptor.findFieldByName("str_to_str").value.isMapField must be(true)
    MapsTest.scalaDescriptor.findFieldByName("string_to_int32").value.isMapField must be(true)
    MapsTest.scalaDescriptor.findFieldByName("int32_to_string").value.isMapField must be(true)
    MapsTest.scalaDescriptor.findFieldByName("not_a_map").value.isMapField must be(false)
    MapsTest.scalaDescriptor.findFieldByName("repeated_not_a_map").value.isMapField must be(false)
    MapsTest2.scalaDescriptor.findFieldByName("str_to_str").value.isMapField must be(true)
    MapsTest2.scalaDescriptor.findFieldByName("string_to_int32").value.isMapField must be(true)
    MapsTest2.scalaDescriptor.findFieldByName("int32_to_string").value.isMapField must be(true)
    MapsTest2.scalaDescriptor.findFieldByName("not_a_map").value.isMapField must be(false)
    MapsTest2.scalaDescriptor.findFieldByName("repeated_not_a_map").value.isMapField must be(false)
  }

  "clear" should "empty the map" in {
    forAll(mapsGen) { map =>
      map.clearStrToStr must be(map.copy(strToStr = Map.empty))
      map.clearStringToInt32 must be(map.copy(stringToInt32 = Map.empty))
      map.clearInt32ToString must be(map.copy(int32ToString = Map.empty))
      map.clearBoolToColor must be(map.copy(boolToColor = Map.empty))
    }
  }

  "addAll" should "merge the maps" in {
    forAll(mapsGen, mapsGen) { (map, other) =>
      map
        .addAllStrToStr(other.strToStr)
        .addAllStringToInt32(other.stringToInt32)
        .addAllInt32ToString(other.int32ToString)
        .addAllBoolToColor(other.boolToColor) must be(mergeMaps(map, other))
    }
  }

  "with" should "set the entire map" in {
    forAll(mapsGen, mapsGen) { (map, other) =>
      map
        .withStrToStr(other.strToStr)
        .withStringToInt32(other.stringToInt32)
        .withInt32ToString(other.int32ToString)
        .withBoolToColor(other.boolToColor) must be(other)
    }
  }

  "updates" should "allow adding a key by assignment" in {
    forAll(mapsGen) { map =>
      map.update(_.int32ToString(-17) := "foo").int32ToString must be(
        map.int32ToString.updated(-17, "foo")
      )
    }
  }

  "updates" should "allow adding a key-value" in {
    forAll(mapsGen) { map =>
      map.update(_.int32ToString :+= (12 -> "foo")).int32ToString must be(
        map.int32ToString.updated(12, "foo")
      )
    }
  }

  "parse" should "be the inverse of toByteArray" in {
    forAll(mapsGen) { map =>
      MapsTest.parseFrom(map.toByteArray) must be(map)
    }
  }

  "parse" should "be the inverse of toByteArray for proto2" in {
    forAll(mapsGen2) { map =>
      MapsTest2.parseFrom(map.toByteArray) must be(map)
    }
  }

  "concatenate message" should "result in merged maps" in {
    forAll(mapsGen, mapsGen) { (map1, map2) =>
      MapsTest.parseFrom(map1.toByteArray ++ map2.toByteArray) must be(mergeMaps(map1, map2))
    }
  }

  "concatenate message" should "result in merged maps for proto2" in {
    forAll(mapsGen2, mapsGen2) { (map1, map2) =>
      MapsTest2.parseFrom(map1.toByteArray ++ map2.toByteArray) must be(mergeMaps2(map1, map2))
    }
  }

  "custom map types" should "provide custom key and value types" in {
    import MapSpec.{c1, c2}
    CustomMaps.parseFrom(c1.toByteArray) must be(c1)
    CustomMaps.fromAscii(c1.toProtoString) must be(c1)

    CustomMaps2.parseFrom(c2.toByteArray) must be(c2)
    CustomMaps2.fromAscii(c2.toProtoString) must be(c2)
  }
}

object MapSpec {
  val c1 = CustomMaps(
    stringToYear = Map("314" -> Years(314)),
    personToInt = Map(PersonId("315") -> 314),
    personToYear = Map(PersonId("275") -> Years(188))
  )

  val c2 = CustomMaps2(
    stringToYear = Map("314" -> Years(314)),
    personToInt = Map(PersonId("315") -> 314),
    personToYear = Map(PersonId("275") -> Years(188))
  )
}
