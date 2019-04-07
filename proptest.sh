#!/usr/bin/env sh
set -e
sbt -J-XX:LoopStripMiningIter=0 ++$TRAVIS_SCALA_VERSION test
