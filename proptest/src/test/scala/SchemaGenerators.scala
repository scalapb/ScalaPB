import java.io.{File, PrintWriter}
import java.net.{URL, URLClassLoader}
import java.nio.file.Files
import javax.tools.ToolProvider

import com.google.protobuf.Message.Builder
import scalapb.compiler._
import org.scalacheck.Gen
import scalapb._
import protocbridge.ProtocBridge

import scala.reflect.ClassTag
import _root_.scalapb.ScalaPbCodeGenerator

object SchemaGenerators {

  import Nodes._

  val snakeRegex = "_[0-9][a-z]".r

  val RESERVED = Seq(
    // JAVA_KEYWORDS
    "abstract",
    "continue",
    "for",
    "new",
    "switch",
    "assert",
    "default",
    "goto",
    "package",
    "synchronized",
    "boolean",
    "copy",
    "do",
    "if",
    "private",
    "this",
    "break",
    "double",
    "implements",
    "protected",
    "throw",
    "byte",
    "else",
    "import",
    "public",
    "throws",
    "case",
    "enum",
    "instanceof",
    "return",
    "transient",
    "catch",
    "extends",
    "int",
    "short",
    "try",
    "char",
    "final",
    "interface",
    "static",
    "void",
    "class",
    "finally",
    "long",
    "strictfp",
    "volatile",
    "const",
    "float",
    "native",
    "super",
    "while",
    // From scala.Product
    "productArity",
    "productElementName",
    "productElementNames",
    "productIterator",
    "productPrefix",
    // Java object methods
    "clone",
    "equals",
    "finalize",
    "getclass",
    "hashcode",
    "notify",
    "notifyall",
    "tostring",
    "wait",
    // Other java stuff
    "true",
    "false",
    "null",
    // Package names
    "java",
    "com",
    "google",
    // Scala
    "ne",
    "eq",
    "val",
    "var",
    "def",
    "any",
    "map",
    "nil",
    "seq",
    "type",
    // Words that are not allowed by the Java protocol buffer compiler:
    "tag",
    // internal names
    "of",
    "java_pb_source",
    "scala_pb_source",
    "pb_byte_array_source",
    "get",
    "set",
    "compose"
  )

  // identifier must not have be of the Java keywords.
  val identifier = Gen.resize(4, Gen.identifier).retryUntil(e => !RESERVED.contains(e))

  /** Generates an alphanumerical character */
  def snakeIdChar = Gen.frequency((1, Gen.numChar), (1, Gen.const("_")), (9, Gen.alphaChar))

  //// String Generators ////

  /** Generates a string that starts with a lower-case alpha character,
    * and only contains alphanumerical characters */
  def snakeIdentifier: Gen[String] =
    (for {
      c  <- Gen.alphaChar
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
    rootNode.files.foreach { fileNode =>
      val file = new File(tmpDir, fileNode.baseFileName + ".proto")
      val pw   = new PrintWriter(file)
      pw.write(fileNode.print(rootNode, FunctionalPrinter()).result())
      pw.close()
    }
    tmpDir
  }

  private def runProtoc(args: String*) =
    ProtocBridge.runWithGenerators(
      args => com.github.os72.protocjar.Protoc.runProtoc("-v370" +: args.toArray),
      Seq("scala" -> ScalaPbCodeGenerator),
      args
    )

  def compileProtos(rootNode: RootNode, tmpDir: File): Unit = {
    val files = rootNode.files.map { fileNode =>
      val file = new File(tmpDir, fileNode.baseFileName + ".proto")
      println(file.getAbsolutePath)
      file.getAbsolutePath
    }
    val args = Seq(
      "--proto_path",
      (tmpDir.toString + ":protobuf:third_party"),
      "--java_out",
      tmpDir.toString,
      "--scala_out",
      "grpc,java_conversions:" + tmpDir.toString
    ) ++ files
    runProtoc(args: _*)
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
           else Stream.empty)

  def jarForClass[T](implicit c: ClassTag[T]): URL =
    c.runtimeClass.getProtectionDomain.getCodeSource.getLocation

  def compileJavaInDir(rootDir: File): Unit = {
    println("Compiling Java sources.")
    val protobufJar = Seq(
      jarForClass[com.google.protobuf.Message].getPath,
      jarForClass[scalapb.options.Scalapb].getPath
    )

    val compiler = ToolProvider.getSystemJavaCompiler()
    getFileTree(rootDir)
      .filter(f => f.isFile && f.getName.endsWith(".java"))
      .foreach { file =>
        if (compiler.run(
              null,
              null,
              null,
              "-sourcepath",
              rootDir.toString,
              "-cp",
              protobufJar.mkString(":"),
              "-d",
              rootDir.toString,
              file.getAbsolutePath
            ) != 0) {
          throw new RuntimeException(s"Compilation of $file failed.")
        }
      }
  }

  def compileScalaInDir(rootDir: File): Unit = {
    print("Compiling Scala sources. ")
    val classPath = Seq(
      jarForClass[annotation.Annotation].getPath,
      jarForClass[scalapb.GeneratedMessage].getPath,
      jarForClass[scalapb.options.Scalapb].getPath,
      jarForClass[scalapb.grpc.Grpc.type].getPath,
      jarForClass[com.google.protobuf.Message].getPath,
      jarForClass[io.grpc.Channel].getPath,
      jarForClass[io.grpc.stub.AbstractStub[_]].getPath,
      jarForClass[io.grpc.protobuf.ProtoFileDescriptorSupplier].getPath,
      jarForClass[com.google.common.util.concurrent.ListenableFuture[_]],
      jarForClass[javax.annotation.Nullable],
      jarForClass[scalapb.lenses.Lens[_, _]].getPath,
      jarForClass[fastparse.core.Parsed[_, _, _]].getPath,
      rootDir
    )
    val annotationJar =
      classOf[annotation.Annotation].getProtectionDomain.getCodeSource.getLocation.getPath
    import scala.tools.nsc._

    val scalaFiles = getFileTree(rootDir)
      .filter(f => f.isFile && f.getName.endsWith(".scala"))
    val s = new Settings(error => throw new RuntimeException(error))
    val maybeBreakCycles =
      if (!scala.util.Properties.versionNumberString.startsWith("2.10."))
        "-Ybreak-cycles"
      else ""
    s.processArgumentString(
      s"""-cp "${classPath.mkString(":")}" ${maybeBreakCycles} -d "$rootDir""""
    )
    val g = new Global(s)

    val run = new g.Run
    run.compile(scalaFiles.map(_.toString).toList)
    println("[DONE]")
  }

  type CompanionWithJavaSupport[A <: GeneratedMessage with Message[A]] =
    GeneratedMessageCompanion[A] with JavaProtoSupport[A, _]

  case class CompiledSchema(rootNode: RootNode, rootDir: File) {
    lazy val classLoader =
      URLClassLoader.newInstance(Array[URL](rootDir.toURI.toURL), this.getClass.getClassLoader)

    def javaBuilder(m: MessageNode): Builder = {
      val className = rootNode.javaClassName(m)
      val cls       = Class.forName(className, true, classLoader)
      val builder   = cls.getMethod("newBuilder").invoke(null).asInstanceOf[Builder]
      builder
    }

    def javaParse(m: MessageNode, bytes: Array[Byte]): com.google.protobuf.Message = {
      val className = rootNode.javaClassName(m)
      val cls       = Class.forName(className, true, classLoader)
      cls
        .getMethod("parseFrom", classOf[Array[Byte]])
        .invoke(null, bytes)
        .asInstanceOf[com.google.protobuf.Message]
    }

    def scalaObject(m: MessageNode): CompanionWithJavaSupport[_ <: GeneratedMessage] = {
      val className = rootNode.scalaObjectName(m)
      val u         = scala.reflect.runtime.universe
      val mirror    = u.runtimeMirror(classLoader)
      mirror
        .reflectModule(mirror.staticModule(className))
        .instance
        .asInstanceOf[CompanionWithJavaSupport[_ <: GeneratedMessage]]
    }
  }

  def genCompiledSchema: Gen[CompiledSchema] =
    GraphGen.genRootNode.map { rootNode =>
      val tmpDir = writeFileSet(rootNode)
      println(s"Compiling in $tmpDir.")
      compileProtos(rootNode, tmpDir)
      compileJavaInDir(tmpDir)
      compileScalaInDir(tmpDir)

      CompiledSchema(rootNode, tmpDir)
    }
}
