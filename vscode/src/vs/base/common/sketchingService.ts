import * as grpc from '@grpc/grpc-js';
import { createDecorator } from 'vs/platform/instantiation/common/instantiation';
import { promisify } from 'util';
import { SketchingServiceClient } from 'sketching_api_grpc_pb';
import {
	ParseSketchResponse,
	ParseSketchRequest,
	Point,
	Stroke,
} from 'sketching_api_pb';

export const ISketchingService =
	createDecorator<ISketchingService>('sketchingService');

export interface ISketchingService {
	readonly _serviceBrand: undefined;

	addPoint(x: number, y: number): void;
	parseSketch(): Promise<ParseSketchResponse>;
}

export class SketchingService implements ISketchingService {
	_serviceBrand: undefined;

	private _client = new SketchingServiceClient(
		'localhost:8001',
		grpc.credentials.createInsecure(),
	);

	private _points: Point[] = [];

	private _parse = promisify(this._client.parseSketch).bind(this._client);

	addPoint(x: number, y: number): void {
		this._points.push(new Point().setX(x).setY(y));
	}

	parseSketch(): Promise<ParseSketchResponse> {
		const resp = this._parse(
			new ParseSketchRequest().setStrokesList([
				new Stroke().setPointsList(this._points),
			]),
		) as Promise<ParseSketchResponse>;

		this._points = [];

		return resp;
	}
}
