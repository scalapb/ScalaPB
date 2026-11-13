#!/usr/bin/env bash
set -e

# Check the oldest supported Scala 3, then a compiler that rejects legacy
# implicit definitions under -source:future.
SCALA_FUTURE_VERSION=${SCALA_FUTURE_VERSION:-3.8.4}
sbt \
    proptestScala3SourcesJVM3/test \
    "project proptestScala3SourcesJVM3" \
    "set scalaVersion := \"${SCALA_FUTURE_VERSION}\"" \
    'set scalacOptions := scalacOptions.value.filterNot(_.startsWith("-release:")) :+ "-release:17"' \
    proptestScala3SourcesJVM3/test
