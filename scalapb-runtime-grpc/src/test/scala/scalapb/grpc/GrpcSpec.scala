package scalapb.grpc

import io.grpc.{Metadata, Status, StatusException}
import io.grpc.stub.{ServerCallStreamObserver, StreamObserver}
import org.mockito.{ArgumentCaptor, ArgumentMatchers, Mockito}
import org.mockito.Mockito.mock

import scala.util.{Failure, Success}
import munit.FunSuite

class GrpcSpec extends FunSuite {
  test("Complete observer should wrap an exception as a StatusException on failure") {
    val observer = mock(classOf[StreamObserver[?]])

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

  for (serverObserver <- Seq(false, true)) {
    test(s"Response observer normalizes failures (server observer: $serverObserver)") {
      val observer =
        if (serverObserver) mock(classOf[ServerCallStreamObserver[String]])
        else mock(classOf[StreamObserver[String]])
      val wrapped = Grpc.serverResponseObserver(observer)
      val error   = new NullPointerException("boom")
      wrapped.onError(error)
      val received = ArgumentCaptor.forClass(classOf[Throwable])
      Mockito.verify(observer).onError(received.capture())
      val status = Status.fromThrowable(received.getValue)
      assertEquals(status.getCode, Status.Code.INTERNAL)
      assertEquals(status.getDescription, "boom")
      assert(status.getCause eq error)
    }

    test(s"Response observer preserves explicit statuses (server observer: $serverObserver)") {
      val observer =
        if (serverObserver) mock(classOf[ServerCallStreamObserver[String]])
        else mock(classOf[StreamObserver[String]])
      val wrapped  = Grpc.serverResponseObserver(observer)
      val trailers = new Metadata()
      val status   = Status.PERMISSION_DENIED.withDescription("denied")
      val errors   = Seq(status.asException(trailers), status.asRuntimeException(trailers))
      errors.foreach { error =>
        wrapped.onError(error)
        Mockito.verify(observer).onError(ArgumentMatchers.same(error))
      }
      wrapped.onNext("response")
      wrapped.onCompleted()
      Mockito.verify(observer).onNext("response")
      Mockito.verify(observer).onCompleted()
    }
  }

  test("Response observer delegates server flow control and cancellation") {
    val observer = mock(classOf[ServerCallStreamObserver[String]])
    Mockito.when(observer.isReady).thenReturn(true)
    Mockito.when(observer.isCancelled).thenReturn(true)
    val wrapped =
      Grpc.serverResponseObserver(observer).asInstanceOf[ServerCallStreamObserver[String]]
    val handler = new Runnable { override def run(): Unit = () }
    assert(wrapped.isReady)
    assert(wrapped.isCancelled)
    wrapped.setOnCancelHandler(handler)
    wrapped.setOnReadyHandler(handler)
    wrapped.setOnCloseHandler(handler)
    wrapped.setOnReadyThreshold(1024)
    wrapped.setCompression("gzip")
    wrapped.setMessageCompression(true)
    wrapped.disableAutoInboundFlowControl()
    wrapped.disableAutoRequest()
    wrapped.request(2)
    Mockito.verify(observer).setOnCancelHandler(handler)
    Mockito.verify(observer).setOnReadyHandler(handler)
    Mockito.verify(observer).setOnCloseHandler(handler)
    Mockito.verify(observer).setOnReadyThreshold(1024)
    Mockito.verify(observer).setCompression("gzip")
    Mockito.verify(observer).setMessageCompression(true)
    Mockito.verify(observer).disableAutoInboundFlowControl()
    Mockito.verify(observer).disableAutoRequest()
    Mockito.verify(observer).request(2)
  }

}
