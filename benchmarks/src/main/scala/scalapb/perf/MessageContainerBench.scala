package scalapb.perf

import com.google.protobuf.ByteString

class MessageContainerBench extends ProtobufBenchmark[protos.MessageContainer, Protos.MessageContainer](Protos.MessageContainer.parser()) {
  def sample: protos.MessageContainer = protos.MessageContainer(
      opt = Some(
        protos.SimpleMessage(
          i = 14,
          j = 9,
          k = ByteString.copyFromUtf8("foobar"),
          color = protos.Color.RED
        )
      ),
      rep = (for {
        i <- 1 to 20
        j <- 1 to 20
      } yield protos.SimpleMessage(
        i = i * 31 + j,
        j = j * 17 + i,
        k = ByteString.copyFromUtf8(s"foo_${i}_${j}"),
        color = protos.Color.fromValue((i + j) % 4)
      )).toList
    )
}
