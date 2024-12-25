package scalapb.perf

import com.google.protobuf.ByteString

class SimpleMessageBench extends ProtobufBenchmark[protos.SimpleMessage, Protos.SimpleMessage](Protos.SimpleMessage.parser()) {
  def sample: protos.SimpleMessage = protos.SimpleMessage(
    i = 14,
    j = 9,
    k = ByteString.copyFromUtf8("foobar"),
    color = protos.Color.RED
  )
}
