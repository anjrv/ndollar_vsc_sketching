#!/bin/bash

mkdir -p ./gen

OUT_DIR="./src"
IN_DIR="../sketching-server/src/main/proto"

./node_modules/.bin/grpc_tools_node_protoc \
  --js_out=import_style=commonjs,binary:"$OUT_DIR" \
  --ts_out=grpc_js:"$OUT_DIR" \
  --grpc_out=grpc_js:"$OUT_DIR" \
  --plugin=protoc-gen-ts=./node_modules/grpc_tools_node_protoc_ts/bin/protoc-gen-ts \
  -I "$IN_DIR" \
  "$IN_DIR"/*.proto
