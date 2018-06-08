#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-${TRAVIS_SCALA_VERSION:-2.11.11}}

sbt ++$SCALA_VERSION \
    grpcRuntime/mimaReportBinaryIssues \
    lensesJVM/mimaReportBinaryIssues \
    runtimeJVM/mimaReportBinaryIssues \
    # compilerPlugin/mimaReportBinaryIssues
