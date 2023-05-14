package Portal.Storage;

import javafx.scene.paint.Color;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Class that is representative for a Stroke in the Canvas.
 * The stroke is the output resulted after one touch-screen interaction (that is, from the PRESSING EVENT until the RELEASING EVENT of the touch-screen)
 * In other words, a drawn line would represent 1 stroke, while a '+' sign would represent 2 strokes (1 vertical line and 1 horizontal line).
 * It is a continuous stream of points.
 */
public class Stroke {
    /**
     * Unique identifier.
     */
    private UUID Id;

    /**
     * Starting point of the path.
     */
    private Point startPoint;

    /**
     * End point of the path.
     */
    private Point endPoint;

    /**
     * All the points (including startPoint and endPoint) in the path.
     */
    private ArrayList<Point> pathPoints;

    /**
     * The leftmost coordinate. Left side of the bounding box
     */
    private double leftBorderX;

    /**
     * The rightmost coordinate. Right side of the bounding box
     */
    private double rightBorderX;

    /**
     * The topmost coordinate. Top side of the bounding box
     */
    private double topBorderY;

    /**
     * The bottommost coordinate. Bottom side of the bounding box
     */
    private double bottomBorderY;

    /**
     * The exact time when the first point was added to the path of the stroke.
     */
    private Timestamp timeStartDrawing;

    /**
     * The exact time when the last point was added to the path of the stroke.
     */
    private Timestamp timeStopDrawing;

    /**
     * Time (in Milliseconds) since the last drawing
     * The First Regular (non-Extra) Stroke will have the time equals to 0, as well as each RED Stroke.
     */
    private long timeSinceLastDrawing;

    /**
     * Color of the Stroke (mainly for the Extra INK)
     */
    private Color color;

    /**
     * The thickness of the stroke
     */
    private double lineThickness = 0;

    /**
     * @return the Id
     */
    public UUID getId(){
        return Id;
    }

    /**
     * @return the Start Point
     */
    public Point getStartPoint(){
        return startPoint;
    }

    /**
     * @return the End Point
     */
    public Point getEndPoint(){
        return endPoint;
    }

    /**
     * @return the Path Points
     */
    public ArrayList<Point> getPathPoints(){
        return pathPoints;
    }

    /**
     * @return the Left Border
     */
    public double getLeftBorderX(){
        return leftBorderX;
    }

    /**
     * @return the Right Border
     */
    public double getRightBorderX(){
        return rightBorderX;
    }

    /**
     * @return the Top Border
     */
    public double getTopBorderY(){
        return topBorderY;
    }

    /**
     * @return the Bottom Border
     */
    public double getBottomBorderY(){
        return bottomBorderY;
    }

    /**
     * @return the Starting Time of the Stroke
     */
    public Timestamp getTimeStartDrawing(){
        return timeStartDrawing;
    }

    /**
     * @return the End Time of the Stroke
     */
    public Timestamp getTimeStopDrawing(){
        return timeStopDrawing;
    }

    /**
     * @return the time passed from previous stroke
     */
    public long getTimeSinceLastDrawing(){
        return timeSinceLastDrawing;
    }

    /**
     * Updates the color of the stroke
     * @param newColor the new Color
     */
    public void updateColor(Color newColor){
        color = newColor;
    }

    /**
     * @return the Color
     */
    public Color getColor(){
        return color;
    }

    /**
     * Updates the thickness of the stroke
     * @param newThickness the new thickness value
     */
    public void updateLineThickness(double newThickness){
        lineThickness = newThickness;
    }

    /**
     * @return the thickness of the stroke
     */
    public double getLineThickness(){
        return lineThickness;
    }

    /**
     * Empty Constructor
     */
    public Stroke() {
        Id = UUID.randomUUID();
        pathPoints = new ArrayList<>();
    }

    /**
     * Duplicate Constructor - used to duplicate a stroke;
     * @param s Stroke to be duplicated
     */
    public Stroke(Stroke s){
        Id = UUID.randomUUID();
        startPoint = s.getStartPoint();
        endPoint = s.getEndPoint();
        pathPoints = new ArrayList<>();
        for(Point p : s.getPathPoints()){
            pathPoints.add(new Point(p.getX(),p.getY()));
        }
        leftBorderX = s.getLeftBorderX();
        rightBorderX = s.getRightBorderX();
        topBorderY = s.getTopBorderY();
        bottomBorderY = s.getBottomBorderY();
        updateTimeStartDrawing(s.getTimeStartDrawing());
        updateTimeStopDrawing(s.getTimeStopDrawing());
        updateTimeSinceLastDrawing(s.getTimeSinceLastDrawing());
    }

    /**
     * UPDATE the stroke - First step.
     * Function called after creating an empty stroke
     * It adds the first points, instantiates the bounding box's border limits, and the starting time of the stroke.
     * @param startingPoint The first point in the path;
     */
    public void beginPath(Point startingPoint) {

        pathPoints.add(startingPoint);

        updateStartAndEndPoints();

        leftBorderX = startingPoint.getX();
        rightBorderX = leftBorderX;
        topBorderY = startingPoint.getY();
        bottomBorderY = topBorderY;

        updateTimeStartDrawing(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * UPDATE the stroke - Second step.
     * Usually done after beginning the path of an empty stroke
     * It adds a point to the Path, updates the End Point, and updates the bounding box's limits.
     * @param newPoint All the new, upcoming points.
     */
    public void addPointToPath(Point newPoint){
        // Add the point to the path
        pathPoints.add(newPoint);

        updateEndPoint(newPoint);

        // Update the border
        if(newPoint.getX() < leftBorderX){
            leftBorderX = newPoint.getX();
        }
        if(newPoint.getX() > rightBorderX){
            rightBorderX = newPoint.getX();
        }
        if(newPoint.getY() < topBorderY){
            topBorderY = newPoint.getY();
        }
        if(newPoint.getY() > bottomBorderY){
            bottomBorderY = newPoint.getY();
        }
    }

    /**
     * Function that adds a point to the path, at a specified position
     * It can trigger updates of the Starting and End Point, and also the limits of the bounding box
     * @param index index where the point will be added to
     * @param newPoint the Point
     */
    public void addPointToPathAt(int index, Point newPoint){
        // Add the point to the path
        pathPoints.add(index, newPoint);

        updateStartAndEndPoints();

        // Update the border
        if(newPoint.getX() < leftBorderX){
            leftBorderX = newPoint.getX();
        }
        if(newPoint.getX() > rightBorderX){
            rightBorderX = newPoint.getX();
        }
        if(newPoint.getY() < topBorderY){
            topBorderY = newPoint.getY();
        }
        if(newPoint.getY() > bottomBorderY){
            bottomBorderY = newPoint.getY();
        }
    }

    /**
     * Updates the start point
     * @param newPoint the new 'Start' Point
     */
    public void updateStartPoint(Point newPoint){
        startPoint = newPoint;
    }

    /**
     * Updates the end point
     * @param newPoint the new 'End' Point
     */
    public void updateEndPoint(Point newPoint) { endPoint = newPoint; }

    /**
     * UPDATE the stroke - Final step.
     * It trims down the path by removing overlapping points, and updates the end timestamp
     * In closing - when the stroke was drawn - it updates the endPoint.
     */
    public void finishPath(){
        if(pathPoints.size() > 2){
            for(int i = 1 ; i < pathPoints.size() ; i++) {
                // remove points that are overlapped
                if(pathPoints.get(i) == pathPoints.get(i - 1)){
                    pathPoints.remove(i);
                    i--;
                }
            }
        }

        if(pathPoints.size() - 1 >= 0) {
            updateEndPoint(pathPoints.get(pathPoints.size() - 1));
        }

        updateTimeStopDrawing(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Updates the time that passed since last drawing
     * @param newTime the new value
     */
    public void updateTimeSinceLastDrawing(long newTime){
        timeSinceLastDrawing = newTime;
    }

    /**
     * Updates the "timeStartDrawing" variable
     * @param newTime new value
     */
    public void updateTimeStartDrawing(Timestamp newTime){ timeStartDrawing = newTime; }

    /**
     * Updates the "timeStopDrawing" variable
     * @param newTime new value
     */
    public void updateTimeStopDrawing(Timestamp newTime){ timeStopDrawing = newTime; }

    /**
     * Function responsible with updating the extreme points: Start Point and End Point
     */
    private void updateStartAndEndPoints(){
        if(pathPoints.size() > 0){
            updateStartPoint(pathPoints.get(0));
            updateEndPoint(pathPoints.get(pathPoints.size() - 1));
        }
    }
}