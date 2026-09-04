# Agents

This file contains instructions for agents working on this project.

## Mindset

**sbt is slow—minutes per compile/test.** Wasted cycles waste hours.

- **Batch edits.** Do not: edit → compile → edit → compile. Do: edit everything → compile once.
- **Design tests with code.** Cover every statement and branch; if coverage reveals gaps, fix all at once.
- **Extreme ownership.** "Optional" means required; do the work completely.

## Commands

```bash
# Discover project names and Scala versions
sbt projects
sbt e2eJVM2_12/test
sbt e2eJVM2_13/test
sbt e2eJVM3/test
```

## Workflow

Phases 1-3 repeat until verify passes. Phase 4 runs once at the end.

### 1. Edit
Batch code and tests together. For large changes, split into batches—each batch includes its tests.

### 2. Fast Loop
One project, one Scala version: get `<project>/test` green. Default Scala 3; use Scala 2 if editing `scala-2/` sources. No coverage here.

### 3. Verify
Enter only when fast loop is green. Run in order:

1. **Coverage** — Scala version you developed with
2. **Cross-Scala** — the other Scala version, same project
3. **Cross-platform** — other platform projects, if cross-built and you touched shared/ or platform sources

If any step fails: return to phase 1, fix, get green in phase 2, rerun the failing step.

### 4. Format

Run once after verify passes.

## Boundaries

### Always
- Follow workflow phases in order
- Batch edits; keep sbt runs scoped to one project
- Update AGENTS.md if you find errors or gaps
- Document new data types in `docs/`; update existing docs when behavior changes
- **README.md is auto-generated.** Never edit `README.md` directly. Edit `docs/index.md` instead, then run `sbt --client generateReadme` to regenerate `README.md`.

### Ask First
- Adding dependencies (even test-only)
- Creating or removing subprojects
- Any repo-wide test or coverage run
- **New modules:** When adding a new module, update the `testJVM`, `testJS`, `docJVM`, and `docJS` command aliases in `build.sbt`.

### Never
- Use coverage as starting point for test design (think first, verify with coverage)
- Iterate coverage in tiny steps (if gaps exist, fix ALL at once, then rerun once)
- Cheat: delete code/tests, lower thresholds, or game metrics to appear compliant
- Retest after formatting (unless formatting broke the build)
- Use `sbt test` (repo-wide) when `sbt <project>/test` works
- Call work "optional" or "good enough" to justify not doing it

## Benchmarks

To run the benchmarks, navigate to the `benchmarks` directory and run the following command:

```bash
amm run_benchmarks.sc --scalapb <scalapb_version> --scala <scala_version> --java <true|false> --mode <fast|full> --benchmarks <benchmark_name>
```

For example:

```bash
amm run_benchmarks.sc --scalapb 0.11.20 --scala 2.13.18 --java true --mode fast --benchmarks LargeStringMessage
```
