import com.thesamet.proto.e2e.service.{Res5, Service1Grpc}
import com.thesamet.proto.e2e.type_level.XYMessage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{LoneElement, OptionValues}
import scalapb.descriptors.ScalaType

class MethodDescriptorSpec extends AnyFlatSpec with Matchers with LoneElement with OptionValues {

  "scala descriptor" must "expose correct input and output message descriptors" in {
    val unaryMethod = Service1Grpc.Service1.scalaDescriptor.methods.find(_.name == "CustomUnary").get
    val inputDescriptor = XYMessage.scalaDescriptor
    val outputDescriptor = Res5.scalaDescriptor

    unaryMethod.inputType mustBe ScalaType.Message(inputDescriptor)
    unaryMethod.outputType mustBe ScalaType.Message(outputDescriptor)
  }
}
