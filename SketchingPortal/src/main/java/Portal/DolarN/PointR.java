package Portal.DolarN;

/**
 * Similar to the Storage.Point class
 * Base class for the DolarNRecognizer
 * holding an x and y fields (Coordinates in a Cartesian system).
 */
public class PointR {
    /**
     * x - Horizontal Coordinate;
     * y - Vertical Coordinate
     */
    public double x, y;

    /**
     * Constructor
     * @param x given Horizontal Coordinate
     * @param y given Vertical Coordinate
     */
    public PointR(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copying constructor
     * @param p given Point of which fields will be copied
     */
    public PointR(PointR p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Setter for the x Coordinate
     * @param value new Value for x
     */
    public void setX(double value){
        this.x = value;
    }

    /**
     * Setter for the y Coordinate
     * @param value new Value for y
     */
    public void setY(double value){
        this.y = value;
    }
}