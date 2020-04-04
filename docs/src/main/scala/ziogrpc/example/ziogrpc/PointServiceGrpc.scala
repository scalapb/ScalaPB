package ziogrpc.example.ziogrpc

object PointServiceGrpc {
  val METHOD_GREET: _root_.io.grpc.MethodDescriptor[ziogrpc.example.ziogrpc.Request, ziogrpc.example.ziogrpc.Response] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("ziogrpc.example.PointService", "Greet"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[ziogrpc.example.ziogrpc.Request])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[ziogrpc.example.ziogrpc.Response])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(ziogrpc.example.ziogrpc.ZiogrpcProto.javaDescriptor.getServices.get(0).getMethods.get(0)))
      .build()
  
  val METHOD_POINTS: _root_.io.grpc.MethodDescriptor[ziogrpc.example.ziogrpc.Request, ziogrpc.example.ziogrpc.Point] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("ziogrpc.example.PointService", "Points"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[ziogrpc.example.ziogrpc.Request])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[ziogrpc.example.ziogrpc.Point])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(ziogrpc.example.ziogrpc.ZiogrpcProto.javaDescriptor.getServices.get(0).getMethods.get(1)))
      .build()
  
  val METHOD_BIDI: _root_.io.grpc.MethodDescriptor[ziogrpc.example.ziogrpc.Point, ziogrpc.example.ziogrpc.Response] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("ziogrpc.example.PointService", "Bidi"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[ziogrpc.example.ziogrpc.Point])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[ziogrpc.example.ziogrpc.Response])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(ziogrpc.example.ziogrpc.ZiogrpcProto.javaDescriptor.getServices.get(0).getMethods.get(2)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("ziogrpc.example.PointService")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(ziogrpc.example.ziogrpc.ZiogrpcProto.javaDescriptor))
      .addMethod(METHOD_GREET)
      .addMethod(METHOD_POINTS)
      .addMethod(METHOD_BIDI)
      .build()
  
  trait PointService extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = PointService
    def greet(request: ziogrpc.example.ziogrpc.Request): scala.concurrent.Future[ziogrpc.example.ziogrpc.Response]
    def points(request: ziogrpc.example.ziogrpc.Request, responseObserver: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Point]): Unit
    def bidi(responseObserver: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Response]): _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Point]
  }
  
  object PointService extends _root_.scalapb.grpc.ServiceCompanion[PointService] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[PointService] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ziogrpc.example.ziogrpc.ZiogrpcProto.javaDescriptor.getServices.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = ziogrpc.example.ziogrpc.ZiogrpcProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: PointService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_GREET,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[ziogrpc.example.ziogrpc.Request, ziogrpc.example.ziogrpc.Response] {
          override def invoke(request: ziogrpc.example.ziogrpc.Request, observer: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Response]): Unit =
            serviceImpl.greet(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_POINTS,
        _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[ziogrpc.example.ziogrpc.Request, ziogrpc.example.ziogrpc.Point] {
          override def invoke(request: ziogrpc.example.ziogrpc.Request, observer: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Point]): Unit =
            serviceImpl.points(request, observer)
        }))
      .addMethod(
        METHOD_BIDI,
        _root_.io.grpc.stub.ServerCalls.asyncBidiStreamingCall(new _root_.io.grpc.stub.ServerCalls.BidiStreamingMethod[ziogrpc.example.ziogrpc.Point, ziogrpc.example.ziogrpc.Response] {
          override def invoke(observer: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Response]): _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Point] =
            serviceImpl.bidi(observer)
        }))
      .build()
  }
  
  trait PointServiceBlockingClient {
    def serviceCompanion = PointService
    def greet(request: ziogrpc.example.ziogrpc.Request): ziogrpc.example.ziogrpc.Response
    def points(request: ziogrpc.example.ziogrpc.Request): scala.collection.Iterator[ziogrpc.example.ziogrpc.Point]
  }
  
  class PointServiceBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[PointServiceBlockingStub](channel, options) with PointServiceBlockingClient {
    override def greet(request: ziogrpc.example.ziogrpc.Request): ziogrpc.example.ziogrpc.Response = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GREET, options, request)
    }
    
    override def points(request: ziogrpc.example.ziogrpc.Request): scala.collection.Iterator[ziogrpc.example.ziogrpc.Point] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_POINTS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): PointServiceBlockingStub = new PointServiceBlockingStub(channel, options)
  }
  
  class PointServiceStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[PointServiceStub](channel, options) with PointService {
    override def greet(request: ziogrpc.example.ziogrpc.Request): scala.concurrent.Future[ziogrpc.example.ziogrpc.Response] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GREET, options, request)
    }
    
    override def points(request: ziogrpc.example.ziogrpc.Request, responseObserver: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Point]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_POINTS, options, request, responseObserver)
    }
    
    override def bidi(responseObserver: _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Response]): _root_.io.grpc.stub.StreamObserver[ziogrpc.example.ziogrpc.Point] = {
      _root_.scalapb.grpc.ClientCalls.asyncBidiStreamingCall(channel, METHOD_BIDI, options, responseObserver)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): PointServiceStub = new PointServiceStub(channel, options)
  }
  
  def bindService(serviceImpl: PointService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = PointService.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): PointServiceBlockingStub = new PointServiceBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): PointServiceStub = new PointServiceStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ziogrpc.example.ziogrpc.ZiogrpcProto.javaDescriptor.getServices.get(0)
  
}