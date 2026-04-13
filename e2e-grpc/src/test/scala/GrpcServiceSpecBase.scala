import java.util.concurrent.TimeUnit
import com.thesamet.pb.{Service1Interceptor, Service1JavaImpl, Service1ScalaImpl}
import com.thesamet.proto.e2e.service.{Service1Grpc => Service1GrpcScala}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder, NettyServerBuilder}
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, Server}
import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Random
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

abstract class GrpcServiceSpecBase extends AnyFunSpec with Matchers {

  protected[this] final def withScalaServer[T](f: ManagedChannel => T): T = {
    withServer(
      _.addService(ProtoReflectionService.newInstance())
        .addService(
          Service1GrpcScala.bindService(new Service1ScalaImpl, singleThreadExecutionContext)
        )
        .intercept(new Service1Interceptor)
        .build()
    )(f)
  }

  protected[this] final def withJavaServer[T](f: ManagedChannel => T): T = {
    withServer(_.addService(new Service1JavaImpl).intercept(new Service1Interceptor).build())(f)
  }

  protected[this] def withInMemoryTransportScalaServer[T](f: ManagedChannel => T): T = {
    val channelName = "test-in-mem-server"
    withManagedServer(
      InProcessServerBuilder
        .forName(channelName)
        .addService(
          Service1GrpcScala.bindService(new Service1ScalaImpl, singleThreadExecutionContext)
        )
        .build()
    ) {
      val channel = InProcessChannelBuilder
        .forName(channelName)
        .usePlaintext()
        .build()
      f(channel)
    }
  }

  private[this] def withServer[T](
      createServer: NettyServerBuilder => Server
  )(f: ManagedChannel => T): T = {
    val port = UniquePortGenerator.get()
    withManagedServer(createServer(NettyServerBuilder.forPort(port))) {
      val channel = NettyChannelBuilder
        .forAddress("localhost", port)
        .negotiationType(NegotiationType.PLAINTEXT)
        .build()
      f(channel)
    }
  }

  private[this] def withManagedServer[T](server: Server)(f: => T): T = try {
    server.start()
    f
  } finally {
    server.shutdown()
    server.awaitTermination(3000, TimeUnit.MILLISECONDS)
    ()
  }

  private[this] val singleThreadExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()

    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  protected[this] final def getObserverAndFuture[T]: (StreamObserver[T], Future[T]) = {
    val promise  = Promise[T]()
    val observer = new StreamObserver[T] {
      override def onError(t: Throwable): Unit = {}

      override def onCompleted(): Unit = {}

      override def onNext(value: T): Unit = promise.success(value)
    }
    (observer, promise.future)
  }

  protected[this] final def getObserverAndFutureVector[T]
      : (StreamObserver[T], Future[Vector[T]]) = {
    val promise  = Promise[Vector[T]]()
    val values   = Vector.newBuilder[T]
    val observer = new StreamObserver[T] {
      override def onError(t: Throwable): Unit = {}

      override def onCompleted(): Unit = promise.success(values.result())

      override def onNext(value: T): Unit = {
        values += value
      }
    }
    (observer, promise.future)
  }

  protected[this] final def randomString(): String =
    Random.alphanumeric.take(Random.nextInt(10)).mkString

}
