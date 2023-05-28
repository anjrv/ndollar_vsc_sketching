package is.nsn.sketching;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import is.nsn.sketching.grpc.SketchingServiceImpl;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class SketchingServer 
{

    public static void main( String[] args ) throws IOException, InterruptedException {
        final int PORT = 8001;

        Server server = ServerBuilder
                .forPort(PORT)
                .addService(new SketchingServiceImpl()).build();

        System.out.println("Starting GRPC sketching api on port: " + PORT);
        server.start();
        server.awaitTermination();
    }
}
