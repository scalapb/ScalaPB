import java.util.concurrent.TimeUnit

import com.trueaccord.pb.{Service1JavaImpl, Service1ScalaImpl}
import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder, NettyServerBuilder}
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, Server}
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Random

abstract class GrpcServiceSpecBase extends FunSpec with MustMatchers {

  protected[this] final def withScalaServer[A](f: ManagedChannel => A): A = {
    withServer(
      _.addService(ProtoReflectionService.newInstance())
       .addService(
         Service1GrpcScala.bindService(new Service1ScalaImpl, singleThreadExecutionContext)
       ).build()
    )(f)
  }

  protected[this] final def withJavaServer[A](f: ManagedChannel => A): A = {
    withServer(_.addService(new Service1JavaImpl).build())(f)
  }

  private[this] def withServer[A](createServer: NettyServerBuilder => Server)(f: ManagedChannel => A): A = {
    val port = UniquePortGenerator.get()
    val server = createServer(NettyServerBuilder.forPort(port))
    try {
      server.start()
      val channel = NettyChannelBuilder.forAddress("localhost", port).negotiationType(NegotiationType.PLAINTEXT).build()
      f(channel)
    } finally {
      server.shutdown()
      server.awaitTermination(3000, TimeUnit.MILLISECONDS)
    }
  }

  private[this] val singleThreadExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()

    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  protected[this] final def getObserverAndFuture[A]: (StreamObserver[A], Future[A]) = {
    val promise = Promise[A]()
    val observer = new StreamObserver[A] {
      override def onError(t: Throwable): Unit = {}

      override def onCompleted(): Unit = {}

      override def onNext(value: A): Unit = promise.success(value)
    }
    (observer, promise.future)
  }

  protected[this] final def getObserverAndFutureVector[A]: (StreamObserver[A], Future[Vector[A]]) = {
    val promise = Promise[Vector[A]]()
    val values = Vector.newBuilder[A]
    val observer = new StreamObserver[A] {
      override def onError(t: Throwable): Unit = {}

      override def onCompleted(): Unit = promise.success(values.result)

      override def onNext(value: A): Unit = {
        values += value
      }
    }
    (observer, promise.future)
  }

  protected[this] final def randomString(): String = Random.alphanumeric.take(Random.nextInt(10)).mkString

}
