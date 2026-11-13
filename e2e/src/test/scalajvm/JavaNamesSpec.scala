import com.thesamet.proto.e2e.`match`.`object`
import com.thesamet.proto.e2e.`match`.java_names.{Container, JavaNamesProto}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class JavaNamesSpec extends AnyFlatSpec with Matchers {
  "Java conversions" should "escape package and outer class names in nested type references" in {
    val value = Container(Some(Container.Nested("hello")), Container.Kind.ONE)
    val javaValue: `object`.Container = Container.toJavaProto(value)
    javaValue.getNested.getValue must be("hello")
    javaValue.getKind must be(`object`.Container.Kind.ONE)
    Container.fromJavaProto(javaValue) must be(value)
    Container.javaDescriptor must be(`object`.Container.getDescriptor)
    JavaNamesProto.javaDescriptor must be(`object`.getDescriptor)
  }
}
