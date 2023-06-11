package is.nsn.sketching.nDollar;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Class that is representative for a Stroke in the Canvas.
 * The stroke is the output resulted after one touch-screen interaction (that is, from the PRESSING EVENT until the RELEASING EVENT of the touch-screen)
 * In other words, a drawn line would represent 1 stroke, while a '+' sign would represent 2 strokes (1 vertical line and 1 horizontal line).
 * It is a continuous stream of points.
 */
public class StrokeR {
    /**
     * Unique identifier.
     */
    private final UUID Id;

    /**
     * Starting point of the path.
     */
    private PointR startPoint;

    /**
     * End point of the path.
     */
    private PointR endPoint;

    /**
     * All the points (including startPoint and endPoint) in the path.
     */
    private final ArrayList<PointR> pathPoints;

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
     * Empty Constructor
     */
    public StrokeR() {
        Id = UUID.randomUUID();
        pathPoints = new ArrayList<>();
    }

    /**
     * Duplicate Constructor - used to duplicate a stroke;
     *
     * @param s Stroke to be duplicated
     */
    public StrokeR(StrokeR s) {
        Id = UUID.randomUUID();
        startPoint = s.getStartPoint();
        endPoint = s.getEndPoint();
        pathPoints = new ArrayList<>();
        for (PointR p : s.getPathPoints()) {
            pathPoints.add(new PointR(p.x, p.y));
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
     * @return the id
     */
    public UUID getId() {
        return Id;
    }

    /**
     * @return the Start Point
     */
    public PointR getStartPoint() {
        return startPoint;
    }

    /**
     * @return the End Point
     */
    public PointR getEndPoint() {
        return endPoint;
    }

    /**
     * @return the Path Points
     */
    public ArrayList<PointR> getPathPoints() {
        return pathPoints;
    }

    /**
     * @return the Left Border
     */
    public double getLeftBorderX() {
        return leftBorderX;
    }

    /**
     * @return the Right Border
     */
    public double getRightBorderX() {
        return rightBorderX;
    }

    /**
     * @return the Top Border
     */
    public double getTopBorderY() {
        return topBorderY;
    }

    /**
     * @return the Bottom Border
     */
    public double getBottomBorderY() {
        return bottomBorderY;
    }

    /**
     * @return the Starting Time of the Stroke
     */
    public Timestamp getTimeStartDrawing() {
        return timeStartDrawing;
    }

    /**
     * @return the End Time of the Stroke
     */
    public Timestamp getTimeStopDrawing() {
        return timeStopDrawing;
    }

    /**
     * @return the time passed from previous stroke
     */
    public long getTimeSinceLastDrawing() {
        return timeSinceLastDrawing;
    }

    /**
     * UPDATE the stroke - First step.
     * Function called after creating an empty stroke
     * It adds the first points, instantiates the bounding box's border limits, and the starting time of the stroke.
     *
     * @param startingPoint The first point in the path;
     */
    public void beginPath(PointR startingPoint) {

        pathPoints.add(startingPoint);

        updateStartAndEndPoints();

        leftBorderX = startingPoint.x;
        rightBorderX = leftBorderX;
        topBorderY = startingPoint.y;
        bottomBorderY = topBorderY;

        updateTimeStartDrawing(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * UPDATE the stroke - Second step.
     * Usually done after beginning the path of an empty stroke
     * It adds a point to the Path, updates the End Point, and updates the bounding box's limits.
     *
     * @param newPoint All the new, upcoming points.
     */
    public void addPointToPath(PointR newPoint) {
        // Add the point to the path
        pathPoints.add(newPoint);

        updateEndPoint(newPoint);

        // Update the border
        updateBoundingBox(newPoint);
    }

    /**
     * Function that adds a point to the path, at a specified position
     * It can trigger updates of the Starting and End Point, and also the limits of the bounding box
     *
     * @param index    index where the point will be added to
     * @param newPoint the Point
     */
    public void addPointToPathAt(int index, PointR newPoint) {
        // Add the point to the path
        pathPoints.add(index, newPoint);

        updateStartAndEndPoints();

        // Update the border
        updateBoundingBox(newPoint);
    }

    private void updateBoundingBox(PointR newPoint) {
        if (newPoint.x < leftBorderX) {
            leftBorderX = newPoint.x;
        }
        if (newPoint.x > rightBorderX) {
            rightBorderX = newPoint.x;
        }
        if (newPoint.y < topBorderY) {
            topBorderY = newPoint.y;
        }
        if (newPoint.y > bottomBorderY) {
            bottomBorderY = newPoint.y;
        }
    }

    /**
     * Updates the start point
     *
     * @param newPoint the new 'Start' Point
     */
    public void updateStartPoint(PointR newPoint) {
        startPoint = newPoint;
    }

    /**
     * Updates the end point
     *
     * @param newPoint the new 'End' Point
     */
    public void updateEndPoint(PointR newPoint) {
        endPoint = newPoint;
    }

    /**
     * UPDATE the stroke - Final step.
     * It trims down the path by removing overlapping points, and updates the end timestamp
     * In closing - when the stroke was drawn - it updates the endPoint.
     */
    public void finishPath() {
        if (pathPoints.size() > 2) {
            for (int i = 1; i < pathPoints.size(); i++) {
                // remove points that are overlapped
                if (pathPoints.get(i) == pathPoints.get(i - 1)) {
                    pathPoints.remove(i);
                    i--;
                }
            }
        }

        if (pathPoints.size() - 1 >= 0) {
            updateEndPoint(pathPoints.get(pathPoints.size() - 1));
        }

        updateTimeStopDrawing(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Updates the time that passed since last drawing
     *
     * @param newTime the new value
     */
    public void updateTimeSinceLastDrawing(long newTime) {
        timeSinceLastDrawing = newTime;
    }

    /**
     * Updates the "timeStartDrawing" variable
     *
     * @param newTime new value
     */
    public void updateTimeStartDrawing(Timestamp newTime) {
        timeStartDrawing = newTime;
    }

    /**
     * Updates the "timeStopDrawing" variable
     *
     * @param newTime new value
     */
    public void updateTimeStopDrawing(Timestamp newTime) {
        timeStopDrawing = newTime;
    }

    /**
     * Function responsible with updating the extreme points: Start Point and End Point
     */
    private void updateStartAndEndPoints() {
        if (pathPoints.size() > 0) {
            updateStartPoint(pathPoints.get(0));
            updateEndPoint(pathPoints.get(pathPoints.size() - 1));
        }
    }
}