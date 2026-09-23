import com.google.protobuf.ByteString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.json4s.JsonFormat
import e2e.json.lazy_record._

class LazyJsonSpec extends AnyFlatSpec with Matchers {

  val address: Address = Address(street = "Baker St 221B", city = "London", zipCode = 12345)

  val full: LazyRecord = LazyRecord(
    name    = "Alice",
    count   = 42,
    enabled = true,
    data    = ByteString.copyFromUtf8("raw"),
    color   = Color.COLOR_RED,
    address = Some(address),
    tags    = Seq("scala", "protobuf", "json"),
    labels  = Map("env" -> "prod", "team" -> "backend"),
    scores  = Map("math" -> 100, "physics" -> 95)
  )

  "JsonFormat" should "round-trip a full LazyRecord" in {
    val json   = JsonFormat.toJsonString(full)
    full.address.get.getFieldByNumber(1)
    val parsed = JsonFormat.fromJsonString[LazyRecord](json)
    parsed shouldBe full
  }

  it should "round-trip an empty LazyRecord" in {
    val empty  = LazyRecord()
    val json   = JsonFormat.toJsonString(empty)
    val parsed = JsonFormat.fromJsonString[LazyRecord](json)
    parsed shouldBe empty
  }

  it should "round-trip a nested Address" in {
    val json   = JsonFormat.toJsonString(address)
    val parsed = JsonFormat.fromJsonString[Address](json)
    parsed shouldBe address
  }

  it should "preserve lazy string field value after JSON round-trip" in {
    val json   = JsonFormat.toJsonString(full)
    val parsed = JsonFormat.fromJsonString[LazyRecord](json)
    parsed.name.value shouldBe "Alice"
  }

  it should "preserve repeated lazy string fields" in {
    val json   = JsonFormat.toJsonString(full)
    val parsed = JsonFormat.fromJsonString[LazyRecord](json)
    parsed.tags.map(_.value) shouldBe Seq("scala", "protobuf", "json")
  }

  it should "preserve map with lazy string values" in {
    val json   = JsonFormat.toJsonString(full)
    val parsed = JsonFormat.fromJsonString[LazyRecord](json)
    parsed.labels.view.mapValues(_.value).toMap shouldBe Map("env" -> "prod", "team" -> "backend")
  }

  it should "preserve map with int32 values" in {
    val json   = JsonFormat.toJsonString(full)
    val parsed = JsonFormat.fromJsonString[LazyRecord](json)
    parsed.scores shouldBe Map("math" -> 100, "physics" -> 95)
  }
}
