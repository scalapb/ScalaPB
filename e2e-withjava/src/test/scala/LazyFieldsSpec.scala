import com.thesamet.proto.e2e.lazy_fields._
import com.thesamet.proto.e2e.LazyFields
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.{LazyDecoder, LazyField}
import scala.language.implicitConversions

class LazyFieldsSpec extends AnyFlatSpec with Matchers {
  "Lazy repeated fields" should "work correctly through java conversion" in {
    val original = LazyMessage(str = "hello", int = 42)
    
    val bytes = original.toByteArray
    val javaParsed = LazyFields.LazyMessage.parseFrom(bytes)
    val javaConverted = LazyMessage.toJavaProto(original)

    javaParsed.getStr() shouldBe "hello"
    javaConverted.getStr() shouldBe "hello"

    val scalaParsed = LazyMessage.parseFrom(javaParsed.toByteArray())
    val scalaConverted = LazyMessage.fromJavaProto(javaConverted)

    scalaParsed.str shouldBe "hello"
    scalaConverted.str shouldBe "hello"
  }
  
}
