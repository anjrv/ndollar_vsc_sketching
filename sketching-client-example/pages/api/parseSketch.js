/**
 * This is a simple NextJS API handler to leverage the grpcClient
 */
import { grpcClient } from '../../lib/grpcClient';

export default async function handler(req, res) {
  try {
    const parsed = await grpcClient.parseSketch({
      strokes: JSON.parse(req.body),
    });

    res.status(200).json({ parsed });
  } catch (err) {
    console.log(err);
    return res.status(500).send({ success: false });
  }
}
