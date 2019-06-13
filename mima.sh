#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-${TRAVIS_SCALA_VERSION:-2.11.11}}

if [ "$SCALA_VERSION" != "2.13.0" ]
then    sbt ++$SCALA_VERSION \
        grpcRuntime/mimaReportBinaryIssues \
        lensesJVM/mimaReportBinaryIssues \
        runtimeJVM/mimaReportBinaryIssues \
        compilerPlugin/mimaReportBinaryIssues
fi
