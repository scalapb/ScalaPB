#!/usr/bin/env bash
set -e
TMPDIR=$(mktemp -d)
PROTOFILES=$(find e2e/src/main/protobuf -name "*.proto" -print)

sbt "set version in ThisBuild := \"SNAPSHOT\"" protoc-gen-scala-unix/assembly
printf "#!/usr/bin/env bash\nset -e\n" > $TMPDIR/plugin.sh
echo export JAVA_OPTS=-agentlib:native-image-agent=config-output-dir=protoc-gen-scala-native-image/native-image-config >> $TMPDIR/plugin.sh
echo $PWD/.protoc-gen-scala-unix/target/scala-2.12/protoc-gen-scala-unix-assembly-SNAPSHOT.jar >> $TMPDIR/plugin.sh

chmod +x $TMPDIR/plugin.sh

mkdir -p protoc-gen-scala-native-image/native-image-config

sbt "scalapbcJVM2_12/run \
    --plugin=protoc-gen-scalaref=$TMPDIR/plugin.sh \
    --scalaref_out=$TMPDIR $PROTOFILES \
    -I third_party \
    -I $PWD/protobuf \
    -I e2e/src/main/protobuf \
    -I e2e-withjava/src/main/protobuf"
