#!/usr/bin/env bash
set -e
TMPDIR=$(mktemp -d)
PROTOFILES=$(find e2e/src/main/protobuf -name "*.proto" -print)
VERSION=${TRAVIS_TAG:1}

sbt protocGenScalaUnix/assembly
printf "#!/usr/bin/env bash\nset -e\n" > $TMPDIR/plugin.sh
echo export JAVA_OPTS=-agentlib:native-image-agent=config-output-dir=protocGenScalaUnix/native-image-config >> $TMPDIR/plugin.sh
echo $PWD/protocGenScalaUnix/target/scala-2.12/protocGenScalaUnix-assembly-$VERSION.jar >> $TMPDIR/plugin.sh

chmod +x $TMPDIR/plugin.sh

mkdir -p protocGenScalaUnix/native-image-config

sbt "++2.12.10" "scalapbc/run \
    --plugin=protoc-gen-scalaref=$TMPDIR/plugin.sh \
    --scalaref_out=$TMPDIR $PROTOFILES \
    -I third_party -I $PWD/protobuf -I e2e/src/main/protobuf"
