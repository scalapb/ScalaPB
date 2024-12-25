import sbt._
import sbtprojectmatrix.ProjectMatrixKeys._
import sbt.plugins.JvmPlugin
import Keys._
import complete._
import DefaultParsers._
import pl.project13.scala.sbt._
import protocbridge._
import sbtprotoc.ProtocPlugin.autoImport.PB
import sbtprotocgenproject.LocalCodeGenPlugin.autoImport._
import java.net.URLClassLoader
import scala.util.matching.Regex
import sbt.internal.ProjectMatrix
import sbtprojectmatrix.ProjectMatrixPlugin
import sbtprotocgenproject.LocalCodeGenPlugin
import sbtprotoc.ProtocPlugin
import oshi.SystemInfo
import oshi.util.FormatUtil

object BenchmarkingPlugin extends AutoPlugin {
  object autoImport {
    val Bench         = config("bench").extend(Compile)
    val Benchmark     = Tags.Tag("benchmark")
    val runBenchmarks = taskKey[Unit]("")
    val sysInfo       = taskKey[SysInfo]("Returns system information")

    sealed trait BenchTarget extends VirtualAxis.WeakAxis {
      def benchFilter: String
      override def suffixOrder: Int = Int.MaxValue
    }
    case class BenchLocal(benchFilter: String = ".*Scala") extends BenchTarget {
      override def directorySuffix: String = "scalapb"

      override def idSuffix: String = "_scalapb"

    }
    case class BenchIvyScalapb(version: String, benchFilter: String = ".*Scala")
        extends BenchTarget {
      override def directorySuffix: String = s"scalapb-$version"

      override def idSuffix: String = s"_scalapb_${version.replace('.', '_')}"
    }
    case class BenchJavapb(
        version: String = Dependencies.protobufJava.revision,
        benchFilter: String = ".*Java"
    ) extends BenchTarget {
      override def directorySuffix: String = s"javapb-$version"

      override def idSuffix: String = s"_javapb_${version.replace('.', '_')}"
    }
  }

  case class SysInfo(
      osArch: String,
      cpu: String,
      freq: String,
      jdkVersion: String = System.getProperty("java.version")
  ) {
    def cpuDirName: String = s"$cpu ($freq)"
    def jdkDirName: String = s"jdk$jdkVersion"
  }

  import autoImport._
  override def requires: Plugins =
    JvmPlugin && JmhPlugin && ProtocPlugin && LocalCodeGenPlugin && ProjectMatrixPlugin

  import JmhPlugin.autoImport._

  override def globalSettings: Seq[Setting[?]] = Seq(
    Global / concurrentRestrictions += Tags.limit(Benchmark, 1),
    Global / concurrentRestrictions += Tags.exclusive(Benchmark)
  )

  override def projectSettings: Seq[Setting[?]] = {
    Seq(
      Compile / PB.protocVersion := {
        virtualAxes.value
          .collectFirst { case BenchJavapb(version, _) =>
            version
          }
          .getOrElse(Dependencies.versions.protobuf)
      },
      Bench / codeGenClasspath := virtualAxes.value
        .collectFirst { case BenchIvyScalapb(version, _) =>
          val dep      = "com.thesamet.scalapb" % "compilerplugin_2.12" % version
          val resolver = (Compile / dependencyResolution).value
          resolver.retrieve(
            dep,
            None,
            (Compile / managedDirectory).value / s"scalapb-$version",
            streams.value.log
          ) match {
            case Right(files) => files.classpath
            case Left(e)      => throw e.resolveException
          }
        }
        .getOrElse(Nil),
      libraryDependencies ++= {
        virtualAxes.value.collect {
          case BenchIvyScalapb(version, _) =>
            Seq(
              "com.thesamet.scalapb" %% "scalapb-runtime" % version,
              "com.thesamet.scalapb" %% "scalapb-runtime" % version % "protobuf"
            )
          case BenchJavapb(version, _) => Seq(Dependencies.protobufJava.withRevision(version))
          case _                       => Nil
        }.flatten ++ Seq(
          "org.scala-lang.modules" %% "scala-collection-compat" % Dependencies.versions.collectionCompat,
          "org.openjdk.jmh" % "jmh-core" % (Jmh / version).value
        )
      },
      Compile / PB.targets := {
        virtualAxes.value.collect {
          case b: BenchTarget =>
            Seq[Target](
              PB.gens.java -> (Compile / sourceManaged).value / s"proto${b.directorySuffix}",
              (genModule("scalapb.ScalaPbCodeGenerator$") -> Seq(
                "java_conversions"
              )) -> (Compile / sourceManaged).value / s"proto${b.directorySuffix}"
            )
          case _ => Nil
        }.flatten
      },
      sysInfo := {
        val hw        = new SystemInfo().getHardware()
        val processor = hw.getProcessor()

        SysInfo(
          SystemDetector.detectedClassifier(),
          processor.getProcessorIdentifier().getName(),
          FormatUtil.formatHertz(processor.getMaxFreq())
        )
      },
      Jmh / parallelExecution := false,
      Jmh / run / mainClass   := Some("jmh.Main"),
      Jmh / run / javaOptions ++= {
        if (sysInfo.value.jdkVersion.split("\\.")(0).toInt >= 12) {
          Seq("-XX:+UseShenandoahGC", "-Xmx2G") // less STW pauses when available - less dispersion of results
        } else {
          Seq("-Xmx2G")
        }
      },
      runBenchmarks := {
        Def
          .taskDyn {
            virtualAxes.value
              .collectFirst { case b: BenchTarget =>
                val sys = sysInfo.value
                val scalaV = b match {
                  case BenchLocal(_)         => scalaVersion.value
                  case BenchIvyScalapb(_, _) => scalaVersion.value
                  case BenchJavapb(_, _)     => "java"
                }
                val resultsDir =
                  (ThisBuild / baseDirectory).value / projectMatrixBaseDirectory.value.name / "results" / sys.osArch / sys.cpuDirName / sys.jdkDirName / b.directorySuffix / scalaV
                val params =
                  s"""-rf json -rff "${resultsDir / "results.json"}" ${b.benchFilter}"""
                (Jmh / run).toTask(" " + params)
              }
              .getOrElse(Def.task(()))
          }
          .tag(Benchmark)
          .value
      }
    )
  }
}
