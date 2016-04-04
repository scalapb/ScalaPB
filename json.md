---
title: "ScalaPB: Using JSON"
layout: page
---

# ScalaPB and JSON

## Setting up your project

Make sure that you are using ScalaPB 0.5.x or later.

In `build.sbt` add a dependency on `scalapb-json4s`:

{%highlight scala%}
libraryDepenencies += "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.1"
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
protos to json4s's `JValue`.

Finally, in JsonFormat there are two implicit methods that instantiate
`Reader[Proto]` and `Writer[Proto]`.

