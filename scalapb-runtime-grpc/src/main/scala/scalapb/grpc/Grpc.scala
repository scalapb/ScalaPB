package scalapb.grpc

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture, MoreExecutors}
import io.grpc.{Status, StatusException, StatusRuntimeException}
import io.grpc.stub.{ServerCallStreamObserver, StreamObserver}
import scala.concurrent.{Future, Promise}
import scala.util.Try

object Grpc {
  def guavaFuture2ScalaFuture[A](guavaFuture: ListenableFuture[A]): Future[A] = {
    val p = Promise[A]()
    Futures.addCallback(
      guavaFuture,
      new FutureCallback[A] {
        override def onFailure(t: Throwable): Unit = p.failure(t)
        override def onSuccess(a: A): Unit         = p.success(a)
      },
      MoreExecutors.directExecutor()
    )
    p.future
  }

  def completeObserver[T](observer: StreamObserver[T])(t: Try[T]): Unit =
    t.map(observer.onNext) match {
      case scala.util.Success(_) =>
        observer.onCompleted()
      case scala.util.Failure(e) =>
        observer.onError(statusException(e))
    }

  private def statusException(t: Throwable): Throwable = t match {
    case s: StatusException        => s
    case s: StatusRuntimeException => s
    case e => Status.INTERNAL.withDescription(e.getMessage).withCause(e).asException()
  }

  /** Normalizes service response errors consistently with unary RPCs, preserving explicit gRPC
    * statuses and trailers as well as server flow control and cancellation support.
    */
  def serverResponseObserver[T](observer: StreamObserver[T]): StreamObserver[T] = observer match {
    case server: ServerCallStreamObserver[T] =>
      new ServerCallStreamObserver[T] {
        override def onNext(value: T): Unit      = server.onNext(value)
        override def onError(t: Throwable): Unit = server.onError(statusException(t))
        override def onCompleted(): Unit         = server.onCompleted()
        override def isCancelled: Boolean        = server.isCancelled
        override def setOnCancelHandler(handler: Runnable): Unit =
          server.setOnCancelHandler(handler)
        override def setCompression(compression: String): Unit  = server.setCompression(compression)
        override def isReady: Boolean                           = server.isReady
        override def setOnReadyHandler(handler: Runnable): Unit = server.setOnReadyHandler(handler)
        override def disableAutoInboundFlowControl(): Unit = server.disableAutoInboundFlowControl()
        override def disableAutoRequest(): Unit            = server.disableAutoRequest()
        override def request(count: Int): Unit             = server.request(count)
        override def setMessageCompression(enable: Boolean): Unit =
          server.setMessageCompression(enable)
        override def setOnCloseHandler(handler: Runnable): Unit = server.setOnCloseHandler(handler)
        override def setOnReadyThreshold(bytes: Int): Unit      = server.setOnReadyThreshold(bytes)
      }
    case _ =>
      new StreamObserver[T] {
        override def onNext(value: T): Unit      = observer.onNext(value)
        override def onError(t: Throwable): Unit = observer.onError(statusException(t))
        override def onCompleted(): Unit         = observer.onCompleted()
      }
  }
}
