package jmh

import org.openjdk.jmh._
import runner._
import options.{OptionsBuilder, VerboseMode}
import runner.format.OutputFormatFactory
import results._
import results.format._
import org.openjdk.jmh.runner.options.CommandLineOptionException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.util.Collections
import scala.jdk.CollectionConverters._

object Main {
  val SILENT = OutputFormatFactory.createFormatInstance(null, VerboseMode.SILENT)

  val x = new org.openjdk.jmh.Main
  def main(args: Array[String]): Unit = {
    try {
        val cmd = new options.CommandLineOptions(args: _*)
        val opts = new OptionsBuilder().parent(cmd).build()
        val outDir = Paths.get(opts.getResult().orElse("results")).getParent()
        val benchmarks = BenchmarkList.defaultList()
        for (bench <- benchmarks.find(SILENT, opts.getIncludes(), opts.getExcludes()).asScala) {
          bench.getUsername().split("\\.").toList.reverse match {
            case method :: benchName :: _ =>
              val outPath = outDir.resolve(benchName).resolve(method)
              outPath.toFile.mkdirs()
              val profilePath = outPath.resolve("profile")
              profilePath.toFile.mkdirs()
              val out = outPath.resolve("results.json")
              val o = new OptionsBuilder()
                .result(out.toAbsolutePath.toString())
                .resultFormat(ResultFormatType.JSON)
                .include(bench.getUsername())
                .addProfiler("async", s"output=flamegraph;event=cpu;sig=true;ann=true;dir=${profilePath.toAbsolutePath()}")
              new Runner(o.build()).run()
            case _ =>
          }
        }
    } catch {
      case e: CommandLineOptionException =>
        println("Error parsing command line:")
        println(" " + e.getMessage())
        System.exit(1)
      case e: NoBenchmarksException =>
        println("No matching benchmarks. Miss-spelled regexp?")
        System.exit(1)
      case e: ProfilersFailedException =>
        println("Profiler failed to initialize:")
        e.printStackTrace(System.out)
        System.exit(1)
      case e: RunnerException =>
        println("Error running the benchmarks:")
        e.printStackTrace(System.out)
        System.exit(1)
    }
  }
}
