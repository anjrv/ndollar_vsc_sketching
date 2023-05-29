/**
 * Super simple wrapper class around the underlying gRPC client
 *
 * Shows how to load the proto definition file and add callable methods
 */
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { promisify } from 'util';

const PROTO_FILE = 'sketching_api.proto';
// Hack to step backwards out of the NodeJs project and grab the server proto definitions
// For now this allows deduplication but we can adjust as needed
const INCLUDE_DIR = [
  process.env.INIT_CWD + '/../sketching-server/src/main/proto/',
];

// Reads the server proto file and loads the definitions into memory
const protoDefinition = protoLoader.loadSync(INCLUDE_DIR + PROTO_FILE, {
  /* Some options we could explore*/
});
const proto = grpc.loadPackageDefinition(protoDefinition);

// This is a slightly scuffed way to make it spit out the definitions
// Could be simplified with typescript generated types
// But I think we won't have access or hopefully need that yet
// console.log(JSON.stringify(proto.is.nsn.sketching, null, 2));

class GrpcClient {
  client = new proto.is.nsn.sketching.SketchingService(
    'localhost:8001', // Port defined for the server
    grpc.credentials.createInsecure() // All local so we dont need ssl
  );

  // Bind the Service methods to promises to make them easier to work with
  // This enables simple async/await calls
  parseSketch = promisify(this.client.parseSketch).bind(this.client);
  addTemplate = promisify(this.client.addTemplate).bind(this.client);
}

export const grpcClient = new GrpcClient();
