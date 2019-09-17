#!/usr/bin/env sh
set -e
sbt -J-XX:LoopStripMiningIter=0 -J-Xmx4500M -J-XX:GCTimeRatio=19 -J-XX:MinHeapFreeRatio=10 -J-XX:MaxHeapFreeRatio=20 ++$TRAVIS_SCALA_VERSION test
