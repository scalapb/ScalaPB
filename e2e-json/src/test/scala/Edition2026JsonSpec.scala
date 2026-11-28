import e2e.json.edition2026.Json2026
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.json4s.JsonFormat

class Edition2026JsonSpec extends AnyFlatSpec with Matchers {
  "Edition 2026 JSON" should "honor presence and round trip open enums and DELIMITED messages" in {
    JsonFormat.toJsonString(Json2026()) must be("{}")
    JsonFormat.toJsonString(Json2026(explicitValue = Some(0))) must be("""{"explicitValue":0}""")
    val value = Json2026(
      kind = Some(Json2026.Kind.Unrecognized(123)),
      child = Some(Json2026(explicitValue = Some(7)))
    )
    JsonFormat.fromJsonString[Json2026](JsonFormat.toJsonString(value)) must be(value)
  }
}
