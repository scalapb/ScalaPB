package com.trueaccord.scalapb

import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class EncodingSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  property("fromBase64 is the inverse of toBase64") {
    forAll {
      b: Array[Byte] =>
        Encoding.fromBase64(Encoding.toBase64(b)) should be(b)
    }
  }

  property("fromBase64 is compatible with javax.printBase64") {
    forAll {
      b: Array[Byte] =>
        Encoding.fromBase64(javax.xml.bind.DatatypeConverter.printBase64Binary(b)) should be(b)
    }
  }

  property("toBase64 is compatible with javax.parseBase64") {
    forAll {
      b: Array[Byte] =>
        javax.xml.bind.DatatypeConverter.parseBase64Binary(
          Encoding.toBase64(b)) should be(b)
    }
  }
}
