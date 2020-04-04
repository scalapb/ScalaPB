#!/usr/bin/env bash
#
# Script that invokes ScalaPB to generate code for some protos.
#
set -e

GOOGLE_PROTOS=$(find third_party/google/protobuf/ -name '*.proto' -print)
ZIOGRPC_VERSION=0.2.0

sbt "++2.12.10" "scalapbc/run --scala_out=java_conversions:scalapb-runtime/jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/non-jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/shared/src/main/scala \
    --proto_path=./protobuf:./third_party ./protobuf/scalapb/scalapb.proto" \
    "scalapbc/run
    --plugin-artifact=com.thesamet.scalapb.zio-grpc:protoc-gen-zio:${ZIOGRPC_VERSION}:default,classifier=unix,ext=sh,type=jar \
    --proto_path=./protobuf:./third_party:./docs/src/main/protobuf:./benchmarks/src/main/protobuf \
    --scala_out=grpc:docs/src/main/scala \
    --zio_out=docs/src/main/scala \
    ./docs/src/main/protobuf/duration.proto \
    ./docs/src/main/protobuf/json.proto \
    ./docs/src/main/protobuf/person.proto \
    ./docs/src/main/protobuf/ziogrpc.proto \
    ./benchmarks/src/main/protobuf/protos.proto \
    "

echo Done!
