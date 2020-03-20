package com.thesamet.pb

import java.util.concurrent.atomic.AtomicInteger

import com.thesamet.proto.e2e.service.SealedRequest
import com.thesamet.proto.e2e.service.Service1Grpc.Service1
import com.thesamet.proto.e2e.service._
import io.grpc.stub.StreamObserver

import scala.concurrent.Future

class Service1ScalaImpl extends Service1 {
  override def unaryStringLength(request: Req1): Future[Res1] =
    Future.successful(Res1(length = request.request.length))

  override def customUnary(request: Point2D): Future[Res5] =
    Future.successful(Res5())

  override def customOption(request: Req5): Future[Res5] = {
    Service1Interceptor.contextKey.get() match {
      case "custom_value" => Future.successful(Res5())
      case _              => Future.failed(new RuntimeException("error"))
    }
  }

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
        ()
      }
    }

  override def serverStreamingFan(request: Req3, observer: StreamObserver[Res3]): Unit = {
    (1 to request.num).foreach { _ =>
      observer.onNext(Res3())
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

  override def sealedUnary(request: SealedRequest): Future[SealedResponse] = {
    Future.successful(request match {
      case Req1(l)          => Res1(l.toInt)
      case Req2()             => Res2(17)
      case SealedRequest.Empty => SealedResponse.Empty
    })
  }

  override def sealedClientStreaming(
      observer: StreamObserver[SealedResponse]
  ): StreamObserver[SealedRequest] = new StreamObserver[SealedRequest] {
    private[this] val counter = new AtomicInteger()

    override def onError(e: Throwable): Unit =
      observer.onError(e)

    override def onCompleted(): Unit = {
      observer.onNext(Res2(counter.getAndSet(0)))
    }

    override def onNext(v: SealedRequest): Unit = {
      counter.incrementAndGet()
      ()
    }
  }

  override def sealedServerStreaming(
      request: SealedRequest,
      observer: StreamObserver[SealedResponse]
  ): Unit = {
    val count = request match {
      case Req1(r)          => r.length
      case Req2()             => 14
      case SealedRequest.Empty => 17
    }
    (1 to count).foreach { _ =>
      observer.onNext(Res2())
    }
    observer.onCompleted()
  }

  override def sealedBidiStreaming(
      observer: StreamObserver[SealedResponse]
  ): StreamObserver[SealedRequest] = new StreamObserver[SealedRequest] {
    override def onNext(value: SealedRequest): Unit = {
      observer.onNext(Res1(17))
      observer.onNext(Res2(3))
    }

    override def onError(t: Throwable): Unit = {}

    override def onCompleted(): Unit = {
      observer.onCompleted()
    }
  }

  override def primitiveValues(request: Int): Future[String] = Future.successful("boo")
}
