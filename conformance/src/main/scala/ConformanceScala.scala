package scalapb

import java.io.InputStream
import com.google.protobuf.conformance.conformance._
import scala.annotation.tailrec
import com.google.protobuf_test_messages.proto2.test_messages_proto2
import com.google.protobuf_test_messages.proto3.test_messages_proto3
import scala.util.Try
import scalapb._
import scalapb.json4s.JsonFormat

sealed trait ExitStatus

object ExitStatus {
  case object Finished               extends ExitStatus
  case class Error(error: Throwable) extends ExitStatus
}

object ConformanceScala {
  def main(args: Array[String]): Unit = {
    runTests()
  }

  private def readFromStdin(len: Int): Option[Array[Byte]] = {
    val buf       = new Array[Byte](len)
    var offset    = 0
    var remaining = len
    while (offset < len) {
      val read: Int = System.in.read(buf, offset, remaining)
      if (read == -1) {
        return None
      }
      offset += read
      remaining -= read
    }
    Some(buf)
  }

  def readLittleEndianIntFromStdin: Option[Int] = {
    readFromStdin(4).map { buf =>
      ((buf(0) & 0xff)
        | ((buf(1) & 0xff) << 8)
        | ((buf(2) & 0xff) << 16)
        | ((buf(3) & 0xff) << 24))
    }
  }

  def writeToStdout(b: Array[Byte]) = {
    System.out.write(b)
  }

  def writeLittleEndianIntToStdout(value: Int): Unit = {
    val buf = new Array[Byte](4)
    buf(0) = value.toByte
    buf(1) = (value >> 8).toByte
    buf(2) = (value >> 16).toByte
    buf(3) = (value >> 24).toByte
    writeToStdout(buf)
  }

  val typeRegistry = scalapb.json4s.TypeRegistry.default
    .addMessage[test_messages_proto2.TestAllTypesProto2]
    .addMessage[test_messages_proto3.TestAllTypesProto3]

  val JsonParser = JsonFormat.parser.withTypeRegistry(typeRegistry)

  val IgnoringJsonParser = JsonParser.ignoringUnknownFields

  val JsonPrinter = JsonFormat.printer.withTypeRegistry(typeRegistry)

  def jsonParser(request: ConformanceRequest): scalapb.json4s.Parser = {
    if (request.testCategory.isJsonIgnoreUnknownParsingTest) IgnoringJsonParser
    else JsonParser
  }

  def readInputMessage[T <: GeneratedMessage](
      request: ConformanceRequest
  )(implicit cmp: GeneratedMessageCompanion[T]): Either[Throwable, T] = {
    request.payload match {
      case ConformanceRequest.Payload.ProtobufPayload(bytes) =>
        Try(
          cmp.parseFrom(bytes.toByteArray)
        ).toEither
      case ConformanceRequest.Payload.JsonPayload(json) =>
        Try(
          jsonParser(request).fromJsonString(json)
        ).toEither
      case ConformanceRequest.Payload.TextPayload(text) =>
        Try(cmp.fromAscii(text)).toEither
      case _ => Left(new RuntimeException("Unsupported input format"))
    }
  }

  def handleTestTyped[T <: GeneratedMessage: GeneratedMessageCompanion](
      request: ConformanceRequest
  ): ConformanceResponse = {
    readInputMessage(request) match {
      case Right(input) =>
        request.requestedOutputFormat match {
          case WireFormat.PROTOBUF =>
            ConformanceResponse().withProtobufPayload(input.toByteString)
          case WireFormat.JSON =>
            try {
              val res = ConformanceResponse().withJsonPayload(JsonPrinter.print(input))
              res
            } catch {
              case err: Throwable =>
                ConformanceResponse().withSerializeError(err.getMessage)
            }
          case WireFormat.TEXT_FORMAT =>
            ConformanceResponse().withTextPayload(input.toProtoString)
          case _ => ConformanceResponse()
        }
      case Left(err) =>
        ConformanceResponse().withParseError(err.getMessage)
    }
  }

  def handleTest(request: ConformanceRequest): ConformanceResponse = {
    request.messageType match {
      case "protobuf_test_messages.proto3.TestAllTypesProto3" =>
        handleTestTyped[test_messages_proto3.TestAllTypesProto3](request)
      case "protobuf_test_messages.proto2.TestAllTypesProto2" =>
        handleTestTyped[test_messages_proto2.TestAllTypesProto2](request)
      case "conformance.FailureSet" =>
        ConformanceResponse().withProtobufPayload(FailureSet().toByteString)
    }
  }

  def doTestIO(): Either[ExitStatus, Unit] = {
    for {
      bytes <- readLittleEndianIntFromStdin.toRight(ExitStatus.Finished)
      input <- readFromStdin(bytes).toRight(
        ExitStatus.Error(new RuntimeException("Unexpected EOF while reading request."))
      )
      request  = ConformanceRequest.parseFrom(input)
      response = handleTest(request).toByteArray
      _        = writeLittleEndianIntToStdout(response.length)
      _        = writeToStdout(response)
    } yield ()
  }

  @tailrec
  def runTests(): Unit = {
    import ExitStatus._
    doTestIO() match {
      case Right(())              => runTests
      case Left(Error(throwable)) =>
        throwable.printStackTrace()
        sys.exit(1)
      case Left(Finished) =>
        sys.exit(0)
    }
  }
}
