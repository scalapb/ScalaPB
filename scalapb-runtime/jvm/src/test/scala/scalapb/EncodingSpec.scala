package scalapb

import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.apache.commons.codec.binary.Base64
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec

class EncodingSpec extends AnyPropSpec with ScalaCheckDrivenPropertyChecks with Matchers {
  property("fromBase64 is the inverse of toBase64") {
    forAll { b: Array[Byte] => Encoding.fromBase64(Encoding.toBase64(b)) should be(b) }
  }

  property("fromBase64 is compatible with javax.printBase64") {
    forAll { b: Array[Byte] => Encoding.fromBase64(Base64.encodeBase64String(b)) should be(b) }
  }

  property("toBase64 is compatible with javax.parseBase64") {
    forAll { b: Array[Byte] => Base64.decodeBase64(Encoding.toBase64(b)) should be(b) }
  }
}
