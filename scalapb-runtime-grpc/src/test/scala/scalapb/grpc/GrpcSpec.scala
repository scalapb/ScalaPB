package scalapb.grpc

import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

import scala.util.{Failure, Success}

class GrpcSpec extends FlatSpec with MockitoSugar {

  "Complete observer" should "wrap an exception as a StatusException on failure" in {
    val observer = mock[StreamObserver[_]]

    Grpc.completeObserver(observer)(Failure(new RuntimeException("Error!")))

    Mockito.verify(observer).onError(ArgumentMatchers.any(classOf[StatusException]))
  }

  "Complete observer" should "call onError when onNext fails" in {
    val observer = mock[StreamObserver[String]]
    Mockito
      .when(observer.onNext(ArgumentMatchers.anyString()))
      .thenThrow(new RuntimeException("Error!"))

    Grpc.completeObserver(observer)(Success("Success!"))

    Mockito.verify(observer).onError(ArgumentMatchers.any(classOf[StatusException]))
  }

}
