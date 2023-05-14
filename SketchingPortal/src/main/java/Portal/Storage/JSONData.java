package Portal.Storage;

import Portal.Helper.Direction;
import Portal.Storage.Point;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Model class for the data, that will be sent to the IDE
 * The recognition results are going to be mold into this form.
 */
public class JSONData {
    /**
     * Unique identifier.
     */
    private UUID Id;

    /**
     * The top-left corner of the enclosing rectangle
     */
    private Point root;

    /**
     * The point in the middle of a group
     */
    private Point middlePoint;

    /**
     * Height of the group
     */
    private double height;

    /**
     * Width of the group
     */
    private double width;

    /**
     * Radius of the Group.
     * it is important just for Circles
     */
    private double radius;

    /**
     * Starting Point of the group.
     * important for Arrow
     */
    private Point startingPoint;

    /**
     * Final Point of the group.
     * important for Arrow
     */
    private Point endPoint;

    /**
     * End Point of the group.
     * important for Arrow
     */
    private Point tip;

    /**
     * Direction of which the group is pointing at
     * important for Arrow
     * it is represented in degrees, N=0, E=90, S=180, W=270 and so on
     */
    private double pointingDirectionDegrees;

    /**
     * The abbreviated name of the Pointing Direction;
     */
    private String pointingDirectionName;

    /**
     * The time of the first point in the group
     */
    private Timestamp timeOfStartDrawing;

    /**
     * The time of the last point in the group
     */
    private Timestamp timeOfStopDrawing;

    /**
     * Time taken by the recognizer to recognize the group
     */
    private double timeOfRecognition;

    /**
     * Text / The type of Shape that the group is recognized as.
     */
    private String recognizedAs;

    /**
     * The text that the group is recognized by the OCR
     */
    private String text;

    /**
     * The score of the recognized shape
     */
    private double score;

    /**
     * Constructor
     */
    public JSONData(){
        Id = UUID.randomUUID();
        recognizedAs = "";
        root = new Point(0,0);
        middlePoint = new Point(0,0);
        startingPoint = new Point(0,0);
        endPoint = new Point(0,0);
        tip = new Point(0,0);
        height = 0;
        width = 0;
        radius = 0;
        score = 0;
        pointingDirectionDegrees = 0.0;
        pointingDirectionName = "Not important";
        timeOfStartDrawing = new Timestamp(System.currentTimeMillis());
        text = "";
        timeOfRecognition = 0;
    } 

    /**
     * Updates the Type of Group - Text/Line/Bracket/Arrow/Triangle/Square/Circle
     * @param newRecognizedAs the Type
     */
    public void updateRecognizedAs(String newRecognizedAs){
        recognizedAs = newRecognizedAs;
    }

    /**
     * Updates the root of Group
     * @param newRoot the new Root
     */
    public void updateRoot(Point newRoot){
        root = new Point(newRoot.getX(),newRoot.getY());
    }

    /**
     * Updates the middle point of Group
     * @param newMiddlePoint the new MiddlePoint
     */
    public void updateMiddlePoint(Point newMiddlePoint){
        middlePoint = new Point(newMiddlePoint.getX(),newMiddlePoint.getY());
    }

    /**
     * updates the Height, Width and Radius of the Group.
     * Radius is computed based on height and width.
     * @param newHeight the new Height
     * @param newWidth the new Width
     */
    public void updateDimensions(double newHeight, double newWidth){
        height = newHeight;
        width = newWidth;

        if(height > width){
            radius = height/2;
        }
        else{
            radius = width/2;
        }
    }

    /**
     * Updates the starting point of the group - that is the very first point.
     * @param newStartingPoint the new Starting Point
     */
    public void updateStartingPoint(Point newStartingPoint){
        startingPoint = new Point(newStartingPoint.getX(),newStartingPoint.getY());
    }

    public void updateEndPoint(Point newEndPoint){
        endPoint = new Point(newEndPoint.getX(), newEndPoint.getY());
    }

    /**
     * Updates the Directional variables: "Tip" and "Pointing Direction"
     * @param newTip the Tip
     * @param newPointingDirection the Direction
     */
    public void updatePointingAt(Point newTip, double newPointingDirection){
        tip = new Point(newTip.getX(),newTip.getY());
        pointingDirectionDegrees = newPointingDirection;
        pointingDirectionName = Direction.getDirectionByDegrees(pointingDirectionDegrees).getDirectionNameAbbreviated();
    }

    /**
     * Updates the Time variable when the stroke was drawn
     * @param newTime the Time
     */
    public void updateTimeOfStartingDrawing(Timestamp newTime){
        timeOfStartDrawing = newTime;
    }

    public void updateTimeOfStopDrawing(Timestamp newTime){
        timeOfStopDrawing = newTime;
    }

    /**
     * Updates the Text returned by the OCR
     * @param newText the Text
     */
    public void updateText(String newText){
        text = newText;
    }

    /**
     * Updates the Time it took the recognizer to recognize the group
     * @param newTimeOfRecognition the time
     */
    public void updateTimeOfRecognition(double newTimeOfRecognition){
        this.timeOfRecognition = newTimeOfRecognition;
    }

    /**
     * Update the Score of the recognition
     * @param newScore the Score
     */
    public void updateScore(double newScore){
        this.score = newScore;
    }

    /**
     * @return the Score
     */
    public double getScore(){
        return this.score;
    }

    /**
     * @return the unique ID
     */
    public UUID getId(){
        return this.Id;
    }

    /**
     * @return the Time taken by the Recognition engine to recognize the group
     */
    public double getTimeOfRecognition(){
        return this.timeOfRecognition;
    }

    /**
     * @return Type of Group
     */
    public String getRecognizedAs(){
        return recognizedAs;
    }

    /**
     * @return Root of Group
     */
    public Point getRoot(){
        return root;
    }

    /**
     * @return Middle Point of the Group
     */
    public Point getMiddlePoint(){
        return middlePoint;
    }

    /**
     * @return Very first point in the path of the Group
     */
    public Point getStartingPoint(){
        return startingPoint;
    }

    /**
     * @return Tip of the Group
     */
    public Point getTip(){
        return tip;
    }

    /**
     * @return Height of the Group
     */
    public double getHeight(){
        return height;
    }

    /**
     * @return Width of the Group
     */
    public double getWidth(){
        return width;
    }

    /**
     * @return Radius of the Group
     */
    public double getRadius(){
        return radius;
    }

    /**
     * @return Time when the Group was drawn
     */
    public Timestamp getTimeOfStartDrawing(){
        return timeOfStartDrawing;
    }

    public Timestamp getTimeOfStopDrawing(){
        return timeOfStopDrawing;
    }

    /**
     * @return Direction of which the Tip points at
     */
    public double getPointingDirectionDegrees(){
        return pointingDirectionDegrees;
    }

    public String getPointingDirectionName(){
        return pointingDirectionName;
    }

    /**
     * @return Recognized Text [OCR]
     */
    public String getText(){
        return text;
    }
}
