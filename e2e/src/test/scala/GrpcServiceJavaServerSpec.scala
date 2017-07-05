
import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala, _}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

class GrpcServiceJavaServerSpec extends GrpcServiceSpecBase {

  describe("java server") {

    it("unaryStringLength blockingStub") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.blockingStub(channel)
        val string = randomString()
        client.unaryStringLength(Req1(string)).length must be(string.length)
      }
    }

    it("unaryStringLength stub") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.stub(channel)
        val string = randomString()
        Await.result(client.unaryStringLength(Req1(string)), 2.seconds).length must be(string.length)
      }
    }

    it("clientStreamingCount") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.stub(channel)
        val (responseObserver, future) = getObserverAndFuture[Res2]
        val requestObserver = client.clientStreamingCount(responseObserver)
        val n = Random.nextInt(10)
        for (_ <- 1 to n) {
          requestObserver.onNext(Req2())
        }

        requestObserver.onCompleted()
        Await.result(future, 2.seconds).count must be(n)
      }
    }

    it("serverStreamingFan") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.stub(channel)
        val (observer, future) = getObserverAndFutureVector[Res3]
        client.serverStreamingFan(Req3(100), observer)
        Await.result(future, 2.seconds) must be(Vector.fill(100)(Res3()))
      }
    }

    it("bidiStreamingDoubler") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.stub(channel)
        val (responseObserver, future) = getObserverAndFutureVector[Res4]
        val requestObserver = client.bidiStreamingDoubler(responseObserver)
        requestObserver.onNext(Req4(11))
        requestObserver.onNext(Req4(3))
        requestObserver.onNext(Req4(6))
        requestObserver.onCompleted()
        Await.result(future, 2.seconds).map(_.b) must be(Vector(22, 6, 12))
      }
    }

    it("should wrap an exception as a StatusRuntimeException") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.stub(channel)

        intercept[io.grpc.StatusRuntimeException] {
          Await.result( client.throwException( Req5() ), 2.seconds )
        }
      }
    }

  }

}
