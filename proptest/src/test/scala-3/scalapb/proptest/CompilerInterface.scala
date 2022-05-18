package scalapb.proptest

import java.io.File
import dotty.tools.dotc.Main
import dotty.tools.dotc.reporting.Reporter

object CompilerInterface {
  def compile(files: Seq[File], classPath: Seq[String], outDir: File): Unit = {
    val args = files.map(_.toString()) ++
      Seq("-d", outDir.toString(), "-usejavacp", "-classpath", classPath.mkString(":"))
    val reporter: Reporter = Main.process(args.toArray)
    if (reporter.hasErrors) {
      throw RuntimeException("Scala sources had errors")
    }
  }
}
