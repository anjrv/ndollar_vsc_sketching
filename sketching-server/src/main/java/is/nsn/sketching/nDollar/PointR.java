package is.nsn.sketching.nDollar;

import is.nsn.sketching.Point;

public class PointR {
    public double x, y;

    public PointR(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointR() {
        this.x = 0;
        this.y = 0;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public static PointR fromProto(Point proto) {
        return new PointR(proto.getX(), proto.getY());
    }

    public Point toProto() {
        return Point.newBuilder().setX((float) this.x).setY((float) this.y).build();
    }
}