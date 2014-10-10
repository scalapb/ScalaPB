import java.io.{File, PrintWriter}
import java.net.{URL, URLClassLoader}
import java.nio.file.Files
import javax.tools.ToolProvider

import com.google.protobuf
import com.google.protobuf.Message.Builder
import com.google.protobuf.{TextFormat}
import com.trueaccord.scalapb.compiler.FunctionalPrinter
import org.scalacheck.Prop.{forAll, forAllNoShrink}
import org.scalacheck.{Gen, Properties}
import com.trueaccord.scalapb.{GeneratedMessage, compiler, GeneratedMessageCompanion}

object GenerateProtos extends Properties("Proto") {

  import Nodes._

  val snakeRegex = "_[0-9][a-z]".r


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

    // Java object methods
    "clone", "equals", "finalize", "getclass", "hashcode", "notify",
    "notifyall", "tostring", "wait",

    // Other java stuff
    "true", "false", "null",

    // Package names
    "java", "com", "google",

    // Scala
    "ne", "eq", "val", "var", "def", "Nil",

    // internal names
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

  def number[T](implicit num: Numeric[T], c: Gen.Choose[T]): Gen[T] = {
    import num._
    Gen.sized(max => c.choose(-fromInt(max), fromInt(max)))
  }

  def escapeString(raw: String): String = {
    import scala.reflect.runtime.universe._
    Literal(Constant(raw)).toString
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
    val files = rootNode.files.map {
      fileNode =>
        val file = new File(tmpDir, fileNode.baseFileName + ".proto")
        println(file.getAbsolutePath)
        file.getAbsolutePath
    }
    val args = Seq("--proto_path",
      tmpDir.toString,
      "--java_out", tmpDir.toString,
      "--scala_out", tmpDir.toString) ++ files
    compiler.Process.runProtoc(args: _*)
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

  def getJavaBuilder(rootDir: File, rootNode: RootNode, m: MessageNode): Builder = {
    val classLoader = URLClassLoader.newInstance(Array[URL](rootDir.toURI.toURL))
    val className = rootNode.javaClassName(m)
    val cls = Class.forName(className, true, classLoader)
    val builder = cls.getMethod("newBuilder").invoke(null).asInstanceOf[Builder]
    builder
  }

  def getScalaObject(rootDir: File, rootNode: RootNode, m: MessageNode): GeneratedMessageCompanion[_ <: GeneratedMessage] = {
    val classLoader = URLClassLoader.newInstance(Array[URL](rootDir.toURI.toURL))
    val className = rootNode.scalaObjectName(m)
    val u = scala.reflect.runtime.universe
    val mirror = u.runtimeMirror(classLoader)
    mirror.reflectModule(mirror.staticModule(className)).instance.asInstanceOf[GeneratedMessageCompanion[_ <: GeneratedMessage]]
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

  def rootNodeMessageAndAscii: Gen[(RootNode, MessageNode, String)] =
    for {
      rootNode <- GraphGen.genRootNode
      (msg, ascii) <- GenData.genProtoAsciiInstance(rootNode)
    } yield (rootNode, msg, ascii)

  def scalaParseAndSerialize[T <: GeneratedMessage](comp: GeneratedMessageCompanion[T], bytes: Array[Byte]) = {
    val instance: T = comp.parseFrom(bytes)
    val ser: Array[Byte] = comp.toByteArray(instance)
    ser
  }

  property("protos compile") =
    forAll(GraphGen.genRootNode) {
      case rootNode =>
        val tmpDir = writeFileSet(rootNode)
        println(tmpDir)
        compileProtos(rootNode, tmpDir)
        compileJavaInDir(tmpDir)
        compileScalaInDir(tmpDir)
        forAllNoShrink(GenData.genProtoAsciiInstance(rootNode)) {
          case (message, protoAscii) =>
            val builder = getJavaBuilder(tmpDir, rootNode, message)
            TextFormat.merge(protoAscii, builder)
            val originalProto: protobuf.Message = builder.build()
            val javaBytes = originalProto.toByteArray
            val obj: GeneratedMessageCompanion[_ <: GeneratedMessage] = getScalaObject(tmpDir, rootNode, message)
            val scalaBytes = scalaParseAndSerialize(obj, javaBytes)
            val updatedProto: protobuf.Message = getJavaBuilder(tmpDir, rootNode, message).mergeFrom(scalaBytes).build
            updatedProto.toByteString == originalProto.toByteString
        }
    }
}

