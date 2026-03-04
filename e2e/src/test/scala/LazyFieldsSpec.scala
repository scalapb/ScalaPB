import java.util.concurrent.atomic.AtomicInteger
import com.google.protobuf.ByteString
import com.thesamet.proto.e2e.lazy_fields._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalapb.{LazyDecoder, LazyField}
import scala.language.implicitConversions

class LazyFieldsSpec extends AnyFlatSpec with Matchers {
  "NotLazyMessage" should "have normal string fields" in {
    val notLazyMessage = NotLazyMessage(
      notLazyString = "not a lazy string"
    )
    notLazyMessage.notLazyString shouldBe a[String]
    notLazyMessage.notLazyString shouldBe "not a lazy string"
  }

  "LazyField" should "be decoded only once" in {
    val callCount = new AtomicInteger(0)
    implicit val countingDecoder: LazyDecoder[String] = new LazyDecoder[String] {
      def decode(bytes: ByteString): String = {
        callCount.incrementAndGet()
        bytes.toStringUtf8()
      }
    }

    // We test LazyField directly, since parseFrom does not pick up local implicits.
    // LazyField.apply however, does.
    val lazyField = LazyField(ByteString.copyFromUtf8("a lazy string"))

    // Field is not evaluated yet.
    lazyField.toByteString shouldBe a[ByteString]
    callCount.get() shouldBe 0

    // First access evaluates the field.
    lazyField.value shouldBe "a lazy string"
    callCount.get() shouldBe 1

    // Second access does not re-evaluate.
    lazyField.value shouldBe "a lazy string"
    callCount.get() shouldBe 1
  }

  "Lazy repeated fields" should "work correctly through serialization" in {
    val originalItems = Seq("hello", "world", "again")
    val lazyItems = originalItems.map(s => LazyField(ByteString.copyFromUtf8(s)))
    val original = LazyRepeated(items = lazyItems)
    
    val bytes = original.toByteArray
    val parsed = LazyRepeated.parseFrom(bytes)

    parsed.items.map(_.value) should contain theSameElementsAs originalItems
  }

  "Lazy nested messages" should "work correctly through serialization" in {
    val nested = LazyWithRecursion(data = LazyField(ByteString.copyFromUtf8("inside")))
    val original = LazyWithRecursion(data = LazyField(ByteString.copyFromUtf8("outside")), nested = Some(nested))

    val bytes = original.toByteArray
    val parsed = LazyWithRecursion.parseFrom(bytes)

    parsed.data shouldBe a [LazyField[_]]
    parsed.nested.get.data shouldBe a [LazyField[_]]

    parsed.data.value shouldBe "outside"
    parsed.nested.get.data.value shouldBe "inside"
  }

  "LazyField" should "implicitly convert to and from its underlying type" in {
    val original = LazyWithRecursion(data = "a lazy string")
    original.data shouldBe a [LazyField[_]]

    original.data shouldBe "a lazy string"
    original.data.toUpperCase shouldBe "A LAZY STRING"
  }

  "Lenses" should "work with LazyField" in {
    val original = LazyWithRecursion(data = "a lazy string", nested = Some(LazyWithRecursion(data = "nested string", nested = Some(LazyWithRecursion(data = "another one nested string")))))

    val updated = original.update(_.nested.nested.data := "updated string")

    updated.nested.get.nested.get.data shouldBe "updated string"

  }
}
