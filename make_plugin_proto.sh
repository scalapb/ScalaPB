#!/usr/bin/env sh
#
# Script that compiles plugin.proto from Protocol buffer distribution into
# Java, so it can be used by our compiler.
#
set -e
OUTDIR=compiler-plugin/src/main/java
mkdir -p compiler-plugin/src/main/java
protoc --java_out="$OUTDIR" --proto_path=./protobuf/ \
    ./protobuf/google/protobuf/compiler/plugin.proto \

protoc --java_out="$OUTDIR" --proto_path=./protobuf \
    ./protobuf/scalapb/scalapb.proto

# Generated code will be dependent on it.
protoc --java_out=scalapb-runtime/src/main/java --proto_path=./protobuf \
    ./protobuf/scalapb/scalapb.proto
# protoc --java_out=scalapb-runtime/src/main/java --proto_path=./protobuf \
#     ./protobuf/google/protobuf/descriptor.proto
# 
