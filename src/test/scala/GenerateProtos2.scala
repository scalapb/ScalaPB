import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path}

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

import scala.collection.mutable

/*
object FileGraph {
  case class FileSetNode(files: Seq[FileNode])

  case class FileNode(messages: Seq[MessageNode], enums: Seq[EnumNode])

  case class EnumNode(values: Int)

  case class MessageNode(messages: Seq[MessageNode], enums: Seq[EnumNode], fields: Int)

  def genMessageNode(depth: Int = 0): Gen[MessageNode] = for {
    messages <- if (depth <= 2) Gen.listOf(genMessageNode(depth + 1)) else Gen.const(Seq.empty)
    enums <- Gen.listOf(genEnum)
    fields <- Gen.posNum
  } yield MessageNode(messages, enums, fields)

  def genEnum: Gen[EnumNode] = for {
    values <- Gen.posNum
  } yield EnumNode(values)

  def genFileNode: Gen[FileNode] = for {
    messages <- Gen.listOf(genMessageNode(0))
    enums <- Gen.listOf(genEnum)
  } yield FileNode(messages, enums)

  def getFileSet: Gen[FileSetNode] = for {
    files <- Gen.nonEmptyListOf(genFileNode)
  } yield FileSetNode(files)
}
*/

object GenerateProtos2 extends Properties("Proto") {

  val identifier = Gen.resize(2, Gen.identifier)

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

  def PRIMITIVE_GEN = Gen.oneOf(protoSint32, protoUint32, protoInt32, protoFixed32, protoSfixed32,
    protoSint64, protoUint64, protoInt64, protoFixed64, protoSfixed64,
    protoDouble, protoFloat, protoBool, protoString, protoBytes)

  def genProtoType(messages: Seq[MessageReference], enums: Seq[EnumReference]): Gen[ProtoType] = {
    val baseFreq = List((5, PRIMITIVE_GEN))
    val withMessages = if (messages.nonEmpty)
      (1, Gen.oneOf(messages).map(l => ProtoType(l.name, l.file))) :: baseFreq
    else baseFreq
    val withEnums = if (enums.nonEmpty)
      (1, Gen.oneOf(enums).map(l => ProtoType(l.name, l.file))) :: withMessages
    else withMessages
    Gen.frequency(withEnums: _*)
  }

  case class MessageReference(name: String, file: Option[ProtoFile])

  case class EnumReference(name: String, file: Option[ProtoFile])

  case class ProtoFile(name: String, protoPackage: Option[String],
                       javaPackage: Option[String],
                       messages: Vector[ProtoMessage],
                       enums: Vector[ProtoEnum]) extends Printable {
    lazy val allMessages: Vector[MessageReference] = {
      val pkgPrefix = protoPackage.fold("")(pkg => pkg + ".")
      messages.flatMap(_.allMessages(pkgPrefix, this))
    }

    lazy val allEnums: Vector[EnumReference] = {
      val pkgPrefix = protoPackage.fold("")(pkg => pkg + ".")
      messages.flatMap(_.allEnums(pkgPrefix, this)) ++
        enums.map(enum => EnumReference(pkgPrefix + enum.name, Some(this)))
    }

    def fileReferences: Set[String] = messages.flatMap(_.fileReferences).toSet

    def print(printer: CodePrinter) = {
      protoPackage.foreach(pkg => printer.add(s"package $pkg;"))
      javaPackage.foreach(pkg => printer.add( s"""option java_package = "$pkg";"""))
      fileReferences
        .filter(_ != fileName)
        .foreach(f => printer.add( s"""import "${f}";"""))
      printer
        .print(enums: _*)
        .print(messages: _*)
    }

    def topLevelNames: Vector[String] = enums.flatMap {
      enum => enum.values.map(_._1) :+ enum.name
    } ++ messages.map(_.name)

    def fileName = name + ".proto"
  }

  def genProtoFile(name: String, existingFiles: Seq[ProtoFile]): Gen[ProtoFile] = {
    val existingTopLevelNames = existingFiles.flatMap { ef =>
      ef.protoPackage match {
        case Some(pkg) => Vector(pkg)
        case None => ef.topLevelNames
      }
    }
    val namesGen = nonRepeating(identifier, existingTopLevelNames)
    for {
      protoPackage <- Gen.option(namesGen)
      javaPackage <- Gen.option(Gen.resize(4, Gen.listOf(identifier)).map(_.mkString(".")))
      typeNames <- Gen.resize(12, Gen.listOf(namesGen))
      enumNames <- Gen.resize(12, Gen.listOf(namesGen))
      baseName = protoPackage.fold("")(pkg => pkg + ".")
      newMessageRefs = (existingFiles.flatMap(_.allMessages) ++
        typeNames.map(t => MessageReference(t, None))).toVector
      newEnumRefs = (existingFiles.flatMap(_.allEnums) ++ enumNames.map(t => EnumReference(t, None))).toVector
      enums <- Gen.sequence[Vector, ProtoEnum](enumNames.map(l => genEnum(l, namesGen)))
      avoidInner = existingTopLevelNames ++
        typeNames ++
        enumNames ++ enums.flatMap(_.values.map(_._1)) ++ protoPackage.toSeq
      types <- Gen.sequence[Vector, ProtoMessage](typeNames.map(l => genMessage(l, baseName + l,
        newMessageRefs, newEnumRefs, avoidInner, 0)))
    } yield ProtoFile(name, protoPackage, javaPackage, types, enums)
  }

  case class ProtoEnum(name: String, values: Seq[(String, Int)]) extends Printable {
    override def print(printer: CodePrinter): Unit = {
      printer.add(s"enum $name {")
        .indent
        .add(values.map { case (s, v) => s"$s = $v;"}: _*)
        .outdent
        .add("}")
    }
  }

  def nonRepeating[A](gen: Gen[A], avoid: Traversable[A] = Nil): Gen[A] = {
    val seen: mutable.Set[A] = mutable.Set.empty[A] ++ avoid
    gen.retryUntil(x => !seen.contains(x)).map {
      t =>
        seen += t
        t
    }
  }

  def genListOfDistinctPositiveNumbers(size: Int) = Gen.parameterized {
    params =>
      Gen.listOfN(size, Gen.chooseNum(1, 10)).map(_.scan(0)(_ + _).tail).map(params.rng.shuffle[Int, Seq])
  }

  def genEnum(name: String, namesGen: Gen[String]): Gen[ProtoEnum] = for {
    names <- Gen.resize(10, Gen.nonEmptyListOf(namesGen))
    values <- genListOfDistinctPositiveNumbers(names.size)
  } yield ProtoEnum(name, names zip values)


  def genFieldOptions = Gen.oneOf(FieldOptions.OPTIONAL, FieldOptions.REQUIRED, FieldOptions.REPEATED)

  case class ProtoField(name: String, fieldType: ProtoType, options: FieldOptions, tagNumber: Int)
    extends Printable {
    def print(printer: CodePrinter) =
      printer.add(s"$options ${fieldType.typeName} $name = $tagNumber;")
  }

  def genField(name: String, tag: Int,
               messages: Seq[MessageReference],
               enums: Seq[EnumReference]): Gen[ProtoField] = for {
    fieldType <- genProtoType(messages, enums)
    fieldOptions <- genFieldOptions
  } yield ProtoField(name, fieldType, fieldOptions, tag)

  def genMessage(name: String,
                 fullName: String,
                 existingMessages: Vector[MessageReference],
                 existingEnums: Vector[EnumReference],
                 namesToAvoid: Seq[String],
                 depth: Int = 0): Gen[ProtoMessage] = {
    val names = nonRepeating(identifier, namesToAvoid :+ name)
    for {
      typeNames <- if (depth < 2) Gen.resize(3, Gen.listOf(names)) else Gen.const(Nil)
      enumNames <- Gen.resize(3, Gen.listOf(names))
      newMessageRefs = existingMessages ++ typeNames.map(t => MessageReference(fullName + "." + t, None))
      newEnumRefs = existingEnums ++ enumNames.map(t => EnumReference(fullName + "." + t, None))
      enums <- Gen.sequence[Seq, ProtoEnum](enumNames.map(l => genEnum(l, names)))

      namesToAvoidInner = namesToAvoid ++ typeNames ++ enumNames ++ enums.flatMap(_.values.map(_._1)) :+ name

      types <- Gen.sequence[Seq, ProtoMessage](typeNames.map(l => genMessage(l, fullName + "." + l,
        newMessageRefs, newEnumRefs, namesToAvoidInner, depth + 1)))
      fieldCount <- Gen.posNum[Int]
      fieldTags <- genListOfDistinctPositiveNumbers(fieldCount)
      fieldNames <- Gen.listOfN(fieldCount, names)
      fieldGens = (fieldTags zip fieldNames) map {
        case (tag, name) => genField(name, tag, newMessageRefs, newEnumRefs)
      }
      fields <- Gen.sequence[Seq, ProtoField](fieldGens)
    } yield ProtoMessage(name, types, enums, fields)
  }

  case class ProtoMessage(name: String, messageTypes: Seq[ProtoMessage],
                          enumTypes: Seq[ProtoEnum], fields: Seq[ProtoField]) extends Printable {
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
      messageTypes.flatMap(m => m.allMessages(subPrefix + ".", file)) :+ MessageReference(subPrefix, Some(file))
    }

    def allEnums(prefix: String, file: ProtoFile): Seq[EnumReference] = {
      val subPrefix = prefix + name
      messageTypes.flatMap(m => m.allEnums(subPrefix + ".", file)) ++
        enumTypes.map(e => EnumReference(subPrefix + "." + e.name, Some(file)))
    }

    def fileReferences: Set[String] = fields.flatMap(_.fieldType.reference.map(_.fileName)).toSet ++
      messageTypes.flatMap(_.fileReferences)
  }

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

  def genProtoFileSet: Gen[ProtoFileSet] = {
    val namesGen = nonRepeating(identifier)

    def genProtoFiles(names: List[String], acc: List[ProtoFile]): Gen[Seq[ProtoFile]] = {
      names match {
        case Nil => acc
        case name :: rest =>
          genProtoFile(name, acc).flatMap {
            protoFile => genProtoFiles(rest, protoFile :: acc)
          }
      }
    }

    for {
      count <- Gen.resize(7, Gen.posNum[Int])
      names <- Gen.listOfN(count, namesGen)
      protoFiles <- genProtoFiles(names, acc = List())
    } yield ProtoFileSet(protoFiles)
  }

  def writeFileSet(fs: ProtoFileSet, size: Int) = {
    val tmpDir: Path = Files.createTempDirectory(s"set_${size}_")
    val tmpDirAbsolute = tmpDir.toAbsolutePath.toString
    fs.files.foreach {
      protoFile =>
        val file = new File(tmpDir.toFile, protoFile.fileName)
        val pw = new PrintWriter(file)
        val cp = new CodePrinter
        cp.print(protoFile)
        pw.write(cp.toString)
        pw.close()
    }
    import scala.sys.process._
    fs.files.foreach {
      protoFile =>
        val file = new File(tmpDir.toFile, protoFile.fileName)
        println(file.getAbsolutePath)
        val cmd = Seq("protoc", file.getAbsolutePath, "--proto_path", tmpDirAbsolute, "--java_out", tmpDirAbsolute)
        try {
          cmd.!!
        } catch {
          case _ => System.exit(1)
        }
    }
  }

  var z = 0
  property("startsWith") = forAll(genProtoFileSet) { fileSet =>
    writeFileSet(fileSet, z)
    println("done " + z)
    z += 1
    true
  }
}

