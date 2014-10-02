import GraphGen.State
import org.scalacheck.{Arbitrary, Gen}

/**
 * Created by thesamet on 9/28/14.
 */
object GenTypes {

  sealed trait ProtoType {
  }

  case class Primitive(name: String, genValue: Gen[String]) extends ProtoType {
    override def toString = s"Primitive($name)"
  }

  private val uint32Gen = Gen.chooseNum[Long](0, 0xffffffffL)
  private val uint64Gen = Gen.chooseNum[Long](0, Long.MaxValue) // TODO: fix high bit

  private def escapeString(raw: String): String = {
    import scala.reflect.runtime.universe._
    Literal(Constant(raw)).toString
  }

  val ProtoSint32 = Primitive("sint32", Arbitrary.arbitrary[Int].map(_.toString))
  val ProtoUint32 = Primitive("uint32", uint32Gen.map(_.toString))
  val ProtoInt32 = Primitive("int32", Arbitrary.arbitrary[Int].map(_.toString))
  val ProtoFixed32 = Primitive("fixed32", uint32Gen.map(_.toString))
  val ProtoSfixed32 = Primitive("sfixed32", Arbitrary.arbitrary[Int].map(_.toString))
  val ProtoSint64 = Primitive("sint64", Arbitrary.arbitrary[Long].map(_.toString))
  val ProtoUint64 = Primitive("uint64", uint64Gen.map(_.toString))
  val ProtoInt64 = Primitive("int64", Arbitrary.arbitrary[Long].map(_.toString))
  val ProtoFixed64 = Primitive("fixed64", uint64Gen.map(_.toString))
  val ProtoSfixed64 = Primitive("sfixed64", Arbitrary.arbitrary[Long].map(_.toString))
  val ProtoDouble = Primitive("double", Arbitrary.arbitrary[Double].map(_.toString))
  val ProtoFloat = Primitive("float", Arbitrary.arbitrary[Float].map(_.toString))
  val ProtoBool = Primitive("bool", Arbitrary.arbitrary[Boolean].map(_.toString))
  val ProtoString = Primitive("string", Arbitrary.arbitrary[String].map(escapeString))
  val ProtoBytes = Primitive("bytes", Arbitrary.arbitrary[String].map(escapeString))

  case class MessageReference(id: Int) extends ProtoType

  case class EnumReference(id: Int) extends ProtoType

  def generatePrimitive = Gen.oneOf(
    ProtoSint32, ProtoUint32, ProtoInt32, ProtoFixed32, ProtoSfixed32,
    ProtoSint64, ProtoUint64, ProtoInt64, ProtoFixed64, ProtoSfixed64,
    ProtoDouble, ProtoFloat, ProtoBool, ProtoString, ProtoBytes)

  object FieldOptions extends Enumeration {
    val OPTIONAL = Value("optional")
    val REQUIRED = Value("required")
    val REPEATED = Value("repeated")
  }

  def genFieldOptions(allowRequired: Boolean): Gen[FieldOptions.Value] =
    if (allowRequired) Gen.oneOf(FieldOptions.OPTIONAL, FieldOptions.REQUIRED, FieldOptions.REPEATED)
    else Gen.oneOf(FieldOptions.OPTIONAL, FieldOptions.REPEATED)

  // For enums and messages we choose a type that was either declared before or is nested within
  // the current message. This is meant to avoid each file to depend only on previous files.
  def genFieldType(state: State): Gen[ProtoType] = {
    val baseFreq = List((5, generatePrimitive))
    val withMessages = if (state._nextMessageId > 0)
      (1, Gen.chooseNum(0, state._nextMessageId - 1).map(MessageReference)) :: baseFreq
    else baseFreq
    val withEnums = if (state._nextEnumId > 0)
      (1, Gen.chooseNum(0, state._nextEnumId - 1).map(EnumReference)) :: withMessages
    else withMessages
    Gen.frequency(withEnums: _*)
  }

  // We allow 'required' only for messages with lower ids. This ensures no cycles of required
  // fields.
  def genOptionsForField(messageId: Int, fieldType: ProtoType) = fieldType match {
    case MessageReference(id) => genFieldOptions(allowRequired = id < messageId)
    case _ => genFieldOptions(true)
  }
}
