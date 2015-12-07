package com.trueaccord.scalapb.grpc

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import io.grpc.stub.StreamObserver

import scala.concurrent.{Promise, Future}
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
    case scala.util.Failure(error) =>
      observer.onError(error)
      observer.onCompleted()
  }
}
