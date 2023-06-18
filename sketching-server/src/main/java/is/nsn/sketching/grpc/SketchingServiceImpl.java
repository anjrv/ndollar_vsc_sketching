package is.nsn.sketching.grpc;

import io.grpc.stub.StreamObserver;
import is.nsn.sketching.*;
import is.nsn.sketching.nDollar.*;
import is.nsn.sketching.templates.Templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SketchingServiceImpl extends SketchingServiceGrpc.SketchingServiceImplBase {

    @Override
    public void parseSketch(ParseSketchRequest request, StreamObserver<ParseSketchResponse> responseObserver) {
        List<Stroke> strokes = request.getStrokesList();
        ArrayList<PointR> points = new ArrayList<>();

        for (Stroke s : strokes) {
            for (Point p : s.getPointsList()) {
               points.add(PointR.fromProto(p));
            }
        }

        BestListR parseResult = Recognizer.getInstance().recognize(points, strokes.size());

        // Example response format along with a debug output from the recognizer
        ParseSketchResponse response = ParseSketchResponse.newBuilder()
                .setStart(Point.newBuilder().setX(2.0F).setY(2.0F).build())
                .setEnd(Point.newBuilder().setX(1.0F).setY(1.0F).build())
                .setShape(ParseSketchResponse.Shape.SHAPE_CIRCLE)
                .setDebug(parseResult.getNameOfTopResult() + ", " + parseResult.getScoreOfTopResult())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addTemplate(AddTemplateRequest request, StreamObserver<AddTemplateResponse> responseObserver) {
        String key = request.getKey();

       List<Stroke> strokes = request.getStrokesList();
       ArrayList<ArrayList<PointR>> points = new ArrayList<>();

        for (Stroke s : strokes) {
            ArrayList<PointR> stroke = new ArrayList<>();
            for (Point p : s.getPointsList()) {
               stroke.add(PointR.fromProto(p));
            }
            points.add(stroke);
        }

        try {
            Templates.storeTemplate(key, points);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AddTemplateResponse response = AddTemplateResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
