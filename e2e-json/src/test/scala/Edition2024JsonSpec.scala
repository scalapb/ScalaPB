import e2e.json.edition2024.Json2024
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.json4s.JsonFormat

class Edition2024JsonSpec extends AnyFlatSpec with Matchers {
  "Edition 2024 JSON" should "honor presence and round trip open enums and DELIMITED messages" in {
    JsonFormat.toJsonString(Json2024()) must be("{}")
    JsonFormat.toJsonString(Json2024(explicitValue = Some(0))) must be("""{"explicitValue":0}""")
    val value = Json2024(
      kind = Some(Json2024.Kind.Unrecognized(123)),
      child = Some(Json2024(explicitValue = Some(7)))
    )
    JsonFormat.fromJsonString[Json2024](JsonFormat.toJsonString(value)) must be(value)
  }
}
