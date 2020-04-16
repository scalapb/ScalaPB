package scalapb.ziogrpc.points

object ZioPoints {
  trait PointService {
    def greet(request: scalapb.ziogrpc.points.Request): _root_.zio.IO[io.grpc.Status, scalapb.ziogrpc.points.Response]
    def points(request: scalapb.ziogrpc.points.Request): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point]
    def bidi(request: _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point]): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Response]
  }
  
  object PointService {
    trait WithContext[-Context] {
      def greet(request: scalapb.ziogrpc.points.Request, context: Context): _root_.zio.IO[io.grpc.Status, scalapb.ziogrpc.points.Response]
      def points(request: scalapb.ziogrpc.points.Request, context: Context): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point]
      def bidi(request: _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point], context: Context): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Response]
    }
    type WithMetadata = WithContext[io.grpc.Metadata]
    
    def withAnyContext(serviceImpl: PointService): WithContext[Any] = new WithContext[Any] {
      def greet(request: scalapb.ziogrpc.points.Request, context: Any): _root_.zio.IO[io.grpc.Status, scalapb.ziogrpc.points.Response] = serviceImpl.greet(request)
      def points(request: scalapb.ziogrpc.points.Request, context: Any): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point] = serviceImpl.points(request)
      def bidi(request: _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point], context: Any): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Response] = serviceImpl.bidi(request)
    }
    
    def transformContext[Context, NewContext](serviceImpl: WithContext[Context], f: NewContext => _root_.zio.IO[io.grpc.Status, Context]): WithContext[NewContext] = new WithContext[NewContext] {
      def greet(request: scalapb.ziogrpc.points.Request, context: NewContext): _root_.zio.IO[io.grpc.Status, scalapb.ziogrpc.points.Response] = f(context).flatMap(serviceImpl.greet(request, _))
      def points(request: scalapb.ziogrpc.points.Request, context: NewContext): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point] = _root_.zio.stream.ZStream.fromEffect(f(context)).flatMap(serviceImpl.points(request, _))
      def bidi(request: _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point], context: NewContext): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Response] = _root_.zio.stream.ZStream.fromEffect(f(context)).flatMap(serviceImpl.bidi(request, _))
    }
    
    def transformContext[NewContext](serviceImpl: PointService, f: NewContext => _root_.zio.IO[io.grpc.Status, Unit]): WithContext[NewContext] = transformContext(withAnyContext(serviceImpl), f)
  }
  
  type PointServiceClient = _root_.zio.Has[PointServiceClient.Service]
  
  object PointServiceClient {
    trait Service extends scalapb.ziogrpc.points.ZioPoints.PointService
    
    // accessor methods
    def greet(request: scalapb.ziogrpc.points.Request): _root_.zio.ZIO[PointServiceClient, io.grpc.Status, scalapb.ziogrpc.points.Response] = _root_.zio.ZIO.accessM(_.get.greet(request))
    def points(request: scalapb.ziogrpc.points.Request): _root_.zio.stream.ZStream[PointServiceClient, io.grpc.Status, scalapb.ziogrpc.points.Point] = _root_.zio.stream.ZStream.accessStream(_.get.points(request))
    def bidi(request: _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point]): _root_.zio.stream.ZStream[PointServiceClient, io.grpc.Status, scalapb.ziogrpc.points.Response] = _root_.zio.stream.ZStream.accessStream(_.get.bidi(request))
    
    def managed(managedChannel: scalapb.zio_grpc.ZManagedChannel, options: io.grpc.CallOptions = io.grpc.CallOptions.DEFAULT, headers: => io.grpc.Metadata = new io.grpc.Metadata()): zio.Managed[Throwable, PointServiceClient.Service] = managedChannel.map {
      channel => new Service {
        def greet(request: scalapb.ziogrpc.points.Request): _root_.zio.IO[io.grpc.Status, scalapb.ziogrpc.points.Response] = scalapb.zio_grpc.client.ClientCalls.unaryCall(
          scalapb.zio_grpc.client.ZClientCall(channel.newCall(scalapb.ziogrpc.points.PointServiceGrpc.METHOD_GREET, options)),
          headers,
          request
        )
        def points(request: scalapb.ziogrpc.points.Request): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point] = scalapb.zio_grpc.client.ClientCalls.serverStreamingCall(
          scalapb.zio_grpc.client.ZClientCall(channel.newCall(scalapb.ziogrpc.points.PointServiceGrpc.METHOD_POINTS, options)),
          headers,
          request
        )
        def bidi(request: _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Point]): _root_.zio.stream.Stream[io.grpc.Status, scalapb.ziogrpc.points.Response] = scalapb.zio_grpc.client.ClientCalls.bidiCall(
          scalapb.zio_grpc.client.ZClientCall(channel.newCall(scalapb.ziogrpc.points.PointServiceGrpc.METHOD_BIDI, options)),
          headers,
          request
        )
      }
    }
    
    def live(managedChannel: scalapb.zio_grpc.ZManagedChannel, options: io.grpc.CallOptions = io.grpc.CallOptions.DEFAULT, headers: => io.grpc.Metadata = new io.grpc.Metadata()): zio.Layer[Throwable, PointServiceClient] = zio.ZLayer.fromManaged(managed(managedChannel, options, headers))
  }
  
  implicit def bindableService: scalapb.zio_grpc.ZBindableService[PointService] = new scalapb.zio_grpc.ZBindableService[PointService] {
    def bindService(serviceImpl: PointService): zio.UIO[_root_.io.grpc.ServerServiceDefinition] = bindableServiceWithContext.bindService(PointService.withAnyContext(serviceImpl))
  }
  implicit def bindableServiceWithContext: scalapb.zio_grpc.ZBindableService[scalapb.ziogrpc.points.ZioPoints.PointService.WithMetadata] = new scalapb.zio_grpc.ZBindableService[scalapb.ziogrpc.points.ZioPoints.PointService.WithMetadata] {
    def bindService(serviceImpl: scalapb.ziogrpc.points.ZioPoints.PointService.WithMetadata): zio.UIO[_root_.io.grpc.ServerServiceDefinition] =
      zio.ZIO.runtime[Any].map {
        runtime: zio.Runtime[Any] =>
          _root_.io.grpc.ServerServiceDefinition.builder(scalapb.ziogrpc.points.PointServiceGrpc.SERVICE)
          .addMethod(
            scalapb.ziogrpc.points.PointServiceGrpc.METHOD_GREET,
            _root_.scalapb.zio_grpc.server.ZServerCallHandler.unaryCallHandler(runtime, serviceImpl.greet)
          )
          .addMethod(
            scalapb.ziogrpc.points.PointServiceGrpc.METHOD_POINTS,
            _root_.scalapb.zio_grpc.server.ZServerCallHandler.serverStreamingCallHandler(runtime, serviceImpl.points)
          )
          .addMethod(
            scalapb.ziogrpc.points.PointServiceGrpc.METHOD_BIDI,
            _root_.scalapb.zio_grpc.server.ZServerCallHandler.bidiCallHandler(runtime, serviceImpl.bidi)
          )
          .build()
      }
  }
}