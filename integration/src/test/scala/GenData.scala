import com.trueaccord.scalapb.compiler.FunctionalPrinter
import org.scalacheck.Gen

object GenData {

  import GenTypes._
  import Nodes._
  import org.scalacheck.Gen._

  sealed trait ProtoValue
  case class PrimitiveValue(value: String) extends ProtoValue
  case class EnumValue(value: String) extends ProtoValue
  case class MessageValue(values: Seq[(String, ProtoValue)]) extends ProtoValue {

    def toAscii: String =
      printAscii(new FunctionalPrinter()).toString

    def printAscii(printer: FunctionalPrinter): FunctionalPrinter = {
      values.foldLeft(printer) {
        case (printer, (name, PrimitiveValue(value))) => printer.add(s"$name: $value")
        case (printer, (name, EnumValue(value))) => printer.add(s"$name: $value")
        case (printer, (name, mv: MessageValue)) => printer.add(s"$name: <")
          .indent
          .call(mv.printAscii)
          .outdent
          .add(">")
      }
    }
  }

  private def genMessageValue(rootNode: RootNode,
                             message: MessageNode,
                             depth: Int = 0): Gen[MessageValue] = {
    def genFieldValueByOptions(field: FieldNode): Gen[Seq[(String, ProtoValue)]] = sized {
      s =>
        def genCount: Gen[Int] = field.fieldOptions match {
          case FieldOptions.OPTIONAL =>
            if (depth > 3) Gen.const(0)
            else
              Gen.oneOf(0, 1)
          case FieldOptions.REQUIRED =>
            Gen.const(1)
          case FieldOptions.REPEATED =>
            // TODO(nadavsr): work on larger messages, limit total field count.
            if (depth > 3) Gen.const(0) else Gen.choose(0, ((s - 2 * depth) max 0) min 10)
        }

        def genSingleFieldValue(field: FieldNode): Gen[ProtoValue] =
          field.fieldType match {
            case Primitive(_, genValue) =>
              genValue.map(v => PrimitiveValue(v))
            case EnumReference(id) =>
              oneOf(rootNode.enumsById(id).values.map(_._1)).map(v => EnumValue(v))
            case MessageReference(id) =>
              genMessageValue(rootNode, rootNode.messagesById(id), depth + 1)
          }

        for {
          count <- genCount
          result <- Gen.listOfN(count, genSingleFieldValue(field).map(field.name -> _))
        } yield result
    }

   //
    Gen.sequence[Seq, Seq[(String, ProtoValue)]](message.fields.map {
      field => genFieldValueByOptions(field)
    }).map(s => MessageValue(s.flatten))
  }

  def genMessageValueInstance(rootNode: RootNode): Gen[(MessageNode, MessageValue)] = for {
    messageId <- Gen.choose(0, rootNode.maxMessageId.get - 1)
    message = rootNode.messagesById(messageId)
    messageValue <- genMessageValue(rootNode, message)
  } yield (message, messageValue)
}
