package scalapb.perf

import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Scope

@State(Scope.Benchmark)
class EnumVectorBench extends ProtobufBenchmark[protos.EnumVector, Protos.EnumVector](Protos.EnumVector.parser()) {
  @Param(Array("0", "1", "100", "10000"))
  var size: Int = _
  
  def sample: protos.EnumVector = protos.EnumVector((0 until size).map(i => protos.Color.fromValue(i % 4)).toVector)
}
