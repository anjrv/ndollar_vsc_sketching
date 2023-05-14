package Portal.Storage;

import Portal.DolarN.PointR;

/**
 * Point class - as in a cartesian system.
 * It represents a location
 */
public class Point extends org.opencv.core.Point {
    /**
     * x - horizontal coordinate in the cartesian system.
     */
    private double x;

    /**
     * y - vertical coordinate in cartesian system.
     */
    private double y;

    /**
     * Constructor
     * @param x Horizontal coordinate
     * @param y Vertical coordinate
     */
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * @return The horizontal coordinate.
     */
    public double getX(){
        return this.x;
    }

    /**
     * @return The vertical coordinate.
     */
    public double getY(){
        return this.y;
    }

    /**
     * Updates the value of current x coordinate;
     *
     * @param newX The new value.
     */
    public void updateX(double newX){
        this.x = newX;
    }

    /**
     * Updates the value of current y coordinate;
     *
     * @param newY The new value.
     */
    public void updateY(double newY){
        this.y = newY;
    }

    public PointR toPointR(){
        return new PointR(this.x, this.y);
    }
}