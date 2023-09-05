package scalapb.descriptors

import com.google.protobuf.ByteString

sealed trait PValue extends Any, Matchable {
  def as[A](implicit reads: Reads[A]): A = reads.read(this)
}
case object PEmpty extends PValue

case class PInt(value: Int) extends AnyVal with PValue

case class PLong(value: Long) extends AnyVal with PValue

case class PString(value: String) extends AnyVal with PValue

case class PDouble(value: Double) extends AnyVal with PValue

case class PFloat(value: Float) extends AnyVal with PValue

case class PByteString(value: ByteString) extends AnyVal with PValue

case class PBoolean(value: Boolean) extends AnyVal with PValue

case class PEnum(value: EnumValueDescriptor) extends AnyVal with PValue

case class PMessage(value: Map[FieldDescriptor, PValue]) extends AnyVal with PValue

case class PRepeated(value: Vector[PValue]) extends AnyVal with PValue

case class Reads[A](read: PValue => A)

class ReadsException(msg: String) extends Exception(msg)

object Reads extends ReadsCompat {
  implicit val intReads: Reads[Int] = Reads[Int] {
    case PInt(value) => value
    case _           => throw new ReadsException("Expected PInt")
  }

  implicit val longReads: Reads[Long] = Reads[Long] {
    case PLong(value) => value
    case _            => throw new ReadsException("Expected PLong")
  }

  implicit val stringReads: Reads[String] = Reads[String] {
    case PString(value) => value
    case _              => throw new ReadsException("Expected PString")
  }

  implicit val doubleReads: Reads[Double] = Reads[Double] {
    case PDouble(value) => value
    case _              => throw new ReadsException("Expected PDouble")
  }

  implicit val floatReads: Reads[Float] = Reads[Float] {
    case PFloat(value) => value
    case _             => throw new ReadsException("Expected PFloat")
  }

  implicit val byteStringReads: Reads[ByteString] = Reads[ByteString] {
    case PByteString(value) => value
    case _                  => throw new ReadsException("Expected PByteString")
  }

  implicit val booleanReads: Reads[Boolean] = Reads[Boolean] {
    case PBoolean(value) => value
    case _               => throw new ReadsException("Expected PBoolean")
  }

  implicit val enumReads: Reads[EnumValueDescriptor] = Reads[EnumValueDescriptor] {
    case PEnum(value) => value
    case _            => throw new ReadsException("Expected PEnum")
  }

  implicit def optional[A](implicit reads: Reads[A]): Reads[Option[A]] = Reads[Option[A]] {
    case PEmpty => None
    case x      => Some(reads.read(x))
  }
}
