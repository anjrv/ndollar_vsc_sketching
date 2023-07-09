// package: is.nsn.sketching
// file: sketching_api.proto

/* tslint:disable */
/* eslint-disable */

import * as jspb from "google-protobuf";

export class Point extends jspb.Message { 
    getX(): number;
    setX(value: number): Point;
    getY(): number;
    setY(value: number): Point;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Point.AsObject;
    static toObject(includeInstance: boolean, msg: Point): Point.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Point, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Point;
    static deserializeBinaryFromReader(message: Point, reader: jspb.BinaryReader): Point;
}

export namespace Point {
    export type AsObject = {
        x: number,
        y: number,
    }
}

export class Stroke extends jspb.Message { 
    clearPointsList(): void;
    getPointsList(): Array<Point>;
    setPointsList(value: Array<Point>): Stroke;
    addPoints(value?: Point, index?: number): Point;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Stroke.AsObject;
    static toObject(includeInstance: boolean, msg: Stroke): Stroke.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Stroke, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Stroke;
    static deserializeBinaryFromReader(message: Stroke, reader: jspb.BinaryReader): Stroke;
}

export namespace Stroke {
    export type AsObject = {
        pointsList: Array<Point.AsObject>,
    }
}

export class ParseSketchRequest extends jspb.Message { 
    clearStrokesList(): void;
    getStrokesList(): Array<Stroke>;
    setStrokesList(value: Array<Stroke>): ParseSketchRequest;
    addStrokes(value?: Stroke, index?: number): Stroke;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): ParseSketchRequest.AsObject;
    static toObject(includeInstance: boolean, msg: ParseSketchRequest): ParseSketchRequest.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: ParseSketchRequest, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): ParseSketchRequest;
    static deserializeBinaryFromReader(message: ParseSketchRequest, reader: jspb.BinaryReader): ParseSketchRequest;
}

export namespace ParseSketchRequest {
    export type AsObject = {
        strokesList: Array<Stroke.AsObject>,
    }
}

export class ParseSketchResponse extends jspb.Message { 

    hasStart(): boolean;
    clearStart(): void;
    getStart(): Point | undefined;
    setStart(value?: Point): ParseSketchResponse;

    hasEnd(): boolean;
    clearEnd(): void;
    getEnd(): Point | undefined;
    setEnd(value?: Point): ParseSketchResponse;
    getShape(): ParseSketchResponse.Shape;
    setShape(value: ParseSketchResponse.Shape): ParseSketchResponse;
    getDebug(): string;
    setDebug(value: string): ParseSketchResponse;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): ParseSketchResponse.AsObject;
    static toObject(includeInstance: boolean, msg: ParseSketchResponse): ParseSketchResponse.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: ParseSketchResponse, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): ParseSketchResponse;
    static deserializeBinaryFromReader(message: ParseSketchResponse, reader: jspb.BinaryReader): ParseSketchResponse;
}

export namespace ParseSketchResponse {
    export type AsObject = {
        start?: Point.AsObject,
        end?: Point.AsObject,
        shape: ParseSketchResponse.Shape,
        debug: string,
    }

    export enum Shape {
    SHAPE_RECTANGLE = 0,
    SHAPE_CIRCLE = 1,
    SHAPE_UNSPECIFIED = 2,
    }

}

export class AddTemplateRequest extends jspb.Message { 
    getKey(): string;
    setKey(value: string): AddTemplateRequest;
    clearStrokesList(): void;
    getStrokesList(): Array<Stroke>;
    setStrokesList(value: Array<Stroke>): AddTemplateRequest;
    addStrokes(value?: Stroke, index?: number): Stroke;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): AddTemplateRequest.AsObject;
    static toObject(includeInstance: boolean, msg: AddTemplateRequest): AddTemplateRequest.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: AddTemplateRequest, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): AddTemplateRequest;
    static deserializeBinaryFromReader(message: AddTemplateRequest, reader: jspb.BinaryReader): AddTemplateRequest;
}

export namespace AddTemplateRequest {
    export type AsObject = {
        key: string,
        strokesList: Array<Stroke.AsObject>,
    }
}

export class AddTemplateResponse extends jspb.Message { 

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): AddTemplateResponse.AsObject;
    static toObject(includeInstance: boolean, msg: AddTemplateResponse): AddTemplateResponse.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: AddTemplateResponse, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): AddTemplateResponse;
    static deserializeBinaryFromReader(message: AddTemplateResponse, reader: jspb.BinaryReader): AddTemplateResponse;
}

export namespace AddTemplateResponse {
    export type AsObject = {
    }
}
