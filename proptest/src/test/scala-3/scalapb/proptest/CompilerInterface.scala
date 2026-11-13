package scalapb.proptest

import java.io.File
import dotty.tools.dotc.Main
import dotty.tools.dotc.reporting.Reporter

object CompilerInterface {
  def compile(
      files: Seq[File],
      classPath: Seq[String],
      outDir: File,
      sourceLevel3: Boolean
  ): Unit = {
    // sbt 2's forked test runner does not put test dependencies on java.class.path.
    val scala3Library = new File(
      classOf[scala.deriving.Mirror].getProtectionDomain.getCodeSource.getLocation.toURI
    ).getPath
    val compilerClassPath = (classPath :+ scala3Library).distinct.mkString(File.pathSeparator)
    val args              = files.map(_.toString()) ++
      Seq("-d", outDir.toString(), "-classpath", compilerClassPath) ++
      (if (sourceLevel3) Seq("-deprecation", "-source", "future", "-feature") else Nil)
    val reporter: Reporter = Main.process(args.toArray)
    if (reporter.hasErrors) {
      throw RuntimeException("Scala sources had errors")
    }
  }
}
