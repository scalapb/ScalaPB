---
title: "ScalaPB: Using with scala.js"
layout: page
---

# ScalaPB in Scala.js

It is possible to use ScalaPB generated classes in ScalaJS and even serialize
and parse byte arrays.

Things that do not work:

- The Java protocol buffers are unavailable (since Java code does not get translated to
  Javascript by ScalaJS), and therefore Java conversions do not work.
- Descriptors are unavailable (since ScalaPB uses the Java implementation of
  descriptors)

## Getting Started

Add to your library dependencies:

    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "scalapb-runtime" % "0.5.18",

      // The following needed only if you include scalapb/scalapb.proto:
      "com.trueaccord.scalapb" %%% "scalapb-runtime" % "0.5.18" % PB.protobufConfig,
    )

## Demo

Example project: [https://github.com/thesamet/scalapbjs-test](https://github.com/thesamet/scalapbjs-test)

Live demo: [http://thesamet.github.io/scalapbjs-test/](http://thesamet.github.io/scalapbjs-test/)

