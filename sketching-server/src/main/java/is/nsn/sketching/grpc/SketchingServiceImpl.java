package is.nsn.sketching.grpc;

import io.grpc.stub.StreamObserver;
import is.nsn.sketching.ParseSketchRequest;
import is.nsn.sketching.ParseSketchResponse;
import is.nsn.sketching.Point;
import is.nsn.sketching.SketchingServiceGrpc;

public class SketchingServiceImpl extends SketchingServiceGrpc.SketchingServiceImplBase {

    @Override
    public void parseSketch(ParseSketchRequest request, StreamObserver<ParseSketchResponse> responseObserver) {
        // Call N$ methods from original builder to estimate response values
        System.out.println(request.getPointsList());

        ParseSketchResponse response = ParseSketchResponse.newBuilder()
                .setStart(Point.newBuilder().setX(0.0F).setY(0.0F).build())
                .setEnd(Point.newBuilder().setX(1.0F).setY(1.0F).build())
                .setShape(ParseSketchResponse.Shape.UNRECOGNIZED)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
