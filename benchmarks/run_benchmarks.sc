#!/usr/bin/env amm

import $ivy.`com.lihaoyi::ammonite-ops:2.4.0`
import ammonite.ops._

import $file.project.TestNames, TestNames.TestNames.testNames

val MODES = Map(
  "slow" -> "-wi 10 -i 10 -f 5 -w 1 -r 1",
  "fast" -> "-wi 5 -i 5 -f 1 -w 1 -r 1"
)

val SCALA_212 = "2.12.10"

val SCALA_213 = "2.13.1"

val ALL_SCALA = Seq(SCALA_212, SCALA_213)

val ALL_SCALAPB = Seq(
  "0.9.6",
  "0.10.0-M4",
  "0.11.20"
)

// Exclude combinations when ScalaPB is not available for Scala 2.13
def isExcluded(scala: String, scalapb: String): Boolean =
  (scalapb.startsWith("0.7") || scalapb.startsWith("0.8")) && scala.startsWith(SCALA_213)

def runSbt(
    mode: String,
    scalapbVersion: String,
    scalaVersion: String,
    tests: String,
    outfile: String
): Unit = {
  val command = Seq(
    "sbt",
    s"++${scalaVersion}!",
    "clean",
    s"jmh:run -t 1 ${MODES(mode)} ${tests} -rf json -rff ${outfile}"
  )
  import sys.process._
  if (command.! != 0) {
    throw new RuntimeException("SBT exit with non-zero code.")
  }
}

@main
def main(
    mode: String = "slow",
    benchmarks: Seq[String] = testNames,
    scalapb: Seq[String] = ALL_SCALAPB,
    scala: Seq[String] = ALL_SCALA,
    java: Boolean = true
): Unit = {
  ops.mkdir ! ops.pwd / 'results
  val benchmarks0 = if (benchmarks.nonEmpty) benchmarks else testNames
  val scalapb0 = if (scalapb.nonEmpty) scalapb else ALL_SCALAPB
  val scala0 = if (scala.nonEmpty) scala else ALL_SCALA
  for {
    scalaVersion   <- scala0
    scalapbVersion <- scalapb0
    b              <- benchmarks0
  } {
    if (isExcluded(scalaVersion, scalapbVersion)) {
      println(s"Skipping excluded Scala $scalaVersion and ScalaPB $scalapbVersion")
    } else {
      println(s"Running for Scala $scalaVersion and ScalaPB $scalapbVersion with benchmark $b")
      runSbt(
        mode,
        scalapbVersion,
        scalaVersion,
        s"${b}Test.*Scala",
        s"results/${b}_${scalapbVersion}_${scalaVersion}.json"
      )
      if (java) {
        val (scalapbVersion, scalaVersion) = (scalapb.last, scala.last)
        runSbt(mode, scalapbVersion, scalaVersion, s"${b}Test.*Java", s"results/${b}__java.json")
      }
    }
  }

}
