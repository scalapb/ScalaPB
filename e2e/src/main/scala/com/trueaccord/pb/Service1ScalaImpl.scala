package com.trueaccord.pb

import java.util.concurrent.atomic.AtomicInteger

import com.trueaccord.proto.e2e.service.Service1Grpc.Service1
import com.trueaccord.proto.e2e.service._
import io.grpc.stub.StreamObserver

import scala.concurrent.Future

object Service1ScalaImpl {

  val method3Limit = 50

}

class Service1ScalaImpl extends Service1[Future]{

  override def method1(request: Req1): Future[Res1] =
    Future.successful(Res1(length = request.request.length))

  override def method2(observer: StreamObserver[Res2]) =
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

  private[this] var method3Counter = 0

  override def method3(request: Req3, observer: StreamObserver[Res3]): Unit = synchronized{
    method3Counter += request.num
    if(method3Counter > Service1ScalaImpl.method3Limit){
      observer.onNext(Res3())
      observer.onCompleted()
    }
  }

  override def method4(observer: StreamObserver[Res4]): StreamObserver[Req4] =
    new StreamObserver[Req4] {
      override def onError(e: Throwable): Unit = {}
      override def onCompleted(): Unit = {}
      override def onNext(request: Req4): Unit = {
        observer.onNext(Res4(request.a * 2))
        observer.onCompleted()
      }
    }
}
