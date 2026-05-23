import io.grpc.CallOptions
import io.grpc.stub.AbstractStub.StubFactory
import io.grpc.reflection.v1alpha.reflection._
import io.grpc.reflection.v1alpha.reflection.ServerReflectionRequest.MessageRequest

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

class GrpcServiceScalaServerSpec extends GrpcServiceSpecBase {
  describe("scala companion object") {
    it("provides descriptor object") {
      import com.thesamet.proto.e2e.service.{Service1Grpc => Service1GrpcScala}
      // Deprecated usage
      Service1GrpcScala.javaDescriptor.getName must be("Service1")

      Service1GrpcScala.Service1.javaDescriptor.getName must be("Service1")
      implicitly[scalapb.grpc.ServiceCompanion[Service1GrpcScala.Service1]].javaDescriptor must be(
        Service1GrpcScala.Service1.javaDescriptor
      )
    }
  }

  describe("scala server") {

    describe("reflection service") {
      // https://github.com/grpc/grpc-java/blob/v1.5.0/services/src/test/java/io/grpc/protobuf/services/ProtoReflectionServiceTest.java

      it("listServices") {
        withScalaServer { channel =>
          val stub                       = ServerReflectionGrpc.stub(channel)
          val (responseObserver, future) = getObserverAndFutureVector[ServerReflectionResponse]
          val requestObserver            = stub.serverReflectionInfo(responseObserver)
          val request                    = ServerReflectionRequest(
            host = "localhost",
            messageRequest = MessageRequest.ListServices("services")
          )
          requestObserver.onNext(request)
          requestObserver.onCompleted()
          val expect = Seq(
            ServerReflectionResponse(
              validHost = "localhost",
              originalRequest = Some(
                request
              ),
              messageResponse = ServerReflectionResponse.MessageResponse.ListServicesResponse(
                ListServiceResponse(
                  service = Seq(
                    "com.thesamet.proto.e2e.Service1",
                    "grpc.reflection.v1alpha.ServerReflection"
                  ).map(ServiceResponse(_))
                )
              )
            )
          )
          assert(Await.result(future, 3.seconds) === expect)
        }
      }
    }

    describe("java client") {
      import com.thesamet.proto.e2e.{Service1Grpc => Service1GrpcJava, _}

      it("unaryStringLength BlockingStub") {
        withScalaServer { channel =>
          val client  = Service1GrpcJava.newBlockingStub(channel)
          val string  = randomString()
          val request = Service.Req1.newBuilder.setRequest(string).build()
          client.unaryStringLength(request).getLength must be(string.length)
        }
      }

      it("unaryStringLength FeatureStub") {
        withScalaServer { channel =>
          val client  = Service1GrpcJava.newFutureStub(channel)
          val string  = randomString()
          val request = Service.Req1.newBuilder.setRequest(string).build()
          client.unaryStringLength(request).get().getLength must be(string.length)
        }
      }
    }

    describe("scala client") {
      import com.thesamet.proto.e2e.service.{Service1Grpc => Service1GrpcScala, _}

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
          Await.result(client.unaryStringLength(Req1(string)), 10.seconds).length must be(
            string.length
          )
        }
      }

      it("customOption") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.blockingStub(channel)
          client.customOption(Req5()) must be(Res5())
        }
      }

      it("clientStreamingCount") {
        withScalaServer { channel =>
          val client                     = Service1GrpcScala.stub(channel)
          val (responseObserver, future) = getObserverAndFuture[Res2]
          val requestObserver            = client.clientStreamingCount(responseObserver)
          val n                          = Random.nextInt(10)
          for (_ <- 1 to n) {
            requestObserver.onNext(Req2())
          }
          requestObserver.onCompleted()
          Await.result(future, 10.seconds).count must be(n)
        }
      }

      it("serverStreamingFan") {
        withScalaServer { channel =>
          val client             = Service1GrpcScala.stub(channel)
          val (observer, future) = getObserverAndFutureVector[Res3]

          client.serverStreamingFan(Req3(100), observer)

          Await.result(future, 10.seconds) must be(Vector.fill(100)(Res3()))
        }
      }

      it("bidiStreamingDoubler") {
        withScalaServer { channel =>
          val client                     = Service1GrpcScala.stub(channel)
          val (responseObserver, future) = getObserverAndFutureVector[Res4]
          val requestObserver            = client.bidiStreamingDoubler(responseObserver)
          requestObserver.onNext(Req4(11))
          requestObserver.onNext(Req4(3))
          requestObserver.onNext(Req4(6))
          requestObserver.onCompleted()
          Await.result(future, 10.seconds).map(_.b) must be(Vector(22, 6, 12))
        }
      }

      it("should wrap an exception as a StatusRuntimeException") {
        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)

          intercept[io.grpc.StatusRuntimeException] {
            Await.result(client.throwException(Req5()), 10.seconds)
          }
        }
      }

      import com.thesamet.proto.e2e.service

      it("sealed unary call should work") {

        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)

          Await.result(client.sealedUnary(service.Req1("5")), 10.seconds) must be(service.Res1(5))
          Await.result(client.sealedUnary(service.Req2()), 10.seconds) must be(service.Res2(17))

        }

      }

      it("clientStreaming with sealed trait should work") {

        withScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)

          val (responseObserver, future) = getObserverAndFuture[service.SealedResponse]
          val requestObserver            = client.sealedClientStreaming(responseObserver)
          val n                          = Random.nextInt(10)
          for (_ <- 1 to n) {
            requestObserver.onNext(Req2())
          }
          requestObserver.onCompleted()
          Await.result(future, 10.seconds) must be(service.Res2(n))

        }

      }

      it("serverStreamingFan with sealed trait should work") {
        withScalaServer { channel =>
          val client             = Service1GrpcScala.stub(channel)
          val (observer, future) = getObserverAndFutureVector[service.SealedResponse]

          client.sealedServerStreaming(service.Req2(), observer)

          Await.result(future, 10.seconds) must be(Vector.fill(14)(Res2()))
        }
      }

      it("bidiStreaming with sealed trait should work") {
        withScalaServer { channel =>
          val client                     = Service1GrpcScala.stub(channel)
          val (responseObserver, future) = getObserverAndFutureVector[SealedResponse]
          val requestObserver            = client.sealedBidiStreaming(responseObserver)
          requestObserver.onNext(Req1())
          requestObserver.onNext(Req2())
          requestObserver.onCompleted()
          Await.result(future, 10.seconds) must be(Vector(Res1(17), Res2(3), Res1(17), Res2(3)))
        }
      }

      it("InProcessTransport skips serialization") {
        withInMemoryTransportScalaServer { channel =>
          val client = Service1GrpcScala.stub(channel)
          val req    = service.Req1(request = "AmIsraelChai")

          val res = Await.result(client.echoRequest(req), 10.seconds)

          res.req.get must be theSameInstanceAs (req)
        }
      }

      it("companion object acts as stub factory") {
        withScalaServer { channel =>
          Service1GrpcScala.Service1Stub
            .newStub(channel, CallOptions.DEFAULT) mustBe a[Service1GrpcScala.Service1Stub]
          implicitly[StubFactory[Service1GrpcScala.Service1Stub]]
            .newStub(channel, CallOptions.DEFAULT) mustBe a[Service1GrpcScala.Service1Stub]
        }

      }

    }
  }
}
