package com.thesamet.pb

import java.util.concurrent.atomic.AtomicInteger

import com.thesamet.proto.e2e.Service.{Unit => _, _}
import com.thesamet.proto.e2e.Service1Grpc._
import io.grpc.stub.StreamObserver

class Service1JavaImpl extends Service1ImplBase {
  override def unaryStringLength(request: Req1, observer: StreamObserver[Res1]): Unit = {
    val res = Res1.newBuilder.setLength(request.getRequest.length).build()
    observer.onNext(res)
    observer.onCompleted()
  }

  override def customOption(request: Req5, observer: StreamObserver[Res5]): Unit = {
    Service1Interceptor.contextKey.get() match {
      case "custom_value" =>
        val res = Res5.newBuilder.build()
        observer.onNext(res)
        observer.onCompleted()
      case _ =>
        observer.onError(new RuntimeException("error"))
    }
  }

  override def clientStreamingCount(observer: StreamObserver[Res2]) =
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
        ()
      }
    }

  override def serverStreamingFan(request: Req3, observer: StreamObserver[Res3]): Unit = {
    (1 to request.getNum()).foreach { _ =>
      observer.onNext(Res3.getDefaultInstance)
    }
    observer.onCompleted()
  }

  override def bidiStreamingDoubler(observer: StreamObserver[Res4]): StreamObserver[Req4] =
    new StreamObserver[Req4] {
      override def onError(e: Throwable): Unit = {}
      override def onCompleted(): Unit         = { observer.onCompleted() }
      override def onNext(request: Req4): Unit = {
        observer.onNext(Res4.newBuilder.setB(request.getA * 2).build())
      }
    }

  override def throwException(request: Req5, observer: StreamObserver[Res5]): Unit = {
    observer.onError(new RuntimeException("Error!"))
  }
}
