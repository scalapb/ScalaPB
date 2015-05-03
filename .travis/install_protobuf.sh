#!/bin/sh
set -e
PROTOBUF_VERSION=3.0.0-alpha-2
wget https://github.com/google/protobuf/archive/v${PROTOBUF_VERSION}.tar.gz
tar -xzvf v${PROTOBUF_VERSION}.tar.gz
cd protobuf-${PROTOBUF_VERSION} && ./autogen.sh && ./configure --prefix=/usr && make && sudo make install

