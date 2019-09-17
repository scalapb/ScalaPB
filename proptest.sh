#!/usr/bin/env sh
set -e
sbt -J-XX:LoopStripMiningIter=0 -J-Xmx4500M ++$TRAVIS_SCALA_VERSION test
