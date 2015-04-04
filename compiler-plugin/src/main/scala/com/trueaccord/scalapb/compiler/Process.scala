package com.trueaccord.scalapb.compiler

import java.io.{InputStream, StringWriter, PrintWriter}
import java.net.ServerSocket
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Path}

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorResponse, CodeGeneratorRequest}
import com.trueaccord.scalapb.Scalapb

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._
import scala.util.Try

object Process {
  private def getStackTrace(e: Throwable): String = {
    val stringWriter = new StringWriter
    val printWriter = new PrintWriter(stringWriter)
    e.printStackTrace(printWriter)
    stringWriter.toString
  }

  def runWithInputStream(fsin: InputStream): CodeGeneratorResponse = {
    val registry = ExtensionRegistry.newInstance()
    Scalapb.registerAllExtensions(registry)

    Try {
      val request = CodeGeneratorRequest.parseFrom(fsin, registry)
      ProtobufGenerator.handleCodeGeneratorRequest(request)
    }.recover {
      case throwable =>
        CodeGeneratorResponse.newBuilder()
          .setError(throwable.toString + "\n" + getStackTrace(throwable))
          .build
    }.get
  }

  def runProtocUsing[A](protocCommand: String, schemas: Seq[String] = Nil,
                        includePaths: Seq[String] = Nil, protocOptions: Seq[String] = Nil)(runner: Seq[String] => A): A = {
    val ss = new ServerSocket(0)
    val (sh, py) = createPythonShellScript(ss.getLocalPort)
    Future {
      val client = ss.accept()
      val response = runWithInputStream(client.getInputStream)
      client.getOutputStream.write(response.toByteArray)
      client.close()
      ss.close()
    }

    try {
      val incPath = includePaths.map("-I" + _)
      val args = Seq("protoc", s"--plugin=protoc-gen-scala=$sh") ++ incPath ++ protocOptions ++ schemas
      runner(args)
    } finally {
      Files.delete(sh)
      Files.delete(py)
    }
  }

  def runProtocUsingOld[A](protocCommand: String, schemas: Seq[String] = Nil,
                           includePaths: Seq[String] = Nil, protocOptions: Seq[String] = Nil)(runner: Seq[String] => A): A = {
    val pipe = createPipe()
    val sh = createShellScript(pipe)
    Future {
      val fsin = Files.newInputStream(pipe)
      val response = runWithInputStream(fsin)
      val fsout = Files.newOutputStream(pipe)
      fsout.write(response.toByteArray)
      fsout.close()
      fsin.close()
    }

    try {
      val incPath = includePaths.map("-I" + _)
      val args = Seq("protoc", s"--plugin=protoc-gen-scala=$sh") ++ incPath ++ protocOptions ++ schemas
      runner(args)
    } finally {
      Files.delete(pipe)
      Files.delete(sh)
    }
  }

  def runProtoc(args: String*) = runProtocUsing("protoc", protocOptions = args)(_.!!)

  private def createPipe(): Path = {
    val pipeName = Files.createTempFile("protopipe-", ".pipe")
    Files.delete(pipeName)
    Seq("mkfifo", "-m", "600", pipeName.toAbsolutePath.toString).!!
    pipeName
  }

  private def createPythonShellScript(port: Int): (Path, Path) = {
    val content =
      s"""|import sys, socket
          |
          |content = sys.stdin.read()
          |s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
          |s.connect(('127.0.0.1', int(sys.argv[1])))
          |s.sendall(content)
          |s.shutdown(socket.SHUT_WR)
          |while 1:
          |    data = s.recv(1024)
          |    if data == '':
          |        break
          |    sys.stdout.write(data)
          |s.close()
      """.stripMargin
    val scriptName = Files.createTempFile("scalapbgen", ".py")
    val os = Files.newOutputStream(scriptName)
    os.write(content.getBytes("UTF-8"))
    os.close()

    val batchContent =
      s"""@echo off
         |python -u $scriptName $port
         """.stripMargin
    val batchFile = Files.createTempFile("scalapbgen", ".bat")
    val os2 = Files.newOutputStream(batchFile)
    os2.write(batchContent.getBytes("UTF-8"))
    os2.close()
    (batchFile, scriptName)
  }

  private def createShellScript(tmpFile: Path): Path = {
    val content =
      s"""|#!/usr/bin/env sh
          |set -e
          |cat /dev/stdin > "$tmpFile"
          |cat "$tmpFile"
      """.stripMargin
    val scriptName = Files.createTempFile("scalapbgen", "")
    val os = Files.newOutputStream(scriptName)
    os.write(content.getBytes("UTF-8"))
    os.close()
    Files.setPosixFilePermissions(scriptName, Set(
      PosixFilePermission.OWNER_EXECUTE,
      PosixFilePermission.OWNER_READ))
    scriptName
  }
}
