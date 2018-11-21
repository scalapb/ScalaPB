package scalapb.grpc
import cats.effect.IO
import io.grpc.stub.StreamObserver
import io.grpc.{CallOptions, Channel, MethodDescriptor}

import scala.collection.JavaConverters._
import scala.concurrent.Future

object ClientCalls {
  def blockingUnaryCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): RespT = {
    io.grpc.stub.ClientCalls.blockingUnaryCall(channel.newCall(method, options), request)
  }

  def asyncUnaryCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): Future[RespT] = {
    Grpc.guavaFuture2ScalaFuture(
      io.grpc.stub.ClientCalls.futureUnaryCall(channel.newCall(method, options), request)
    )
  }

  def ioUnaryCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): IO[RespT] = {
    Grpc.guavaFuture2IO(
      io.grpc.stub.ClientCalls.futureUnaryCall(channel.newCall(method, options), request)
    )
  }

  def blockingServerStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): Iterator[RespT] = {
    io.grpc.stub.ClientCalls
      .blockingServerStreamingCall(channel.newCall(method, options), request)
      .asScala
  }

  def asyncServerStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT,
      responseObserver: StreamObserver[RespT]
  ): Unit = {
    io.grpc.stub.ClientCalls
      .asyncServerStreamingCall(channel.newCall(method, options), request, responseObserver)
  }

  def asyncClientStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      responseObserver: StreamObserver[RespT]
  ): StreamObserver[ReqT] = {
    io.grpc.stub.ClientCalls
      .asyncClientStreamingCall(channel.newCall(method, options), responseObserver)
  }

  def asyncBidiStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      responseObserver: StreamObserver[RespT]
  ): StreamObserver[ReqT] = {
    io.grpc.stub.ClientCalls
      .asyncBidiStreamingCall(channel.newCall(method, options), responseObserver)
  }
}
