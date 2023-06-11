package is.nsn.sketching.nDollar;

/**
 * Class representing a rectangle, holding a "root" point (x and y coordinates), height and width
 * Could be seen as a representative for the Bounding Box (root, and width and height)
 */
public class RectangleR {
    /**
     * Height and Width of the rectangle
     */
    private final double width;
    private final double height;
    /**
     * x and y Coordinates of the ROOT Point
     */
    private double rootX, rootY;

    /**
     * Constructor
     *
     * @param x      x Coordinate of the ROOT
     * @param y      y Coordinate of the ROOT
     * @param width  Width of the Rectangle
     * @param height Height of the Rectangle
     */
    public RectangleR(double x, double y, double width, double height) {
        this.rootX = x;
        this.rootY = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Getter for the x Coordinate of the Root
     *
     * @return x Coordinate
     */
    public double getX() {
        return Math.round(rootX);
    }

    /**
     * Setter of the x Coordinate of the Root
     *
     * @param value new value of the x Coordinate
     */
    public void setX(double value) {
        this.rootX = value;
    }

    /**
     * Getter for the y Coordinate of the Root
     *
     * @return y Coordinate
     */
    public double getY() {
        return Math.round(rootY);
    }

    /**
     * Setter of the y Coordinate of the Root
     *
     * @param value new value of the y Coordinate
     */
    public void setY(double value) {
        this.rootY = value;
    }

    /**
     * Getter for the Width of the Rectangle
     *
     * @return Width of the Rectangle
     */
    public double getWidth() {
        return Math.round(width);
    }

    /**
     * Getter for the Height of the Rectangle
     *
     * @return Height of the Rectangle
     */
    public double getHeight() {
        return Math.round(height);
    }
}