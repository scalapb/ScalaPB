import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.thesamet.proto.e2e.oneof_import_conflict.{OneofImportConflictTest, MultipleConflictTest}

class OneofImportConflictSpec extends AnyFlatSpec with Matchers {

  "oneof field conflicting with imported package name" should "generate correct code with _root_ prefixes" in {
    // Test basic oneof conflict - the fact this compiles means _root_ prefixes work
    val coord = _root_.location.v1.location.Coordinate(latitude = 1.0, longitude = 2.0)

    val message = OneofImportConflictTest(
      location = OneofImportConflictTest.Location.LocationCoordinate(coord)
    )

    // Test functionality works correctly
    message.getLocationCoordinate shouldBe coord
    message.location.isLocationCoordinate shouldBe true
    message.location.locationCoordinate shouldBe Some(coord)

    // Test serialization round-trip
    val bytes  = message.toByteArray
    val parsed = OneofImportConflictTest.parseFrom(bytes)
    parsed shouldBe message
  }

  "multiple field types with import conflicts" should "all use _root_ prefixes consistently" in {
    val coord1 = _root_.location.v1.location.Coordinate(latitude = 3.0, longitude = 4.0)
    val coord2 = _root_.location.v1.location.Coordinate(latitude = 5.0, longitude = 6.0)

    val message = MultipleConflictTest(
      location = MultipleConflictTest.Location.Coord(coord1),
      regularCoord = Some(coord2)
    )

    // Test both oneof and regular fields work correctly
    message.location.isCoord shouldBe true
    message.location.coord shouldBe Some(coord1)
    message.regularCoord shouldBe Some(coord2)

    // Test serialization round-trip
    val bytes  = message.toByteArray
    val parsed = MultipleConflictTest.parseFrom(bytes)
    parsed shouldBe message
  }
}
