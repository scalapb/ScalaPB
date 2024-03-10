package scalapb.perf

class EnumBench extends ProtobufBenchmark[protos.Enum, Protos.Enum](Protos.Enum.parser()){
  def sample: protos.Enum = protos.Enum(protos.Color.RED)
}
