package scalapb.codec

import com.google.protobuf.{CodedInputStream, CodedOutputStream, InvalidProtocolBufferException, WireFormat}

sealed trait Codec[T] {
  def encodeNoTag(output: CodedOutputStream, value: T): Unit
  def decodeNoTag(input: CodedInputStream): T
  def merge(prev: T, next: T): T
}

sealed trait FixedSize {
  def sizeInBytes: Int
}

sealed trait WireType {
  def wireType: Int
}

case class WithTag[FN <: Int with Singleton,T, C <: WireType with Codec[T]](fieldNumber: FN, component: C) extends Codec[T] {
  override def encodeNoTag(output: CodedOutputStream, value: T): Unit = {
    component.encodeNoTag(output, value)
  }

  override def decodeNoTag(input: CodedInputStream): T = {
    val tag = input.readTag()
    if (WireFormat.getTagWireType(tag) != component.wireType) {
      throw new InvalidProtocolBufferException("Protocol message tag had invalid wire type.")
    }
    component.decodeNoTag(input)
  }

  override def merge(prev: T, next: T): T = component.merge(prev, next)
}