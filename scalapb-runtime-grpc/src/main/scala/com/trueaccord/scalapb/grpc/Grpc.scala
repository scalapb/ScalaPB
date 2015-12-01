package com.trueaccord.scalapb.grpc

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{Promise, Future}

object Grpc {
  def guavaFuture2ScalaFuture[A](guavaFuture: ListenableFuture[A]): Future[A] = {
    val p = Promise[A]()
    Futures.addCallback(guavaFuture, new FutureCallback[A] {
      override def onFailure(t: Throwable) = p.failure(t)
      override def onSuccess(a: A) = p.success(a)
    })
    p.future
  }
}
