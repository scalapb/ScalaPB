package scalapb.compiler

import scalapb.compiler.FunctionalPrinter.PrinterEndo
import com.google.protobuf.Descriptors._

sealed trait SealedOneofStyle

object SealedOneofStyle {
  case object Default extends SealedOneofStyle

  case object Optional extends SealedOneofStyle
}

class SealedOneofsGenerator(message: Descriptor, implicits: DescriptorImplicits) {
  import implicits._

  def generateSealedOneofTrait: PrinterEndo = { fp =>
    if (!message.isSealedOneofType) fp
    else {
      val baseType        = message.scalaType.fullName
      val sealedOneofType = message.sealedOneofScalaType
      val sealedOneofName = message.sealedOneofTraitScalaType.nameSymbol
      val typeMapper      = s"_root_.scalapb.TypeMapper[${baseType}, ${sealedOneofType}]"
      val oneof           = message.getRealOneofs.get(0)
      val typeMapperName  = message.sealedOneofTypeMapper.name
      val baseClasses     = message.sealedOneofBaseClasses
      val derives         =
        if (message.sealedOneofDerives.nonEmpty)
          s"derives ${message.sealedOneofDerives.mkString(", ")} "
        else ""
      val bases =
        if (baseClasses.nonEmpty)
          s"extends ${baseClasses.mkString(" with ")} $derives"
        else derives

      val companionBases =
        if (message.sealedOneofCompanionExtendsOption.nonEmpty)
          s"extends ${message.sealedOneofCompanionExtendsOption.mkString(" with ")} "
        else ""

      if (message.sealedOneofStyle != SealedOneofStyle.Optional) {
        val sealedOneofNonEmptyName  = message.sealedOneofNonEmptyScalaType.nameSymbol
        val sealedOneofNonEmptyType  = message.sealedOneofNonEmptyScalaType.fullName
        val sealedOneofUniversalMark = if (message.isUniversalTrait) "Any with " else ""
        val sealedOneofEmptyExtends  =
          (sealedOneofType +: message.sealedOneofEmptyExtendsOption).mkString(" with ")

        fp.add(
          s"sealed trait $sealedOneofName $bases{"
        ).addIndented(
          s"type MessageType = $baseType",
          s"final def isEmpty = this.isInstanceOf[${sealedOneofType}.Empty.type]",
          s"final def isDefined = !isEmpty",
          s"final def asMessage: $baseType = ${message.sealedOneofTypeMapper.fullName}.toBase(this)",
          s"final def asNonEmpty: Option[$sealedOneofNonEmptyType] = if (isEmpty) None else Some(this.asInstanceOf[$sealedOneofNonEmptyType])"
        ).add("}")
          .add("")
          .add(s"object $sealedOneofName $companionBases{")
          .indented(
            _.add(s"case object Empty extends $sealedOneofEmptyExtends", "")
              .add(
                s"sealed trait $sealedOneofNonEmptyName extends $sealedOneofUniversalMark$sealedOneofType"
              )
              .add(
                s"def defaultInstance: ${sealedOneofType} = Empty",
                "",
                s"implicit val $typeMapperName: $typeMapper = new $typeMapper {"
              )
              .indented(
                _.add(
                  s"override def toCustom(__base: $baseType): $sealedOneofType = __base.${oneof.scalaName.nameSymbol} match {"
                ).indented(
                  _.print(oneof.fields) { case (fp, field) =>
                    fp.add(s"case __v: ${field.oneOfTypeName.fullName} => __v.value")
                  }.add(s"case ${oneof.empty.fullName} => Empty")
                ).add("}")
                  .add(
                    s"override def toBase(__custom: $sealedOneofType): $baseType = $baseType(__custom match {"
                  )
                  .indented(
                    _.print(oneof.fields) { case (fp, field) =>
                      fp.add(
                        s"case __v: ${field.scalaTypeName} => ${field.oneOfTypeName.fullName}(__v)"
                      )
                    }.add(s"case Empty => ${oneof.empty.fullName}")
                  )
                  .add("})")
              )
              .add("}")
          )
          .add("}")
      } else {
        fp.add(
          s"sealed trait $sealedOneofName $bases{"
        ).addIndented(
          s"type MessageType = $baseType",
          s"final def asMessage: $baseType = ${message.sealedOneofTypeMapper.fullName}.toBase(Some(this))"
        ).add("}")
          .add("")
          .add(s"object $sealedOneofName $companionBases {")
          .indented(
            _.add(
              s"implicit val $typeMapperName: $typeMapper = new $typeMapper {"
            ).indented(
              _.add(
                s"override def toCustom(__base: $baseType): $sealedOneofType = __base.${oneof.scalaName.nameSymbol} match {"
              ).indented(
                _.print(oneof.fields) { case (fp, field) =>
                  fp.add(s"case __v: ${field.oneOfTypeName.fullName} => Some(__v.value)")
                }.add(s"case ${oneof.empty.fullName} => None")
              ).add("}")
                .add(
                  s"override def toBase(__custom: $sealedOneofType): $baseType = $baseType(__custom match {"
                )
                .indented(
                  _.print(oneof.fields) { case (fp, field) =>
                    fp.add(
                      s"case Some(__v: ${field.scalaTypeName}) => ${field.oneOfTypeName.fullName}(__v)"
                    )
                  }.add(s"case None => ${oneof.empty.fullName}")
                )
                .add("})")
            ).add("}")
          )
          .add("}")
      }
    }
  }
}
