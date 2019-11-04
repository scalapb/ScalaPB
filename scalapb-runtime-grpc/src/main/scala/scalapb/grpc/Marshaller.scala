package scalapb.grpc

import java.io.{ByteArrayInputStream, InputStream}

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message, TypeMapper}

class Marshaller[T <: GeneratedMessage with Message[T]](companion: GeneratedMessageCompanion[T])
    extends io.grpc.MethodDescriptor.Marshaller[T] {
  override def stream(t: T): InputStream = new ByteArrayInputStream(t.toByteArray)

  override def parse(inputStream: InputStream): T =
    companion.parseFrom(inputStream)
}

class TypeMappedMarshaller[T <: GeneratedMessage with Message[T], Custom](
    typeMapper: TypeMapper[T, Custom],
    companion: GeneratedMessageCompanion[T]
) extends io.grpc.MethodDescriptor.Marshaller[Custom] {
  override def stream(t: Custom): InputStream =
    new ByteArrayInputStream(typeMapper.toBase(t).toByteArray)

  override def parse(inputStream: InputStream): Custom =
    typeMapper.toCustom(companion.parseFrom(inputStream))
}

object Marshaller {
  def forMessage[T <: GeneratedMessage with Message[T]](
      implicit companion: GeneratedMessageCompanion[T]
  ) =
    new Marshaller[T](companion)

  def forTypeMappedType[T <: GeneratedMessage with Message[T], Custom](
      implicit typeMapper: TypeMapper[T, Custom],
      companion: GeneratedMessageCompanion[T]
  ) =
    new TypeMappedMarshaller[T, Custom](typeMapper, companion)
}
