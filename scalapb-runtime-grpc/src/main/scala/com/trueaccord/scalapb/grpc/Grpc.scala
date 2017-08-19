package com.trueaccord.scalapb.grpc

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import io.grpc.{Status, StatusException, StatusRuntimeException}
import io.grpc.stub.StreamObserver
import scala.concurrent.{Future, Promise}
import scala.util.Try

object Grpc {
  def guavaFuture2ScalaFuture[A](guavaFuture: ListenableFuture[A]): Future[A] = {
    val p = Promise[A]()
    Futures.addCallback(guavaFuture, new FutureCallback[A] {
      override def onFailure(t: Throwable): Unit = p.failure(t)
      override def onSuccess(a: A): Unit = p.success(a)
    })
    p.future
  }

  def handleError[T](observer: StreamObserver[T], e: Throwable): Unit = e match {
    case s: StatusException =>
      observer.onError(s)
    case s: StatusRuntimeException =>
      observer.onError(s)
    case e =>
      observer.onError(
        Status.INTERNAL.withDescription(e.getMessage).withCause(e).asException()
      )
  }

  def completeObserver[T](observer: StreamObserver[T])(t: Try[T]): Unit = t match {
    case scala.util.Success(value) =>
      observer.onNext(value)
      observer.onCompleted()
    case scala.util.Failure(e) =>
      handleError(observer, e)
  }
}
