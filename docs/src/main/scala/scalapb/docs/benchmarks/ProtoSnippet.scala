package scalapb.docs.benchmarks
import scalapb.{GeneratedMessageCompanion, GeneratedMessage, GeneratedEnum, GeneratedEnumCompanion}
import com.google.protobuf.descriptor.SourceCodeInfo.Location

object ProtoSnippet {
    def showLocation(location: Location): Unit = {
        val span = location.span
        val (startLine, endLine) = if (span.length == 4) (span(0), span(2)) else (span(0), span(0))
        println(
            os.read.lines(os.pwd / "benchmarks" / "src" / "main" / "protobuf" / "protos.proto").slice(startLine, endLine + 1)
            .mkString("```protobuf\n", "\n", "\n```\n")
        )
    }

    def showFile(name: String): Unit = {
        println(
            os.read.lines(os.pwd / "docs" / "src" / "main" / "protobuf" / name)
            .mkString("```protobuf\n", "\n", "\n```\n")
        )
    }

    def source[T <: GeneratedMessage](implicit cmp: GeneratedMessageCompanion[T]): Unit = {
        showLocation(cmp.scalaDescriptor.location.get)
    }

    def source[T <: GeneratedEnum](implicit cmp: GeneratedEnumCompanion[T]): Unit = {
        showLocation(cmp.scalaDescriptor.location.get)
    }
}
