package scalapb.grpc

import java.io.{ByteArrayInputStream, InputStream}

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, TypeMapper}

class Marshaller[T <: GeneratedMessage](companion: GeneratedMessageCompanion[T])
    extends io.grpc.MethodDescriptor.Marshaller[T] {
  override def stream(t: T): InputStream = new ProtoInputStream[T](t)

  override def parse(inputStream: InputStream): T = inputStream match {
    case pis: ProtoInputStream[_] => pis.message.asInstanceOf[T]
    case _ => companion.parseFrom(inputStream)
  }
}

class TypeMappedMarshaller[T <: GeneratedMessage, Custom](
    typeMapper: TypeMapper[T, Custom],
    companion: GeneratedMessageCompanion[T]
) extends io.grpc.MethodDescriptor.Marshaller[Custom] {
  override def stream(t: Custom): InputStream =
    new ByteArrayInputStream(typeMapper.toBase(t).toByteArray)

  override def parse(inputStream: InputStream): Custom =
    typeMapper.toCustom(companion.parseFrom(inputStream))
}

object Marshaller {
  def forMessage[T <: GeneratedMessage](implicit
      companion: GeneratedMessageCompanion[T]
  ) =
    new Marshaller[T](companion)

  def forTypeMappedType[T <: GeneratedMessage, Custom](implicit
      typeMapper: TypeMapper[T, Custom],
      companion: GeneratedMessageCompanion[T]
  ) =
    new TypeMappedMarshaller[T, Custom](typeMapper, companion)
}
