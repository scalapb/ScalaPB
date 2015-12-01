package com.trueaccord.pb

import java.util.concurrent.atomic.AtomicInteger

import com.trueaccord.proto.e2e.Service._
import com.trueaccord.proto.e2e.Service1Grpc._
import io.grpc.stub.StreamObserver

class Service1JavaImpl extends Service1{

  override def method1(request: Req1, observer: StreamObserver[Res1]): Unit = {
    val res = Res1.newBuilder.setLength(request.getRequest.length).build()
    observer.onNext(res)
    observer.onCompleted()
  }

  override def method2(observer: StreamObserver[Res2]) =
    new StreamObserver[Req2] {
      private[this] val counter = new AtomicInteger()
      override def onError(e: Throwable): Unit =
        observer.onError(e)

      override def onCompleted(): Unit = {
        val res = Res2.newBuilder().setCount(counter.getAndSet(0)).build()
        observer.onNext(res)
      }

      override def onNext(v: Req2): Unit = {
        counter.incrementAndGet()
      }
    }

  private[this] var method3Counter = 0

  override def method3(request: Req3, observer: StreamObserver[Res3]): Unit = synchronized{
    method3Counter += request.getNum
    if(method3Counter > Service1ScalaImpl.method3Limit){
      observer.onNext(Res3.getDefaultInstance)
      observer.onCompleted()
    }
  }

  override def method4(observer: StreamObserver[Res4]): StreamObserver[Req4] =
    new StreamObserver[Req4] {
      override def onError(e: Throwable): Unit = {}
      override def onCompleted(): Unit = {}
      override def onNext(request: Req4): Unit = {
        observer.onNext(Res4.newBuilder.setB(request.getA * 2).build())
        observer.onCompleted()
      }
    }

}
