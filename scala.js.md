---
title: "ScalaPB: Using with Scala.js"
layout: page
---

# ScalaPB in Scala.js

[http://scala-js.org](Scala.js) compiles Scala source code to equivalent
Javascript code.  It is possible to use ScalaPB generated case classes and
lenses in ScalaJS, and even serialize and parse byte arrays.

There are a few things that do not work:

- The Java protocol buffers are unavailable, and therefore Java conversions do not work.

- Descriptors are unavailable since ScalaPB uses the descriptors
  implementation provided by Google's Java runtime.

## Getting Started

Add to your library dependencies:

    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,

      // The following needed only if you include scalapb/scalapb.proto:
      "com.trueaccord.scalapb" %%% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"
    )

## Demo

Example project: [https://github.com/thesamet/scalapbjs-test](https://github.com/thesamet/scalapbjs-test)

Live demo: [http://thesamet.github.io/scalapbjs-test/](http://thesamet.github.io/scalapbjs-test/)

