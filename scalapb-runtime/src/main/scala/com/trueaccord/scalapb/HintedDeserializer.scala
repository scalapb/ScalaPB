package com.trueaccord.scalapb

/**
 * Created by peter on 5/29/15.
 */
trait HintedDeserializer {
  def deserialize(data: Array[Byte], hint: String): com.trueaccord.scalapb.GeneratedMessage
}
