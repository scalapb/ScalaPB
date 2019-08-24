#!/usr/bin/env bash
set -e
wget -qO- https://github.com/shyiko/jabba/raw/master/install.sh | bash && . ~/.jabba/jabba.sh;
GRAALVM=graalvm@19.2.0-1
$JABBA_HOME/bin/jabba install ${GRAALVM}
if [[ "$OSTYPE" == "darwin"* ]]; then
    export JAVA_HOME="$JABBA_HOME/jdk/${GRAALVM}/Contents/Home" && java -version
else
    export JAVA_HOME="$JABBA_HOME/jdk/${GRAALVM}" && java -version
fi
export PATH="$JAVA_HOME/bin:$PATH"
gu install native-image
sbt protocGenScalapbUnix/graalvm-native-image:packageBin
zip -j $1 protocGenScalapbUnix/target/graalvm-native-image/protoc-gen-scalapb
