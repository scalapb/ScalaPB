package com.trueaccord.scalapb

import scala.annotation.implicitNotFound

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
}

