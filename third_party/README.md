# Protobuf schemas

`google/protobuf/**/*.proto` is vendored without modifications from
[protocolbuffers/protobuf v36.2](https://github.com/protocolbuffers/protobuf/tree/v36.2/src/google/protobuf).
The schemas, protoc and protobuf-java are aligned at this version for Edition 2026.
Regenerate Scala sources with `./make_plugin_proto.sh` after updating these files.
