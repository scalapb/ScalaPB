package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scalapb.GeneratorOption._

class DefaultOptionsSpec extends AnyFlatSpec with Matchers {
  "grpc" should "be enabled by default" in {
    scalapb.gen()._2 must be(Seq("grpc"))
    scalapb.gen(flatPackage = true)._2 must contain theSameElementsAs (Seq("grpc", "flat_package"))
    scalapb.gen(lenses = false)._2 must be(Seq("grpc", "no_lenses"))
  }

  "grpc" should "be present or absent when passed explicitly" in {
    scalapb.gen(grpc = true)._2 must be(Seq("grpc"))
    scalapb.gen(grpc = false)._2 must be(Seq())
  }

  "grpc" should "be explictly passed when using GeneratorOptions" in {
    scalapb.gen(Grpc, NoLenses)._2 must contain theSameElementsAs (Seq("grpc", "no_lenses"))
    scalapb.gen(NoLenses)._2 must contain theSameElementsAs (Seq("no_lenses"))
    scalapb.gen(FlatPackage)._2 must contain theSameElementsAs (Seq("flat_package"))
  }
}
