import com.thesamet.pb.Service1ScalaImpl
import com.thesamet.proto.e2e.service.{Unit => _, _}
import io.grpc.{Metadata, Status, StatusRuntimeException}
import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import io.grpc.stub.StreamObserver
import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._

class GrpcErrorSpec extends AnyFunSuite {
  private val trailerKey = Metadata.Key.of("error-detail", Metadata.ASCII_STRING_MARSHALLER)

  for (errorKind <- Seq("ordinary", "status", "runtime status")) {
    test(s"all RPC shapes preserve the same status and trailers for $errorKind errors (#1943)") {
      val trailers = new Metadata()
      trailers.put(trailerKey, "details")
      val explicit = Status.PERMISSION_DENIED.withDescription("denied")
      val error    = errorKind match {
        case "ordinary"       => new NullPointerException("boom")
        case "status"         => explicit.asException(trailers)
        case "runtime status" => explicit.asRuntimeException(trailers)
      }
      val service = new Service1ScalaImpl {
        override def unaryStringLength(request: Req1): Future[Res1] = Future.failed(error)

        override def serverStreamingFan(request: Req3, observer: StreamObserver[Res3]): Unit =
          observer.onError(error)

        private def failOnRequest[A, B](observer: StreamObserver[B]): StreamObserver[A] =
          new StreamObserver[A] {
            override def onNext(value: A): Unit      = observer.onError(error)
            override def onError(t: Throwable): Unit = ()
            override def onCompleted(): Unit         = ()
          }

        override def clientStreamingCount(observer: StreamObserver[Res2]): StreamObserver[Req2] =
          failOnRequest(observer)

        override def bidiStreamingDoubler(observer: StreamObserver[Res4]): StreamObserver[Req4] =
          failOnRequest(observer)
      }
      val name   = InProcessServerBuilder.generateName()
      val server = InProcessServerBuilder
        .forName(name)
        .directExecutor()
        .addService(Service1Grpc.bindService(service, ExecutionContext.global))
        .build()
        .start()
      val channel = InProcessChannelBuilder.forName(name).directExecutor().build()
      try {
        val client = Service1Grpc.stub(channel).withDeadlineAfter(5, TimeUnit.SECONDS)
        def check(future: Future[?]): Unit = {
          val received = intercept[StatusRuntimeException](Await.result(future, 10.seconds))
          val expected = if (errorKind == "ordinary") Status.INTERNAL else explicit
          assert(received.getStatus.getCode == expected.getCode)
          assert(
            received.getStatus.getDescription ==
              (if (errorKind == "ordinary") "boom" else "denied")
          )
          if (errorKind != "ordinary") {
            assert(received.getTrailers.get(trailerKey) == "details")
            ()
          }
        }
        def checkObserver[A](call: StreamObserver[A] => Unit): Unit = {
          val result = Promise[Unit]()
          call(new StreamObserver[A] {
            override def onNext(value: A): Unit      = ()
            override def onError(t: Throwable): Unit = { result.tryFailure(t); () }
            override def onCompleted(): Unit         = { result.trySuccess(()); () }
          })
          check(result.future)
        }
        check(client.unaryStringLength(Req1()))
        checkObserver[Res3](client.serverStreamingFan(Req3(), _))
        checkObserver[Res2](observer => client.clientStreamingCount(observer).onNext(Req2()))
        checkObserver[Res4](observer => client.bidiStreamingDoubler(observer).onNext(Req4()))
      } finally {
        channel.shutdownNow()
        server.shutdownNow()
        channel.awaitTermination(5, TimeUnit.SECONDS)
        server.awaitTermination(5, TimeUnit.SECONDS)
        ()
      }
    }
  }
}
