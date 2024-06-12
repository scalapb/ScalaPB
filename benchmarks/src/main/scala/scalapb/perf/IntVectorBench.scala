package scalapb.perf

import protos._
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Scope

@State(Scope.Benchmark)
class IntVectorBench extends ProtobufBenchmark[IntVector, Protos.IntVector](Protos.IntVector.parser()) {
  @Param(Array("0", "1", "100", "10000"))
  var size: Int = _

  def sample: IntVector = IntVector((0 until size).map(1 << _).toVector)
}
