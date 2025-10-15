package scalapb.grpc

import com.google.protobuf.CodedOutputStream
import scalapb.GeneratedMessage

import java.io.{ByteArrayInputStream, InputStream}

/** Allows skipping serialization completely when the io.grpc.inprocess.InProcessTransport is used.
  * Inspired by
  * https://github.com/grpc/grpc-java/blob/master/protobuf-lite/src/main/java/io/grpc/protobuf/lite/ProtoInputStream.java
  */
class ProtoInputStream[T <: GeneratedMessage](msg: T) extends InputStream {

  private var state: State = Message(msg)

  private sealed trait State {
    def message: T = throw new IllegalStateException("message not available")
    def available: Int
    def read(): Int
    def read(b: Array[Byte], off: Int, len: Int): Int
  }

  private object Drained extends State {
    override def available: Int                                = 0
    override def read(): Int                                   = -1
    override def read(b: Array[Byte], off: Int, len: Int): Int = -1
  }

  private case class Message(value: T) extends State {
    override def available: Int                                = value.serializedSize
    override def message: T                                    = value
    override def read(): Int                                   = toStream.read()
    override def read(b: Array[Byte], off: Int, len: Int): Int = {
      value.serializedSize match {
        case 0                   => toDrained.read(b, off, len)
        case size if size <= len =>
          val stream = CodedOutputStream.newInstance(b, off, size)
          message.writeTo(stream)
          stream.flush()
          stream.checkNoSpaceLeft()
          toDrained
          size
        case _ => toStream.read(b, off, len)
      }
    }
    private def toStream: State = {
      state = Stream(new ByteArrayInputStream(value.toByteArray))
      state
    }
    private def toDrained: State = {
      state = Drained
      state
    }
  }

  private case class Stream(value: InputStream) extends State {
    override def available: Int                                = value.available()
    override def read(): Int                                   = value.read()
    override def read(b: Array[Byte], off: Int, len: Int): Int = value.read(b, off, len)
  }

  override def read(): Int                                   = state.read()
  override def read(b: Array[Byte], off: Int, len: Int): Int = state.read(b, off, len)
  override def available(): Int                              = state.available
  def message: T                                             = state.message
}
