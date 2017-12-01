package scalapb.grpc

trait AbstractService {
  def serviceCompanion: ServiceCompanion[_]
}

