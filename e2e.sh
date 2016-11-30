#!/usr/bin/env sh
set -e
sbt ++2.10.6 compilerPlugin/publishLocal runtimeJVM/publishLocal createVersionFile \
    ++2.11.8 runtimeJVM/publishLocal grpcRuntime/publishLocal
cd e2e
for t in scala.collection.immutable.Seq scala.collection.immutable.IndexedSeq List Vector
do
  sbt -DcollectionType=${t} clean compile
done

sbt clean noJava/clean noJava/test test
