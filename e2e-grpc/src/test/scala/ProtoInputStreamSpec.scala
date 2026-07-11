import com.thesamet.proto.e2e.service.SealedResponseMessage
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import scalapb.grpc.ProtoInputStream
import com.thesamet.proto.e2e.service.Res1

import scala.util.Random

class ProtoInputStreamSpec extends AnyFunSpec with Matchers {

  trait Setup {
    // message.serializedSize == 4
    val message = SealedResponseMessage(
      SealedResponseMessage.SealedValue.Res1(Res1(42))
    )
    val stream    = new ProtoInputStream(message)
    def newBuffer = Array.fill[Byte](message.serializedSize * 2)(0)

    def fullyDrainStream() = {
      stream.read(newBuffer, 0, message.serializedSize + 1)
    }

    def partiallyDrainStream() = {
      stream.read(newBuffer, 0, message.serializedSize - 1)
    }
  }

  describe("#available()") {
    it("returns full length for a fresh stream") {
      new Setup {
        stream.available() must be(message.serializedSize)
      }
    }

    it("returns zero for drained stream") {
      new Setup {
        fullyDrainStream()

        stream.available() must be(0)
      }
    }

    it("returns remaining length for partially drained stream") {
      new Setup {
        partiallyDrainStream()

        stream.available() must be(1)
      }
    }
  }

  describe("#read(buffer, offset, length)") {

    it("returns -1 for a fully drained stream") {
      new Setup {
        fullyDrainStream()

        stream.read(newBuffer, 0, 10) must be(-1)
      }
    }

    it("returns requested length and fills the buffer") {
      new Setup {
        val buf1 = newBuffer
        stream.read(buf1, 0, 2) must be(2)
        buf1.take(2) must be(message.toByteArray.take(2))
      }
    }

    it("fully readable in chunks") {
      new Setup {
        var offset = 0
        var count  = 0
        var buf    = newBuffer
        val res    = Array.newBuilder[Byte]
        while ({
          res ++= buf.slice(offset, offset + count)
          buf = newBuffer
          offset += count
          count = stream.read(buf, offset, Random.nextInt(3))
          count !== -1
        }) {}

        res.result() must be(message.toByteArray)
      }
    }
  }

  describe("#read()") {

    it("returns bytes for a fresh stream") {
      new Setup {
        val bytes = message.toByteArray

        stream.read() must be(bytes(0))
        stream.read() must be(bytes(1))
        stream.read() must be(bytes(2))
      }
    }

    it("returns -1 when fully drained") {
      new Setup {
        fullyDrainStream()

        stream.read() must be(-1)
      }
    }

    it("returns next byte when partially drained") {
      new Setup {
        partiallyDrainStream()

        stream.read() must be(message.toByteArray.last)
      }
    }
  }

  describe("#message") {
    it("returns the same instance passed in the constructor") {
      new Setup {
        stream.message must be theSameInstanceAs (message)
      }
    }

    it("throws when fully drained") {
      new Setup {
        fullyDrainStream()

        an[IllegalStateException] should be thrownBy stream.message
      }
    }

    it("throws when partially drained") {
      new Setup {
        partiallyDrainStream()

        an[IllegalStateException] should be thrownBy stream.message
      }
    }
  }
}
