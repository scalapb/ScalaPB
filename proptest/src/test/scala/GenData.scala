import scalapb.compiler.FunctionalPrinter
import org.scalacheck.Gen

object GenData {
  import GenTypes._
  import Nodes._
  import org.scalacheck.Gen.{sized, oneOf}

  sealed trait ProtoValue
  case class PrimitiveValue(value: String)                   extends ProtoValue
  case class EnumValue(value: String)                        extends ProtoValue
  case class MessageValue(values: Seq[(String, ProtoValue)]) extends ProtoValue {
    def toAscii: String =
      printAscii(new FunctionalPrinter()).result()

    def printAscii(printer: FunctionalPrinter): FunctionalPrinter = {
      values.foldLeft(printer) {
        case (printer, (name, PrimitiveValue(value))) => printer.add(s"$name: $value")
        case (printer, (name, EnumValue(value)))      => printer.add(s"$name: $value")
        case (printer, (name, mv: MessageValue))      =>
          printer
            .add(s"$name: <")
            .indent
            .call(mv.printAscii)
            .outdent
            .add(">")
      }
    }
  }

  private def genMessageValue(
      rootNode: RootNode,
      message: MessageNode,
      depth: Int = 0
  ): Gen[MessageValue] = {
    def genFieldValueByOptions(field: FieldNode): Gen[Seq[(String, ProtoValue)]] = sized { s =>
      def genCount: Gen[Int] = field.fieldOptions.modifier match {
        case FieldModifier.OPTIONAL =>
          if (depth > 3) Gen.const(0)
          // If a one of, we already considered not providing a value,
          // so we always return 1
          else if (field.oneOfGroup.isOneof) Gen.const(1)
          else Gen.oneOf(0, 1)
        case FieldModifier.REQUIRED =>
          Gen.const(1)
        case FieldModifier.REPEATED =>
          // TODO(nadavsr): work on larger messages, limit total field count.
          if (depth > 3) Gen.const(0) else Gen.choose(0, ((s - 2 * depth) max 0) min 10)
      }

      def genSingleFieldValue(fieldType: ProtoType): Gen[ProtoValue] =
        fieldType match {
          case Primitive(_, genValue, _, _) =>
            genValue.map(v => PrimitiveValue(v))
          case EnumReference(id) =>
            oneOf(rootNode.enumsById(id).values.map(_._1)).map(v => EnumValue(v))
          case MessageReference(id) =>
            genMessageValue(rootNode, rootNode.messagesById(id), depth + 1)
          case MapType(keyType, valueType) =>
            for {
              k <- genSingleFieldValue(keyType)
              v <- genSingleFieldValue(valueType)
            } yield MessageValue(Seq("key" -> k, "value" -> v))
        }

      for {
        count  <- genCount
        result <- Gen.listOfN(count, genSingleFieldValue(field.fieldType).map(field.name -> _))
      } yield result
    }

    def oneofGroups: Map[GraphGen.OneOfGrouping, Seq[FieldNode]] =
      message.fields.groupBy(_.oneOfGroup) - GraphGen.NotInOneof

    // chooses Some(field) from a oneof group, or None.
    def chooseFieldFromGroup(l: Seq[FieldNode]): Gen[Option[FieldNode]] =
      Gen.oneOf(l.map(Some(_)) :+ None)

    val fieldGens: Seq[Gen[Seq[(String, ProtoValue)]]] = message.fields.collect {
      case field if !field.oneOfGroup.isOneof =>
        genFieldValueByOptions(field)
    }

    // Chooses at most one field from each one of and generates a value for it.
    val oneofGens: Seq[Gen[Seq[(String, ProtoValue)]]] = oneofGroups.values
      .map(group =>
        chooseFieldFromGroup(group).flatMap {
          case None        => Gen.const(Seq())
          case Some(field) => genFieldValueByOptions(field)
        }
      )
      .toSeq

    val x: Seq[Gen[Seq[(String, ProtoValue)]]] = fieldGens ++ oneofGens

    Gen
      .sequence[Seq[Seq[(String, ProtoValue)]], Seq[(String, ProtoValue)]](x)
      .map(s => MessageValue(s.toSeq.flatten))
  }

  def genMessageValueInstance(rootNode: RootNode): Gen[(MessageNode, MessageValue)] =
    for {
      messageId <- Gen.choose(0, rootNode.maxMessageId.get)
      message = rootNode.messagesById(messageId)
      messageValue <- genMessageValue(rootNode, message)
    } yield (message, messageValue)
}
