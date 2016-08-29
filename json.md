---
title: "ScalaPB: Using JSON"
layout: page
---

# ScalaPB and JSON

ScalaPB can convert protocol buffers to and from JSON, using
[json4s](http://json4s.org/).

## Setting up your project

Make sure that you are using ScalaPB 0.5.x or later.

In `build.sbt` add a dependency on `scalapb-json4s`:

{%highlight scala%}
libraryDepenencies += "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.2"
{%endhighlight%}

In your code, you can now convert to JSON:

{%highlight scala%}
import com.trueaccord.scalapb.json.JsonFormat

val r: String = JsonFormat.toJsonString(myProto)
{%endhighlight%}

Parse JSON back to a protocol buffer:

{%highlight scala%}
import com.trueaccord.scalapb.json.JsonFormat

val proto: MyProto = JsonFormat.fromJsonString[MyProto](
    """{"x": "17"}""")
{%endhighlight%}

There are lower-level functions `toJson()` and `fromJson()` that convert from
protos to json4s's `JValue`:

{%highlight scala%}
def toJson(m: GeneratedMessage): JObject

def fromJson[Proto](value: JValue): Proto
{%endhighlight%}

Finally, in JsonFormat there are two implicit methods that instantiate
`Reader[Proto]` and `Writer[Proto]`.

