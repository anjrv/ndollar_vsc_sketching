// package: is.nsn.sketching
// file: sketching_api.proto

/* tslint:disable */
/* eslint-disable */

import * as grpc from "@grpc/grpc-js";
import * as sketching_api_pb from "./sketching_api_pb";

interface ISketchingServiceService extends grpc.ServiceDefinition<grpc.UntypedServiceImplementation> {
    parseSketch: ISketchingServiceService_IParseSketch;
    addTemplate: ISketchingServiceService_IAddTemplate;
}

interface ISketchingServiceService_IParseSketch extends grpc.MethodDefinition<sketching_api_pb.ParseSketchRequest, sketching_api_pb.ParseSketchResponse> {
    path: "/is.nsn.sketching.SketchingService/ParseSketch";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<sketching_api_pb.ParseSketchRequest>;
    requestDeserialize: grpc.deserialize<sketching_api_pb.ParseSketchRequest>;
    responseSerialize: grpc.serialize<sketching_api_pb.ParseSketchResponse>;
    responseDeserialize: grpc.deserialize<sketching_api_pb.ParseSketchResponse>;
}
interface ISketchingServiceService_IAddTemplate extends grpc.MethodDefinition<sketching_api_pb.AddTemplateRequest, sketching_api_pb.AddTemplateResponse> {
    path: "/is.nsn.sketching.SketchingService/AddTemplate";
    requestStream: false;
    responseStream: false;
    requestSerialize: grpc.serialize<sketching_api_pb.AddTemplateRequest>;
    requestDeserialize: grpc.deserialize<sketching_api_pb.AddTemplateRequest>;
    responseSerialize: grpc.serialize<sketching_api_pb.AddTemplateResponse>;
    responseDeserialize: grpc.deserialize<sketching_api_pb.AddTemplateResponse>;
}

export const SketchingServiceService: ISketchingServiceService;

export interface ISketchingServiceServer extends grpc.UntypedServiceImplementation {
    parseSketch: grpc.handleUnaryCall<sketching_api_pb.ParseSketchRequest, sketching_api_pb.ParseSketchResponse>;
    addTemplate: grpc.handleUnaryCall<sketching_api_pb.AddTemplateRequest, sketching_api_pb.AddTemplateResponse>;
}

export interface ISketchingServiceClient {
    parseSketch(request: sketching_api_pb.ParseSketchRequest, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.ParseSketchResponse) => void): grpc.ClientUnaryCall;
    parseSketch(request: sketching_api_pb.ParseSketchRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.ParseSketchResponse) => void): grpc.ClientUnaryCall;
    parseSketch(request: sketching_api_pb.ParseSketchRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.ParseSketchResponse) => void): grpc.ClientUnaryCall;
    addTemplate(request: sketching_api_pb.AddTemplateRequest, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.AddTemplateResponse) => void): grpc.ClientUnaryCall;
    addTemplate(request: sketching_api_pb.AddTemplateRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.AddTemplateResponse) => void): grpc.ClientUnaryCall;
    addTemplate(request: sketching_api_pb.AddTemplateRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.AddTemplateResponse) => void): grpc.ClientUnaryCall;
}

export class SketchingServiceClient extends grpc.Client implements ISketchingServiceClient {
    constructor(address: string, credentials: grpc.ChannelCredentials, options?: Partial<grpc.ClientOptions>);
    public parseSketch(request: sketching_api_pb.ParseSketchRequest, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.ParseSketchResponse) => void): grpc.ClientUnaryCall;
    public parseSketch(request: sketching_api_pb.ParseSketchRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.ParseSketchResponse) => void): grpc.ClientUnaryCall;
    public parseSketch(request: sketching_api_pb.ParseSketchRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.ParseSketchResponse) => void): grpc.ClientUnaryCall;
    public addTemplate(request: sketching_api_pb.AddTemplateRequest, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.AddTemplateResponse) => void): grpc.ClientUnaryCall;
    public addTemplate(request: sketching_api_pb.AddTemplateRequest, metadata: grpc.Metadata, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.AddTemplateResponse) => void): grpc.ClientUnaryCall;
    public addTemplate(request: sketching_api_pb.AddTemplateRequest, metadata: grpc.Metadata, options: Partial<grpc.CallOptions>, callback: (error: grpc.ServiceError | null, response: sketching_api_pb.AddTemplateResponse) => void): grpc.ClientUnaryCall;
}
