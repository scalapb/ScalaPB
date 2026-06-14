# ScalaPB

ScalaPB is a protoc plugin that generates Scala case classes from `.proto` files.
Key modules: `compiler-plugin` (code generator), `scalapb-runtime` (JVM/JS/Native),
`scalapbc` (standalone CLI), `lenses`, `scalapb-runtime-grpc`, `proptest`, `conformance`,
`e2e`/`e2e-grpc`/`e2e-withjava` (integration tests), `docs`/`website` (Docusaurus docs).
Uses sbt-projectmatrix for cross-builds across Scala 2.12, 2.13, 3 and JVM/JS/Native.

## Branches
- `master` — active development
- `0.11.x` — maintenance; fixes that apply to both branches must be committed to each separately

## Git
- **Never push without explicit instruction.** Commit locally and wait to be asked.

## sbt

Matrix project axes appear as suffixes in task names, e.g. `compilerPluginJVM2_13/test`,
`runtimeJS2_13/test`, `scalapbcJVM2_12/run`. Use these to scope commands to avoid
rebuilding everything.

```bash
sbt test                          # all unit tests (slow)
sbt compilerPluginJVM2_13/test    # test compiler plugin on Scala 2.13/JVM
sbt fmt                           # format all sources (alias: scalafmtSbt + scalafmtAll)
sbt scalafmtCheckAll              # check formatting without applying
sbt "scalapbcJVM2_12/run ..."     # run scalapbc code generator directly
```

## CI scripts
Each corresponds to a CI matrix job:
```bash
./test.sh                      # unit tests
./e2e.sh                       # end-to-end: compiles e2e, e2e-grpc, e2e-withjava
./scala3_compat_test.sh        # Scala 3 compatibility tests
./examples_and_formatting.sh   # example projects + scalafmt check + generated code check
./mima.sh                      # binary compatibility check against 0.11.0
```

## Generated code
Several source directories are produced by `make_plugin_proto.sh` and must not be
hand-edited or reformatted by scalafmt (they are excluded in `.scalafmt.conf`):
- `docs/src/main/scala/generated/`
- `scalapb-runtime/src/main/scalajvm/` (com/google/protobuf subtree)
- `scalapb-runtime/src/main/scala/scalapb/options/`

After changing the code generator, regenerate and commit the diff:
```bash
./make_plugin_proto.sh
```
CI verifies the checked-in files match fresh generator output via
`./test_generated_code_checked_in.sh`.

## Website
Docs live in `docs/` (mdoc sources), built to `docs/target/mdoc/`, served by Docusaurus
from `website/`. To test website changes:
```bash
cd website && yarn build
```
