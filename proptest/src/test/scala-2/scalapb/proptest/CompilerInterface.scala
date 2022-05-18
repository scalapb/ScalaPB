package scalapb.proptest

import scala.tools.nsc._
import java.io.File

object CompilerInterface {

  def compile(scalaFiles: Seq[File], classPath: Seq[String], outDir: File): Unit = {

    val s                        = new Settings(error => throw new RuntimeException(error))
    val breakCycles: Seq[String] = Seq("-Ybreak-cycles")

    s.processArgumentString(
      s"""-cp "${classPath.mkString(":")}" ${breakCycles.mkString(" ")} -d "$outDir""""
    )

    val g   = new Global(s)
    val run = new g.Run
    run.compile(scalaFiles.map(_.toString).toList)
  }

}
