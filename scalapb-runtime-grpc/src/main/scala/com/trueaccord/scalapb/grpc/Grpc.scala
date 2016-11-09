package com.trueaccord.scalapb.grpc

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import io.grpc.{Status, StatusException}
import io.grpc.stub.StreamObserver

import scala.concurrent.{Future, Promise}
import scala.util.Try

object Grpc {
  def guavaFuture2ScalaFuture[A](guavaFuture: ListenableFuture[A]): Future[A] = {
    val p = Promise[A]()
    Futures.addCallback(guavaFuture, new FutureCallback[A] {
      override def onFailure(t: Throwable) = p.failure(t)
      override def onSuccess(a: A) = p.success(a)
    })
    p.future
  }

  def completeObserver[T](observer: StreamObserver[T])(t: Try[T]): Unit = t match {
    case scala.util.Success(value) =>
      observer.onNext(value)
      observer.onCompleted()
    case scala.util.Failure(s: StatusException) =>
      observer.onError(s)
    case scala.util.Failure(e) =>
      observer.onError(
        Status.INTERNAL.withDescription(e.getMessage).withCause(e).asException()
      )
  }
}
