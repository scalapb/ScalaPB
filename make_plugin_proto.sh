#!/usr/bin/env sh
#
# Script that compiles plugin.proto from Protocol buffer distribution into
# Java, so it can be used by our compiler.
#
# First argument is protobuf distribution directiory.
OUTDIR=compiler-plugin/src/main/java
mkdir -p compiler-plugin/src/main/java
protoc --java_out="$OUTDIR" --proto_path=$1/src $1/src/google/protobuf/compiler/plugin.proto 

