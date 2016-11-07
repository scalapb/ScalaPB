import GraphGen.State
import Nodes.{Proto3, Proto2, ProtoSyntax}
import org.scalacheck.{Arbitrary, Gen}

/**
 * Created by thesamet on 9/28/14.
 */
object GenTypes {

  sealed trait ProtoType {
    def packable: Boolean
    def isMap: Boolean
  }

  case class Primitive(name: String, genValue: Gen[String],
                       packable: Boolean = true, isMap: Boolean = false) extends ProtoType {
    override def toString = s"Primitive($name)"
  }

  private val genInt32 = Arbitrary.arbitrary[Int]
  private val genInt64 = Arbitrary.arbitrary[Long]
  private val genUInt64 = Gen.chooseNum[Long](0, Long.MaxValue)
  private val genUInt32 = Gen.chooseNum[Int](0, Int.MaxValue)

  // Simple version, since the one at TextFormatUtils is only in runtimne.
  private def escapeBytes(raw: Seq[Byte]): String = {
    val builder = new StringBuilder
    builder.append('"')
    raw.map {
      case b if b == '\"'.toByte => builder.append("\\\"")
      case b if b == '\''.toByte => builder.append("\\\'")
      case b if b == '\\'.toByte => builder.append("\\\\")
      case b if b >= 0x20 => builder.append(b.toChar)
      case b =>
        builder.append('\\')
        builder.append((48 + ((b >>> 6) & 3)).toChar)
        builder.append((48 + ((b >>> 3) & 7)).toChar)
        builder.append((48 + (b & 7)).toChar)
    }
    builder.append('"')
    builder.result()
  }

  val ProtoSint32 = Primitive("sint32", genInt32.map(_.toString))
  val ProtoUint32 = Primitive("uint32", genUInt32.map(_.toString))
  val ProtoInt32 = Primitive("int32", genInt32.map(_.toString))
  val ProtoFixed32 = Primitive("fixed32", genUInt32.map(_.toString))
  val ProtoSfixed32 = Primitive("sfixed32", genInt32.map(_.toString))
  val ProtoSint64 = Primitive("sint64", genInt64.map(_.toString))
  val ProtoUint64 = Primitive("uint64", genUInt64.map(_.toString))
  val ProtoInt64 = Primitive("int64", genInt64.map(_.toString))
  val ProtoFixed64 = Primitive("fixed64", genUInt64.map(_.toString))
  val ProtoSfixed64 = Primitive("sfixed64", genInt64.map(_.toString))
  val ProtoDouble = Primitive("double", Arbitrary.arbitrary[Double].map(_.toString))
  val ProtoFloat = Primitive("float", Arbitrary.arbitrary[Float].map(_.toString))
  val ProtoBool = Primitive("bool", Arbitrary.arbitrary[Boolean].map(_.toString))
  val ProtoString = Primitive("string", Arbitrary.arbitrary[String].map(
    _.getBytes("UTF-8").toSeq).map(escapeBytes),
    packable = false)
  val ProtoBytes = Primitive("bytes", Gen.listOf(Arbitrary.arbitrary[Byte]).map(escapeBytes),
    packable = false)

  case class MessageReference(id: Int) extends ProtoType {
    def packable = false
    def isMap = false
  }

  case class EnumReference(id: Int) extends ProtoType {
    def packable = true
    def isMap = false
  }

  case class MapType(keyType: ProtoType, valueType: ProtoType) extends ProtoType {
    def packable = false
    def isMap = true
  }

  def generatePrimitive = Gen.oneOf(
    ProtoSint32, ProtoUint32, ProtoInt32, ProtoFixed32, ProtoSfixed32,
    ProtoSint64, ProtoUint64, ProtoInt64, ProtoFixed64, ProtoSfixed64,
    ProtoDouble, ProtoFloat, ProtoBool, ProtoString, ProtoBytes)

  def genProto3EnumReference(state: State) =
    Gen.oneOf(state.proto3EnumIds).map(EnumReference)

  def generateMapKey: Gen[ProtoType] = Gen.oneOf(
      ProtoSint32, ProtoUint32, ProtoInt32, ProtoFixed32, ProtoSfixed32,
      ProtoSint64, ProtoUint64, ProtoInt64, ProtoFixed64, ProtoSfixed64,
      ProtoBool, ProtoString)

  object FieldModifier extends Enumeration {
    val OPTIONAL = Value("optional")
    val REQUIRED = Value("required")
    val REPEATED = Value("repeated")
  }

  case class FieldOptions(modifier: FieldModifier.Value, isPacked: Boolean)

  def genFieldModifier(allowRequired: Boolean): Gen[FieldModifier.Value] =
    if (allowRequired) Gen.oneOf(FieldModifier.OPTIONAL, FieldModifier.REQUIRED, FieldModifier.REPEATED)
    else Gen.oneOf(FieldModifier.OPTIONAL, FieldModifier.REPEATED)

  // For enums and messages we choose a type that was either declared before or is nested within
  // the current message. This is meant to avoid each file to depend only on previous files.
  def genFieldType(
    state: State, syntax: ProtoSyntax,
    allowMaps: Boolean = true,
    allowCurrentMessage: Boolean = true,
    enumMustHaveZeroDefined: Boolean = false): Gen[ProtoType] = {
    val baseFreq = List((5, generatePrimitive))
    val withMessages = if (state._nextMessageId > 0 && allowCurrentMessage)
      (1, Gen.chooseNum(0, state._nextMessageId - 1).map(MessageReference)) :: baseFreq
    else if (!allowCurrentMessage && state.currentFileInitialMessageId > 0)
      (1, Gen.chooseNum(0, state.currentFileInitialMessageId - 1).map(MessageReference)) :: baseFreq
    else baseFreq
    val withEnums = syntax match {
      case Proto2 =>
        if (enumMustHaveZeroDefined && state.enumsWithZeroDefined.nonEmpty) {
          (1, Gen.oneOf(state.enumsWithZeroDefined).map(EnumReference)) :: withMessages
        } else if (!enumMustHaveZeroDefined && state._nextEnumId > 0) {
          (1, Gen.chooseNum(0, state._nextEnumId - 1).map(EnumReference)) :: withMessages
        } else withMessages
      case Proto3 =>
        // Proto3 can not include proto2 enums (which always have zero defined)
        if (state.proto3EnumIds.nonEmpty)
          (1, genProto3EnumReference(state)) :: withMessages
          else withMessages
    }
    val withMaps = if (!allowMaps)
      withEnums else (1, genMapType(state, syntax)) :: withEnums
    Gen.frequency(withMaps: _*)
  }

  def genMapType(state: State, syntax: ProtoSyntax): Gen[MapType] = for {
    keyType <- generateMapKey
    valueType <- genFieldType(state, syntax, allowMaps = false,
      // until https://github.com/google/protobuf/issues/355 is fixed.
      allowCurrentMessage = false, enumMustHaveZeroDefined = true)
  } yield MapType(keyType, valueType)

  // We allow 'required' only for messages with lower ids. This ensures no cycles of required
  // fields.
  def genOptionsForField(messageId: Int, fieldType: ProtoType, protoSyntax: ProtoSyntax, inOneof: Boolean): Gen[FieldOptions] =
    if (inOneof) Gen.const(FieldOptions(FieldModifier.OPTIONAL, isPacked = false)) else
      fieldType match {
        case MessageReference(id) => genFieldModifier(allowRequired = protoSyntax.isProto2 && id < messageId).map(
          mod => FieldOptions(mod, isPacked = false))
        case MapType(_, _) => Gen.const(FieldOptions(FieldModifier.REPEATED, isPacked = false))
        case _ => for {
          mod <- genFieldModifier(allowRequired = protoSyntax.isProto2)
          packed <- if (fieldType.packable && mod == FieldModifier.REPEATED)
            Gen.oneOf(true, false) else Gen.const(false)
        } yield FieldOptions(mod, isPacked = packed)
      }
}
