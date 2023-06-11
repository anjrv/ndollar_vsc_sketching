package is.nsn.sketching.grpc;

import io.grpc.stub.StreamObserver;
import is.nsn.sketching.AddTemplateRequest;
import is.nsn.sketching.AddTemplateResponse;
import is.nsn.sketching.ParseSketchRequest;
import is.nsn.sketching.ParseSketchResponse;
import is.nsn.sketching.Point;
import is.nsn.sketching.SketchingServiceGrpc;
import is.nsn.sketching.nDollar.MultistrokeR;
import is.nsn.sketching.templates.Templates;

import java.io.IOException;

public class SketchingServiceImpl extends SketchingServiceGrpc.SketchingServiceImplBase {

    @Override
    public void parseSketch(ParseSketchRequest request, StreamObserver<ParseSketchResponse> responseObserver) {
        // TODO: Call N$ methods from original builder to estimate response values
        System.out.println(request);

        // Example response format
        ParseSketchResponse response = ParseSketchResponse.newBuilder()
                .setStart(Point.newBuilder().setX(2.0F).setY(2.0F).build())
                .setEnd(Point.newBuilder().setX(1.0F).setY(1.0F).build())
                .setShape(ParseSketchResponse.Shape.SHAPE_CIRCLE)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTemplate(AddTemplateRequest request, StreamObserver<AddTemplateResponse> responseObserver) {
        System.out.println(request);

        String key = request.getKey();
        try {
            Templates.storeTemplate(key, MultistrokeR.fromProto(key, request.getStrokesList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AddTemplateResponse response = AddTemplateResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
