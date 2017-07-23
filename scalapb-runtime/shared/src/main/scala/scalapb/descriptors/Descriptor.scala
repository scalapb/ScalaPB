package scalapb.descriptors

import com.google.protobuf.descriptor._

import annotation.tailrec
import scala.collection.breakOut

sealed trait ScalaType {
  type PValueType <: PValue
}

object ScalaType {

  case object Boolean extends ScalaType {
    type PValueType = PBoolean
  }

  case object ByteString extends ScalaType {
    type PValueType = PByteString
  }

  case object Double extends ScalaType {
    type PValueType = PDouble
  }

  case object Float extends ScalaType {
    type PValueType = PFloat
  }

  case object Int extends ScalaType {
    type PValueType = PInt
  }

  case object Long extends ScalaType {
    type PValueType = PLong
  }

  case object String extends ScalaType {
    type PValueType = PString
  }

  case class Message(descriptor: Descriptor) extends ScalaType {
    type PValueType = PMessage
  }

  case class Enum(descriptor: EnumDescriptor) extends ScalaType {
    type PValueType = PEnum
  }

}

class DescriptorValidationException(descriptor: BaseDescriptor,
  msg: String) extends Exception(descriptor.fullName + ": " + msg)

sealed trait BaseDescriptor {
  def fullName: String
}

class PackageDescriptor private[descriptors](val fullName: String) extends BaseDescriptor {
  override def toString: String = fullName
}

class Descriptor private[descriptors](
  val fullName: String,
  val asProto: DescriptorProto,
  val containingMessage: Option[Descriptor],
  val file: FileDescriptor) extends BaseDescriptor {

  val nestedMessages: Vector[Descriptor] = asProto.nestedType.map(
    d => new Descriptor(FileDescriptor.join(fullName, d.getName), d, Some(this), file))(breakOut)

  val enums: Vector[EnumDescriptor] = asProto.enumType.map(
    d => new EnumDescriptor(FileDescriptor.join(fullName, d.getName), d, Some(this), file))(breakOut)

  lazy val fields: Vector[FieldDescriptor] = asProto.field.map(
    fd => FieldDescriptor.buildFieldDescriptor(fd, this))(breakOut)

  lazy val oneofs = asProto.oneofDecl.toVector.zipWithIndex.map {
    case (oneof, index) =>
      val oneofFields = fields.filter {
        t =>
          t.asProto.oneofIndex.isDefined && t.asProto.oneofIndex.get == index
      }
      new OneofDescriptor(FileDescriptor.join(fullName, oneof.getName), this, oneofFields, oneof)
  }

  def name: String = asProto.getName

  def findFieldByName(name: String): Option[FieldDescriptor] = fields.find(_.name == name)

  def findFieldByNumber(number: Int): Option[FieldDescriptor] = fields.find(_.number == number)

  def getOptions = asProto.getOptions

  override def toString: String = fullName
}

class EnumDescriptor private[descriptors](
  val fullName: String,
  val asProto: EnumDescriptorProto,
  val containingMessage: Option[Descriptor],
  val file: FileDescriptor) extends BaseDescriptor {

  val values: Vector[EnumValueDescriptor] =
    (asProto.value.zipWithIndex).map {
      case (v, index) => new EnumValueDescriptor(FileDescriptor.join(fullName, v.getName), this, v, index)
    }(breakOut)

  def name = asProto.getName

  def findValueByNumber(number: Int): Option[EnumValueDescriptor] = values.find(_.number == number)

  // We port the trick described here to Scala:
  // https://github.com/google/protobuf/blob/d36c0c538a545fac5d9db6ba65c525246d4efa95/java/core/src/main/java/com/google/protobuf/Descriptors.java#L1600
  // With one difference that we use an Option[Int] as key instead of java.lang.Integer.  We need to have the key
  // reachable from the EnumValueDescriptor, so we take advantage of enumValueDescriptor.proto.number which happens
  // to be Option[Int].
  private val unknownValues = new ConcurrentWeakReferenceMap[Option[Int], EnumValueDescriptor]

  def findValueByNumberCreatingIfUnknown(number: Int): EnumValueDescriptor = {
    findValueByNumber(number).getOrElse {
      val numberKey: Option[Int] = Some(number)
      unknownValues.getOrElseUpdate(numberKey, {
        val valueName = s"UNKNOWN_ENUM_VALUE_${name}_${number}"
        val proto = EnumValueDescriptorProto(name = Some(valueName), number = numberKey)
        new EnumValueDescriptor(FileDescriptor.join(fullName, "Unrecognized"), this, proto, -1)
      })
    }
  }

  def getOptions = asProto.getOptions

  override def toString: String = fullName
}

class EnumValueDescriptor private[descriptors](
  val fullName: String,
  val containingEnum: EnumDescriptor,
  val asProto: EnumValueDescriptorProto,
  val index: Int) extends BaseDescriptor {
  def number = asProto.getNumber

  def name = asProto.getName

  def isUnrecognized = (index == -1)

  override def toString: String = fullName
}

class FieldDescriptor private[descriptors](val containingMessage: Descriptor,
  val scalaType: ScalaType,
  val file: FileDescriptor,
  val asProto: FieldDescriptorProto) extends BaseDescriptor {
  def name: String = asProto.getName

  def number: Int = asProto.getNumber

  def containingOneof: Option[OneofDescriptor] = asProto.oneofIndex.map(containingMessage.oneofs)

  def isOptional = asProto.getLabel.isLabelOptional

  def isRequired = asProto.getLabel.isLabelRequired

  def isRepeated = asProto.getLabel.isLabelRepeated

  def isMapField = scalaType match {
    case ScalaType.Message(msgDesc) if isRepeated && msgDesc.asProto.getOptions.getMapEntry => true
    case _ => false
  }

  def getOptions = asProto.getOptions

  val fullName: String = FileDescriptor.join(containingMessage.fullName, name)

  def protoType = asProto.getType

  override def toString: String = fullName
}

object FieldDescriptor {
  private[descriptors] def buildFieldDescriptor(field: FieldDescriptorProto, m: Descriptor): FieldDescriptor = {
    val scalaType = field.getType match {
      case FieldDescriptorProto.Type.TYPE_BOOL => ScalaType.Boolean
      case FieldDescriptorProto.Type.TYPE_BYTES => ScalaType.ByteString
      case FieldDescriptorProto.Type.TYPE_DOUBLE => ScalaType.Double
      case FieldDescriptorProto.Type.TYPE_ENUM =>
        FileDescriptor.find(m.file, m, field.getTypeName) match {
          case Some(e: EnumDescriptor) =>
            ScalaType.Enum(e)
          case None =>
            throw new DescriptorValidationException(m, s"Could not find enum ${field.getTypeName} for field ${field.getName}")
          case Some(_) =>
            throw new DescriptorValidationException(m, s"Invalid type ${field.getTypeName} for field ${field.getName}")
        }
      case FieldDescriptorProto.Type.TYPE_FIXED32 => ScalaType.Int
      case FieldDescriptorProto.Type.TYPE_FIXED64 => ScalaType.Long
      case FieldDescriptorProto.Type.TYPE_FLOAT => ScalaType.Float
      case FieldDescriptorProto.Type.TYPE_GROUP => throw new DescriptorValidationException(m, s"Groups are not supported.")
      case FieldDescriptorProto.Type.TYPE_INT32 => ScalaType.Int
      case FieldDescriptorProto.Type.TYPE_INT64 => ScalaType.Long
      case FieldDescriptorProto.Type.TYPE_MESSAGE =>
        FileDescriptor.find(m.file, m, field.getTypeName) match {
          case Some(d: Descriptor) =>
            ScalaType.Message(d)
          case None =>
            throw new DescriptorValidationException(m, s"Could not find message ${field.getTypeName} for field ${field.getName}")
          case Some(_) =>
            throw new DescriptorValidationException(m, s"Invalid type ${field.getTypeName} for field ${field.getName}")
        }
      case FieldDescriptorProto.Type.TYPE_SFIXED32 => ScalaType.Int
      case FieldDescriptorProto.Type.TYPE_SFIXED64 => ScalaType.Long
      case FieldDescriptorProto.Type.TYPE_SINT32 => ScalaType.Int
      case FieldDescriptorProto.Type.TYPE_SINT64 => ScalaType.Long
      case FieldDescriptorProto.Type.TYPE_STRING => ScalaType.String
      case FieldDescriptorProto.Type.TYPE_UINT32 => ScalaType.Int
      case FieldDescriptorProto.Type.TYPE_UINT64 => ScalaType.Long
      case FieldDescriptorProto.Type.Unrecognized(x) => throw new DescriptorValidationException(m, s"Unrecognized type for field ${field.getName}: $x")
    }
    new FieldDescriptor(m, scalaType, m.file, field)
  }
}

class OneofDescriptor private[descriptors](
  val fullName: String,
  val containingMessage: Descriptor,
  val fields: Vector[FieldDescriptor],
  val asProto: OneofDescriptorProto) extends BaseDescriptor {
  def name: String = asProto.getName
}

class FileDescriptor private[descriptors](
  val asProto: FileDescriptorProto, dependencies: Seq[FileDescriptor]) extends BaseDescriptor {
  val messages: Vector[Descriptor] = asProto.messageType.map(
    d => new Descriptor(FileDescriptor.join(asProto.getPackage, d.getName), d, None, this))(scala.collection.breakOut)

  val enums: Vector[EnumDescriptor] = asProto.enumType.map(
    d => new EnumDescriptor(FileDescriptor.join(asProto.getPackage, d.getName), d, None, this))(scala.collection.breakOut)

  private val descriptorsByName: Map[String, BaseDescriptor] = {
    def getAllDescriptors(m: Descriptor): Vector[(String, BaseDescriptor)] =
      m.nestedMessages.flatMap(getAllDescriptors) ++
        m.enums.flatMap(getAllEnumDescriptors) :+ (m.fullName, m)

    def getAllEnumDescriptors(m: EnumDescriptor): Vector[(String, BaseDescriptor)] =
      m.values.map(v => (v.fullName, v)) :+ (m.fullName, m)

    val allDescs = FileDescriptor.nameChain(asProto.getPackage).map { f => (f, new PackageDescriptor(f)) } ++
      messages.flatMap(getAllDescriptors) ++
      enums.flatMap(getAllEnumDescriptors)

    val result = allDescs.toMap
    val keySet = result.keySet
    if (allDescs.size != result.size) {
      throw new DescriptorValidationException(
        this, s"Duplicate names found: " +
          (allDescs.map(_._1) diff keySet.toSeq).mkString(", "))
    }
    for {
      dep <- dependencies
      (name, desc) <- dep.descriptorsByName if (keySet.contains(name))
    } {
      desc match {
        case _: PackageDescriptor if result(name).isInstanceOf[PackageDescriptor] =>
        // It's fine if both files has the same package descriptor.
        case _ =>
          throw new DescriptorValidationException(this,
            s"Name already defined in '${dep.asProto.getName}': ${name}")
      }
    }
    result
  }

  // Force fields and one-ofs lazy vals to evaluate.
  descriptorsByName.values.foreach {
    case c: Descriptor =>
      c.fields
      c.oneofs
    case _ =>
  }

  def getOptions = asProto.getOptions

  def fullName: String = asProto.getName

  def packageName: String = asProto.getPackage

  private def findSymbol(name: String): Option[BaseDescriptor] = {
    descriptorsByName.get(name).orElse {
      dependencies.view.flatMap(_.findSymbol(name)).headOption
    }
  }

  def isProto3: Boolean = (asProto.getSyntax == "proto3")
}

object FileDescriptor {
  def buildFrom(proto: FileDescriptorProto, dependencies: Seq[FileDescriptor]): FileDescriptor = {
    new FileDescriptor(proto, dependencies)
  }

  private[scalapb] def join(a: String, b: String) = if (a.isEmpty) b else (a + "." + b)

  private[scalapb] def parentOf(context: String) = {
    require(context.nonEmpty)
    val dotIndex = context.lastIndexOf(".")
    if (dotIndex == -1) "" else context.substring(0, dotIndex)
  }

  @tailrec
  private[scalapb] def nameChain(fullName: String, acc: List[String] = Nil): List[String] = {
    require(!fullName.startsWith(".") && !fullName.endsWith("."))
    if (fullName.isEmpty) fullName :: acc
    else nameChain(parentOf(fullName), fullName :: acc)
  }

  /** Looks up a name in the given file and its dependencies relative to the given contenxt.
    *
    * If name starts with a dot (.) then name is considered to be a full name (and context is ignored)
    * Otherwise, name is looked inside the given context and then on each enclosing namespace.
    */
  private[descriptors] def find(file: FileDescriptor, context: BaseDescriptor, name: String): Option[BaseDescriptor] = {
    def findFirstParent(context: String, name: String): Option[BaseDescriptor] = {
      file.findSymbol(join(context, name)).orElse {
        if (context.nonEmpty) findFirstParent(parentOf(context), name) else None
      }
    }

    if (name.startsWith(".")) {
      file.findSymbol(name.substring(1))
    } else {
      val dotIndex = name.indexOf('.')
      val (baseName, suffix) = if (dotIndex == -1) (name, "") else (name.substring(0, dotIndex), name.substring(dotIndex))
      findFirstParent(context.fullName, baseName).flatMap {
        gd =>
          file.findSymbol(gd.fullName + suffix)
      }
    }
  }
}
