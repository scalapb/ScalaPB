---
sidebar_label: "Introduction"
title: "ScalaPB: Scala Protocol Buffer Compiler"
---

ScalaPB is a protocol buffer compiler (`protoc`) plugin for Scala. It will
generate Scala case classes, parsers and serializers for your protocol
buffers.

ScalaPB is hosted on [Github](https://github.com/scalapb/ScalaPB).

## Main features

* Built on top of Google's protocol buffer compiler to ensure perfect
  compatibility with the language specification.

* Supports both proto2 and proto3.

* Nested updates are easy by using lenses:
```scala
val newOrder = order.update(_.creditCard.expirationYear := 2015)
```

* Generated case classes can co-exist alongside the Java-generated code (the
  class names will not clash). This allows gradual transition from Java to
  Scala.

* Can optionally generate conversion methods between the Java generated
  version of Protocol Buffers to the Scala generated version. This makes
  it possible to migrate your project gradually.

* **New:** Supports for
  [Oneof](https://developers.google.com/protocol-buffers/docs/proto#oneof)'s
  that were introduced in Protocol Buffers 2.6.0.

* **Newer:** Supports [Scala.js](scala.js.md) (in 0.5.x).

* **Newer:** Supports [gRPC](http://www.grpc.io/) (in 0.5.x).

* **Newest:** Supports [SparkSQL](sparksql.md) (in 0.5.23).

* **Newest:** Supports [converting to and from JSON](json.md) (in 0.5.x).

* **Newest:** Supports [User-defined options](user_defined_options.md) (in 0.5.29).
