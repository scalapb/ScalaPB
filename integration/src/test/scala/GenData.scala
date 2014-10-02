import com.trueaccord.scalapb.compiler
import org.scalacheck.Gen

/**
 * Created by thesamet on 9/28/14.
 */
object GenData {

  import GenTypes._
  import GenUtils._
  import Nodes._
  import org.scalacheck.Gen._

  private def genProtoAscii(rootNode: RootNode,
                            message: MessageNode,
                            printer: compiler.FunctionalPrinter,
                            depth: Int = 0): Gen[compiler.FunctionalPrinter] = {

    def genFieldValueByOptions(field: FieldNode)(printer: compiler.FunctionalPrinter): Gen[compiler.FunctionalPrinter] = sized {
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

        def genFieldValue(field: FieldNode)(printer: compiler.FunctionalPrinter): Gen[(Unit, compiler.FunctionalPrinter)] =
          field.fieldType match {
            case Primitive(_, genValue) =>
              genValue.map(v => ((), printer.add(s"${field.name}: $v")))
            case EnumReference(id) =>
              oneOf(rootNode.enumsById(id).values.map(_._1)).map(v => ((), printer.add(s"${field.name}: $v")))
            case MessageReference(id) =>
              val p0 = printer.add(s"${field.name}: <").indent
              for {
                p <- genProtoAscii(rootNode, rootNode.messagesById(id), p0, depth + 1)
              } yield ((), p.outdent.add(">"))
          }

        for {
          count <- genCount
          (result, printer) <- listOfNWithStatefulGen(count, printer)(genFieldValue(field))
        } yield printer
    }

    message.fields.foldLeft(Gen.const(printer)) {
      case (printer, field) =>
        for {
          p <- printer
          p <- genFieldValueByOptions(field)(p)
        } yield p
    }
  }

  def genProtoAsciiInstance(rootNode: RootNode): Gen[(MessageNode, String)] = for {
    messageId <- Gen.choose(0, rootNode.maxMessageId.get - 1)
    message = rootNode.messagesById(messageId)
    printer <- genProtoAscii(rootNode, message, compiler.FunctionalPrinter(), 0)
  } yield (message, printer.toString)
}
