import * as grpc from '@grpc/grpc-js';
import { promisify } from 'util';
import { SketchingServiceClient } from './sketching_api_grpc_pb';

class SketchingClient {
  client = new SketchingServiceClient(
    'localhost:8001',
    grpc.credentials.createInsecure()
  );

  parseSketch = promisify(this.client.parseSketch).bind(this.client);
  // Do we want to allow standard clients to also add tempaltes? If so how?
  // addTemplate = promisify(this.client.addTemplate.bind(this.client));
}

export const sketchingClient = new SketchingClient();
