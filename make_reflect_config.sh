#!/usr/bin/env bash
set -e
REFLECT_TMPDIR=$(mktemp -d)
PROTOFILES=$(find e2e/src/main/protobuf -name "*.proto" -print)

sbt 'set every version := "SNAPSHOT"; protoc-gen-scala-unix/assembly'
printf "#!/usr/bin/env bash\nset -e\n" > $REFLECT_TMPDIR/plugin.sh
echo export JAVA_OPTS=-agentlib:native-image-agent=config-output-dir=protoc-gen-scala-native-image/native-image-config >> $REFLECT_TMPDIR/plugin.sh
echo "$PWD"/target/out/jvm/scala-*/protoc-gen-scala-unix/protoc-gen-scala-unix-assembly-SNAPSHOT.jar >> $REFLECT_TMPDIR/plugin.sh

chmod +x $REFLECT_TMPDIR/plugin.sh

mkdir -p protoc-gen-scala-native-image/native-image-config

sbt "scalapbcJVM2_12/run \
    --plugin=protoc-gen-scalaref=$REFLECT_TMPDIR/plugin.sh \
    --scalaref_out=$REFLECT_TMPDIR $PROTOFILES \
    --experimental_allow_proto3_optional \
    -I third_party \
    -I $PWD/protobuf \
    -I e2e/src/main/protobuf \
    -I e2e-withjava/src/main/protobuf"
