package scalapb

import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.apache.commons.codec.binary.Base64

class EncodingSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  property("fromBase64 is the inverse of toBase64") {
    forAll { b: Array[Byte] =>
      Encoding.fromBase64(Encoding.toBase64(b)) should be(b)
    }
  }

  property("fromBase64 is compatible with javax.printBase64") {
    forAll { b: Array[Byte] =>
      Encoding.fromBase64(Base64.encodeBase64String(b)) should be(b)
    }
  }

  property("toBase64 is compatible with javax.parseBase64") {
    forAll { b: Array[Byte] =>
      Base64.decodeBase64(Encoding.toBase64(b)) should be(b)
    }
  }
}
