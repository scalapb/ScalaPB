import GraphGen.State
import org.scalacheck.{Arbitrary, Gen}

/**
 * Created by thesamet on 9/28/14.
 */
object GenTypes {

  sealed trait ProtoType {
    def packable: Boolean
  }

  case class Primitive(name: String, genValue: Gen[String],
                       packable: Boolean = true) extends ProtoType {
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
  val ProtoString = Primitive("string", Arbitrary.arbitrary[String].map(escapeString),
    packable = false)
  val ProtoBytes = Primitive("bytes", Arbitrary.arbitrary[String].map(escapeString),
    packable = false)

  case class MessageReference(id: Int) extends ProtoType {
    def packable = false
  }

  case class EnumReference(id: Int) extends ProtoType {
    def packable = true
  }

  def generatePrimitive = Gen.oneOf(
    ProtoSint32, ProtoUint32, ProtoInt32, ProtoFixed32, ProtoSfixed32,
    ProtoSint64, ProtoUint64, ProtoInt64, ProtoFixed64, ProtoSfixed64,
    ProtoDouble, ProtoFloat, ProtoBool, ProtoString, ProtoBytes)

  object FieldModifier extends Enumeration {
    val OPTIONAL = Value("optional")
    val REQUIRED = Value("required")
    val REPEATED = Value("repeated")
  }

  case class FieldOptions(modifier: FieldModifier.Value, isPacked: Boolean = false)

  def genFieldModifier(allowRequired: Boolean): Gen[FieldModifier.Value] =
    if (allowRequired) Gen.oneOf(FieldModifier.OPTIONAL, FieldModifier.REQUIRED, FieldModifier.REPEATED)
    else Gen.oneOf(FieldModifier.OPTIONAL, FieldModifier.REPEATED)

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
    case MessageReference(id) => genFieldModifier(allowRequired = id < messageId).map(
      mod => FieldOptions(mod))
    case _ => for {
      mod <- genFieldModifier(true)
      packed <- if (fieldType.packable && mod == FieldModifier.REPEATED) Gen.oneOf(true, false) else Gen.const(false)
    } yield FieldOptions(mod, packed)
  }
}
