#!/usr/bin/env sh
#
# Script that compiles plugin.proto from Protocol buffer distribution into
# Java, so it can be used by our compiler.
#
set -e
OUTDIR=compiler-plugin/src/main/java

TMPDIR=$(mktemp -d)

# SBT 1.x depends on scalapb-runtime which contains a compiled copy of
# scalapb.proto.  When the compiler plugin is loaded into SBT it may cause a
# conflict. To prevent that, we use a different package name for the generated
# code for the compiler-plugin.  In the past, we used shading for this
# purpose, but this makes it harder to create more protoc plugins that depend
# on compiler-plugin
sed 's/scalapb\.options/scalapb.options.compiler/' \
    ./protobuf/scalapb/scalapb.proto > $TMPDIR/scalapb.proto

protoc --java_out="$OUTDIR" \
    --proto_path=$TMPDIR \
    --proto_path=./third_party \
    $TMPDIR/scalapb.proto

rm -rf $TMPDIR

protoc --java_out=scalapb-runtime/jvm/src/main/java --proto_path=./protobuf \
    --proto_path=./third_party \
    ./protobuf/scalapb/scalapb.proto

GOOGLE_PROTOS=$(find third_party/google/protobuf/ -name '*.proto' -print)

sbt "scalapbc/run --scala_out=java_conversions:scalapb-runtime/jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/js/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/shared/src/main/scala \
    --proto_path=./protobuf:./third_party ./protobuf/scalapb/scalapb.proto"

echo Done!

