package scalapb.compiler

import com.google.protobuf.Descriptors.FileDescriptor
import java.nio.file.Files
import java.io.File
import java.io.PrintWriter
import java.io.FileInputStream
import com.google.protobuf.ExtensionRegistry
import scalapb.options.Scalapb
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import scala.jdk.CollectionConverters._

trait ProtocInvocationHelper {
  def generateFileSet(files: Seq[(String, String)]): Seq[FileDescriptor] = {
    val tmpDir    = Files.createTempDirectory("validation").toFile
    val fileNames = files.map { case (name, content) =>
      val file = new File(tmpDir, name)
      val pw   = new PrintWriter(file)
      pw.write(content)
      pw.close()
      file.getAbsoluteFile
    }
    val outFile = new File(tmpDir, "descriptor.out")

    require(
      ProtocRunner
        .forVersion(Version.protobufVersion)
        .run(
          Seq(
            "-I",
            tmpDir.toString + ":protobuf:third_party",
            s"--descriptor_set_out=${outFile.toString}",
            "--include_imports"
          ) ++ fileNames.map(_.toString),
          Seq.empty
        ) == 0,
      "protoc exited with an error"
    )

    val fileset: Seq[FileDescriptor] = {
      val fin      = new FileInputStream(outFile)
      val registry = ExtensionRegistry.newInstance()
      Scalapb.registerAllExtensions(registry)
      val fileset =
        try {
          FileDescriptorSet.parseFrom(fin, registry)
        } finally {
          fin.close()
        }
      fileset.getFileList.asScala
        .foldLeft[Map[String, FileDescriptor]](Map.empty) { case (acc, fp) =>
          val deps = fp.getDependencyList.asScala.map(acc)
          acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
        }
        .values
        .toVector
    }
    fileset
  }
}
