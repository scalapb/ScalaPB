package com.trueaccord.scalapb.grpc

import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import org.mockito.{ ArgumentMatchers, Mockito }
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

import scala.util.Failure

class GrpcSpec extends FlatSpec with MockitoSugar {

  "Complete observer" should "wrap an exception as a StatusException on failure" in {
    val observer = mock[StreamObserver[_]]

    Grpc.completeObserver(observer)(Failure(new RuntimeException("Error!")))

    Mockito.verify(observer).onError(ArgumentMatchers.any(classOf[StatusException]))
  }

}
