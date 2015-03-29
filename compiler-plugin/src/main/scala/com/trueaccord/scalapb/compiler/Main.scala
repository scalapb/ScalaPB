package com.trueaccord.scalapb.compiler

object Main extends App {
  val response = Process.runWithInputStream(System.in)
  System.out.write(response.toByteArray)
}
