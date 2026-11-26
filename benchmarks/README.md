# Benchmarks

## How to run

Execute `run_benchmarks.sc` with [ammonite](https://ammonite.io/):

```bash
amm run_benchmarks.sc --help
```

Example:

```bash
amm run_benchmarks.sc --scalapb 0.11.21 --scala 2.13.18 --java false
```

Runs all benchmarks for scalapb version 0.11.21 and Scala version 2.13.18 without java benchmarks.

## Comparing eager and lazy strings

Use a locally published version containing `lazy_utf8_string_fields` for these benchmarks.
The ASCII eager/lazy/no-validation variants share one seeded wire payload; the non-ASCII
eager/lazy variants share another. Java fixtures parse the same bytes as Scala fixtures.
The proto2 no-validation variant still differs in field presence and allocation, so it
does not isolate the cost of validation alone.

Benchmark methods return their complete parsed message or serialized byte array for JMH
to consume. Partial-access benchmarks also consume the accessed string lengths through
a `Blackhole`. Payload creation and initial parsing happen outside the timed methods.

Validate fixture equality before measuring:

```bash
sbt 'Test/runMain scalapb.perf.BenchmarkFixturesCheck'
```

Results measured before these changes should be rerun before comparing speedups.
