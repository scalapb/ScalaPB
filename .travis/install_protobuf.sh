#!/bin/sh
set -e
PROTOBUF_VERSION=2.6.0
wget https://protobuf.googlecode.com/svn/rc/protobuf-${PROTOBUF_VERSION}.tar.gz
tar -xzvf protobuf-${PROTOBUF_VERSION}.tar.gz
cd protobuf-${PROTOBUF_VERSION} && ./configure --prefix=/usr && make && sudo make install

