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

  def loadClasses(rootNode: RootNode, rootDir: File): Unit = {
    val classLoader = URLClassLoader.newInstance(Array[URL](rootDir.toURI.toURL))
    // val message = rootNode.files.head.allMessages.head
//    val className = message.javaClassName
//    val cls = Class.forName(className, true, classLoader)
    //    val builder = cls.getMethod("newBuilder").invoke(null).asInstanceOf[Builder]
    //    TextFormat.merge("", builder)
    //    println(builder.build())
    //Class<?> cls = Class.forName("test.Test", true, classLoader); // Should print "hello".
    //Object instance = cls.newInstance(); // Should print "world".
    //System.out.println(instance); // Should print "test.Test@hashcode".
    //    2
  }


//  property("startsWith") =
//    forAll(genProtoFileSet) {
//      fileSet =>
//        forAll(genProtoAsciiInstance(fileSet)) {
//          protoAscii =>
//            val tmpDir = writeFileSet(fileSet)
//            println(tmpDir)
//            compileProtos(fileSet, tmpDir)
//            compileJavaInDir(tmpDir)
//            compileScalaInDir(tmpDir)
//            loadClasses(fileSet, tmpDir)
//            println(protoAscii)
//            println("----")
//            println("done ")
//            true
//        }
//    }

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
        forAll(GenData.genProtoAsciiInstance(rootNode)) {
          protoAscii =>
            compileProtos(rootNode, tmpDir)
            compileJavaInDir(tmpDir)
            compileScalaInDir(tmpDir)
            loadClasses(rootNode, tmpDir)
            //        println(protoAscii)
            println("----")
            println("done ")
            true
        }
    }
}

