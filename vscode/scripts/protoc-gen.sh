#!/bin/bash

echo "This currently assumes a scuffed global install of grpc_tools_node_protoc_ts"

OUT_DIR="./src"
IN_DIR="../sketching-server/src/main/proto"

grpc_tools_node_protoc \
  --js_out=import_style=commonjs,binary:"$OUT_DIR" \
  --ts_out=grpc_js:"$OUT_DIR" \
  --grpc_out=grpc_js:"$OUT_DIR" \
	--plugin=protoc-gen-ts="$HOME"/.npm-global/lib/node_modules/grpc_tools_node_protoc_ts/bin/protoc-gen-ts \
  -I "$IN_DIR" \
  "$IN_DIR"/*.proto
