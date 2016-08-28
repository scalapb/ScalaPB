package com.trueaccord.scalapb.grpc

trait AbstractService {
  def serviceCompanion: ServiceCompanion[_]
}

