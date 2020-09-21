---
title: "Using ScalaPB with Scala.js"
sidebar_label: "Scala.js"
---

[Scala.js](http://scala-js.org) compiles Scala source code to equivalent
Javascript code.  It is possible to use ScalaPB generated case classes and
lenses in Scala.js, and even serialize and parse byte arrays.

Limitations:

- The Java protocol buffers are unavailable, and therefore Java conversions
  and Java descriptors do not work (though Scala descriptors in ScalaPB >=
  0.6.0 would cover most use cases)

## Getting Started

Add to your library dependencies:

    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,

      // The following needed only if you include scalapb/scalapb.proto:
      "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    )

## Demo

Example project: [https://github.com/thesamet/scalapbjs-test](https://github.com/thesamet/scalapbjs-test)

Example with multi-project build: [https://github.com/thesamet/sbt-protoc/tree/master/examples/scalajs-multiproject](https://github.com/thesamet/sbt-protoc/tree/master/examples/scalajs-multiproject)

Live demo: [http://thesamet.github.io/scalapbjs-test/](http://thesamet.github.io/scalapbjs-test/)

