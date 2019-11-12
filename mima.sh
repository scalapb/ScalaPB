#!/usr/bin/env sh
set -e
exit 0
SCALA_VERSION=${SCALA_VERSION:-${TRAVIS_SCALA_VERSION:-2.11.12}}

if [ "$SCALA_VERSION" != "2.13.1" ]
then    sbt ++$SCALA_VERSION \
        grpcRuntime/mimaReportBinaryIssues \
        lensesJVM/mimaReportBinaryIssues \
        runtimeJVM/mimaReportBinaryIssues \
        compilerPlugin/mimaReportBinaryIssues
fi
