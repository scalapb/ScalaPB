#!/usr/bin/env bash
set -e

if [[ "$SCALA_VERSION" = 3* ]]
then
    echo Skipping test for Scala 3.
    exit 0
fi

sbt \
grpcRuntimeJVM${SCALA_VERSION}/mimaReportBinaryIssues \
lensesJVM${SCALA_VERSION}/mimaReportBinaryIssues \
runtimeJVM${SCALA_VERSION}/mimaReportBinaryIssues \
compilerPluginJVM${SCALA_VERSION}/mimaReportBinaryIssues
