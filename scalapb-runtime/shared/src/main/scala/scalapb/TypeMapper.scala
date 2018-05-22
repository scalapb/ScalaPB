package scalapb

import scala.annotation.implicitNotFound
import com.google.protobuf.wrappers._
import com.google.protobuf.ByteString

@implicitNotFound(
  """No TypeMapper found for conversion between ${BaseType} and ${CustomType}.
    Try to implement an implicit TypeMapper[${BaseType}, ${CustomType}]. You can implement it
    in ${CustomType} companion class.
    Alternatively you can import this implicit using file-level Scala imports (see documentation)
    """)
abstract class TypeMapper[BaseType, CustomType] {
  def toCustom(base: BaseType): CustomType
  def toBase(custom: CustomType): BaseType

  def map2[Other](f: CustomType => Other)(g: Other => CustomType) = TypeMapper[BaseType, Other](
    f.compose(toCustom))((toBase _).compose(g))
}

object TypeMapper {
  def apply[BaseType, CustomType](baseToCustom: BaseType => CustomType)(customToBase: CustomType => BaseType):
  TypeMapper[BaseType, CustomType] = new TypeMapper[BaseType, CustomType] {
    def toCustom(base: BaseType): CustomType = baseToCustom(base)
    def toBase(custom: CustomType): BaseType = customToBase(custom)
  }

  implicit val DoubleValueTypeMapper:TypeMapper[DoubleValue, Double] = TypeMapper[DoubleValue, Double](_.value)(DoubleValue.apply(_))
  implicit val FloatValueTypeMapper:TypeMapper[FloatValue, Float] = TypeMapper[FloatValue, Float](_.value)(FloatValue.apply(_))
  implicit val Int64ValueTypeMapper:TypeMapper[Int64Value, Long] = TypeMapper[Int64Value, Long](_.value)(Int64Value.apply(_))
  implicit val UInt64ValueTypeMapper:TypeMapper[UInt64Value, Long] = TypeMapper[UInt64Value, Long](_.value)(UInt64Value.apply(_))
  implicit val Int32ValueTypeMapper:TypeMapper[Int32Value, Int] = TypeMapper[Int32Value, Int](_.value)(Int32Value.apply(_))
  implicit val UInt32ValueTypeMapper:TypeMapper[UInt32Value, Int] = TypeMapper[UInt32Value, Int](_.value)(UInt32Value.apply(_))
  implicit val BoolValueTypeMapper:TypeMapper[BoolValue, Boolean] = TypeMapper[BoolValue, Boolean](_.value)(BoolValue.apply(_))
  implicit val StringValueTypeMapper:TypeMapper[StringValue, String] = TypeMapper[StringValue, String](_.value)(StringValue.apply(_))
  implicit val BytesValueTypeMapper:TypeMapper[BytesValue, ByteString] = TypeMapper[BytesValue, ByteString](_.value)(BytesValue.apply(_))
}

