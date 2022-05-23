package scalapb.compiler.fp

import scalapb.compiler.FunctionalPrinter.PrinterEndo
import scalapb.compiler.DescriptorImplicits._

import scala.language.implicitConversions
import scala.annotation.nowarn

case object Scala {
  private lazy val ClassPrefix = "_root_."
  case class Type(name: Identifier, typeParams: Seq[Type] = Seq.empty, noRoot: Boolean = false) {
    override def toString: String = {
      { if (noRoot) "" else  ClassPrefix } + s"$name${mkNonEmptyString(typeParams, ", ", "[", "]")}"
    }
  }

  object Type {
    implicit def fromClass(c: Class[_]): Type = Type(c.getCanonicalName())
  }

  case class TypeDef(name: Identifier, params: Seq[TypeArg] = Seq.empty) {
    override def toString: String = s"$name${mkNonEmptyString(params, ", ", "[", "]")}"
  }

  case class TypeArg(name: Identifier, covariant: Type = null, contravariant: Type = null, contextBounds: Seq[Type] = Seq.empty) {
    override def toString: String = s"$name${mkNonNull(covariant, " <: ")}${mkNonNull(contravariant, " >: ")}${mkNonEmptyString(contextBounds, " : ", " : ")}"
  }

  case class Arg(name: Identifier, tpe: Type) {
    override def toString: String = s"$name: $tpe"
  }

  object Arg {
    implicit def fromTuple(t: (Identifier, Type)): Arg = Arg(t._1, t._2)
  }

  case class Identifier(name: String) {
    override def toString: String = name.asSymbol
    def map(f: String => String): Identifier = Identifier(f(name))
    def prefix(p: String): Identifier = Identifier(p + name)
    def suffix(s: String): Identifier = Identifier(name + s)
  }

  object Identifier {
    implicit def stringToIdentifier(s: String): Identifier = Identifier(s)
  }

  case class CtorCall(typeDef: Type, args: Seq[Seq[Identifier]] = Seq.empty) {
    override def toString: String = s"$typeDef${args.map(_.mkString(", ")).mkString("(", ")(", ")")}"
  }

  private def mkNonEmptyString(seq: Seq[AnyRef], delimiter: String, @nowarn prefix: String = "", suffix: String = "") = {
    if (seq.nonEmpty) {
      seq.mkString(prefix, delimiter, suffix)
    } else ""
  }

  private def mkNonNull(obj: AnyRef, @nowarn prefix: String = "", suffix: String = "") = {
    if (obj ne null) {
      s"$prefix$obj$suffix"
    } else ""
  }

  def caseClass(typeDef: TypeDef, args: Seq[Arg] = Seq.empty, extending: CtorCall = null, extWith: Seq[Type] = Seq.empty)(body: PrinterEndo): PrinterEndo = {
    _.add(s"case class $typeDef(${args.mkString(", ")})${mkNonNull(extending, " extends ")}${mkNonEmptyString(extWith, " with ", " with ")} {")
      .indented(body)
      .add("}")
  }

  def companionObject(typeDef: TypeDef, extending: CtorCall = null, extWith: Seq[Type] = Seq.empty)(body: PrinterEndo): PrinterEndo = {
    _.add(s"case object $typeDef${mkNonNull(extending, " extends ")}${mkNonEmptyString(extWith, " with ", " with ")} {")
      .indented(body)
      .add("}")
  }

  def method(name: Identifier, typeArgs: Seq[TypeArg] = Seq.empty, args: Seq[Arg] = Seq.empty, returnType: Type = null, access: String = "")(body: PrinterEndo): PrinterEndo = { p =>
    val typeArgsRender = mkNonEmptyString(typeArgs, ", ", "[", "]")
    val argsRender = mkNonEmptyString(args, ", ", "(", ")")
    val retTypeRender = mkNonNull(returnType, ": ")
    p.add(s"$access def $name$typeArgsRender$argsRender$retTypeRender = {")
      .indented(body)
      .add("}")
  }
}
