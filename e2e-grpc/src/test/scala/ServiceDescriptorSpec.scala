import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import com.thesamet.proto.e2e.service.Service1Grpc.Service1
import com.thesamet.proto.e2e.comments.CommentedServiceGrpc.CommentedService

class ScalaDescriptorSpec extends AnyFlatSpec with Matchers with LoneElement with OptionValues {
  "scalaDescriptor" must "contain methods" in {
    Service1.scalaDescriptor.fullName must be("com.thesamet.proto.e2e.Service1")
    val method = Service1.scalaDescriptor.methods.find(_.name == "SealedUnary").get
    method.fullName must be("com.thesamet.proto.e2e.Service1.SealedUnary")
  }

  "commentedService" must "contain descriptor comments" in {
    val serviceLocation = CommentedService.scalaDescriptor.location.get
    val methodLocation  = CommentedService.scalaDescriptor.methods(0).location.get
    serviceLocation.getLeadingComments must be(" a commented service\n")

    methodLocation.getLeadingComments must be(" a commented RPC\n")
  }
}
