package scalapb.ziogrpc.points

object PointServiceGrpc {
  val METHOD_GREET: _root_.io.grpc.MethodDescriptor[scalapb.ziogrpc.points.Request, scalapb.ziogrpc.points.Response] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("scalapb.ziogrpc.PointService", "Greet"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[scalapb.ziogrpc.points.Request])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[scalapb.ziogrpc.points.Response])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(scalapb.ziogrpc.points.PointsProto.javaDescriptor.getServices.get(0).getMethods.get(0)))
      .build()
  
  val METHOD_POINTS: _root_.io.grpc.MethodDescriptor[scalapb.ziogrpc.points.Request, scalapb.ziogrpc.points.Point] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("scalapb.ziogrpc.PointService", "Points"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[scalapb.ziogrpc.points.Request])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[scalapb.ziogrpc.points.Point])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(scalapb.ziogrpc.points.PointsProto.javaDescriptor.getServices.get(0).getMethods.get(1)))
      .build()
  
  val METHOD_BIDI: _root_.io.grpc.MethodDescriptor[scalapb.ziogrpc.points.Point, scalapb.ziogrpc.points.Response] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("scalapb.ziogrpc.PointService", "Bidi"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[scalapb.ziogrpc.points.Point])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[scalapb.ziogrpc.points.Response])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(scalapb.ziogrpc.points.PointsProto.javaDescriptor.getServices.get(0).getMethods.get(2)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("scalapb.ziogrpc.PointService")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(scalapb.ziogrpc.points.PointsProto.javaDescriptor))
      .addMethod(METHOD_GREET)
      .addMethod(METHOD_POINTS)
      .addMethod(METHOD_BIDI)
      .build()
  
  trait PointService extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = PointService
    def greet(request: scalapb.ziogrpc.points.Request): scala.concurrent.Future[scalapb.ziogrpc.points.Response]
    def points(request: scalapb.ziogrpc.points.Request, responseObserver: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Point]): Unit
    def bidi(responseObserver: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Response]): _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Point]
  }
  
  object PointService extends _root_.scalapb.grpc.ServiceCompanion[PointService] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[PointService] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = scalapb.ziogrpc.points.PointsProto.javaDescriptor.getServices.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = scalapb.ziogrpc.points.PointsProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: PointService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_GREET,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[scalapb.ziogrpc.points.Request, scalapb.ziogrpc.points.Response] {
          override def invoke(request: scalapb.ziogrpc.points.Request, observer: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Response]): Unit =
            serviceImpl.greet(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_POINTS,
        _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[scalapb.ziogrpc.points.Request, scalapb.ziogrpc.points.Point] {
          override def invoke(request: scalapb.ziogrpc.points.Request, observer: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Point]): Unit =
            serviceImpl.points(request, observer)
        }))
      .addMethod(
        METHOD_BIDI,
        _root_.io.grpc.stub.ServerCalls.asyncBidiStreamingCall(new _root_.io.grpc.stub.ServerCalls.BidiStreamingMethod[scalapb.ziogrpc.points.Point, scalapb.ziogrpc.points.Response] {
          override def invoke(observer: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Response]): _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Point] =
            serviceImpl.bidi(observer)
        }))
      .build()
  }
  
  trait PointServiceBlockingClient {
    def serviceCompanion = PointService
    def greet(request: scalapb.ziogrpc.points.Request): scalapb.ziogrpc.points.Response
    def points(request: scalapb.ziogrpc.points.Request): scala.collection.Iterator[scalapb.ziogrpc.points.Point]
  }
  
  class PointServiceBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[PointServiceBlockingStub](channel, options) with PointServiceBlockingClient {
    override def greet(request: scalapb.ziogrpc.points.Request): scalapb.ziogrpc.points.Response = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GREET, options, request)
    }
    
    override def points(request: scalapb.ziogrpc.points.Request): scala.collection.Iterator[scalapb.ziogrpc.points.Point] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_POINTS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): PointServiceBlockingStub = new PointServiceBlockingStub(channel, options)
  }
  
  class PointServiceStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[PointServiceStub](channel, options) with PointService {
    override def greet(request: scalapb.ziogrpc.points.Request): scala.concurrent.Future[scalapb.ziogrpc.points.Response] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GREET, options, request)
    }
    
    override def points(request: scalapb.ziogrpc.points.Request, responseObserver: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Point]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_POINTS, options, request, responseObserver)
    }
    
    override def bidi(responseObserver: _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Response]): _root_.io.grpc.stub.StreamObserver[scalapb.ziogrpc.points.Point] = {
      _root_.scalapb.grpc.ClientCalls.asyncBidiStreamingCall(channel, METHOD_BIDI, options, responseObserver)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): PointServiceStub = new PointServiceStub(channel, options)
  }
  
  def bindService(serviceImpl: PointService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = PointService.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): PointServiceBlockingStub = new PointServiceBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): PointServiceStub = new PointServiceStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = scalapb.ziogrpc.points.PointsProto.javaDescriptor.getServices.get(0)
  
}