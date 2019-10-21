package scalapb.compiler

import scalapb.compiler.FunctionalPrinter.PrinterEndo
import com.google.protobuf.Descriptors._

sealed trait SealedOneofStyle

object SealedOneofStyle {
  case object Default extends SealedOneofStyle

  case object OrEmpty extends SealedOneofStyle
}

class SealedOneofsGenerator(message: Descriptor, implicits: DescriptorImplicits) {
  import implicits._

  def generateSealedOneofTrait: PrinterEndo = { fp =>
    if (!message.isSealedOneofType) fp
    else {
      val baseType                = message.scalaTypeName
      val sealedOneofType         = message.sealedOneofScalaType
      val sealedOneofNonEmptyName = message.sealedOneofNonEmptyName
      val sealedOneofNonEmptyType = message.sealedOneofNonEmptyScalaType
      val sealedOneofName         = message.sealedOneofNameSymbol
      val typeMapper              = s"_root_.scalapb.TypeMapper[${baseType}, ${sealedOneofType}]"
      val oneof                   = message.getOneofs.get(0)
      val typeMapperName          = message.sealedOneofNameSymbol + "TypeMapper"
      val nonEmptyTopLevel        = message.sealedOneofStyle == SealedOneofStyle.OrEmpty

      def addNonEmptySealedTrait: PrinterEndo = _.add(
        s"sealed trait $sealedOneofNonEmptyName extends $sealedOneofType",
        ""
      )

      fp.add(
          s"sealed trait $sealedOneofName extends ${message.sealedOneofBaseClasses.mkString(" with ")} {"
        )
        .addIndented(
          s"type MessageType = $baseType",
          s"final def isEmpty = this.isInstanceOf[${sealedOneofType}.Empty.type]",
          s"final def isDefined = !isEmpty",
          s"final def asMessage: $baseType = ${message.sealedOneofScalaType}.$typeMapperName.toBase(this)",
          s"final def asNonEmpty: Option[$sealedOneofNonEmptyType] = if (isEmpty) None else Some(this.asInstanceOf[$sealedOneofNonEmptyType])"
        )
        .add("}")
        .add("")
        .when(nonEmptyTopLevel)(addNonEmptySealedTrait)
        .add(s"object $sealedOneofName {")
        .indented(
          _.add(s"case object Empty extends $sealedOneofType", "")
            .when(!nonEmptyTopLevel)(addNonEmptySealedTrait)
            .add(
              s"def defaultInstance: ${sealedOneofType} = Empty",
              "",
              s"implicit val $typeMapperName: $typeMapper = new $typeMapper {"
            )
            .indented(
              _.add(
                s"override def toCustom(__base: $baseType): $sealedOneofType = __base.${oneof.scalaName} match {"
              ).indented(
                  _.print(oneof.fields) {
                    case (fp, field) =>
                      fp.add(s"case __v: ${field.oneOfTypeName} => __v.value")
                  }.add(s"case ${oneof.scalaTypeName}.Empty => Empty")
                )
                .add("}")
                .add(
                  s"override def toBase(__custom: $sealedOneofType): $baseType = $baseType(__custom match {"
                )
                .indented(
                  _.print(oneof.fields) {
                    case (fp, field) =>
                      fp.add(s"case __v: ${field.scalaTypeName} => ${field.oneOfTypeName}(__v)")
                  }.add(s"case Empty => ${oneof.scalaTypeName}.Empty")
                )
                .add("})")
            )
            .add("}")
        )
        .add("}")
    }
  }
}
