import java.io.{PrintWriter, File}

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll
import java.nio.file.{Path, Paths, Files}

/*
object GenerateProtos extends Properties("Proto") {
  val identifier = Gen.identifier

  /** Generates an alphanumerical character */
  def snakeIdChar = Gen.frequency((1, Gen.numChar), (1, Gen.const("_")), (9, Gen.alphaChar))

  //// String Generators ////

  /** Generates a string that starts with a lower-case alpha character,
    * and only contains alphanumerical characters */
  def snakeIdentifier: Gen[String] = (for {
    c <- Gen.alphaChar
    cs <- Gen.listOf(snakeIdChar)
  } yield (c :: cs).mkString)

  case class ProtoType(typeName: String, reference: Option[ProtoFile] = None)

  val protoSint32 = ProtoType("sint32")
  val protoUint32 = ProtoType("uint32")
  val protoInt32 = ProtoType("int32")
  val protoFixed32 = ProtoType("fixed32")
  val protoSfixed32 = ProtoType("sfixed32")
  val protoSint64 = ProtoType("sint64")
  val protoUint64 = ProtoType("uint64")
  val protoInt64 = ProtoType("int64")
  val protoFixed64 = ProtoType("fixed64")
  val protoSfixed64 = ProtoType("sfixed64")
  val protoDouble = ProtoType("double")
  val protoFloat = ProtoType("float")
  val protoBool = ProtoType("bool")
  val protoString = ProtoType("string")
  val protoBytes = ProtoType("bytes")

  object FieldOptions extends Enumeration {
    val OPTIONAL = Value("optional")
    val REQUIRED = Value("required")
    val REPEATED = Value("repeated")
  }

  type FieldOptions = FieldOptions.Value

  def genProtoType(messages: Seq[MessageReference], enums: Seq[EnumReference]): Gen[ProtoType] = Gen.frequency(
    (5, Gen.oneOf(protoSint32, protoUint32, protoInt32, protoFixed32, protoSfixed32,
      protoSint64, protoUint64, protoInt64, protoFixed64, protoSfixed64,
      protoDouble, protoFloat, protoBool, protoString, protoBytes)),
    (1, Gen.oneOf(messages).map(l => ProtoType(l.name, Some(l.file)))),
    (1, Gen.oneOf(enums).map(l => ProtoType(l.name, Some(l.file)))))

  case class MessageReference(name: String, file: ProtoFile)
  case class EnumReference(name: String, file: ProtoFile)

  case class ProtoFileSet(files: Seq[ProtoFile]) extends Printable {
    def allMessages = files.flatMap(_.allMessages)

    def allEnums = files.flatMap(_.allEnums)

    def print(printer: CodePrinter) = files.foreach {
      file =>
        printer.add("file: " + file.name)
        file.print(printer)
        printer.add("\n------------------------\n")
    }
  }

  case class ProtoFile(name: String, protoPackage: Option[String],
                       javaPackage: Option[String], messages: Seq[ProtoMessage], enums: Seq[ProtoEnum]) extends Printable {
    def allMessages: Seq[MessageReference] = {
      val pkgPrefix = protoPackage.fold("")(pkg => pkg + ".")
      messages.flatMap(_.allMessages(pkgPrefix, this))
    }

    def allEnums = {
      val pkgPrefix = protoPackage.fold("")(pkg => pkg + ".")
      messages.flatMap(_.allEnums(pkgPrefix, this)) ++
        enums.map(name => EnumReference(pkgPrefix + name, this))
    }

    def fileReferences: Set[ProtoFile] = messages.flatMap(_.fileReferences).toSet

    def print(printer: CodePrinter) = {
      println(s"writing $name")
      println(s"filerefs: ${fileReferences.map(_.name)}")
      protoPackage.foreach(pkg => printer.add(s"package $pkg;"))
      javaPackage.foreach(pkg => printer.add(s"""option java_package = "$pkg";"""))
      fileReferences
        .filter(_.name != name)
        .foreach(f => printer.add(s"""import "${f.fileName}";"""))
      printer
        .print(enums: _*)
        .print(messages: _*)
    }

    def fileName = name + ".proto"
  }

  case class ProtoEnum(name: String, values: Seq[(String, Int)]) extends Printable {
    override def print(printer: CodePrinter): Unit = {
      printer.add(s"enum $name {")
        .indent
        .add(values.map { case (s, v) => s"$s = $v;" }: _*)
        .outdent
        .add("}")
    }
  }

  case class ProtoMessage(name: String, fields: Seq[ProtoField],
                          messageTypes: Seq[ProtoMessage], enumTypes: Seq[ProtoEnum])
    extends Printable {
    def print(printer: CodePrinter) =
      printer
        .add(s"message $name {")
        .indent
        .print(enumTypes: _*)
        .print(messageTypes: _*)
        .print(fields: _*)
        .outdent
        .add("}\n")

    def allMessages(prefix: String, file: ProtoFile): Seq[MessageReference] = {
      val subPrefix = prefix + name
      messageTypes.flatMap(m => m.allMessages(subPrefix + ".", file)) :+ MessageReference(subPrefix, file)
    }

    def allEnums(prefix: String, file: ProtoFile): Seq[EnumReference] = {
      val subPrefix = prefix + name
      messageTypes.flatMap(m => m.allEnums(subPrefix + ".", file)) ++
        enumTypes.map(e => EnumReference(subPrefix + "." + e.name, file))
    }

    def noConflicts: Boolean = {
      val symbols = fields.map(_.name) ++ messageTypes.map(_.name) ++ enumTypes.map(_.name)
      symbols.distinct == symbols
    }

    def fileReferences: Set[ProtoFile] = fields.flatMap(_.fieldType.reference).toSet ++
        messageTypes.flatMap(_.fileReferences)
  }

  def genListOfDistinctPositiveNumbers(size: Int) = Gen.parameterized {
    params =>
      Gen.listOfN(size, Gen.chooseNum(1, 10)).map(_.scan(0)(_ + _).tail).map(params.rng.shuffle[Int, Seq])
  }

  case class ProtoField(name: String, fieldType: ProtoType, options: FieldOptions, tagNumber: Int)
    extends Printable {
    def print(printer: CodePrinter) =
      printer.add(s"$options ${fieldType.typeName} $name = $tagNumber;")
  }

  def genShallowMessage(name: String, depth: Int = 0): Gen[ProtoMessage] = for {
    typesNames <- if (depth < 2) Gen.listOf(identifier) else Gen.const(Nil)
    types <- Gen.sequence[Seq, ProtoMessage](typesNames.map(l => genShallowMessage(l, depth + 1)))
    enums <- Gen.listOf(genEnum)
  } yield ProtoMessage(name, Nil, types, enums)

  def genShallowProtoFile: Gen[ProtoFile] = for {
    name <- identifier
    protoPackage <- Gen.option(identifier)
    javaPackage <- Gen.option(Gen.listOf(identifier).map(_.mkString(".")))
    messages <- Gen.listOf(genShallowMessage(0))
    enums <- Gen.listOf(genEnum)
  } yield ProtoFile(name, protoPackage, javaPackage, messages, enums)

  def genShallowProtoFileSet: Gen[ProtoFileSet] = {
    Gen.sized {
      sz =>
        println(sz)
        for {
          pf <- Gen.resize(7, Gen.listOf(genShallowProtoFile))
        } yield ProtoFileSet(pf)
    }
  }

  def genFieldOptions = Gen.oneOf(FieldOptions.OPTIONAL, FieldOptions.REQUIRED, FieldOptions.REPEATED)

  def genField(messages: Seq[MessageReference], enums: Seq[EnumReference]): Gen[ProtoField] = for {
    name <- snakeIdentifier
    fieldType <- genProtoType(messages, enums)
    fieldOptions <- genFieldOptions
  } yield ProtoField(name, fieldType, fieldOptions, 1)

  def genEnum: Gen[ProtoEnum] = for {
    name <- snakeIdentifier
    names <- Gen.nonEmptyListOf(identifier).map(_.map(_ + "_" + name)).map(_.distinct)
    values <- genListOfDistinctPositiveNumbers(names.size)
  } yield ProtoEnum(name, names zip values)

  def genCompleteProtoFileSet(fileSet: ProtoFileSet): Gen[ProtoFileSet] = Gen.sized {
    sz =>
      println("complete: " + sz)
    val allMessages = fileSet.allMessages
    val allEnums = fileSet.allEnums

    def genCompleteProtoFile(file: ProtoFile): Gen[ProtoFile] =
      Gen.sequence[Seq, ProtoMessage](file.messages.map(genCompleteProtoMessage)).map(msgs => file.copy(messages = msgs))

    def genCompleteProtoMessage(msg: ProtoMessage): Gen[ProtoMessage] = (for {
      newFields <- Gen.listOf(genField(allMessages, allEnums))
      fieldNumbers <- genListOfDistinctPositiveNumbers(newFields.size)
      newSubmessages <- Gen.sequence[Seq, ProtoMessage](msg.messageTypes.map(genCompleteProtoMessage))
    } yield msg.copy(
        fields = (newFields zip fieldNumbers).map(
          fn => fn._1.copy(tagNumber = fn._2)),
        messageTypes = newSubmessages)).suchThat(_.noConflicts)

    Gen.sequence[Seq, ProtoFile](fileSet.files.map(genCompleteProtoFile)).map(
      newFiles => fileSet.copy(files = newFiles))
  }

  def genProtoFileSet: Gen[ProtoFileSet] =
    genShallowProtoFileSet.flatMap(genCompleteProtoFileSet)


  /*
  var z = 0
  property("startsWith") = forAll(genProtoFileSet) { fileSet =>
    writeFileSet(fileSet, z)
    println("done " + z)
    z+=1
    true
  }
  */
}
*/
