#!/usr/bin/env bash
#
# Script that invokes ScalaPB to generate code for some protos.
#
set -e

GOOGLE_PROTOS=$(find third_party/google/protobuf/ -name '*.proto' -print)

sbt "scalapbcJVM2_12/run --scala_out=java_conversions:scalapb-runtime/src/main/scalajvm \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbcJVM2_12/run --scala_out=scalapb-runtime/src/main/js-native \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbcJVM2_12/run --scala_out=scalapb-runtime/src/main/scala \
    --proto_path=./protobuf:./third_party ./protobuf/scalapb/scalapb.proto" \
    "scalapbcJVM2_12/run --scala_out=docs/src/main/scala \
    --proto_path=./protobuf:./third_party:./docs/src/main/protobuf:./benchmarks/src/main/protobuf \
    ./docs/src/main/protobuf/duration.proto \
    ./docs/src/main/protobuf/json.proto \
    ./docs/src/main/protobuf/person.proto \
    ./benchmarks/src/main/protobuf/protos.proto \
    "

echo Done!
