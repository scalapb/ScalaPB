package scalapb.docs.benchmarks

import ujson._
import os.pwd

case class DataPoint(
    scalapbVersion: String,
    scalaVersion: String,
    method: String,
    score: Double,
    low: Double,
    high: Double
)

case class LineKey(method: String, scalaVersion: String)

object Charts {
  val jsonDir = pwd / "benchmarks" / "results"
  val files   = os.list(jsonDir).filter(_.ext == "json")

  val points = (for {
    file <- files
    in                                     = ujson.read(os.read(file))
    Array(_, scalapbVersion, scalaVersion) = file.baseName.split("_")
    bs <- in.arr
  } yield DataPoint(
    scalapbVersion,
    scalaVersion,
    bs("benchmark").str,
    bs("primaryMetric")("score").num,
    bs("primaryMetric")("scoreConfidence").arr(0).num,
    bs("primaryMetric")("scoreConfidence").arr(1).num
  ))

  val ts = points
    .groupBy(p => LineKey(p.method, p.scalaVersion))
    .mapValues(dps => dps.map(dp => (dp.scalapbVersion -> ((dp.score, dp.low, dp.high)))).toMap)
    .toMap

  def versionKey(s: String): (Int, Int, String) = {
    val Array(major, minor, patch) = s.split("[.]")
    (major.toInt, minor.toInt, patch)
  }

  def traceForKey(key: LineKey, showLegend: Boolean) = {
    val (x, y) = ts(key).toVector.sortBy(v => versionKey(v._1)).unzip
    Obj(
      "x"       -> x,
      "y"       -> y.map(_._1),
      "name"    -> ("Scala " + key.scalaVersion.split('.').dropRight(1).mkString(".")),
      "error_y" -> Obj(
        "array"      -> y.map(t => t._3 - t._1),
        "arrayminus" -> y.map(t => t._1 - t._2),
        "type"       -> "data"
      ),
      "legendgroup" -> "a",
      "showlegend"  -> showLegend,
      "type"        -> "scatter"
    )
  }

  def javaTraceForKey(key: LineKey, scalapbVersions: Seq[String], showLegend: Boolean) = {
    val Vector(("", y)) = ts(key).toVector
    Obj(
      "x"           -> scalapbVersions,
      "y"           -> Vector.fill(scalapbVersions.size)(y._1),
      "name"        -> "Java",
      "legendgroup" -> "b",
      "showlegend"  -> showLegend,
      "mode"        -> "lines"
    )
  }

  def makeChart(baseName: String, title: String, showLegend: Boolean) = {
    val key1 = LineKey(baseName + "Scala", "2.12.10")
    val key2 = LineKey(baseName + "Scala", "2.13.1")
    val java = LineKey(baseName + "Java", "java")

    val allVersions = (ts(key1).keySet ++ ts(key2).keySet).toVector
    val data        = Arr(
      traceForKey(key1, showLegend),
      traceForKey(key2, showLegend),
      javaTraceForKey(java, allVersions, showLegend)
    )
    val layout = Obj(
      "title" -> title,
      "xaxis" -> Obj(
        "title" -> "ScalaPB Version"
      ),
      "yaxis" -> Obj(
        "title"     -> "ns/op",
        "rangemode" -> "tozero"
      )
    )
    val divName = s"div-$baseName"
    (
      s"""<div id="$divName"></div>""",
      s"""<script>Plotly.newPlot('${divName}', ${data.render()}, ${layout
          .render()}, {responsive: true});</script>"""
    )
  }

  def makeParseChart(fn: String) = {
    makeChart(s"scalapb.perf.${fn}Test.parse", s"$fn parse", showLegend = false)
  }

  def makeSerializeChart(fn: String) = {
    makeChart(s"scalapb.perf.${fn}Test.serialize", s"$fn serialize", showLegend = true)
  }

  def makeChartPair(fn: String) = {
    val (d1, s1) = makeParseChart(fn)
    val (d2, s2) = makeSerializeChart(fn)

    println(
      """<table class="table"><tr>""" +
        s"""<td width="50%">${d1}</td>""" +
        s"""<td width="50%">${d2}</td></tr></table>${s1}${s2}"""
    )
  }
}
