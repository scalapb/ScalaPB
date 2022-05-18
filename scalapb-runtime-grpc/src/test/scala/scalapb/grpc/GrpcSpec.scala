package scalapb.grpc

import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.Mockito.mock

import scala.util.{Failure, Success}
import munit.FunSuite

class GrpcSpec extends FunSuite {
  test("Complete observer should wrap an exception as a StatusException on failure") {
    val observer = mock(classOf[StreamObserver[_]])

    Grpc.completeObserver(observer)(Failure(new RuntimeException("Error!")))

    Mockito.verify(observer).onError(ArgumentMatchers.any(classOf[StatusException]))
  }

  test("Complete observer should call onError when onNext fails") {
    val observer = mock(classOf[StreamObserver[String]])
    Mockito
      .when(observer.onNext(ArgumentMatchers.anyString()))
      .thenThrow(new RuntimeException("Error!"))

    Grpc.completeObserver(observer)(Success("Success!"))

    Mockito.verify(observer).onError(ArgumentMatchers.any(classOf[StatusException]))
  }
}
