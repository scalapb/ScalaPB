package com.trueaccord.scalapb.textformat

import scala.collection.mutable

class TextGenerator(singleLine: Boolean = true) {
  private val sb = mutable.StringBuilder.newBuilder
  private var indentLevel = 0
  private var lineStart = true

  private def maybeNewLine(): Unit = {
    if (lineStart) {
      if (!singleLine)
        sb.append(" " * (indentLevel * 2))
      else if (sb.nonEmpty) sb.append(' ')
    }
  }

  def add(s: String): TextGenerator = {
    maybeNewLine()
    sb.append(s)
    lineStart = false
    this
  }

  def addNewLine(s: String): TextGenerator = {
    maybeNewLine()
    sb.append(s)
    if (!singleLine) {
      sb.append('\n')
    }
    lineStart = true
    this
  }

  def indent(): TextGenerator = {
    indentLevel += 1
    this
  }

  def outdent(): TextGenerator = {
    indentLevel -= 1
    this
  }

  def result(): String = sb.result()
}
