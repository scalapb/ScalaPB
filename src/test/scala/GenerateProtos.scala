import java.io.{File, PrintWriter}
import java.net.{URL, URLClassLoader}
import java.nio.file.Files
import javax.tools.ToolProvider

import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties}

import scala.collection.mutable

object GenerateProtos extends Properties("Proto") {

  import Nodes._

  val snakeRegex = "_[0-9][a-z]".r

  def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new mutable.StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2) = name.head match {
        case c if c.isLower => (Some(if (capNext) c.toUpper else c), false)
        case c if c.isUpper =>
          // force first letter to lower unless forced to capitalize it.
          (Some(if (index == 0 && !capNext) c.toLower else c), false)
        case c if c.isDigit => (Some(c), true)
        case _ => (None, true)
      }
      r.foreach(b +=)
      inner(name.tail, index + 1, capNext2)
    }
    inner(name, 0, upperInitial)
    b.toString()
  }


  val RESERVED = Seq(
    // JAVA_KEYWORDS
    "abstract", "continue", "for", "new", "switch",
    "assert", "default", "goto", "package", "synchronized",
    "boolean", "do", "if", "private", "this",
    "break", "double", "implements", "protected", "throw",
    "byte", "else", "import", "public", "throws",
    "case", "enum", "instanceof", "return", "transient",
    "catch", "extends", "int", "short", "try", "char", "final",
    "interface", "static", "void", "class", "finally",
    "long", "strictfp", "volatile", "const", "float",
    "native", "super", "while",

    // Package names
    "java", "com", "google",

    // Scala
    "ne",

    // internal namess
    "java_pb_source", "scala_pb_source", "pb_byte_array_source"
  )

  // identifier must not have be of the Java keywords.
  val identifier = Gen.resize(4, Gen.identifier).retryUntil(e => !RESERVED.contains(e))

  /** Generates an alphanumerical character */
  def snakeIdChar = Gen.frequency((1, Gen.numChar), (1, Gen.const("_")), (9, Gen.alphaChar))

  //// String Generators ////

  /** Generates a string that starts with a lower-case alpha character,
    * and only contains alphanumerical characters */
  def snakeIdentifier: Gen[String] = (for {
    c <- Gen.alphaChar
    cs <- Gen.listOf(snakeIdChar)
  } yield (c :: cs).mkString)

  case class ProtoType(typeName: String, reference: Option[ProtoFile] = None,
                       isMessage: Boolean = false, isEnum: Boolean = false)

  def number[T](implicit num: Numeric[T], c: Gen.Choose[T]): Gen[T] = {
    import num._
    Gen.sized(max => c.choose(-fromInt(max), fromInt(max)))
  }

  def escapeString(raw: String): String = {
    import scala.reflect.runtime.universe._
    Literal(Constant(raw)).toString
  }

  val ProtoSint32 = ProtoType("sint32")
  val ProtoUint32 = ProtoType("uint32")
  val ProtoInt32 = ProtoType("int32")
  val ProtoFixed32 = ProtoType("fixed32")
  val ProtoSfixed32 = ProtoType("sfixed32")
  val ProtoSint64 = ProtoType("sint64")
  val ProtoUint64 = ProtoType("uint64")
  val ProtoInt64 = ProtoType("int64")
  val ProtoFixed64 = ProtoType("fixed64")
  val ProtoSfixed64 = ProtoType("sfixed64")
  val ProtoDouble = ProtoType("double")
  val ProtoFloat = ProtoType("float")
  val ProtoBool = ProtoType("bool")
  val ProtoString = ProtoType("string")
  val ProtoBytes = ProtoType("bytes")

  val uint32Gen = Gen.chooseNum[Long](0, 0xffffffffL)
  val uint64Gen = Gen.chooseNum[Long](0, Long.MaxValue) // TODO: fix high bit

  def genMessageInstance(message: ProtoMessage, fileSet: ProtoFileSet,
                         enclose: Boolean = false, depth: Int = 0): Gen[Seq[String]] = {
    println("depth " + depth + ": " + message.name)
    def genFieldValue(field: ProtoField): Gen[String] = for {
      value <- genValueForProtoType(field.fieldType, fileSet, depth)
    } yield ("  " * depth) + field.name + ": " + value

    def genFieldValueByOptions(field: ProtoField): Gen[Seq[String]] = field.options match {
      case FieldOptions.OPTIONAL => if (depth > 3) Gen.const(Seq.empty) else Gen.resize(1, Gen.listOf(genFieldValue(field)))
      case FieldOptions.REQUIRED => Gen.listOfN(1, genFieldValue(field))
      case FieldOptions.REPEATED => if (depth > 3) Gen.const(Seq.empty) else Gen.listOf(genFieldValue(field))
    }

    Gen.sequence[Seq, String] {
      val fields = message.fields.map(genFieldValue)
      if (enclose) Seq(Gen.const("  " * depth + "<")) ++ fields ++ Seq(Gen.const("  " * depth + ">"))
      else fields
    }
  }

  def genProtoAsciiInstance(fileSet: ProtoFileSet): Gen[(MessageReference, Seq[String])] = for {
    message <- Gen.oneOf(fileSet.allMessages)
    ascii <- genMessageInstance(message.message.get, fileSet)
  } yield (message, ascii)

  def genValueForProtoType(pt: ProtoType, fileSet: ProtoFileSet, depth: Int = 0): Gen[String] = pt match {
    case ProtoSint32 => Arbitrary.arbitrary[Int].map(_.toString)
    case ProtoUint32 => uint32Gen.map(_.toString)
    case ProtoInt32 => Arbitrary.arbitrary[Int].map(_.toString)
    case ProtoFixed32 => Arbitrary.arbitrary[Int].map(_.toString)
    case ProtoSfixed32 => Arbitrary.arbitrary[Int].map(_.toString)
    case ProtoSint64 => Arbitrary.arbitrary[Long].map(_.toString)
    case ProtoUint64 => uint64Gen.map(_.toString)
    case ProtoInt64 => Arbitrary.arbitrary[Long].map(_.toString)
    case ProtoFixed64 => Arbitrary.arbitrary[Long].map(_.toString)
    case ProtoSfixed64 => Arbitrary.arbitrary[Long].map(_.toString)
    case ProtoDouble => Arbitrary.arbitrary[Double].map(_.toString)
    case ProtoFloat => Arbitrary.arbitrary[Float].map(_.toString)
    case ProtoBool => Arbitrary.arbitrary[Boolean].map(_.toString)
    case ProtoString => Arbitrary.arbitrary[String].map(escapeString)
    case ProtoBytes => Arbitrary.arbitrary[String].map(escapeString)
    case ProtoType(typeName, _, true, false) =>
      val t: Gen[String] = genMessageInstance(fileSet.messageMap(typeName).message.get, fileSet,
        enclose = true, depth + 1).map(_.mkString("\n"))
      t
    case ProtoType(typeName, _, false, true) =>
      /*
      val t: Gen[String] = genMessageInstance(fileSet.enumMap(typeName).message.get, fileSet,
        enclose = true, depth + 1).map(_.mkString("\n"))
      t
      */ Gen.const("enum")
  }

  object FieldOptions extends Enumeration {
    val OPTIONAL = Value("optional")
    val REQUIRED = Value("required")
    val REPEATED = Value("repeated")
  }

  type FieldOptions = FieldOptions.Value

  def PRIMITIVE_GEN = Gen.oneOf(ProtoSint32, ProtoUint32, ProtoInt32, ProtoFixed32, ProtoSfixed32,
    ProtoSint64, ProtoUint64, ProtoInt64, ProtoFixed64, ProtoSfixed64,
    ProtoDouble, ProtoFloat, ProtoBool, ProtoString, ProtoBytes)

  def genProtoType(messages: Seq[MessageReference], enums: Seq[EnumReference]): Gen[ProtoType] = {
    val baseFreq = List((5, PRIMITIVE_GEN))
    val withMessages = if (messages.nonEmpty)
      (1, Gen.oneOf(messages).map(l => ProtoType(l.name, l.file, isMessage = true))) :: baseFreq
    else baseFreq
    val withEnums = if (enums.nonEmpty)
      (1, Gen.oneOf(enums).map(l => ProtoType(l.name, l.file, isEnum = true))) :: withMessages
    else withMessages
    Gen.frequency(withEnums: _*)
  }

  case class MessageReference(name: String, file: Option[ProtoFile], message: Option[ProtoMessage]) {
    def javaName = file.map(_.javaPrefix).get + "." +
      name.substring(file.get.protoPackage.map(_.length + 1).getOrElse(0))

    def javaClassName = file.map(_.javaPrefix).get + "$" +
      name.substring(file.get.protoPackage.map(_.length + 1).getOrElse(0)).replace('.', '$')
  }

  case class EnumReference(name: String, file: Option[ProtoFile])

  case class ProtoFile(name: String, protoPackage: Option[String],
                       maybeJavaPackage: Option[String],
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
      maybeJavaPackage.foreach(pkg => printer.add( s"""option java_package = "$pkg";"""))
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

    def outerObjectName = snakeCaseToCamelCase(name, upperInitial = true)

    def javaPackage = maybeJavaPackage.orElse(protoPackage)

    def javaPrefix = javaPackage.fold(outerObjectName)(
      p => p + "." + outerObjectName)

    def scalaPrefix = javaPrefix + "Scala"

  }

  def genProtoFile(name: String, existingFiles: Seq[ProtoFile]): Gen[ProtoFile] = {
    val existingTopLevelNames: Seq[String] = existingFiles.flatMap { ef =>
      (ef.protoPackage match {
        case Some(pkg) => Vector(pkg)
        case None => ef.topLevelNames
      }) ++ (ef.javaPackage match {
        case Some(str) =>
          str.split('.').toSeq
        case None => Seq.empty
      })
    }
    val namesGen = nonRepeatingStringIgnoreCase(identifier, existingTopLevelNames)
    for {
      protoPackage <- Gen.option(namesGen)
      javaPackageNames <- Gen.resize(4, Gen.nonEmptyListOf(namesGen))
      javaPackage = Some(javaPackageNames mkString ".")
      typeNames <- Gen.resize(12, Gen.listOf(namesGen))
      enumNames <- Gen.resize(12, Gen.listOf(namesGen))
      baseName = protoPackage.fold("")(pkg => pkg + ".")
      newMessageRefs = (existingFiles.flatMap(_.allMessages) ++
        typeNames.map(t => MessageReference(t, None, null))).toVector
      newEnumRefs = (existingFiles.flatMap(_.allEnums) ++ enumNames.map(t => EnumReference(t, None))).toVector
      enums <- Gen.sequence[Vector, ProtoEnum](enumNames.map(l => genEnum(l, namesGen)))
      avoidInner = existingTopLevelNames ++
        typeNames ++
        javaPackageNames ++
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

  def nonRepeatingStringIgnoreCase(gen: Gen[String], avoid: Traversable[String] = Nil): Gen[String] = {
    val seen: mutable.Set[String] =
      mutable.Set.empty[String] ++ avoid.map(_.toLowerCase)
    gen.retryUntil(x => !seen.contains(x.toLowerCase)).map {
      t =>
        seen += t.toLowerCase
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


  def genFieldOptions(allowRequired: Boolean = false) = {
    if (false) Gen.oneOf(FieldOptions.OPTIONAL, FieldOptions.REQUIRED, FieldOptions.REPEATED)
    else Gen.oneOf(FieldOptions.OPTIONAL, FieldOptions.REPEATED)
  }

  case class ProtoField(name: String, fieldType: ProtoType, options: FieldOptions, tagNumber: Int)
    extends Printable {
    def print(printer: CodePrinter) =
      printer.add(s"$options ${fieldType.typeName} $name = $tagNumber;")
  }

  def genField(name: String, tag: Int,
               messages: Seq[MessageReference],
               enums: Seq[EnumReference]): Gen[ProtoField] = (for {
    fieldType <- genProtoType(messages, enums)
    fieldOptions <- genFieldOptions(allowRequired =
      // If a field is required then it must be from a previous file, otherwise we can have a cycle
      // of required objects.
      (!fieldType.isMessage && !fieldType.isEnum) || fieldType.reference.isDefined)
  } yield ProtoField(name, fieldType, fieldOptions, tag))

  def genMessage(name: String,
                 fullName: String,
                 existingMessages: Vector[MessageReference],
                 existingEnums: Vector[EnumReference],
                 namesToAvoid: Seq[String],
                 depth: Int = 0): Gen[ProtoMessage] = {
    val names = nonRepeatingStringIgnoreCase(identifier, namesToAvoid :+ name)
    for {
      typeNames <- if (depth < 2) Gen.resize(3, Gen.listOf(names)) else Gen.const(Nil)
      enumNames <- Gen.resize(3, Gen.listOf(names))
      newMessageRefs = existingMessages ++ typeNames.map(t => MessageReference(fullName + "." + t, None, None))
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
      messageTypes.flatMap(m => m.allMessages(subPrefix + ".", file)) :+
        MessageReference(subPrefix, Some(file), Some(this))
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
    lazy val allMessages: Seq[MessageReference] = files.flatMap(_.allMessages)

    lazy val allEnums = files.flatMap(_.allEnums)

    lazy val messageMap: Map[String, MessageReference] = allMessages.map(m => (m.name, m)).toMap

    lazy val enumMap: Map[String, EnumReference] = allEnums.map(m => (m.name, m)).toMap


    def print(printer: CodePrinter) = files.foreach {
      file =>
        printer.add("file: " + file.name)
        file.print(printer)
        printer.add("\n------------------------\n")
    }

    override def toString = "ProtoFileSet[" + files.map(_.fileName).mkString(", ") + "]"
  }

  def genProtoFileSet: Gen[ProtoFileSet] = {
    val namesGen = nonRepeatingStringIgnoreCase(identifier)

    def genProtoFiles(names: List[String], acc: List[ProtoFile]): Gen[Seq[ProtoFile]] = {
      names match {
        case Nil => acc
        case name :: rest =>
          genProtoFile(name, acc).flatMap {
            protoFile => genProtoFiles(rest, protoFile :: acc)
          }
      }
    }

    (for {
      count <- Gen.resize(7, Gen.posNum[Int])
      names <- Gen.listOfN(count, namesGen)
      protoFiles <- genProtoFiles(names, acc = List())
    } yield ProtoFileSet(protoFiles.reverse)).suchThat(_.files.exists(_.messages.nonEmpty))
  }

  def writeFileSet(rootNode: RootNode) = {
    val tmpDir = Files.createTempDirectory(s"set_").toFile.getAbsoluteFile
    rootNode.files.foreach {
      fileNode =>
        val file = new File(tmpDir, fileNode.baseFileName + ".proto")
        val pw = new PrintWriter(file)
        pw.write(fileNode.print(rootNode, FunctionalPrinter()).toString)
        pw.close()
    }
    tmpDir
  }

  def compileProtos(rootNode: RootNode, tmpDir: File): Unit = {
    import scala.sys.process._
    rootNode.files.foreach {
      fileNode =>
        val file = new File(tmpDir, fileNode.baseFileName + ".proto")
        println(file.getAbsolutePath)
        val cmd = Seq("protoc", file.getAbsolutePath, "--proto_path",
          tmpDir.toString,
          "--plugin=protoc-gen-scala=/home/thesamet/Development/ScalaPB/ScalaPB",
          "--java_out", tmpDir.toString,
          "--scala_out", tmpDir.toString
        )
        cmd.!!
    }
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)

  def compileJavaInDir(rootDir: File): Unit = {
    println("Compiling Java sources.")
    val compiler = ToolProvider.getSystemJavaCompiler()
    getFileTree(rootDir)
      .filter(f => f.isFile && f.getName.endsWith(".java"))
      .foreach {
      file =>
        if (compiler.run(null, null, null,
          "-sourcepath", rootDir.toString,
          "-d", rootDir.toString,
          file.getAbsolutePath) != 0) {
          throw new RuntimeException(s"Compilation of $file failed.")
        }
    }
  }

  def compileScalaInDir(rootDir: File): Unit = {
    println("Compiling Scala sources.")
    import scala.tools.nsc._

    val scalaFiles = getFileTree(rootDir)
      .filter(f => f.isFile && f.getName.endsWith(".scala"))
    val s = new Settings(error => throw new RuntimeException(error))
    s.processArgumentString( s"""-usejavacp -cp "$rootDir" -d "$rootDir"""")
    val g = new Global(s)

    val run = new g.Run
    run.compile(scalaFiles.map(_.toString).toList)
  }

  def loadClasses(fileSet: ProtoFileSet, rootDir: File): Unit = {
    val classLoader = URLClassLoader.newInstance(Array[URL](rootDir.toURI.toURL))
    val message = fileSet.allMessages.head
    val className = message.javaClassName
    val cls = Class.forName(className, true, classLoader)
    //    val builder = cls.getMethod("newBuilder").invoke(null).asInstanceOf[Builder]
    //    TextFormat.merge("", builder)
    //    println(builder.build())
    //Class<?> cls = Class.forName("test.Test", true, classLoader); // Should print "hello".
    //Object instance = cls.newInstance(); // Should print "world".
    //System.out.println(instance); // Should print "test.Test@hashcode".
    //    2
  }


  property("startsWith") =
    forAll(genProtoFileSet) {
      fileSet =>
        forAll(genProtoAsciiInstance(fileSet)) {
          protoAscii =>
            val tmpDir = writeFileSet(fileSet)
            println(tmpDir)
            compileProtos(fileSet, tmpDir)
            compileJavaInDir(tmpDir)
            compileScalaInDir(tmpDir)
            loadClasses(fileSet, tmpDir)
            println(protoAscii)
            println("----")
            println("done ")
            true
        }
    }

  property("min and max id are consecutive over files") = forAll(GraphGen.genRootNode) {
    node =>
      def validateMinMax(pairs: Seq[(Int, Int)]) =
        pairs.sliding(2).filter(_.size == 2).forall {
          case Seq((min1, max1), (min2, max2)) => min2 == max1 + 1 && min2 <= max2
        }
      val messageIdPairs: Seq[(Int, Int)] = node.files.flatMap { f => (f.minMessageId.map((_, f.maxMessageId.get)))}
      val enumIdPairs: Seq[(Int, Int)] = node.files.flatMap { f => (f.minEnumId.map((_, f.maxEnumId.get)))}
      validateMinMax(messageIdPairs) && validateMinMax(enumIdPairs)
  }

  property("protos compile") =
    forAll(GraphGen.genRootNode) {
      rootNode =>
        val tmpDir = writeFileSet(rootNode)
        println(tmpDir)
        compileProtos(rootNode, tmpDir)
        compileJavaInDir(tmpDir)
//        compileScalaInDir(tmpDir)
//        loadClasses(fileSet, tmpDir)
//        println(protoAscii)
        println("----")
        println("done ")
        true
    }
}

