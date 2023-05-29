/**
 * This is a simple NextJS API handler to leverage the grpcClient
 */
import { grpcClient } from '../../lib/grpcClient';

export default async function handler(_req, res) {
  try {
    // Format of the argument to the gRPC method
    // This is effectively what the .proto file is describing
    const parsed = await grpcClient.parseSketch({
      strokes: [
        {
          points: [
            { x: 2.0, y: 2.0 },
            { x: 1.0, y: 1.0 },
          ],
        },
      ],
    });

    res.status(200).json({ parsed });
  } catch (err) {
    console.log(err);
    return res.status(500).send({ success: false });
  }
}
