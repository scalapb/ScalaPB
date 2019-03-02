#!/usr/bin/env bash
#
# Script that compiles plugin.proto from Protocol buffer distribution into
# Java, so it can be used by our compiler.
#
set -e
OUTDIR=compiler-plugin/src/main/java

TMPDIR=$(mktemp -d)

# Setup protoc
PROTOC_JAR_VERSION=3.7.0
JAR_FILENAME=protoc-jar-$PROTOC_JAR_VERSION.jar
JAR_PATH=${HOME}/.ivy2/cache/com.github.os72/protoc-jar/jars/$JAR_FILENAME
JAR_URL=http://central.maven.org/maven2/com/github/os72/protoc-jar/$PROTOC_JAR_VERSION/$JAR_FILENAME

if [ ! -f $JAR_PATH ]; then
    mkdir -p $(dirname $JAR_PATH);
    curl $JAR_URL -o $JAR_PATH;
fi

function protoc_jar() 
{
  java -jar $JAR_PATH -v370 "$@"
}

# SBT 1.x depends on scalapb-runtime which contains a compiled copy of
# scalapb.proto.  When the compiler plugin is loaded into SBT it may cause a
# conflict. To prevent that, we use a different package name for the generated
# code for the compiler-plugin.  In the past, we used shading for this
# purpose, but this makes it harder to create more protoc plugins that depend
# on compiler-plugin
sed 's/scalapb\.options/scalapb.options.compiler/' \
    ./protobuf/scalapb/scalapb.proto > $TMPDIR/scalapb.proto

protoc_jar --java_out="$OUTDIR" \
    --proto_path=$TMPDIR \
    --proto_path=./third_party \
    $TMPDIR/scalapb.proto

rm -rf $TMPDIR

protoc_jar --java_out=scalapb-runtime/jvm/src/main/java --proto_path=./protobuf \
    --proto_path=./third_party \
    ./protobuf/scalapb/scalapb.proto

GOOGLE_PROTOS=$(find third_party/google/protobuf/ -name '*.proto' -print)

sbt "scalapbc/run --scala_out=java_conversions:scalapb-runtime/jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/non-jvm/src/main/scala \
    --proto_path=./third_party \
    $GOOGLE_PROTOS" \
    "scalapbc/run --scala_out=scalapb-runtime/shared/src/main/scala \
    --proto_path=./protobuf:./third_party ./protobuf/scalapb/scalapb.proto"

echo Done!

