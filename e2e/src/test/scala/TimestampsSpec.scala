import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import com.google.protobuf.timestamp.Timestamp
import com.google.protobuf.duration.Duration

class TimestampsSpec extends AnyFlatSpec with Matchers {
    "Timestamps and Duration" should "have implicit ordering" in {
        implicitly[Ordering[Timestamp]]
        implicitly[Ordering[Duration]]
    }
}
