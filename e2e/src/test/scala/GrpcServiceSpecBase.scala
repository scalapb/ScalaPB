import java.util.concurrent.TimeUnit

import com.trueaccord.pb.{Service1JavaImpl, Service1ScalaImpl}
import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala}
import com.trueaccord.proto.e2e.{Service1Grpc => Service1GrpcJava}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder, NettyServerBuilder}
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, ServerServiceDefinition}
import org.scalatest.FunSpec

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Random

abstract class GrpcServiceSpecBase extends FunSpec {

  protected[this] final def withScalaServer[A](f: ManagedChannel => A): A = {
    withServer(Service1GrpcScala.bindService(new Service1ScalaImpl, singleThreadExecutionContext))(f)
  }

  protected[this] final def withJavaServer[A](f: ManagedChannel => A): A = {
    withServer(Service1GrpcJava.bindService(new Service1JavaImpl))(f)
  }

  private[this] def withServer[A](services: ServerServiceDefinition*)(f: ManagedChannel => A): A = {
    val port = UniquePortGenerator.get()
    val server = services.foldLeft(NettyServerBuilder.forPort(port))(_.addService(_)).build()
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

  protected[this] final def randomString(): String = Random.alphanumeric.take(Random.nextInt(10)).mkString

}
