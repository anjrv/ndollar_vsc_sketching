/**
 * This is a simple NextJS API handler to leverage the grpcClient
 */
import { grpcClient } from '../../lib/grpcClient';

export default async function handler(req, res) {
  const request = JSON.parse(req.body);

  try {
    await grpcClient.addTemplate(request);

    res.status(200).end();
  } catch (err) {
    console.log(err);
    return res.status(500).send({ success: false });
  }
}
