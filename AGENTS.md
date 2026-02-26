# Agents

This file contains instructions for agents working on this project.

## Benchmarks

To run the benchmarks, navigate to the `benchmarks` directory and run the following command:

```bash
amm run_benchmarks.sc --scalapb <scalapb_version> --scala <scala_version> --java <true|false> --mode <fast|full> --benchmarks <benchmark_name>
```

For example:

```bash
amm run_benchmarks.sc --scalapb 0.11.20 --scala 2.13.18 --java true --mode fast --benchmarks LargeStringMessage
```
