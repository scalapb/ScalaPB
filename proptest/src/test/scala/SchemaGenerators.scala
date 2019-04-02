import java.io.{File, PrintWriter}
import java.net.{URL, URLClassLoader}
import java.nio.file.{Files, Paths}

import com.google.protobuf.Message.Builder
import org.scalacheck.Gen
import scalapb._
import scalapb.compiler.FunctionalPrinter

import scala.reflect.ClassTag

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

  private def writeFile(f: File, content: String): Unit = {
    val pw = new PrintWriter(f)
    pw.write(content)
    pw.close()
  }

  def writeFileSet(rootNode: RootNode) = {
    val baseDir    = Files.createTempDirectory(s"set_").toFile.getAbsoluteFile
    val protosDir  = Paths.get(baseDir.getAbsolutePath, "src", "main", "protobuf").toFile
    val projectDir = Paths.get(baseDir.getAbsolutePath, "project").toFile
    protosDir.mkdirs()
    projectDir.mkdirs()
    rootNode.files.foreach { fileNode =>
      val file = new File(protosDir, fileNode.baseFileName + ".proto")
      writeFile(file, fileNode.print(rootNode, FunctionalPrinter()).result())
    }
    writeFile(
      Paths.get(projectDir.toString, "plugins.sbt").toFile,
      s"""
         |addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.20")
         |libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "${scalapb.compiler.Version.scalapbVersion}"
      """.stripMargin
    )
    writeFile(
      Paths.get(baseDir.toString, "build.sbt").toFile,
      s"""
         |name := "testproj"
         |
         |scalaVersion := "${scala.util.Properties.versionNumberString}"
         |
         |classDirectory in Compile := baseDirectory.value / "out"
         |
         |compileOrder := CompileOrder.JavaThenScala
         |
         |scalacOptions in ThisBuild ++= Seq("-Ybreak-cycles")
         |
         |updateOptions := updateOptions.value.withLatestSnapshots(false)
         |
         |PB.targets in Compile := Seq(
         |   PB.gens.java -> (sourceManaged in Compile).value,
         |   scalapb.gen(javaConversions=true, grpc=true) -> (sourceManaged in Compile).value
         |)
         |
         |libraryDependencies ++= Seq(
         |  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
         |  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
         |)
      """.stripMargin
    )
    baseDir
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
           else Stream.empty)

  def jarForClass[T](implicit c: ClassTag[T]): URL =
    c.runtimeClass.getProtectionDomain.getCodeSource.getLocation

  type CompanionWithJavaSupport[A <: GeneratedMessage with Message[A]] =
    GeneratedMessageCompanion[A] with JavaProtoSupport[A, _]

  case class CompiledSchema(rootNode: RootNode, rootDir: File) {
    lazy val classLoader =
      URLClassLoader.newInstance(
        Array[URL](Paths.get(rootDir.toString, "out").toFile.toURI.toURL),
        this.getClass.getClassLoader
      )

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
      try {
        val res = sys.process.Process(Seq("sbt", "-J-Xmx2G", "compile"), tmpDir).!
        if (res != 0) throw new RuntimeException("sub-project sbt failed")
      } catch {
        case e: Exception =>
          sys.process
            .Process(
              Seq("tar", "czf", "/tmp/protos.tgz", "--exclude=*/target", "--exclude=./out", "."),
              tmpDir
            )
            .!!
          throw e
      }

      CompiledSchema(rootNode, tmpDir)
    }
}
