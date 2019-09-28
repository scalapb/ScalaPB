#!/usr/bin/env bash
set -e
TMPDIR=$(mktemp -d)
PROTOFILES=$(find e2e/src/main/protobuf -name "*.proto" -print)
VERSION=${TRAVIS_TAG:1}

sbt protocGenScalapbUnix/assembly
printf "#!/usr/bin/env bash\nset -e\n" > $TMPDIR/plugin.sh
echo export JAVA_OPTS=-agentlib:native-image-agent=config-output-dir=protocGenScalapbUnix/native-image-config >> $TMPDIR/plugin.sh
echo $PWD/protocGenScalapbUnix/target/scala-2.12/protocGenScalapbUnix-assembly-$VERSION.jar >> $TMPDIR/plugin.sh

chmod +x $TMPDIR/plugin.sh

mkdir -p protocGenScalapbUnix/native-image-config

sbt "++2.12.10" "scalapbc/run \
    --plugin=protoc-gen-scalapbref=$TMPDIR/plugin.sh \
    --scalapbref_out=$TMPDIR $PROTOFILES \
    -I third_party -I $PWD/protobuf -I e2e/src/main/protobuf"
