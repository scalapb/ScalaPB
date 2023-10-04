#!/usr/bin/env bash
set -e
SCALA_VERSION=${SCALA_VERSION:-2_13}

sbt \
    proptestScala3SourcesJVM${SCALA_VERSION}/test
