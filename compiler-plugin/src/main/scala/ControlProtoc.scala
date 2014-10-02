import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Path}

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import com.trueaccord.scalapb.compiler.ProtobufGenerator

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._

object ControlProtoc extends App {
  def createPipe(): Path = {
    val pipeName = Files.createTempFile("protopipe-", ".pipe")
    Files.delete(pipeName)
    Seq("mkfifo", "-m", "600", pipeName.toAbsolutePath.toString).!!
    pipeName
  }

  def createShellScript(tmpFile: Path): Path = {
    val content =
      s"""|#!/usr/bin/env sh
          |cat /dev/stdin > "$tmpFile"
          |cat "$tmpFile"
      """.stripMargin
    val scriptName = Files.createTempFile("compgen", "")
    val os = Files.newOutputStream(scriptName)
    os.write(content.getBytes("UTF-8"))
    os.close()
    Files.setPosixFilePermissions(scriptName, Set(
      PosixFilePermission.OWNER_EXECUTE,
      PosixFilePermission.OWNER_READ))
    scriptName
  }

  def handleCodeGeneratorRequest(request: CodeGeneratorRequest): CodeGeneratorResponse = {
    val fileProtosByName = request.getProtoFileList.map(n => n.getName -> n).toMap
    val b = CodeGeneratorResponse.newBuilder
    val filesByName: Map[String, FileDescriptor] =
      request.getProtoFileList.foldLeft[Map[String, FileDescriptor]](Map.empty) {
        case (acc, fp) =>
          val deps = fp.getDependencyList.map(acc)
          acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
      }
    request.getFileToGenerateList.foreach {
      name =>
        val file = filesByName(name)
        val responseFile = ProtobufGenerator.generateFile(file)
        b.addFile(responseFile)
    }
    b.build
  }

  def runProtoc(args: String*) = {
    val pipe = createPipe()
    val sh = createShellScript(pipe)

    Future {
      try {
        val fsin = Files.newInputStream(pipe)
        val request = CodeGeneratorRequest.parseFrom(fsin)
        val response = handleCodeGeneratorRequest(request)
        val fsout = Files.newOutputStream(pipe)
        fsout.write(response.toByteArray)
        fsout.close()
        fsin.close()
        println("here")
      } catch {
        case e: Exception =>
          println("Exc: ", e)
      }
    }

    (Seq("protoc",
      s"--plugin=protoc-gen-scala=$sh") ++ args).!!
    Files.delete(pipe)
    Files.delete(sh)
  }
}
