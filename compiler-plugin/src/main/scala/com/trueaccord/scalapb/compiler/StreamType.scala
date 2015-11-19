package com.trueaccord.scalapb.compiler

sealed abstract class StreamType extends Product with Serializable
object StreamType {
  case object Unary extends StreamType
  case object ClientStreaming extends StreamType
  case object ServerStreaming extends StreamType
  case object Bidirectional extends StreamType
}
