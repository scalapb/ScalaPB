package scalapb.perf

import scalapb.perf.protos.StringMessage

class StringMessageBench extends ProtobufBenchmark[protos.StringMessage, Protos.StringMessage](Protos.StringMessage.parser()) {
  def sample: StringMessage = protos.StringMessage("foobar", "foobaz")
}
