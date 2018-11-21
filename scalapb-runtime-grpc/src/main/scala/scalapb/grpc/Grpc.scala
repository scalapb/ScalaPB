package scalapb.grpc

import cats.effect.IO
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture, MoreExecutors}
import io.grpc.{Status, StatusException, StatusRuntimeException}
import io.grpc.stub.StreamObserver

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

  def guavaFuture2IO[A](guavaFuture: ListenableFuture[A]): IO[A] = {
    IO.async {callback =>
      Futures.addCallback(guavaFuture, new FutureCallback[A] {
        override def onFailure(t: Throwable): Unit = callback(Left(t))
        override def onSuccess(a: A): Unit         = callback(Right(a))
      }, MoreExecutors.directExecutor())
    }
  }

  def completeObserver[T](observer: StreamObserver[T])(t: Try[T]): Unit =
    t.map(observer.onNext) match {
      case scala.util.Success(_) =>
        observer.onCompleted()
      case scala.util.Failure(s: StatusException) =>
        observer.onError(s)
      case scala.util.Failure(s: StatusRuntimeException) =>
        observer.onError(s)
      case scala.util.Failure(e) =>
        observer.onError(
          Status.INTERNAL.withDescription(e.getMessage).withCause(e).asException()
        )
    }
}
