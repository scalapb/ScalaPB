import com.trueaccord.pb.Service1ScalaImpl

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

class GrpcServiceScalaServerSpec extends GrpcServiceSpecBase {
  describe("scala companion object") {
    it("provides descriptor object") {
      import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala}
      // Deprecated usage
      Service1GrpcScala.javaDescriptor.getName must be("Service1")

      Service1GrpcScala.Service1.javaDescriptor.getName must be("Service1")
      implicitly[com.trueaccord.scalapb.grpc.ServiceCompanion[Service1GrpcScala.Service1]].javaDescriptor must be(
        Service1GrpcScala.Service1.javaDescriptor)
    }
  }

  describe("scala server") {

    describe("java client") {
      import com.trueaccord.proto.e2e.{Service1Grpc => Service1GrpcJava, _}

      it("unaryStringLength BlockingStub") {
        withScalaServer { channel =>
          val client = Service1GrpcJava.newBlockingStub(channel)
          val string = randomString()
          val request = Service.Req1.newBuilder.setRequest(string).build()
          client.unaryStringLength(request).getLength must be(string.length)
        }
      }

      it("unaryStringLength FeatureStub") {
        withScalaServer { channel =>
          val client = Service1GrpcJava.newFutureStub(channel)
          val string = randomString()
          val request = Service.Req1.newBuilder.setRequest(string).build()
          client.unaryStringLength(request).get().getLength must be(string.length)
        }
      }
    }

    describe("scala client") {
      import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala, _}

      it("unaryStringLength blockingStub") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.blockingStub(channel)
          val string = randomString()
          client.unaryStringLength(Req1(string)).length must be(string.length)
        }
      }

      it("unaryStringLength stub") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)
          val string = randomString()
          Await.result(client.unaryStringLength(Req1(string)), 2.seconds).length must be(string.length)
        }
      }

      it("clientStreamingCount") {
        withScalaServer { channel =>
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
        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)
          val (observer, future) = getObserverAndFutureVector[Res3]

          client.serverStreamingFan(Req3(100), observer)

          Await.result(future, 2.seconds) must be(Vector.fill(100)(Res3()))
        }
      }

      it("bidiStreamingDoubler") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)
          val (responseObserver, future) = getObserverAndFutureVector[Res4]
          val requestObserver = client.bidiStreamingDoubler(responseObserver)
          val request = Req4(a = Random.nextInt())
          requestObserver.onNext(Req4(11))
          requestObserver.onNext(Req4(3))
          requestObserver.onNext(Req4(6))
          requestObserver.onCompleted()
          Await.result(future, 2.seconds).map(_.b) must be(Vector(22, 6, 12))
        }
      }

      it("should wrap an exception as a StatusRuntimeException") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)

          intercept[io.grpc.StatusRuntimeException] {
            Await.result( client.throwException( Req5() ), 2.seconds )
          }
        }
      }
    }
  }
}
