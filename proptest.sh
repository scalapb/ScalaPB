#!/usr/bin/env sh
set -e
sbt -J-XX:LoopStripMiningIter=0 ++$SCALA_VERSION test
