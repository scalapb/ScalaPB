import org.scalacheck.{Arbitrary, Gen}

/**
 * Created by thesamet on 9/28/14.
 */
object GenData {
  import Nodes._
  import GenTypes._
  import Gen._

  private def genProtoAscii(rootNode: RootNode,
                    message: MessageNode,
                    enclose: Boolean = false,
                    depth: Int = 0): Gen[Seq[String]] = {

    def genFieldValueByOptions(field: FieldNode): Gen[Seq[String]] = sized {
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

        def genFieldValue(field: FieldNode): Gen[String] = for {
          value <- genValueForProtoType(field.fieldType, rootNode, depth)
        } yield ("  " * depth) + field.name + ": " + value

        for {
          count <- genCount
          result <- listOfN(count, genFieldValue(field))
        } yield result
    }

    Gen.sequence[Seq, Seq[String]] {
      val fields: Seq[Gen[Seq[String]]] = message.fields.map(genFieldValueByOptions)
      if (enclose) Seq(Gen.const(Seq("  " * depth + "<"))) ++ fields ++ Seq(Gen.const(Seq("  " * depth + ">")))
      else fields
    }.map(_.flatten)
  }

  def genProtoAsciiInstance(rootNode: RootNode): Gen[(MessageNode, Seq[String])] = for {
    messageId <- Gen.choose(0, rootNode.maxMessageId.get - 1)
    message = rootNode.messagesById(messageId)
    ascii <- genProtoAscii(rootNode, message)
  } yield (message, ascii)

  def genValueForProtoType(pt: ProtoType, rootNode: RootNode, depth: Int = 0): Gen[String] = pt match {
    case Primitive(_, genValue) => genValue
    case MessageReference(id) =>
      val msg = rootNode.messagesById(id)
      val t: Gen[String] = genProtoAscii(rootNode, msg,  enclose = true, depth + 1).map(_.mkString("\n"))
      t
    case EnumReference(id) =>
      /*
      val t: Gen[String] = genMessageInstance(fileSet.enumMap(typeName).message.get, fileSet,
        enclose = true, depth + 1).map(_.mkString("\n"))
      t
      */
      Gen.const("enum")
  }

}
