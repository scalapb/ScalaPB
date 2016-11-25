#!/usr/bin/env sh
#
# Script that compiles plugin.proto from Protocol buffer distribution into
# Java, so it can be used by our compiler.
#
set -e
OUTDIR=compiler-plugin/src/main/java

protoc --java_out="$OUTDIR" --proto_path=./protobuf \
    --proto_path=./third_party \
    ./protobuf/scalapb/scalapb.proto

protoc --java_out=scalapb-runtime/jvm/src/main/java --proto_path=./protobuf \
    --proto_path=./third_party \
    ./protobuf/scalapb/scalapb.proto

GOOGLE_PROTOS=$(find third_party/google/protobuf/ -name '*.proto' -print)

sbt "scalapbc/run --scala_out=java_conversions:scalapb-runtime/jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/js/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS"

