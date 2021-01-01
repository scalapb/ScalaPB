package scalapb.compiler

import scalapb.options.Scalapb.PreprocesserOutput
import java.io.File
import java.nio.file.Files
import com.google.protobuf.InvalidProtocolBufferException
import scala.collection.mutable.HashMap
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import protocgen.CodeGenRequest
import protocbridge.ExtraEnvParser
import scala.util.Try
import scala.util.Success
import scala.util.Failure

trait SecondaryOutputProvider {
  def get(name: String): Try[PreprocesserOutput]
}

private final class FileBasedSecondaryOutputProvider(inputDir: File)
    extends SecondaryOutputProvider {
  val cache = new HashMap[String, Try[PreprocesserOutput]]

  def get(name: String): Try[PreprocesserOutput] = {
    cache.getOrElseUpdate(name, doGet(name))
  }

  private def doGet(name: String): Try[PreprocesserOutput] = {
    // names are checked in ProtoValidation. We check here again in case we somehow got here
    // through a different path.
    if (!SecondaryOutputProvider.isNameValid(name))
      throw new IllegalArgumentException(s"Invalid secondary output name: '$name'.")
    val in = inputDir.toPath.resolve(name)
    if (!in.toFile().exists()) {
      throw new RuntimeException(
        s"Could not find secondary output for '$name'. Check that the preprocessor plugin is executed before this plugin."
      )
    }
    val bytes = Files.readAllBytes(in)
    Try {
      com.google.protobuf.Any.parseFrom(bytes).unpack(classOf[PreprocesserOutput])
    }.recoverWith { case e: InvalidProtocolBufferException =>
      throw new GeneratorException(
        s"Invalid secondary output file format for '$name': ${e.toString()}"
      )
    }
  }
}

private final class InMemorySecondaryOutputProvider(map: Map[String, PreprocesserOutput])
    extends SecondaryOutputProvider {
  def get(name: String): Try[PreprocesserOutput] = map.get(name) match {
    case Some(v) => Success(v)
    case None    => Failure(new GeneratorException(s"Preprocessor '$name' was not found."))
  }
}

private object EmptySecondaryOutputProvider extends SecondaryOutputProvider {
  def get(name: String): Try[PreprocesserOutput] =
    Try(
      throw new GeneratorException(
        "No secondary outputs available. The most likely causes are that " +
          "you are using an older version of sbt-protoc, or the build tool you are using does not " +
          "support secondary outputs."
      )
    )
}

object SecondaryOutputProvider {
  def fromDirectory(dir: File): SecondaryOutputProvider = new FileBasedSecondaryOutputProvider(dir)

  def secondaryOutputDir(req: CodeGeneratorRequest): Option[File] = {
    Some(ExtraEnvParser.fromCodeGeneratorRequest(req).secondaryOutputDir)
      .filter(_.nonEmpty)
      .orElse(sys.env.get(protocbridge.ExtraEnv.ENV_SECONDARY_DIR))
      .map(new File(_))
  }

  def fromCodeGenRequestOrEnv(req: CodeGenRequest): SecondaryOutputProvider = {
    secondaryOutputDir(req.asProto).fold(SecondaryOutputProvider.empty)(dir =>
      SecondaryOutputProvider.fromDirectory(dir)
    )
  }

  private val VALID_NAME_REGEX = """^[a-zA-Z][a-zA-Z0-9_.-]*$""".r

  def empty: SecondaryOutputProvider = EmptySecondaryOutputProvider

  def isNameValid(name: String) = VALID_NAME_REGEX.pattern.matcher(name).matches()

  // For testing only
  def fromMap(map: Map[String, PreprocesserOutput]): SecondaryOutputProvider =
    new InMemorySecondaryOutputProvider(map)
}
