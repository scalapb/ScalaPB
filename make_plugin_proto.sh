#!/usr/bin/env bash
#
# Script that invokes ScalaPB to generate code for some protos.
#
set -e

GOOGLE_PROTOS=$(find third_party/google/protobuf/ -name '*.proto' -print)

sbt "++2.12.10" "scalapbc/run --scala_out=java_conversions:scalapb-runtime/jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/non-jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/shared/src/main/scala \
    --proto_path=./protobuf:./third_party ./protobuf/scalapb/scalapb.proto" \
    "scalapbc/run --scala_out=docs/src/main/scala \
    --proto_path=./protobuf:./third_party:./docs/src/main/protobuf \
    ./docs/src/main/protobuf/duration.proto \
    ./docs/src/main/protobuf/json.proto \
    ./docs/src/main/protobuf/person.proto \
    "

echo Done!
