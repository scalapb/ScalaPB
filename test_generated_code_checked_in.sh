#!/usr/bin/env bash
# Test that all generated code is checked in.
set -e
PROTOC_JAR_VERSION=3.6.0
JAR_FILENAME=protoc-jar-$PROTOC_JAR_VERSION.jar
JAR_PATH=${HOME}/.ivy2/cache/com.github.os72/protoc-jar/jars/$JAR_FILENAME
JAR_URL=http://central.maven.org/maven2/com/github/os72/protoc-jar/$PROTOC_JAR_VERSION/$JAR_FILENAME

if [ ! -f $JAR_PATH ]; then
    curl $JAR_URL -o $JAR_PATH;
fi

echo "java -jar $JAR_PATH -v360 \$@" > protoc
chmod +x protoc
PATH=${PATH}:${PWD} ./make_plugin_proto.sh
rm protoc
git diff --exit-code || { \
    echo "Generated code changed. Please run make_plugin_proto.sh."; exit 1; }

