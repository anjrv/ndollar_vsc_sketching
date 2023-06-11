package is.nsn.sketching.nDollar;

import is.nsn.sketching.Point;

public class PointR {
    public double x, y;

    public PointR(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointR fromProto(Point proto) {
        return new PointR(proto.getX(), proto.getY());
    }

    public Point toProto() {
        return Point.newBuilder().setX((float) this.x).setY((float) this.y).build();
    }
}