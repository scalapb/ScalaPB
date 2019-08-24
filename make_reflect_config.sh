TMPDIR=$(mktemp -d)
PROTOFILES=$(find e2e/src/main/protobuf -name "*.proto" -print)
VERSION=0.9.1-SNAPSHOT
export JAVA_OPTS=-agentlib:native-image-agent=config-output-dir=protocGenScalapbUnix/native-image-config
protoc \
    --plugin=protoc-gen-scala=$PWD/protocGenScalapbUnix/target/scala-2.12/protocGenScalapbUnix-assembly-$VERSION.jar \
    --scala_out=$TMPDIR $PROTOFILES -I $PWD/protobuf -I \
    e2e/src/main/protobuf

