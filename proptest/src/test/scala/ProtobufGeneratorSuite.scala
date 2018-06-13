import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import org.scalatest.FunSuite
import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.io.VirtualFile
import scala.tools.nsc
import scala.collection.JavaConverters._
import scala.tools.nsc.reporters.StoreReporter
import scalapb.compiler.ProtobufGenerator

class Compiler {
  private val settings = new nsc.Settings()
  private val reporter = new StoreReporter
  private val d        = Files.createTempDirectory("ogen")
  private val classpath =
    this.getClass.getClassLoader match {
      case u: URLClassLoader =>
        u.getURLs
          .map(u => Paths.get(u.toURI).toString)
          .mkString(File.pathSeparator)
    }
  d.toFile.deleteOnExit()
  settings.classpath.value = classpath
  settings.d.value = d.toString
  val global = new nsc.Global(settings, reporter)
  def printDiagnostic(d: reporter.Info): Unit = {
    println(s"${d.pos.source.file.name}:${d.pos.line}:${d.pos.column} error: ${d.msg}")
    println(d.pos.lineContent)
    println(d.pos.lineCaret)
  }
  private var counter = 0

  def compile(snippet: String): Int = {
    counter += 1
    val template =
      s"""
         |object Main${counter} {
         |$snippet
         |}
      """.stripMargin
    reporter.reset()
    val run    = new global.Run
    val file   = new VirtualFile("<input>")
    val source = new BatchSourceFile(file, template.toCharArray)
    run.compileSources(source :: Nil)
    val errors = reporter.infos.filter(_.severity.id == reporter.ERROR.id)
    errors.foreach(printDiagnostic)
    errors.size
  }

  def compile(response: CodeGeneratorResponse): Int = {
    reporter.reset()
    val files = response.getFileList.asScala.map { file =>
      val virtualFile = new VirtualFile(file.getName)
      new BatchSourceFile(virtualFile, file.getContent.toCharArray)
    }
    val run = new global.Run
    println(s"Compiling ${files.length} sources")
    run.compileSources(files.toList)
    reporter.infos.find(_.severity.id == reporter.ERROR.id).foreach { diag =>
      response.getFileList.asScala.find(_.getName == diag.pos.source.file.name).foreach { file =>
        file.getContent.lines.zipWithIndex.foreach {
          case (line, i) =>
            val prefix = s"${i + 1}:"
            println(f"$prefix%4s $line")
        }
        printDiagnostic(diag)
      }
    }
    val errors = reporter.infos.count(_.severity.id == reporter.ERROR.id)
    println(s"$errors errors")
    errors
  }
}

class ProtobufGeneratorSuite extends FunSuite {
  val compiler = new Compiler

  test("basic") {
    val request  = CodeGeneratorRequest.parseFrom(Files.readAllBytes(Paths.get("target/semanticdb")))
    val response = ProtobufGenerator.handleCodeGeneratorRequest(request)
    val errors   = compiler.compile(response)
    if (errors == 0) {
      response.getFileList.asScala.foreach { file =>
        if (file.getName.contains("Expr")) {
          println(file.getContent)
        }
      }
      compiler.compile(
        """
          |import myproto.test._
          |val expr = Add(Lit(1), Add(Lit(2), Expr.Empty))
        """.stripMargin
      )
    }
  }
}
