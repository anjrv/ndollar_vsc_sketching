// GENERATED CODE -- DO NOT EDIT!

'use strict';
var grpc = require('@grpc/grpc-js');
var sketching_api_pb = require('./sketching_api_pb.js');

function serialize_is_nsn_sketching_AddTemplateRequest(arg) {
  if (!(arg instanceof sketching_api_pb.AddTemplateRequest)) {
    throw new Error('Expected argument of type is.nsn.sketching.AddTemplateRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_is_nsn_sketching_AddTemplateRequest(buffer_arg) {
  return sketching_api_pb.AddTemplateRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_is_nsn_sketching_AddTemplateResponse(arg) {
  if (!(arg instanceof sketching_api_pb.AddTemplateResponse)) {
    throw new Error('Expected argument of type is.nsn.sketching.AddTemplateResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_is_nsn_sketching_AddTemplateResponse(buffer_arg) {
  return sketching_api_pb.AddTemplateResponse.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_is_nsn_sketching_ParseSketchRequest(arg) {
  if (!(arg instanceof sketching_api_pb.ParseSketchRequest)) {
    throw new Error('Expected argument of type is.nsn.sketching.ParseSketchRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_is_nsn_sketching_ParseSketchRequest(buffer_arg) {
  return sketching_api_pb.ParseSketchRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_is_nsn_sketching_ParseSketchResponse(arg) {
  if (!(arg instanceof sketching_api_pb.ParseSketchResponse)) {
    throw new Error('Expected argument of type is.nsn.sketching.ParseSketchResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_is_nsn_sketching_ParseSketchResponse(buffer_arg) {
  return sketching_api_pb.ParseSketchResponse.deserializeBinary(new Uint8Array(buffer_arg));
}


var SketchingServiceService = exports.SketchingServiceService = {
  parseSketch: {
    path: '/is.nsn.sketching.SketchingService/ParseSketch',
    requestStream: false,
    responseStream: false,
    requestType: sketching_api_pb.ParseSketchRequest,
    responseType: sketching_api_pb.ParseSketchResponse,
    requestSerialize: serialize_is_nsn_sketching_ParseSketchRequest,
    requestDeserialize: deserialize_is_nsn_sketching_ParseSketchRequest,
    responseSerialize: serialize_is_nsn_sketching_ParseSketchResponse,
    responseDeserialize: deserialize_is_nsn_sketching_ParseSketchResponse,
  },
  addTemplate: {
    path: '/is.nsn.sketching.SketchingService/AddTemplate',
    requestStream: false,
    responseStream: false,
    requestType: sketching_api_pb.AddTemplateRequest,
    responseType: sketching_api_pb.AddTemplateResponse,
    requestSerialize: serialize_is_nsn_sketching_AddTemplateRequest,
    requestDeserialize: deserialize_is_nsn_sketching_AddTemplateRequest,
    responseSerialize: serialize_is_nsn_sketching_AddTemplateResponse,
    responseDeserialize: deserialize_is_nsn_sketching_AddTemplateResponse,
  },
};

exports.SketchingServiceClient = grpc.makeGenericClientConstructor(SketchingServiceService);
