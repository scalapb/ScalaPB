// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.google.protobuf.struct

object StructProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq.empty
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      com.google.protobuf.struct.Struct,
      com.google.protobuf.struct.Value,
      com.google.protobuf.struct.ListValue
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """Chxnb29nbGUvcHJvdG9idWYvc3RydWN0LnByb3RvEg9nb29nbGUucHJvdG9idWYiuwEKBlN0cnVjdBJICgZmaWVsZHMYASADK
  AsyIy5nb29nbGUucHJvdG9idWYuU3RydWN0LkZpZWxkc0VudHJ5QgviPwgSBmZpZWxkc1IGZmllbGRzGmcKC0ZpZWxkc0VudHJ5E
  hoKA2tleRgBIAEoCUII4j8FEgNrZXlSA2tleRI4CgV2YWx1ZRgCIAEoCzIWLmdvb2dsZS5wcm90b2J1Zi5WYWx1ZUIK4j8HEgV2Y
  Wx1ZVIFdmFsdWU6AjgBIpgDCgVWYWx1ZRJLCgpudWxsX3ZhbHVlGAEgASgOMhouZ29vZ2xlLnByb3RvYnVmLk51bGxWYWx1ZUIO4
  j8LEgludWxsVmFsdWVIAFIJbnVsbFZhbHVlEjUKDG51bWJlcl92YWx1ZRgCIAEoAUIQ4j8NEgtudW1iZXJWYWx1ZUgAUgtudW1iZ
  XJWYWx1ZRI1CgxzdHJpbmdfdmFsdWUYAyABKAlCEOI/DRILc3RyaW5nVmFsdWVIAFILc3RyaW5nVmFsdWUSLwoKYm9vbF92YWx1Z
  RgEIAEoCEIO4j8LEglib29sVmFsdWVIAFIJYm9vbFZhbHVlEk4KDHN0cnVjdF92YWx1ZRgFIAEoCzIXLmdvb2dsZS5wcm90b2J1Z
  i5TdHJ1Y3RCEOI/DRILc3RydWN0VmFsdWVIAFILc3RydWN0VmFsdWUSSwoKbGlzdF92YWx1ZRgGIAEoCzIaLmdvb2dsZS5wcm90b
  2J1Zi5MaXN0VmFsdWVCDuI/CxIJbGlzdFZhbHVlSABSCWxpc3RWYWx1ZUIGCgRraW5kIkgKCUxpc3RWYWx1ZRI7CgZ2YWx1ZXMYA
  SADKAsyFi5nb29nbGUucHJvdG9idWYuVmFsdWVCC+I/CBIGdmFsdWVzUgZ2YWx1ZXMqGwoJTnVsbFZhbHVlEg4KCk5VTExfVkFMV
  UUQAEKBAQoTY29tLmdvb2dsZS5wcm90b2J1ZkILU3RydWN0UHJvdG9QAVoxZ2l0aHViLmNvbS9nb2xhbmcvcHJvdG9idWYvcHR5c
  GVzL3N0cnVjdDtzdHJ1Y3RwYvgBAaICA0dQQqoCHkdvb2dsZS5Qcm90b2J1Zi5XZWxsS25vd25UeXBlc2IGcHJvdG8z"""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}