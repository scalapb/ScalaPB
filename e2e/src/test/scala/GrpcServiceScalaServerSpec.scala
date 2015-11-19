import java.util.concurrent.TimeoutException

import com.trueaccord.pb.Service1ScalaImpl

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

class GrpcServiceScalaServerSpec extends GrpcServiceSpecBase {

  describe("scala server") {

    describe("java client") {
      import com.trueaccord.proto.e2e.{Service1Grpc => Service1GrpcJava, _}

      it("method1 BlockingStub") {
        withScalaServer { channel =>
          val client = Service1GrpcJava.newBlockingStub(channel)
          val string = randomString()
          val request = Service.Req1.newBuilder.setRequest(string).build()
          assert(client.method1(request).getLength === string.length)
        }
      }

      it("method1 FeatureStub") {
        withScalaServer { channel =>
          val client = Service1GrpcJava.newFutureStub(channel)
          val string = randomString()
          val request = Service.Req1.newBuilder.setRequest(string).build()
          assert(client.method1(request).get().getLength === string.length)
        }
      }
    }

    describe("scala client") {
      import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala, _}

      it("method1 blockingClient") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.blockingClient(channel)
          val string = randomString()
          assert(client.method1(Req1(string)).length === string.length)
        }
      }

      it("method1 futureClient") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.futureClient(channel)
          val string = randomString()
          assert(Await.result(client.method1(Req1(string)), 2.seconds).length === string.length)
        }
      }

      it("method2") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.futureClient(channel)
          val (responseObserver, future) = getObserverAndFuture[Res2]
          val requestObserver = client.method2(responseObserver)
          val n = Random.nextInt(10)
          for (_ <- 1 to n) {
            requestObserver.onNext(Req2())
          }

          intercept[TimeoutException]{
            Await.result(future, 2.seconds)
          }

          requestObserver.onCompleted()
          assert(Await.result(future, 2.seconds).count === n)
        }
      }

      it("method3") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.futureClient(channel)
          val (observer, future) = getObserverAndFuture[Res3]
          val requests = Stream.continually(Req3(num = Random.nextInt(10)))
          val count = requests.scanLeft(0)(_ + _.num).takeWhile(_ < Service1ScalaImpl.method3Limit).size - 1

          requests.take(count).foreach { req =>
            client.method3(req, observer)
          }

          intercept[TimeoutException]{
            Await.result(future, 2.seconds)
          }

          client.method3(Req3(1000), observer)
          Await.result(future, 2.seconds)
        }
      }

      it("method4") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.futureClient(channel)
          val (responseObserver, future) = getObserverAndFuture[Res4]
          val requestObserver = client.method4(responseObserver)
          intercept[TimeoutException]{
          Await.result(future, 2.seconds)
        }
          val request = Req4(a = Random.nextInt())
          requestObserver.onNext(request)
          assert(Await.result(future, 2.seconds).b === (request.a * 2))
        }
      }
    }
  }

}
