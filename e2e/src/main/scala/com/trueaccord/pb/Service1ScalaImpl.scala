package com.trueaccord.pb

import java.util.concurrent.atomic.AtomicInteger

import com.trueaccord.proto.e2e.service.Service1Grpc.Service1
import com.trueaccord.proto.e2e.service._
import io.grpc.stub.StreamObserver

import scala.concurrent.Future

class Service1ScalaImpl extends Service1 {

  override def unaryStringLength(request: Req1): Future[Res1] =
    Future.successful(Res1(length = request.request.length))

  override def clientStreamingCount(observer: StreamObserver[Res2]) =
    new StreamObserver[Req2] {
      private[this] val counter = new AtomicInteger()

      override def onError(e: Throwable): Unit =
        observer.onError(e)

      override def onCompleted(): Unit = {
        observer.onNext(Res2(counter.getAndSet(0)))
      }

      override def onNext(v: Req2): Unit = {
        counter.incrementAndGet()
      }
    }

  override def serverStreamingFan(request: Req3, observer: StreamObserver[Res3]): Unit = {
    (1 to request.num).foreach {
      _ => observer.onNext(Res3())
    }
    observer.onCompleted()
  }

  override def bidiStreamingDoubler(observer: StreamObserver[Res4]): StreamObserver[Req4] =
    new StreamObserver[Req4] {
      override def onError(e: Throwable): Unit = {}

      override def onCompleted(): Unit = {
        observer.onCompleted()
      }
      override def onNext(request: Req4): Unit = {
        observer.onNext(Res4(request.a * 2))
      }
    }

  override def throwException(request: Req5): Future[Res5] = {
    Future.failed(new RuntimeException("Error!"))
  }
}
