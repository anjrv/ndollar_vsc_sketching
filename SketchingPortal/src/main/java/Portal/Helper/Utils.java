package Portal.Helper;

import Portal.CanvasUI;
import Portal.DolarN.NDolarRecognizer;
import Portal.DolarN.PointR;
import Portal.DolarN.RectangleR;
import Portal.DolarN.SizeR;
import Portal.Storage.JSONData;
import Portal.Storage.Point;
import Portal.Storage.Stroke;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class that provides multiple helpful functions.
 * Used mainly by the Canvas(Logic) and by the DolarNRecognizer
 */
public class Utils {

    public static String templateFolderPath = System.getProperty("user.dir")+"\\resource\\templateData";

    // string variables representing the general directional turn
    private static final String CLOCKWISE = "CLOCKWISE",
                                COUNTERCLOCKWISE = "COUNTERCLOCKWISE";

    /**
     * The minimum value of an angle to be considered sharp
     */
    public static final double MINIMUM_SHARP_ANGLE_NEW = 60.0;

    /**
     * Value needed to differentiate complex shapes from arrows
     * Will be used to check if the end of a stroke is close enough to the first part of the stroke.
     */
    public static final double MAX_MARGIN_ERROR_FOR_EXTREMITIES_TO_BE_CONSIDERED_CLOSE = 40.0;

    /**
     * VARIABLE USED ESPECIALLY WHEN CREATING THE GROUPS
     *
     * The minimum distance that show if two strokes have their extreme points (startPoint and endPoint) close enough in order to be considered close
     * ALSO USED when checking for the PROXIMITY of Strokes. That is to see if groups are close on each other's LEFT/RIGHT/UPPER/LOWER parts
     *
     * It was observed that, for a value lower than 35.0 (such as 15, 20, 25, 30), the text is always split into multiple groups, resulting into wrong results
     *  e.g. "HI" would result into 3 groups: H, the LINE from I, and the DOT from I
     * However, if the value goes over 50.0 (such 60, 70, 80, and above), too many strokes are grouped together, resulting also in wrong results.
     *  in this case, "HI" would be a correct group. However, if one is drawing a Circle, and an arrow starting from the software, both the arrow and the circle would be grouped together, resulting into a wrong recognition result.
     *
     * These observations are based on my personal style of writing text, used on both the Promethean board and the Lenovo Yoga 13'' laptop.
     *
     * A good value was observed to be 45.0, and at this value, the right groups would be created.
     */
    private static double MINIMUM_DISTANCE_FOR_EXTREME_POINTS = 45.0;

    /**
     * The offset used for creating Image Objects [helpful for the Recognizer]
     */
    private static int imgOffset = 100;

    /**
     * Parameter used by the removeConsecutiveClosePointsFilter function
     * It checks if consecutive points are far enough (3.0 dots is the maximu;
     */
    private static double MIN_DISTANCE_FILTER_1 = 3.0;

    /**
     * represents the minimum width a new "Letter" should have within a Text
     */
    private static final double TEXT_MIN_WIDTH = 15.0;

    /**
     * Variables used for scoring the Curvedness and Sharpness of a path
     * C -> Curvedness;
     * S -> Sharpness;
     * delta - lower bound
     * epsilon - upper bound
     */
    public static final double someConstantC = 0.5;
    public static final double deltaC = 10.0;
    public static final double epsilonC = 50.0;
    public static final double betaC = 160.0;
    public static final double maxObservedChange = 130.0;

    public static final double someConstantS = 0.12;
    public static final double deltaS = 15.0;
    public static final double epsilonS = 55.0;
    public static final double betaS = 160.0;

    /**
     * Given n consecutive points, this function returns the
     *      average weighted direction of the first one.
     *      for instance, for P1, P2, P3, P4 and P5, it computes:
     *          1) Angle of P1 towards P2,
     *          2)          P1 towards P3,
     *          3)          P1 towards P4
     *          4)          P1 towards P5
     *          Than it sums it up, and divides it by 4
     *
     * There is one direction (North) that was troublesome - on the way that it contains angles
     *      in the range of [0, 11.25] and [348.75, 360); Hence, an extra check is made.
     *          as any value that passes 360 starts again from 0.
     *
     *  This function is used by another function (getCompleteListOfAverageValuesOfDirections), and usually not called alone.
     *
     * @param subPathOfPoints ArrayList of given Points
     * @return The average weighted directional angle
     */
    public static double getAverageWeightedDirection(ArrayList<Point> subPathOfPoints){
        double averageDirection = 0;
        if(subPathOfPoints.size() > 1) {
            // These 3 next variables are added to correctly compute the values inside the [348.75, 11.25] interval
            int numberOfValuesImmediatelyGreaterThanZero = 0;
            boolean containsAtLeastOneAngleTowardsZeroFromTheRightSide = false;

            double angleBetweenConsecutivePoints;

            for (int i = 1; i < subPathOfPoints.size(); i++) {

                angleBetweenConsecutivePoints = getDirectionGivenByTwoPointsUpTo360(subPathOfPoints.get(0), subPathOfPoints.get(i));

                // the Higher Limit of NNE was used instead of the one of N just to take into consideration some margin of error.
                if (angleBetweenConsecutivePoints >= 0 && angleBetweenConsecutivePoints <= Direction.NNE.getHigherDegreeLimit()) {
                    numberOfValuesImmediatelyGreaterThanZero++;
                }

                if (angleBetweenConsecutivePoints >= Direction.N.getLowerDegreeLimit()) {
                    containsAtLeastOneAngleTowardsZeroFromTheRightSide = true;
                }

                averageDirection += angleBetweenConsecutivePoints;
            }

            if (containsAtLeastOneAngleTowardsZeroFromTheRightSide) {
                averageDirection += numberOfValuesImmediatelyGreaterThanZero * 360;
            }

            averageDirection /= (subPathOfPoints.size() - 1);

            return averageDirection % 360;
        }
        else{
            return 0;
        }
    }

    /**
     * Returns a list of directions for the entire path.
     * Its size will be 1 shorter than the size of the path, because the last point doesn't have any direction, as it is stationary.
     * This list is computed using average weighted values
     *      - taking minimum 1 and maximum 4 consecutive directions)
     * A detailed explanation can be found on the description of getAverageWeightedDirection
     *
     * @param pathPoints The list of Points representing the entire path.
     * @return A list of directional angle values.
     */
    public static ArrayList<Double> getCompleteListOfAverageValuesOfDirections(ArrayList<Point> pathPoints) {
        ArrayList<Double> arrayListOfAllAngleValues = new ArrayList<>();

        ArrayList<Point> subPath;

        for(int i = 0; i < pathPoints.size() - 1; i++) {
            // Creates the sub-path for each point
            subPath = new ArrayList<>();
            subPath.add(pathPoints.get(i));

            // takes up to 4 consecutive points
            for (int j = i + 1; j < i + 5; j++) {
                if (j < pathPoints.size()) {
                    subPath.add(pathPoints.get(j));
                }
            }

            // get their average weighted value;
            arrayListOfAllAngleValues.add(getAverageWeightedDirection(subPath));
        }
        return arrayListOfAllAngleValues;
    }

    /**
     * Given a list of points, it computes the average weighted list of all angles in the path.
     * For any given point, it uses up to 3 neighbours (on each side) to compute its angle
     *
     * @param pathList The list of Points representing the entire path.
     * @return A complete list of all the angles in the path.
     */
    public static ArrayList<Double> getCompleteListOfAngles(ArrayList<Point> pathList) {
        ArrayList<Double> completeListOfValuesOfAngles = new ArrayList<>();

        int n = pathList.size();

        ArrayList<Point> subPathOfConsecutivePoints;

        double currentAngle = 0;

        for(int i = 1 ; i < n - 1 ; i++){
            // takes up to
            int numberOfNeighbours = 3;
            if(i == 1 || i == n - 2){
                numberOfNeighbours = 1;
            }
            else if(i == 2 || i == n - 3){
                numberOfNeighbours = 2;
            }

            subPathOfConsecutivePoints = new ArrayList<>();
            // take a list of the neighbours
            for(int j = i - numberOfNeighbours ; j <= i + numberOfNeighbours ; j++){
                subPathOfConsecutivePoints.add(pathList.get(j));
            }

            currentAngle = getAverageWeightedAngleOfStreamOfPoints(subPathOfConsecutivePoints);
            completeListOfValuesOfAngles.add(currentAngle);
        }

        return completeListOfValuesOfAngles;
    }

    /**
     * Function that, if given an odd number of consecutive points (up to 7)
     *  gives the average weighted value of it.
     *  The way it is computed is:
     *      For each 2 consecutive points, the Line in between them will have a direction (computed through getDirectionGivenByTwoPointsUpTo180Reflected)
     *      Thus, there will be n-1 (even) consecutive such lines.
     *      the closest to the middle, the more weight it will have over the final result.
     *      The final result is the average weight of the directions (of these lines), normalized (this means that from all the direction, the first direction is subtracted -> first line will have the direction 0 - East. This means that all the other 'lines' will shift their direction with '- direction_of_line_one').
     *      Final adjustment to the result: Because an angle has 2 sides, the result will represent the actual angle/2.
     *      Hence, to get the real angle, it must be multiplied by 2.
     *
     * @param pointList the Points themselves
     * @return the average weighted value (of the middle point)
     */
    public static double getAverageWeightedAngleOfStreamOfPoints(ArrayList<Point> pointList){
        double numberOfPoints = pointList.size();
        // Get the weights - based on the number of points provided; there are 10 weights, of which at least 1 is non-zero
        double[] weights = getWeights((int)numberOfPoints);

        // if there are not enough points, the provided list doesn't have the specified number of points, or it has an Even number of points (4,6,8...)
        if( numberOfPoints < 3 ||
                numberOfPoints % 2 == 0 ||
                numberOfPoints > 7){
            return -1;
        }

        double  averageAngle = 0.0,
                angleBetweenConsecutivePoints = 0.0;
        Point P1, P2;

        int middlePointIndex = (int)Math.floor(numberOfPoints/2);
        int weightNumber = 0;

        ArrayList<Double> valuesOfAngles = new ArrayList<>();

        for(int i = 1 ; i <= numberOfPoints-1; i++){
            P1 = new Point(pointList.get(i-1).getX(), pointList.get(i-1).getY());
            P2 = new Point(pointList.get(i).getX(), pointList.get(i).getY());

            angleBetweenConsecutivePoints = getDirectionGivenByTwoPointsUpTo180Reflected(P1, P2);
            valuesOfAngles.add(angleBetweenConsecutivePoints);
        }

        double firstDirection = valuesOfAngles.get(0);
        for(int i = 0 ; i< valuesOfAngles.size() ; i++){
            double direction = Math.abs(valuesOfAngles.get(i) - firstDirection);
            if(direction > 180)
                direction = 360 - direction;
            valuesOfAngles.set(i, direction);
        }

        for(int i = 0 ; i < valuesOfAngles.size() ; i++){

            if((i + 1) <= middlePointIndex){
                weightNumber = middlePointIndex - (i + 1);
            }
            else{
                weightNumber = (i + 1) - 1 - middlePointIndex;
            }
            averageAngle += weights[weightNumber] * valuesOfAngles.get(i);
        }
        averageAngle = Math.abs(averageAngle * 2);

        return averageAngle;
    }

    /**
     * Holds the values of the weights used at getAverageWeightedAngleOfStreamOfPoints
     * @param numberOfContinuousPoints How many points are provided
     * @return The according weights
     */
    private static double[] getWeights(int numberOfContinuousPoints) {
        // the max number of weights that can be returned.
        int maxWeights = 3;
        double[] weights = new double[maxWeights];

        for(int i = 0 ; i < maxWeights ; i++){
            weights[i] = 0;
        }
        if (numberOfContinuousPoints == 3){ // the basic -> 1 neighbour in each side -> 2 consecutive lines
            weights[0] = 0.5;

        }
        else if (numberOfContinuousPoints == 5){ // 2 neighbours in each side -> 4 consecutive lines
            weights[0] = 0.275;
            weights[1] = 0.225;
        }
        else{ // 3 neighbours in each side -> 6 lines
            weights[0] = 0.2;
            weights[1] = 0.175;
            weights[2] = 0.125;
        }

        return weights;
    }

    /**
     * Returns the curvedness of a path
     * Based on the angle of each point
     * each angle will provide a different curvedness score
     * the curvedness is the sum of these scores, over the number of changes.
     *
     * @param pathPoints List of points representing a stroke
     * @return the computed curvedness
     */
    public double getCurvednessOfPath(ArrayList<Point> pathPoints, boolean newValuesAreGiven, double givenDeltaC, double givenEpsilonC){
        double curvedness = 0;

        ArrayList<Double> listOfDirectionalAngleChanges = getCompleteListOfAngles(pathPoints);
        int numberOfImportantChanges = 0;
        for(double alpha : listOfDirectionalAngleChanges){
            double score = getCScore(alpha, newValuesAreGiven, givenDeltaC, givenEpsilonC);

            curvedness = curvedness + score;

            numberOfImportantChanges++;
        }

        curvedness = curvedness / numberOfImportantChanges;

        if(curvedness < 0 || Double.isNaN(curvedness)){
            curvedness = 0;
        }

        return curvedness;
    }

    /**
     *  Given a change in angle (in degrees), it returns a curvedness score following:
     *      0, if alpha is small enough (< delta) or it doesn't make sense (>beta or is NaN)
     *      [Rewarded] 1, if it is curved - but not too curved (that is in between delta and epsilon)
     *      [Punished] -x, if it gets sharp (going >epsilon)
     *   where x depends on a constant variable, and a percentage of how close the angle itself is to a maximum observed change

     * @param alpha The given change in angle
     * @return Curvedness Score
     */
    public double getCScore(double alpha, boolean newValuesAreGiven, double givenDeltaC, double givenEpsilonC){

        double delta, epsilon;
        if(newValuesAreGiven){
            delta = givenDeltaC;
            epsilon = givenEpsilonC;
        }
        else{
            delta = deltaC;
            epsilon = epsilonC;
        }

        // doesn't make sense
        if(alpha < delta || alpha > betaC || Double.isNaN(alpha)) {
            // Change is too small, or it is too big (beta will never be touched, unless points are added from keyboard)
            return 0.0;
        }
        else if(alpha >= delta && alpha <= epsilon){
            // perfect curvedness
            return 1.0;
        }
        else{ // alpha > epsilon
            // here it gets too sharp, so it must be punished
            return (-1.0) * someConstantC * Math.pow((alpha / (maxObservedChange/2)),2);
        }
    }

    /**
     * Similar to the Curvedness Score
     *  Given a change in angle (in degrees), it returns a sharpness score following:
     *      0, if alpha is small enough (< delta) or it doesn't make sense (> beta or is NaN)
     *      [Punished] -x, if it is curved - but not too curved (that is in between delta and epsilon)
     *      [Rewarded]1, if it gets sharp (going >epsilon)
     *   where x depends on a constant variable, and a percentage of how close the angle itself is to a maximum observed change
     *
     * @param alpha The given change in angle
     * @return Sharpness Score
     */
    public static double getSScore(double alpha, boolean newValuesAreGiven, double givenDeltaS, double givenEpsilonS){

        double delta, epsilon;
        if(newValuesAreGiven){
            delta = givenDeltaS;
            epsilon = givenEpsilonS;
        }
        else{
            delta = deltaS;
            epsilon = epsilonS;
        }

        // doesn't make sense
        if(alpha < delta || alpha > betaS || Double.isNaN(alpha)) {
            // Change is too small, or it is too big (beta will never be touched, unless points are added from keyboard)
            return 0.0;
        }
        // curved - got to be punished
        else if(alpha >= delta && alpha <= epsilon){
            // here it starts to curve, so it must be punished
            double penalty = (-1.0) * (someConstantS) * (alpha / ((epsilon+delta) * 2 / 3));
            if(penalty < - 0.15)
                penalty = - 0.15;
            return penalty;
        }
        else{
            // perfect sharpness
            return 1.0;
        }
    }

    /**
     * Gets a list of points where the directions changes (from Clockwise to Counterclockwise and vice-versa)
     * @param pathPoints List of points representing a stroke
     * @return The list of "Critical" Points
     */
    public static Pair<ArrayList<Point>,String> getClockwiseAndCounterClockwiseVariables(ArrayList<Point> pathPoints){

        // -1 = CounterClockwise;
        // 0 = no change between two consecutive Directions
        // 1 = Clockwise
        ArrayList<Integer> arrayListOfClockwiseAndCounterClockwiseValues = new ArrayList<>();

        // will contain sizeOfPathPoints-1 values
        ArrayList<Double> arrayListOfAllAngleValues = getCompleteListOfAverageValuesOfDirections(pathPoints);

        for(int i = 1 ; i < arrayListOfAllAngleValues.size() ; i++){

            // directions between 0 and 15
            int indexOfPreviousDirection = Direction.getDirectionByDegrees(arrayListOfAllAngleValues.get(i-1)).getIndex(),
                indexOfCurrentDirection = Direction.getDirectionByDegrees(arrayListOfAllAngleValues.get(i)).getIndex();

            if(indexOfCurrentDirection == indexOfPreviousDirection){
                // no change
                arrayListOfClockwiseAndCounterClockwiseValues.add(0);
            }
            else if( (  indexOfCurrentDirection > indexOfPreviousDirection &&
                        Math.abs(indexOfCurrentDirection - indexOfPreviousDirection) <= 2) ||
                     (  indexOfPreviousDirection >= Direction.getNumberOfDirections() - 2 &&
                        indexOfCurrentDirection == 0)){
                // Counter-Clockwise
                arrayListOfClockwiseAndCounterClockwiseValues.add(1);
            }
            else{
                // Clockwise
                arrayListOfClockwiseAndCounterClockwiseValues.add(-1);
            }
        }

        ArrayList<Point> arrayListOfCriticalPointsRegardingClockwiseAndCounterClockwise = new ArrayList<>();

        int previousDirection = 0,
            indexOfPreviousDirection = 0;

        int indexOfCriticalPoint = 0;

        int numberOfClockwise = 0,
            numberOfCounterclockwise = 0;

        // see their Changes
        for(int i = 0 ; i < arrayListOfClockwiseAndCounterClockwiseValues.size() ; i++){

            if(arrayListOfClockwiseAndCounterClockwiseValues.get(i) == 1) {
                numberOfClockwise++;
            }
            else if(arrayListOfClockwiseAndCounterClockwiseValues.get(i) == -1) {
                numberOfCounterclockwise++;
            }

            if(previousDirection != 0){
                // check for changes in clockwise/counterclockwise;
                if(arrayListOfClockwiseAndCounterClockwiseValues.get(i) != 0) {
                    if (arrayListOfClockwiseAndCounterClockwiseValues.get(i) != previousDirection) {
                        indexOfCriticalPoint = (int) (Math.ceil((indexOfPreviousDirection + i)) / 2);

                        arrayListOfCriticalPointsRegardingClockwiseAndCounterClockwise.add(pathPoints.get(indexOfCriticalPoint));
                        previousDirection = arrayListOfClockwiseAndCounterClockwiseValues.get(i);
                    }
                    indexOfPreviousDirection = i;
                }
            }
            else{
                if(arrayListOfClockwiseAndCounterClockwiseValues.get(i) != 0){
                    previousDirection = arrayListOfClockwiseAndCounterClockwiseValues.get(i);
                    indexOfPreviousDirection = i;
                }
            }
        }

        String generalDirection = "";
        if(numberOfClockwise>numberOfCounterclockwise){
            generalDirection = CLOCKWISE;
        }
        else{
            generalDirection = COUNTERCLOCKWISE;
        }

        return new Pair<>(arrayListOfCriticalPointsRegardingClockwiseAndCounterClockwise, generalDirection);
    }

    /**
     * Similar to countTotalNumberOfZerosInDirectionalArray()
     * But instead of a higher margin of error used above (MAX_MARGIN_ERROR_FOR_DIRECTION_COUNT_INDEX_TO_BE_CONSIDERED_ZERO, or 3)
     * It checks if the count is < 1 (or == 0)
     * @param givenArray Direction count
     * @return Number of total directions that are not influencing.
     */
    public static int countPreciseTotalNumberOfZerosInDirectionalArray(int[] givenArray){
        int rez = 0;
        for(int i = 0 ; i < givenArray.length ; i++){
            if(givenArray[i] < 1)
                rez++;
        }
        return rez;
    }

    /**
     * Get the most predominant direction and
     *         second most predominant direction as pairs of (How many times it appears, Indexes of Direction)
     * @param arrayListOfDirectionCount Directional Count
     * @return both MostPredominantDirection (Apparitions, Index) and SecondMostPredominantDirection (Apparitions, Index)
     */
    public static Pair<Pair<Integer, Integer>,
                Pair<Integer, Integer>> getPredominantDirectionsVariables(int[] arrayListOfDirectionCount){
        int mostPredominantDirectionValue = 0,
                mostPredominantDirectionIndex = -1,
                secondMostPredominantDirectionValue = 0,
                secondMostPredominantDirectionIndex = -1;

        for(int i = 0; i < arrayListOfDirectionCount.length ; i++) {

            if (arrayListOfDirectionCount[i] > mostPredominantDirectionValue) {
                if(mostPredominantDirectionValue > secondMostPredominantDirectionValue){
                    secondMostPredominantDirectionValue = mostPredominantDirectionValue;
                    secondMostPredominantDirectionIndex = mostPredominantDirectionIndex;
                }

                mostPredominantDirectionValue = arrayListOfDirectionCount[i];
                mostPredominantDirectionIndex = i;

            } else if (arrayListOfDirectionCount[i] > secondMostPredominantDirectionValue) {
                secondMostPredominantDirectionValue = arrayListOfDirectionCount[i];
                secondMostPredominantDirectionIndex = i;
            }
        }
        return new Pair<>(new Pair<>(mostPredominantDirectionIndex,mostPredominantDirectionValue),new Pair<>(secondMostPredominantDirectionIndex,secondMostPredominantDirectionValue));
    }

    /**
     * Mostly used for the LINE recognition.
     * This function returns the direction (in degrees) of the most common or predominant direction
     * @param givenStroke the provided stroke
     * @return the direction (in degrees)
     */
    public static double getDirectionInDegreesOfMostPredominantDirection(Stroke givenStroke){
        // Direction count
        var directionCount = getDirectionCountEnhanced(givenStroke.getPathPoints());

        // the Direction of the Line
        int directionIndex = getPredominantDirectionsVariables(directionCount).getKey().getKey();

        double directionAverageDegrees = getAverageDirectionBasedOnMostPredominantDirection(directionIndex, givenStroke.getPathPoints());

        return directionAverageDegrees;
    }

    /**
     * Returns the degrees between two Points
     * @param P1 First Point
     * @param P2 Second Point
     * @return the degrees between these points
     */
    public static double getDirectionGivenByTwoPointsUpTo360(Point P1, Point P2){
        double angle = Math.toDegrees(Math.atan2(P2.getY() - P1.getY(), P2.getX() - P1.getX()));

        if(angle < 0){
            angle = (angle + 360);
        }

        return (angle + 90) % 360;
    }

    /**
     * Returns the direction, in degrees, between two Points, relative to the horizontal axis
     * The upper part is reflexive, meaning that going clockwise up to 180 degrees is similar to going counterclockwise
     *
     * @param P1 First Point
     * @param P2 Second Point
     * @return the degrees between these points
     */
    public static double getDirectionGivenByTwoPointsUpTo180Reflected(Point P1, Point P2){
        double theta = Math.atan2((P2.getY() - P1.getY()), (P2.getX() - P1.getX()));
        theta *= 180 / Math.PI;

        return (-1.0) * theta;
    }

    /**
     * Given the index of the general direction of a path, this function will return the AVERAGE direction (in degrees) of all appearing directions (in that directional index category) in that path
     * @param givenDirectionIndex index of the predominant (given) direction
     * @param pathPoints list of points provided
     * @return Average direction.
     */
    public static double getAverageDirectionBasedOnMostPredominantDirection(int givenDirectionIndex, ArrayList<Point> pathPoints){

        double rez = 0;
        int countNumber = 0;

        for(int i = 1 ; i < pathPoints.size() ; i++){
            double currentDirectionDegrees = getDirectionGivenByTwoPointsUpTo360(pathPoints.get(i - 1),pathPoints.get(i));

            if(Direction.getDirectionByDegrees(currentDirectionDegrees).equals(Direction.getDirectionByIndex(givenDirectionIndex))){
                rez += currentDirectionDegrees;
                countNumber++;
                if(givenDirectionIndex == Direction.N.getIndex()) {
                    if (currentDirectionDegrees <= Direction.N.getHigherDegreeLimit()) {
                        rez += 360;
                    }
                }
            }
        }

        if(countNumber > 0) {
            rez /= countNumber;
            rez %= 360;
        }
        else{
            rez = Direction.getDirectionByIndex(givenDirectionIndex).getMiddleDirection();
        }
        return rez;
    }

    /**
     * Provides the sharpness of a path, as well as a List of sharp points and their values
     *
     * @param pathPoints List of Points representing the Path
     * @param newValuesAreGiven That is a boolean variable stating if it is a TEST or not
     * @param givenDeltaS Delta TEST variable to see different values of the sharpness
     * @param givenEpsilonS Epsilon TEST variable to see different values of the sharpness
     * @return The Sharpness and list of <sharp points, their values>
     */
    public static Pair<Double,Pair<ArrayList<Double>,ArrayList<Point>>> getSharpnessAndAngleVariables(ArrayList<Point> pathPoints, boolean newValuesAreGiven, double givenDeltaS, double givenEpsilonS){

        double computedSharpness = 0.0;

        ArrayList<Double> listOfValuesRepresentingSharpestAngles = new ArrayList<>();
        ArrayList<Point> listOfPointsRepresentingSharpAngles = new ArrayList<>();

        int n = pathPoints.size();

        // these 2 next arrayLists will contain n-2 values each. [like the original list of points, but without their ends]
        ArrayList<Double> completeListOfValuesOfSharpAngles = getCompleteListOfAngles(pathPoints);
        ArrayList<Boolean> completeListOfBoolValuesRegardingSharpAngles = new ArrayList<>();

        for(int i = 0 ; i < completeListOfValuesOfSharpAngles.size() ; i++){
            if(completeListOfValuesOfSharpAngles.get(i) >= MINIMUM_SHARP_ANGLE_NEW){
                completeListOfBoolValuesRegardingSharpAngles.add(true);
            }
            else{
                completeListOfBoolValuesRegardingSharpAngles.add(false);
            }
        }

        if(n >= 3) {
            // there 2 next arrayLists will contain n-2 values each. [like the original list of points, but without their ends]

            // Clusters of angles are to be removed
            //   For it, I will take the sublist of sharp consecutive angles and get the highest angle there;
            //    The sharpest angle there will give the position of the second point after the actual sharp angle.
            //      Afterwards, I will modify the next to angles after the actual sharp angle to have the exact values of the 2 angles before it

            double sharpestAngleInTheCluster = 0.0;
            int clusterStartingIndex = 0,
                clusterFinishIndex = 0,
                indexOfSharpestAngleInTheCluster = 0;

            for(int i = 0; i< completeListOfBoolValuesRegardingSharpAngles.size(); i++){
                if(completeListOfBoolValuesRegardingSharpAngles.get(i) == true){
                    clusterStartingIndex = i;

                    for(int j = i + 1 ; j < i + 5 ; j++) {
                        if(j < completeListOfBoolValuesRegardingSharpAngles.size() &&
                                completeListOfBoolValuesRegardingSharpAngles.get(j) == true){
                            clusterFinishIndex = j;
                        }
                    }

                    if(clusterFinishIndex > clusterStartingIndex) {
                        // get the index of sharpest value in between start & finish indexes
                        // add the index_of_sharpest - 2 as sharp angle;
                        // set the true/false values in completeListOfBoolValuesRegardingSharpAngles of [index_of_sharpest] and [index_of_sharpest - 1] to false;
                        // update the new values of [index_of_sharpest]  and [index_of_sharpest - 1]

                        // getting the index;
                        sharpestAngleInTheCluster = completeListOfValuesOfSharpAngles.get(clusterStartingIndex);
                        indexOfSharpestAngleInTheCluster = clusterStartingIndex;
                        for (int j = clusterStartingIndex + 1; j <= clusterFinishIndex; j++) {
                            if (completeListOfValuesOfSharpAngles.get(j) > sharpestAngleInTheCluster) {
                                sharpestAngleInTheCluster = completeListOfValuesOfSharpAngles.get(j);
                                indexOfSharpestAngleInTheCluster = j;
                            }
                        }

                        // the actual sharp angle
                        int backStep = 0;
                        if (indexOfSharpestAngleInTheCluster != clusterStartingIndex) {
                            if (((clusterFinishIndex - clusterStartingIndex) + 1) >= 3) {
                                if(indexOfSharpestAngleInTheCluster - clusterStartingIndex >= 2) {
                                    backStep = 2;
                                } else{
                                    backStep = 1;
                                }
                            } else {
                                backStep = 1;
                            }
                        }
                        indexOfSharpestAngleInTheCluster = indexOfSharpestAngleInTheCluster - backStep;


                        // setting the false positives to false;
                        for(int j = clusterStartingIndex; j <= clusterFinishIndex ; j++){
                            if(j != indexOfSharpestAngleInTheCluster){
                                completeListOfBoolValuesRegardingSharpAngles.set(j, false);
                            }
                            else{
                                completeListOfBoolValuesRegardingSharpAngles.set(j, true);
                            }
                        }

                        // update the new values;
                        if(backStep == 2){
                            if(indexOfSharpestAngleInTheCluster - 1 >= 0) {
                                completeListOfValuesOfSharpAngles.set(indexOfSharpestAngleInTheCluster + 1, completeListOfValuesOfSharpAngles.get(indexOfSharpestAngleInTheCluster - 1));
                            }
                            if(indexOfSharpestAngleInTheCluster - 2 >= 0) {
                                completeListOfValuesOfSharpAngles.set(indexOfSharpestAngleInTheCluster + 2, completeListOfValuesOfSharpAngles.get(indexOfSharpestAngleInTheCluster - 2));
                            }
                        }
                        else{
                            if(indexOfSharpestAngleInTheCluster - 1 >= 0) {
                                completeListOfValuesOfSharpAngles.set(indexOfSharpestAngleInTheCluster + 1, completeListOfValuesOfSharpAngles.get(indexOfSharpestAngleInTheCluster - 1));
                            }
                        }
                        // if it is 0, nothing will happen.

                        // skip the tested values.
                        i = clusterFinishIndex;

                    }
                    // if there is only one sharp angle, it will be taken as sharp - it will be > 70
                }
                clusterFinishIndex = 0;
            }
        }

        // if it is a 'continuous' shape && there are not sharp angles in the beginning of the path
        // The intersection must be checked.
        if(isTheEndPointCloseToTheOverallPath(pathPoints) &&
                pathPoints.size() >= 8 &&
                !completeListOfBoolValuesRegardingSharpAngles.get(0) &&
                !completeListOfBoolValuesRegardingSharpAngles.get(1)&&
                !completeListOfBoolValuesRegardingSharpAngles.get(2)){

            ArrayList<Point> intersection = new ArrayList<>();

            for(int i = pathPoints.size() - 3 ; i < pathPoints.size() ; i++){
                intersection.add(pathPoints.get(i));
            }

            // physically shifted points
            double displacementX = (pathPoints.get(pathPoints.size()-1).getX() -  pathPoints.get(0).getX()),
                    displacementY = (pathPoints.get(pathPoints.size()-1).getY() -  pathPoints.get(0).getY());

            // index 0 is used to shift the first few points
            for(int i = 1 ; i <= 4 ; i++){
                Point newPoint = new Point(pathPoints.get(i).getX() + displacementX,
                                            pathPoints.get(i).getY() + displacementY);
                intersection.add(newPoint);
            }

            // Angle of intersection
            double averageAngleOfIntersection = getAverageWeightedAngleOfStreamOfPoints(intersection);

            if(averageAngleOfIntersection >= MINIMUM_SHARP_ANGLE_NEW){
                completeListOfBoolValuesRegardingSharpAngles.set(0,true);
                completeListOfValuesOfSharpAngles.set(0, averageAngleOfIntersection);
            }
        }
        // Remove points that are in a proximity of 3
        for(int i = 3 ; i < completeListOfBoolValuesRegardingSharpAngles.size() ; i++){
            if( completeListOfBoolValuesRegardingSharpAngles.get(i) &&
                (   completeListOfBoolValuesRegardingSharpAngles.get(i-1) ||
                    completeListOfBoolValuesRegardingSharpAngles.get(i-2) ||
                    completeListOfBoolValuesRegardingSharpAngles.get(i-3))){
                completeListOfBoolValuesRegardingSharpAngles.set(i, false);
            }
        }

        // add the points that are still true to the final result
        for(int i = 0 ; i < completeListOfValuesOfSharpAngles.size() ; i++){
            if(completeListOfBoolValuesRegardingSharpAngles.get(i)){
                listOfValuesRepresentingSharpestAngles.add(completeListOfValuesOfSharpAngles.get(i));
                listOfPointsRepresentingSharpAngles.add(pathPoints.get(i + 1));
            }
        }

        computedSharpness = 0;

        // There are values to work with
        int numberOfImportantChanges = 0;
        for (int i = 0 ; i < completeListOfValuesOfSharpAngles.size() ; i++) {

            double alpha = completeListOfValuesOfSharpAngles.get(i);

            double score = getSScore(alpha, newValuesAreGiven, givenDeltaS, givenEpsilonS);

            if(score == 1.0 && !completeListOfBoolValuesRegardingSharpAngles.get(i)){
                score = 0.9;
            }

            computedSharpness += score;

            // if it was rewarded, it was an angle. Hence, it will be added to the number of angles (that will be used later to get the final sharpness
            if (score > 0.0) {
                numberOfImportantChanges++;
            }
        }

        computedSharpness = computedSharpness / numberOfImportantChanges;

        if (computedSharpness < 0 || Double.isNaN(computedSharpness)) {
            computedSharpness = 0;
        }

        if(listOfPointsRepresentingSharpAngles.size() == 0){
            listOfPointsRepresentingSharpAngles = new ArrayList<>();
            listOfValuesRepresentingSharpestAngles = new ArrayList<>();
        }
        return new Pair<>(computedSharpness, new Pair<>(listOfValuesRepresentingSharpestAngles,listOfPointsRepresentingSharpAngles));
    }

    /**
     * Provides the distance between two given points
     * @param firstPoint First given Point
     * @param secondPoint Second given Point
     * @return The distance between them
     */
    public static double getDistanceBetweenTwoPoints(Point firstPoint, Point secondPoint){
        return Math.sqrt(Math.pow((firstPoint.getX() - secondPoint.getX()), 2) + Math.pow((firstPoint.getY() - secondPoint.getY()), 2));
    }

    /**
     * Checks if the End of a path is close to the first 25% of the stroke.
     * @param pathList the Path
     * @return True if the end is close enough, False otherwise
     */
    public static boolean isTheEndPointCloseToTheOverallPath(ArrayList<Point> pathList){
        // the first "25%"
        double beginningPercentage = 0.25;

        Point lastPoint = pathList.get(pathList.size() - 1);
        int firstPartOfThePath = (int)Math.ceil(beginningPercentage * pathList.size());

        for(int i = 0 ; i < firstPartOfThePath ; i++){
            if(getDistanceBetweenTwoPoints(pathList.get(i),lastPoint) <= MAX_MARGIN_ERROR_FOR_EXTREMITIES_TO_BE_CONSIDERED_CLOSE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the Direction Count of a path
     *  New approach: using 16 directions
     * @param pathPoints The path
     * @return Direction Count
     */
        public static int[] getDirectionCountEnhanced(ArrayList<Point> pathPoints){

        /* How many times the directions:
                N / N-NE / NE / E-NE /
                E / E-SE / SE / S-SE /
                S / S-SW / SW / W-SW /
                W / W-NW / NW / N-NW
            appears in the path
         */
        int[] directionCount = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        ArrayList<Double> listOfAverageDirectionInDegrees = getCompleteListOfAverageValuesOfDirections(pathPoints);

        for(int i = 0; i < listOfAverageDirectionInDegrees.size(); i++){
            int myDirection = Direction.getDirectionByDegrees(listOfAverageDirectionInDegrees.get(i)).getIndex();
            directionCount[myDirection]++;
        }

        return directionCount;
    }

    /**
     * Checks if this stroke overlap another given stroke.
     * @param stroke1 first stroke
     * @param stroke2 second (comparing) stroke
     * @return weather it overlap it or not.
     */
    public static boolean overlap(Stroke stroke1, Stroke stroke2){
        return stroke1.getLeftBorderX() < stroke2.getRightBorderX() &&
                stroke1.getRightBorderX() > stroke2.getLeftBorderX() &&
                stroke1.getTopBorderY() < stroke2.getBottomBorderY() &&
                stroke1.getBottomBorderY() > stroke2.getTopBorderY();
    }

    /**
     * Checks if this stroke is inside a given stroke
     * @param stroke1 first stroke
     * @param stroke2 second (comparing) stroke
     * @return complying to the conditions
     */
    public static boolean isInsideOf(Stroke stroke1, Stroke stroke2){
        return stroke1.getLeftBorderX() > stroke2.getLeftBorderX() &&
                stroke1.getRightBorderX() < stroke2.getRightBorderX() &&
                stroke1.getTopBorderY() > stroke2.getTopBorderY() &&
                stroke1.getBottomBorderY() < stroke2.getBottomBorderY();
    }

    /**
     * First filter: remove consecutive points that are too close
     * @param providedStroke Array List containing ONE stroke
     * @return the same list, but where there close points are removed.
     */
    public static ArrayList<Stroke> removeConsecutiveClosePointsFilter(ArrayList<Stroke> providedStroke){
        ArrayList<Stroke> rez = new ArrayList<>();
        Stroke resultingStroke = new Stroke();
        Stroke copyingStroke = providedStroke.get(0);

        resultingStroke.beginPath(copyingStroke.getStartPoint());
        if(copyingStroke.getPathPoints().size() > 2) {
            for (int i = 1; i < copyingStroke.getPathPoints().size(); i++) {
                if(getDistanceBetweenTwoPoints(resultingStroke.getPathPoints().get(resultingStroke.getPathPoints().size() - 1), copyingStroke.getPathPoints().get(i)) > MIN_DISTANCE_FILTER_1){
                    resultingStroke.addPointToPath(copyingStroke.getPathPoints().get(i));
               }
            }
        }
        if(resultingStroke.getPathPoints().size() == 1){
            if(copyingStroke.getPathPoints().size() > 1){
                resultingStroke.addPointToPath(copyingStroke.getEndPoint());
            }
        }
        resultingStroke.finishPath();
        rez.add(resultingStroke);
        return rez;
    }

    /**
     * Given two sets of strokes - (Start point, End Point) of each stroke - it returns the minimum distance,
     *  as well as a set of lal 4 distances
     * @param s1Start Start Point of First Stroke
     * @param s1End End Point of First Stroke
     * @param s2Start Start Point of Last Stroke
     * @param s2End End Point of Last Stroke
     * @return the minimum distance, as well as a set of all 4 distances (s1Start-s2Start, s1Start-S2End, S1End-S2Start, S1End-S2End)
     */
    public static Pair< Double,
                 Pair<Pair<Double,Double>,
                      Pair<Double,Double>>> getMinDistanceBetweenExtremitiesOf(Point s1Start, Point s1End, Point s2Start, Point s2End){
        double minDist = 0.0;
        double distanceBetweenStartPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke = getDistanceBetweenTwoPoints(s1Start,s2Start);
        double distanceBetweenStartPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke = getDistanceBetweenTwoPoints(s1Start,s2End);
        double distanceBetweenEndPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke = getDistanceBetweenTwoPoints(s1End,s2Start);
        double distanceBetweenEndPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke = getDistanceBetweenTwoPoints(s1End,s2End);

        // Minimum distance between these 4 just computed:
        minDist = distanceBetweenEndPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke;
        if (distanceBetweenEndPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke < minDist)
            minDist = distanceBetweenEndPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke;
        if (distanceBetweenStartPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke < minDist)
            minDist = distanceBetweenStartPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke;
        if (distanceBetweenStartPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke < minDist)
            minDist = distanceBetweenStartPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke;

        return new Pair<>(  minDist,
                            new Pair<>( new Pair<>( distanceBetweenStartPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke,
                                                    distanceBetweenStartPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke),
                                        new Pair<>( distanceBetweenEndPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke,
                                                    distanceBetweenEndPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke)));
    }

    /**
     * Returns the Merged Stroke of a group of strokes
     * @param originalStrokeList List of Strokes
     * @return Merged Stroke
     */
    public static ArrayList<Stroke> getStrokesAsAMergedStroke(ArrayList<Stroke> originalStrokeList){

        ArrayList<Stroke> mergedStrokeList = new ArrayList<>();
        Stroke mergedStroke = new Stroke();
        // If the current group has more then one stroke
        if (originalStrokeList.size() > 1) {
            // Merge them together into one stroke:

            // First, add the first stroke
            mergedStroke.beginPath(new Point(originalStrokeList.get(0).getStartPoint().getX(), originalStrokeList.get(0).getStartPoint().getY()));
            for(int i = 1 ; i < originalStrokeList.get(0).getPathPoints().size() ; i++){
                mergedStroke.addPointToPath(new Point(originalStrokeList.get(0).getPathPoints().get(i).getX(), originalStrokeList.get(0).getPathPoints().get(i).getY()));
            }

            // The starting point of the currently merged strokes. It is not complete yet, so these points might get updated.
            Portal.Storage.Point startPointOfMergedPath = new Point(mergedStroke.getStartPoint().getX(), mergedStroke.getStartPoint().getY());//mergedStroke.getPathPoints().get(0);

            // The end point of the currently merged strokes
            Portal.Storage.Point endPointOfMergedPath = new Point(mergedStroke.getEndPoint().getX(), mergedStroke.getEndPoint().getY());//mergedStroke.getPathPoints().get(mergedStroke.getPathPoints().size() - 1);

            int indexOfNextStroke = 0;

            ArrayList<Integer> strokesAdded = new ArrayList<>();
            strokesAdded.add(0);

            // For each remaining stroke in the group:
            // Compute the smallest distance between their extreme points;
            //      and the extreme points of the current shape
            //          and merge them together
            // Shift the strokes so their ends meet;
            //      and add the points to the merged stroke.
            for (int iteration = 0; iteration < originalStrokeList.size() - 1; iteration++) {
                double minDist = 9999999;

                for(int i = 0 ; i < originalStrokeList.size() ; i++){
                    if(!strokesAdded.contains(i)){
                        double minDistInCurrent = getMinDistanceBetweenExtremitiesOf(   startPointOfMergedPath,
                                                                                        endPointOfMergedPath,
                                                                                        new Point(  originalStrokeList.get(i).getStartPoint().getX(),
                                                                                                    originalStrokeList.get(i).getStartPoint().getY()),
                                                                                        new Point(  originalStrokeList.get(i).getEndPoint().getX(),
                                                                                                    originalStrokeList.get(i).getEndPoint().getY())
                                                                                    ).getKey();

                        if(minDistInCurrent < minDist){
                            minDist = minDistInCurrent;
                            indexOfNextStroke = i;
                        }
                    }
                }

                strokesAdded.add(indexOfNextStroke);
                // get the list of Points in their path

                var minimumDistVariable = getMinDistanceBetweenExtremitiesOf(startPointOfMergedPath,
                                                                                                                            endPointOfMergedPath,
                                                                                                                            new Point(  originalStrokeList.get(indexOfNextStroke).getStartPoint().getX(),
                                                                                                                                    originalStrokeList.get(indexOfNextStroke).getStartPoint().getY()),
                                                                                                                            new Point(  originalStrokeList.get(indexOfNextStroke).getEndPoint().getX(),
                                                                                                                                    originalStrokeList.get(indexOfNextStroke).getEndPoint().getY()));

                double distanceBetweenStartPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke = minimumDistVariable.getValue().getKey().getKey();
                double distanceBetweenStartPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke = minimumDistVariable.getValue().getKey().getValue();
                double distanceBetweenEndPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke = minimumDistVariable.getValue().getValue().getKey();
                double distanceBetweenEndPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke = minimumDistVariable.getValue().getValue().getValue();

                // remember to update StartPoint or EndPoint

                ArrayList<Point> pointArrayList = originalStrokeList.get(indexOfNextStroke).getPathPoints();

                double  displacementX = 0,
                        displacementY = 0;

                // Add the points for each case.
                if (minDist == distanceBetweenEndPointOfWhatWeHaveSoFarAndStartPointOfCurrentStroke) {
                    endPointOfMergedPath = new Point(originalStrokeList.get(indexOfNextStroke).getEndPoint().getX(),originalStrokeList.get(indexOfNextStroke).getEndPoint().getY());
                    // add the current stroke in THIS ORDER at the END of what we have so far
                    displacementX = (mergedStroke.getEndPoint().getX() -  originalStrokeList.get(indexOfNextStroke).getStartPoint().getX());
                    displacementY = (mergedStroke.getEndPoint().getY() -  originalStrokeList.get(indexOfNextStroke).getStartPoint().getY());

                    for(int point_index = 1 ; point_index < pointArrayList.size() ; point_index++){
                        mergedStroke.addPointToPath(new Portal.Storage.Point(pointArrayList.get(point_index).getX() + displacementX,
                                pointArrayList.get(point_index).getY() + displacementY));
                    }

                }
                else if(minDist == distanceBetweenEndPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke) {
                    endPointOfMergedPath = new Point(originalStrokeList.get(indexOfNextStroke).getStartPoint().getX(),originalStrokeList.get(indexOfNextStroke).getStartPoint().getY());
                    // add the current stroke in REVERSE ORDER at the END of what we have so far
                    displacementX = (mergedStroke.getEndPoint().getX() -  originalStrokeList.get(indexOfNextStroke).getEndPoint().getX());
                    displacementY = (mergedStroke.getEndPoint().getY() - originalStrokeList.get(indexOfNextStroke).getEndPoint().getY());

                    for(int point_index = pointArrayList.size() - 2; point_index >= 0; point_index--) {
                        mergedStroke.addPointToPath(new Portal.Storage.Point(pointArrayList.get(point_index).getX() + displacementX,
                                pointArrayList.get(point_index).getY() + displacementY));
                    }

                } else if(minDist == distanceBetweenStartPointOfWhatWeHaveSoFarAndEndPointOfCurrentStroke) {

                    startPointOfMergedPath = new Point( originalStrokeList.get(indexOfNextStroke).getStartPoint().getX(),
                                                        originalStrokeList.get(indexOfNextStroke).getStartPoint().getY());
                    // add the current stroke in THIS ORDER at the BEGINNING of what we have so far + update STARTPOINT
                    displacementX = (mergedStroke.getStartPoint().getX() - originalStrokeList.get(indexOfNextStroke).getEndPoint().getX());
                    displacementY = (mergedStroke.getStartPoint().getY() - originalStrokeList.get(indexOfNextStroke).getEndPoint().getY());

                    for(int point_index = pointArrayList.size() - 2; point_index >= 0; point_index--) {
                        mergedStroke.addPointToPathAt(0, new Portal.Storage.Point(pointArrayList.get(point_index).getX() + displacementX,
                                pointArrayList.get(point_index).getY() + displacementY));
                    }

                } else{
                    startPointOfMergedPath = new Point(originalStrokeList.get(indexOfNextStroke).getEndPoint().getX(), originalStrokeList.get(indexOfNextStroke).getEndPoint().getY());

                    // start to start
                    // add the current stroke in REVERSE ORDER at the BEGINNING of what we have so far + update STARTPOINT
                    displacementX = (mergedStroke.getStartPoint().getX() - originalStrokeList.get(indexOfNextStroke).getStartPoint().getX());
                    displacementY = (mergedStroke.getStartPoint().getY() - originalStrokeList.get(indexOfNextStroke).getStartPoint().getY());

                    for(int point_index=1;point_index<pointArrayList.size();point_index++){
                        mergedStroke.addPointToPathAt(0, new Portal.Storage.Point(pointArrayList.get(point_index).getX() + displacementX,
                                pointArrayList.get(point_index).getY() + displacementY));
                    }
                }
            }
            mergedStroke.finishPath();
        }
        else{
            mergedStroke = originalStrokeList.get(0);
        }

        mergedStrokeList.add(mergedStroke);
        return mergedStrokeList;
    }

    /**
     * Steps:
     *  - Determine the strokes representing the Tail & Head
     *          Longest initial stroke will be the tail
     *          head is the last stroke having the size smaller than the tail.
     *  - Determine the Direction by looking at the ~Last 35% of the tail
     *                  and the Tip of the Tail
     *   - Determine the TIP by finding the point in the head, with the Tip of the Tail.
     * @param originalGroupOfStrokes provided list of strokes.
     * @return Pair<Direction, Pair<Starting_Point,Tip>> of arrow
     */
    public static Pair<Double, Pair<Point, Point>> getArrowPointingVariables(ArrayList<Stroke> originalGroupOfStrokes){
        double direction = -1;

        Point tip = new Point(0,0);
        Point initialPoint = new Point(0,0);

        ArrayList<Point> tail = new ArrayList<>(),
                         head = new ArrayList<>();

        if(originalGroupOfStrokes.size() == 1){
            // if there is only 1 stroke, 70% will be the TAIL and 30% will be the TIP
            for(int i = 0 ; i < originalGroupOfStrokes.get(0).getPathPoints().size() ; i++){
                if(i < 0.65 * originalGroupOfStrokes.get(0).getPathPoints().size()) {
                    tail.add(originalGroupOfStrokes.get(0).getPathPoints().get(i));
                }
                else{
                    head.add(originalGroupOfStrokes.get(0).getPathPoints().get(i));
                }
            }
        }
        else{
            double longestPathLength = 0.0;
            int indexOfTail = 0;
            // the stroke (Points) having the longest path is the TAIL, and last stroke having the biggest dimension lower than 1/2 of the TAIL, or the last one.
            for(int i = 0 ; i < originalGroupOfStrokes.size() ; i++){
                double currentPathLength = getPathLength(originalGroupOfStrokes.get(i).getPathPoints());
                if(currentPathLength > longestPathLength){
                    indexOfTail = i;
                    longestPathLength = currentPathLength;
                    tail = new ArrayList<>();
                    tail.addAll(originalGroupOfStrokes.get(i).getPathPoints());
                }
            }

            double highestDimensionInTail = getBiggestDimensionOfStroke(originalGroupOfStrokes.get(indexOfTail));
            double shortestDimensionPath = highestDimensionInTail;
            int indexOfHead = -1;
            for(int i = 0 ; i < originalGroupOfStrokes.size() ; i++){
                double currentPathLength = getBiggestDimensionOfStroke(originalGroupOfStrokes.get(i));
                if(currentPathLength <= shortestDimensionPath){
                    indexOfHead = i;
                    shortestDimensionPath = currentPathLength;
                    if(currentPathLength <= shortestDimensionPath*(2/3)){
                        head = new ArrayList<>();
                        head.addAll(originalGroupOfStrokes.get(i).getPathPoints());
                    }
                }
            }
            if(head.size() == 0){
                head.addAll(originalGroupOfStrokes.get(indexOfHead).getPathPoints());
            }
        }

        // if the head is closer to the current tip of the tail, it doesn't need any reverse function.
        // otherwise, if the head is closer to the beginning of the current tail, than reverse the tail
        if( getDistanceBetweenTwoPoints(tail.get(tail.size()-1), head.get(0)) >
                getDistanceBetweenTwoPoints(tail.get(0), head.get(0))){
            //Tail was reversed
            ArrayList<Point> tailReversed = reversePathPointsInStroke(tail);
            tail = new ArrayList<>();
            tail.addAll(tailReversed);
        }

        // Compute the direction
        // First instinct: Based on the last 30% of the tail, - good, but not if there is only 1 stroke.
        // After testing more thoroughly the arrow with 1 stoke: based on a part of 30%, before the last 35% (between 35%- 65%)
        double lowerPercentageInTail = 0.70;
        double higherPercentageInTail = 1.00;
        if(originalGroupOfStrokes.size() == 1){
            lowerPercentageInTail = 0.35;
            higherPercentageInTail = 0.65;
        }

        Stroke lastPartTailStroke = new Stroke();
        boolean instantiated = false;
        for(int i = (int)(lowerPercentageInTail * tail.size()); i < (int)(higherPercentageInTail * tail.size()) ; i++){
            if(instantiated) {
                lastPartTailStroke.addPointToPath(tail.get(i));
            }
            else{
                instantiated = true;
                lastPartTailStroke.beginPath(tail.get(i));
            }
        }
        lastPartTailStroke.finishPath();

        direction = getDirectionInDegreesOfMostPredominantDirection(lastPartTailStroke);

        // Next, find the tip. It must be a point in the head.
        // The direction between the tip of the tail and this point (candidate for the Arrow's tip) must be similar (with a small margin of error) with the direction given by the tail.
        double biggestDistance = 0.0;
        Point tipOnTail = tail.get(tail.size()-1);
        boolean tipFound = false;
        int indexOfDirectionGivenByTail = Direction.getDirectionByDegrees(direction).getIndex();

        for(int i = 0 ; i < head.size() ; i++){
            double currentDistance = getDistanceBetweenTwoPoints(tipOnTail, head.get(i));
            double directionBetweenTailAndTip = getDirectionGivenByTwoPointsUpTo360(tipOnTail, head.get(i));
            int indexOfDirectionBetweenTailAndTip = Direction.getDirectionByDegrees(directionBetweenTailAndTip).getIndex();
            int indexDiff = Math.abs((indexOfDirectionBetweenTailAndTip - indexOfDirectionGivenByTail)) % Direction.getNumberOfDirections();
            if(currentDistance > biggestDistance && indexDiff <= 1){
                tipFound = true;
                biggestDistance = currentDistance;
                tip.updateX(head.get(i).getX());
                tip.updateY(head.get(i).getY());
            }
        }

        // in case none of the points have the same direction, as a last resort, take the first point in Head.
        if(!tipFound){
            //tip was not found! proceeding to find a tip
            var sharpnessVars = getSharpnessAndAngleVariables(head,false,0,0).getValue();
            if(sharpnessVars.getValue().size() == 0) {
                tip.updateX(head.get(0).getX());
                tip.updateY(head.get(0).getY());
            }
            else{
                tip.updateX(sharpnessVars.getValue().get(0).getX());
                tip.updateY(sharpnessVars.getValue().get(0).getY());
            }
        }

        // the initialPoint (or starting point) will be a point in the tail, either the first or the last one, which is furthest from the arrow's tip
        initialPoint = tail.get(0);
        if(tail.size() > 1 && getDistanceBetweenTwoPoints(tip, tail.get(tail.size()-1)) > getDistanceBetweenTwoPoints(tip, tail.get(0))){
            initialPoint = tail.get(tail.size() - 1);
        }

        return new Pair<>(direction, new Pair<>(initialPoint, tip));
    }

    /**
     * returns the biggest dimension (between height and width) of a stroke
     * @param s the stroke
     * @return the (size of the) biggest dimension
     */
    public static double getBiggestDimensionOfStroke(Stroke s){
        double height = s.getBottomBorderY()-s.getTopBorderY(),
                width = s.getRightBorderX() - s.getLeftBorderX();
        return Math.max(height, width);
    }

    /**
     * @param s path // list of points
     * @return reversed version of the provided path
     */
    public static ArrayList<Point> reversePathPointsInStroke(ArrayList<Point> s){
        ArrayList<Point> reverseS = new ArrayList<>();

        for(int i = s.size() - 1 ; i >= 0 ; i--){
            reverseS.add(s.get(i));
        }
        return reverseS;
    }

    /**
     * Recognition of Enclosure
     *
     * @param originalGroupOfStrokes the group of strokes - must be 1
     * @return the Score of the enclosure
     */
    public static double getEnclosureScoreHeuristics(ArrayList<Stroke> originalGroupOfStrokes) {
        // initial score is set to 0
        double enclosureScore = 0.0;
        // if the stroke was made in one go - it is eligible to be an Enclosure
        if(originalGroupOfStrokes.size() == 1) {

            // gets a copy, 'as a merged version' (unchanged, as it has exactly one stroke anyway)
            ArrayList<Portal.Storage.Stroke> groupOfStrokesMerged = getStrokesAsAMergedStroke(originalGroupOfStrokes);
            // over this copy, a filtering is applied (removing all close points)
            groupOfStrokesMerged = removeConsecutiveClosePointsFilter(groupOfStrokesMerged);

            //counting how many missing directions are there
            int numberOfMissingDirections = countPreciseTotalNumberOfZerosInDirectionalArray(getDirectionCountEnhanced(groupOfStrokesMerged.get(0).getPathPoints()));

            // constant: the allowed maximum number of missing directions
            int NUMBER_OF_MAXIM_MISSING_DIRECTIONS = 5;

            // if there are few enough missing directions
            // or, in other words, if it has a 'round-ish' shape - going towards all the possible directions
            if(numberOfMissingDirections <= NUMBER_OF_MAXIM_MISSING_DIRECTIONS) {

                // there are some tests that are checked:
                // - small amount of sharp angles
                // - small amount of clockwise/counterclockwise rotational changes
                // - it is a continuous shape

                var sharpnessVariables = getSharpnessAndAngleVariables(groupOfStrokesMerged.get(0).getPathPoints(), false, 0.0, 0.0);

                int enclosureTestsPassed = 0;
                int maxNumberOfSharpAngles = 2,
                        maxNumberOfDirectionBreakages = 5;

                int numberOfActualSharpAngles = 0;
                for (int i = 0; i < sharpnessVariables.getValue().getKey().size(); i++) {
                    if (sharpnessVariables.getValue().getKey().get(i) > (MINIMUM_SHARP_ANGLE_NEW * (5.0 / 4.0))) {
                        numberOfActualSharpAngles++;
                    }
                }

                // it must have few angles (max 2)
                boolean smallNumberOfSharpAngles = false;
                if (numberOfActualSharpAngles <= maxNumberOfSharpAngles) {
                    smallNumberOfSharpAngles = true;
                    enclosureTestsPassed++;
                }

                ArrayList<Portal.Storage.Point> clockwiseFaults = getClockwiseAndCounterClockwiseVariables(groupOfStrokesMerged.get(0).getPathPoints()).getKey();

                // small amount of clockwise changes
                boolean smallAmountOfClockwiseChanges = false;
                if (clockwiseFaults.size() >= 1 && // at least one directional change - Different than circle
                    clockwiseFaults.size() <= maxNumberOfDirectionBreakages) {
                    smallAmountOfClockwiseChanges = true;
                    enclosureTestsPassed++;
                }

                // and its ends must be close enough
                boolean continuousShape = false;
                if (isTheEndPointCloseToTheOverallPath(groupOfStrokesMerged.get(0).getPathPoints())) {
                    continuousShape = true;
                    enclosureTestsPassed++;
                }

                if (enclosureTestsPassed == 3) {
                    enclosureScore = 0.0;

                    if (smallNumberOfSharpAngles) {
                        enclosureScore += ((1.0 / 3.0) - 0.15 * (1 - Math.max(0.0, Math.min(1.0, ((maxNumberOfSharpAngles - numberOfActualSharpAngles) / maxNumberOfSharpAngles)))));
                    }

                    if (smallAmountOfClockwiseChanges) {
                        enclosureScore += (1.0 / 3.0);
                    }
                    if (continuousShape) {
                        enclosureScore += (1.0 / 3.0);
                    }
                }
            }
        }
        return enclosureScore;
    }

    /**
     * Function returning the pointing variable of a Line, previously recognized by the $N appraoch
     *
     * @param originalGroupOfStrokes group of provided strokes - must be one
     * @return tee highest score, alongside the Direction and the Tip of the line
     */
    public static Pair<Double, Portal.Storage.Point> getLinePointingVariables(ArrayList<Stroke> originalGroupOfStrokes) {
        var mergedStroke = getStrokesAsAMergedStroke(originalGroupOfStrokes).get(0);

        double lineDirection = getDirectionInDegreesOfMostPredominantDirection(mergedStroke);

        Point lineTip = mergedStroke.getEndPoint();

        return new Pair<>(lineDirection, lineTip);
    }

    /**
     * Function checking if a group (of strokes) represents a text or a shape
     * Main idea: it looks at the increase of width in the group, between consecutive strokes
     *            by excluding the vertical (and  slightly slant) lines
     *      - it takes the list of "added width" for the consecutive strokes
     *      - finds the highest addition (usually represents a character such 'O' or 'R')
     *      - checks how many 'big areas' are there throughout this list (>= 60% of the biggest found size)
     *      - if there are at least 2 (biggest character, and at least one more), it will switch the group from "Shape" into "Text"
     *
     * Also, consider a min/max limit of strokes (1/7)
     *  if the group contains only 1 stroke, it is not be a candidate for TEXT
     *  if the group contains > 7 strokes, it is, by default, TEXT
     *                                      That is because the shape with a potential high amount of strokes needed to be drawn is the "Split Screen"
     *
     *
     * Breaking case: IN - will not work, as the "I" brings a small increase in the size of the group
     * Accepted case: IRL
     *
     * @param groupOfStrokes provided Group (of Strokes)
     * @return true if it represents TEXT, false otherwise
     */
    public static boolean isText(ArrayList<Stroke> groupOfStrokes){
        // The initial check: group size
        int minLimit = 2,
            maxLimit = 7;
        if(groupOfStrokes.size() > maxLimit) {
            return true;
        }
        if(groupOfStrokes.size() < minLimit){
            return false;
        }

        // get array of width addition
        double[] sizeIncrease = new double[groupOfStrokes.size()];

        // The first character's addition
        sizeIncrease[0] = groupOfStrokes.get(0).getRightBorderX() - groupOfStrokes.get(0).getLeftBorderX();

        // variable holding the current right-most point - used to compute the addition
        double latestRightBorder = groupOfStrokes.get(0).getRightBorderX();

        // for every other stroke, computes the addition.
        for (int i = 1; i < groupOfStrokes.size(); i++) {
            double addition = groupOfStrokes.get(i).getRightBorderX() - latestRightBorder;
            double widthOfCurrentStroke = groupOfStrokes.get(i).getRightBorderX() - groupOfStrokes.get(i).getLeftBorderX();

            // if it goes on the right side (as all the languages - except Arabic and some more languages - have their vocabulary written from left to right)
            if (addition > 0 && widthOfCurrentStroke >= TEXT_MIN_WIDTH) { // it's not a simply vertical line
                // the current addition is saved as the size Increase - done by that particular stroke
                sizeIncrease[i] = addition;
                latestRightBorder = groupOfStrokes.get(i).getRightBorderX();
            } else {
                // the current stroke was drawn before(on the left side) the current max width
                sizeIncrease[i] = 0;
            }
        }

        // finding the biggest increase in the list - to have it as a guideline
        double maxIncrease = 0.0;
        for (int i = 0; i < sizeIncrease.length; i++) {
            if (maxIncrease < sizeIncrease[i]) {
                maxIncrease = sizeIncrease[i];
            }
        }

        // finds the number of "areas" which are big - flagging CHARACTERS
        int numBigIncreases = 0;
        for (int i = 0; i < sizeIncrease.length; i++) {
            if (sizeIncrease[i] >= 0.6 * maxIncrease) {
                numBigIncreases++;
            }
        }
        if (numBigIncreases >= 2) { // beside the "biggest" increase, there is at least one more increase (Letters having around the same width), than it might be a
            System.out.println("current SHAPE is going to be shifted to TEXT");
            return true;
        }

        return false;
    }

    /**
     * Function checking whether the provided list contains an item
     * @param givenList provided list
     * @param itemToBeTested checked item
     * @return TRUE if the item is included in the list; FALSE otherwise
     */
    public static boolean listOfJSONContainsItem(ArrayList<JSONData> givenList, JSONData itemToBeTested){
        if(givenList.size() == 0) {
            return false;
        }

        for(int i = 0 ; i < givenList.size() ; i++) {
            JSONData currentItem =  givenList.get(i);
            if( currentItem.getScore() == itemToBeTested.getScore() &&
                currentItem.getRecognizedAs().equals(itemToBeTested.getRecognizedAs()) &&
                currentItem.getRoot().getX() == itemToBeTested.getRoot().getX() &&
                currentItem.getRoot().getY() == itemToBeTested.getRoot().getY() &&
                currentItem.getHeight() == itemToBeTested.getHeight() &&
                currentItem.getWidth() == itemToBeTested.getWidth() &&
                currentItem.getMiddlePoint().getX() == itemToBeTested.getMiddlePoint().getX() &&
                currentItem.getMiddlePoint().getY() == itemToBeTested.getMiddlePoint().getY()
            ){
                return true;
            }
        }
        return false;
    }

    /**
     * Function returning whether two strokes are in each other's proximity
     * That is, if they are intersecting, or spatially close to each other (within 50px on the screen0
     *
     * Mainly used by the grouping algorithm,
     *
     * @param s1 Stroke #1
     * @param s2 Stroke #2
     * @param maxDist maximum checked - if negative, it will take the default value [25px]
     * @return Whether the two strokes are spatially close
     */
    public static boolean strokesAreInProximity(Stroke s1, Stroke s2, double maxDist){
        if(maxDist < 0){
            maxDist = MINIMUM_DISTANCE_FOR_EXTREME_POINTS; // TO BE TESTED on the big board. if not working properly, increase this distance to 2x
        }
        double stroke1LeftBorder = s1.getLeftBorderX(),
                stroke1RightBorder = s1.getRightBorderX(),
                stroke1TopBorder = s1.getTopBorderY(),
                stroke1BottomBorder = s1.getBottomBorderY();

        double stroke2LeftBorder = s2.getLeftBorderX(),
                stroke2RightBorder = s2.getRightBorderX(),
                stroke2TopBorder = s2.getTopBorderY(),
                stroke2BottomBorder = s2.getBottomBorderY();

        boolean horizontalProximityCheck = false,
                verticalProximityCheck = false;

        // check if both HORIZONTAL and VERTICAL spaces are within each other's proximity

        // VERTICAL proximity:
        // if stroke1 is within stroke2 (vertically speaking)
        if(stroke2LeftBorder <= stroke1LeftBorder && stroke2RightBorder >= stroke1RightBorder)
            verticalProximityCheck = true;
            // if stroke2 is within stroke1 (vertically speaking)
        else if(stroke1LeftBorder <= stroke2LeftBorder && stroke1RightBorder >= stroke2RightBorder)
            verticalProximityCheck = true;
            // if stroke1 intersects stroke2 on (stroke1's) Right side (vertically speaking)
        else if(stroke1LeftBorder <= stroke2LeftBorder && stroke1RightBorder <= stroke2RightBorder && stroke1RightBorder >= stroke2LeftBorder)
            verticalProximityCheck = true;
            // if stroke1 intersects stroke2 on (stroke1's) Left side (vertically speaking)
        else if(stroke1LeftBorder >= stroke2LeftBorder && stroke1RightBorder >= stroke2RightBorder && stroke1LeftBorder <= stroke2RightBorder)
            verticalProximityCheck = true;
            // if stroke1 and stroke2 do not intersect,
            // but any parameter from stroke1 is close to any parameter from stroke2 (vertically speaking) -
        else if(Math.abs(stroke1LeftBorder - stroke2LeftBorder) <= maxDist ||
                Math.abs(stroke1LeftBorder - stroke2RightBorder) <= maxDist ||
                Math.abs(stroke1RightBorder - stroke2LeftBorder) <= maxDist ||
                Math.abs(stroke1RightBorder - stroke2RightBorder) <= maxDist)
            verticalProximityCheck = true;

        // HORIZONTAL proximity:
        // if stroke1 is within stroke2 (horizontally speaking)
        if(stroke2TopBorder <= stroke1TopBorder && stroke2BottomBorder >= stroke1BottomBorder)
            horizontalProximityCheck = true;
            // if stroke2 is within stroke1 (horizontally speaking)
        else if(stroke1TopBorder <= stroke2TopBorder && stroke1BottomBorder >= stroke2BottomBorder)
            horizontalProximityCheck = true;
            // if stroke1 intersects stroke2 on (stroke1's) Bottom side (horizontally speaking)
        else if(stroke1TopBorder <= stroke2TopBorder && stroke1BottomBorder <= stroke2BottomBorder && stroke1BottomBorder >= stroke2TopBorder)
            horizontalProximityCheck = true;
            // if stroke1 intersects stroke2 on (stroke1's) Top side (horizontally speaking)
        else if(stroke1TopBorder >= stroke2TopBorder && stroke1BottomBorder >= stroke2BottomBorder && stroke1TopBorder <= stroke2BottomBorder)
            horizontalProximityCheck = true;
            // if stroke1 and stroke2 do not intersect,
            // but any parameter from stroke1 is close to any parameter from stroke2 (horizontally speaking) -
        else if(Math.abs(stroke1TopBorder - stroke2TopBorder) <= maxDist ||
                Math.abs(stroke1TopBorder - stroke2BottomBorder) <= maxDist ||
                Math.abs(stroke1BottomBorder - stroke2TopBorder) <= maxDist ||
                Math.abs(stroke1BottomBorder - stroke2BottomBorder) <= maxDist)
            horizontalProximityCheck = true;

        // if both checks are passing, the two strokes are in each other's proximity
        return (horizontalProximityCheck && verticalProximityCheck);
    }

    /**
     * Finds the bounding box of a list of Points
     * @param points List of Points
     * @return the Bounding Box
     */
    public static RectangleR findBoundingBox(ArrayList<PointR> points) {
        double minX = points.get(0).x;
        double maxX = points.get(0).x;
        double minY = points.get(0).y;
        double maxY = points.get(0).y;

        for(PointR p : points) {
            if (p.x < minX) {
                minX = p.x;
            }

            if (p.x > maxX) {
                maxX = p.x;
            }

            if (p.y < minY) {
                minY = p.y;
            }

            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        return new RectangleR(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Calculates the distanceBetweenPointRs between two points
     * @param p1 first given point
     * @param p2 second given point
     * @return the distance between p1 and p2
     */
    public static double distanceBetweenPointRs(PointR p1, PointR p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Compute the centroid of the points given
     * @param points given List of Points
     * @return the Centroid Point
     */
    public static PointR Centroid(ArrayList<PointR> points) {
        double xsum = 0.0;
        double ysum = 0.0;

        for(PointR p : points)
        {
            xsum += p.x;
            ysum += p.y;
        }
        return new PointR(xsum / points.size(), ysum / points.size());
    }

    /**
     * Computes the complete length of a path (between consecutive points)
     * @param points list of Points
     * @return complete length of the path
     */
    public static double getPathLengthR(ArrayList<PointR> points) {
        double length = 0;
        for (int i = 1; i < points.size(); i++)
        {
            length += distanceBetweenPointRs((PointR) points.get(i - 1), (PointR) points.get(i));
        }
        return length;
    }

    /**
     * Similar to getPathLengthR, but it applies over an ArrayList<Point> instead of an ArrayList<PointR>
     *
     * Computes the complete length of a path (between consecutive points)
     *
     * @param points list of Points
     * @return complete length of the path
     */
    public static double getPathLength(ArrayList<Point> points) {
        double length = 0;
        for (int i = 1; i < points.size(); i++)
        {
            length += getDistanceBetweenTwoPoints(points.get(i - 1),points.get(i));
        }
        return length;
    }

    /**
     * determines the angle, in radians, between two points. the angle is defined
     * by the circle centered on the start point with a radius to the end point,
     * where 0 radians is straight right from start (+x-axis) and PI/2 radians is
     * straight down (+y-axis).
     *
     * @param start Start Point
     * @param end End Point
     * @param positiveOnly Mirrored (also negative) values, or positive only?
     * @return the Angle between startPoint and endPoint, in Radians
     */
    public static double AngleInRadians(PointR start, PointR end, boolean positiveOnly) {
        double radians = 0.0;
        if (start.x != end.x) {
            radians = Math.atan2(end.y - start.y, end.x - start.x);
        }
        else { // pure vertical movement
            if (end.y < start.y)
                radians = -Math.PI / 2.0; // -90 degrees is straight up
            else if (end.y > start.y)
                radians = Math.PI / 2.0; // 90 degrees is straight down
        }
        if (positiveOnly && radians < 0.0) {
            radians += Math.PI * 2.0;
        }
        return radians;
    }

    /**
     * Converts an angle from Degrees to Radians
     * @param deg angle in Degrees
     * @return angle in Radians
     */
    public static double Deg2Rad(double deg)
    {
        return (deg * Math.PI / 180d);
    }

    /**
     * Function which rotate the points by the given radians about their centroid
     * @param points given List of Points
     * @param radians amount of Radians with which the points will be rotated
     * @return Rotated List of Strokes
     */
    public static ArrayList<PointR> RotateByRadians(ArrayList<PointR> points, double radians) {
        ArrayList<PointR> newPoints = new ArrayList<PointR>(points.size());
        PointR c = Centroid(points);

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double cx = c.x;
        double cy = c.y;

        for(int i = 0; i < points.size(); i++) {
            PointR p = points.get(i);

            double dx = p.x - cx;
            double dy = p.y - cy;

            PointR q = new PointR(dx * cos - dy * sin + cx, dx * sin + dy * cos + cy);

            newPoints.add(q);
        }

        return newPoints;
    }

    /**
     * Calculate the angle from the initial point of the array of Points (points[0])
     * to points[index].
     *
     * The index is, in fact, the 8th element from the list
     *
     * Returns this angle represented by a unit vector (stored in a Point).
     *
     * **This is used in Gesture.cs:Gesture() to compute the start angle in support
     * of the optimization to not compare candidates to templates whose start angles
     * are widely different.
     *
     * @param points given List of Points
     * @param index the index (8 by default)
     * @return the angle between the 1st and the 8th point, as a Unit Vector, from the list of points
     */
    public static PointR CalcStartUnitVector(ArrayList<PointR> points, int index) {
        // v is the vector from points[0] to points[index]
        PointR v = new PointR(((PointR)points.get(index)).x - ((PointR)points.get(0)).x,
                ((PointR)points.get(index)).y - ((PointR)points.get(0)).y);

        // len is the length of vector v
        double len = Math.sqrt(v.x * v.x + v.y * v.y);
        // the unit vector representing the angle between points[0] and points[index]
        // is the vector v divided by its length len
        return new PointR(v.x / len, v.y / len);
    }

    /**
     * Compute the acute angle between unit vectors [ from (0,0) to v1, and (0,0) to v2 ]
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the angle between the vectors
     */
    public static double AngleBetweenUnitVectors(PointR v1, PointR v2) {
        double test = v1.x * v2.x + v1.y * v2.y; // arc cosine of the vector dot product
        // sometimes these two cases can happen because of rounding error in the dot product calculation
        if (test < -1.0)
            test = -1.0; // truncate rounding errors
        if (test > 1.0)
            test = 1.0; // truncate rounding errors
        return Math.acos(test);
    }

    /**
     * translates the points so that their centroid lies at 'toPt'
     *
     * @param points list of Points
     * @param toPt given points
     * @return translated list of points
     */
    public static ArrayList<PointR> TranslateCentroidTo(ArrayList<PointR> points, PointR toPt) {
        ArrayList<PointR> newPoints = new ArrayList<PointR>(points.size());
        PointR centroid = Centroid(points);
        for (int i = 0; i < points.size(); i++) {
            PointR p = (PointR) points.get(i);
            p.x += (toPt.x - centroid.x);
            p.y += (toPt.y - centroid.y);
            newPoints.add(p);
        }
        return newPoints;
    }

    /**
     * Scaling method (to fit the given size)
     *
     * @param pts list of points
     * @param size Resizing scale
     * @return the scaled version of a list of Points.
     */
    public static ArrayList<PointR> Scale(ArrayList<PointR> pts, SizeR size) {

        // scales the oriented bbox based on 1D or 2D
        PointR centroid = Utils.Centroid(pts);
        double radiusSquared = 1.0d;
        for(PointR point : pts) {
            double distanceSquared = Math.pow((centroid.x - point.x), 2.0) + Math.pow((centroid.y - point.y), 2.0);
            if (distanceSquared > radiusSquared)
                radiusSquared = distanceSquared;
        }

        double factor = size.getWidth() / Math.sqrt(radiusSquared);

        ArrayList<PointR> scaledPts = new ArrayList<PointR>();
        for (int i = 0; i < pts.size(); i++) {
            PointR p = new PointR((PointR)pts.get(i));
            p.x *= factor;
            p.y *= factor;
            scaledPts.add(p);
        }
        return scaledPts;
    }

    /**
     * Resample the points in a way that 96 points are returned, equidistant distributed, which follows the same path.
     *
     * @param points list of Points
     * @param n 96, by default
     * @return the resampled version of the list
     */
    public static ArrayList<PointR> Resample(ArrayList<PointR> points, int n) {
        double I = getPathLengthR(points) / (n - 1); // interval length
        double D = 0.0;
        ArrayList<PointR> srcPts = new ArrayList<PointR>(points);
        ArrayList<PointR> dstPts = new ArrayList<PointR>(n);
        dstPts.add(srcPts.get(0));
        for (int i = 1; i < srcPts.size(); i++) {
            PointR pt1 = srcPts.get(i - 1);
            PointR pt2 = srcPts.get(i);

            double d = distanceBetweenPointRs(pt1, pt2);
            if ((D + d) >= I) {
                double qx = pt1.x + ((I - D) / d) * (pt2.x - pt1.x);
                double qy = pt1.y + ((I - D) / d) * (pt2.y - pt1.y);
                PointR q = new PointR(qx, qy);
                dstPts.add(q); // append new point 'q'
                srcPts.add(i, q); // insert 'q' at position i in points s.t. 'q' will be the next i
                D = 0.0;
            }
            else {
                D += d;
            }
        }
        // sometimes we fall a rounding-error short of adding the last point, so add it if so
        if (dstPts.size() == n - 1) {
            dstPts.add(srcPts.get(srcPts.size() - 1));
        }

        return dstPts;
    }

    /**
     * determine if this gesture is 1D or 2D based on ratio of "oriented" (rotated to 0)
     * bounding box compared to a threshold (determined empirically)
     *
     * @param rawPts RAW version of the points
     * @return whether it is a 1D stroke/gesture or not
     */
    public static boolean Is1DGesture(ArrayList<PointR> rawPts) {
        // make a copy of the pts
        ArrayList<PointR> pts = new ArrayList<PointR>(rawPts);

        // rotate points to 0 (temporarily!)
        double radians = AngleInRadians(Centroid(pts), (PointR)pts.get(0), false);
        pts = RotateByRadians(pts, -radians); // undo angle

        // determine ratio of height to width to see which side is shorter
        RectangleR r = findBoundingBox(pts);

        // check for divide by zero
        if ((r.getWidth() == 0) || (r.getHeight() == 0)) {
            return true;
        }
        else if ((r.getWidth() / r.getHeight()) < (r.getHeight() / r.getWidth())) { // width is shorter side
            if ((r.getWidth() / r.getHeight()) < NDolarRecognizer._1DThreshold) {
                return true;
            }
            else {
                return false;
            }
        }
        else { // else height is shorter side
            if ((r.getHeight() / r.getWidth()) < NDolarRecognizer._1DThreshold) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    /**
     * Checks if the first stroke has its extreme points (start/end) close to those of the second stroke
     * @param stroke1 First given stroke
     * @param stroke2 Second given stroke
     * @return The result of the checks: True if the strokes have their extremities close; False otherwise
     */
    public static boolean strokesHaveExtremitiesClose(Stroke stroke1, Stroke stroke2) {
        double start_to_start = 0,
                start_to_end = 0,
                end_to_start = 0,
                end_to_end = 0;

        start_to_start = distanceBetweenPointRs(new PointR(stroke2.getStartPoint().getX(), stroke2.getStartPoint().getY()),
                new PointR(stroke1.getStartPoint().getX(), stroke1.getStartPoint().getY()));
        start_to_end = distanceBetweenPointRs(  new PointR(stroke2.getEndPoint().getX(), stroke2.getEndPoint().getY()),
                new PointR(stroke1.getStartPoint().getX(), stroke1.getStartPoint().getY()));
        end_to_start = distanceBetweenPointRs(  new PointR(stroke2.getStartPoint().getX(), stroke2.getStartPoint().getY()),
                new PointR(stroke1.getEndPoint().getX(), stroke1.getEndPoint().getY()));
        end_to_end = distanceBetweenPointRs(new PointR(stroke2.getEndPoint().getX(), stroke2.getEndPoint().getY()),
                new PointR(stroke1.getEndPoint().getX(), stroke1.getEndPoint().getY()));

        double min_dist = start_to_start;
        if(start_to_end < min_dist)
            min_dist = start_to_end;
        if(end_to_start < min_dist)
            min_dist = end_to_start;
        if(end_to_end < min_dist)
            min_dist = end_to_end;

        return min_dist < MINIMUM_DISTANCE_FOR_EXTREME_POINTS;
    }

    /**
     * Get the type-most distance in a group of strokes;
     * That could be:   LeftMostDistance,
     *                  RightMostDistance,
     *                  TopMostDistance,
     *                  BottomMostDistance;
     * @param groupOfStrokes the group of strokes
     * @param type type of distance: Left/Right/Top/Bottom
     * @return the type-most distance
     */
    public static double getTypeMostLimitOfGroup(ArrayList<Stroke> groupOfStrokes, String type) {
        double result = 0.0;
        switch (type) {
            case "Left":
                result = groupOfStrokes.get(0).getLeftBorderX();
                for (Portal.Storage.Stroke groupOfStroke : groupOfStrokes) {
                    if (result > groupOfStroke.getLeftBorderX())
                        result = groupOfStroke.getLeftBorderX();
                }
                break;
            case "Right":
                result = groupOfStrokes.get(0).getRightBorderX();
                for (Stroke ofStroke : groupOfStrokes) {
                    if (result < ofStroke.getRightBorderX())
                        result = ofStroke.getRightBorderX();
                }
                break;
            case "Top":
                result = groupOfStrokes.get(0).getTopBorderY();
                for(Portal.Storage.Stroke groupOfStroke : groupOfStrokes) {
                    if(result > groupOfStroke.getTopBorderY())
                        result = groupOfStroke.getTopBorderY();
                }
                break;
            case "Bottom":
                result = groupOfStrokes.get(0).getBottomBorderY();
                for(Stroke groupOfStroke : groupOfStrokes) {
                    if(result < groupOfStroke.getBottomBorderY())
                        result = groupOfStroke.getBottomBorderY();
                }
                break;
            default:
                // Default Case
                break;
        }
        return result;
    }

    /**
     * Save a stroke as PNG. Will be modified to a list of Strokes soon.
     * Also, the name under which the strokes will be saved - it iwll be modified
     * @param listOfStrokes The list of strokes that is to be saved.
     */
    public static BufferedImage getImageDataOfGroup(ArrayList<Stroke> listOfStrokes) {
        BufferedImage image = new BufferedImage(
                (int) Math.ceil(10),
                (int) Math.ceil(10),
                BufferedImage.TYPE_BYTE_GRAY);

        // compute the size of the entire list
        double topBorderYOfList, bottomBorderYOfList,
                leftBorderXOfList, rightBorderXOfList;

        if(listOfStrokes.size() > 0) {
            topBorderYOfList = listOfStrokes.get(0).getTopBorderY();
            bottomBorderYOfList = listOfStrokes.get(0).getBottomBorderY();
            leftBorderXOfList = listOfStrokes.get(0).getLeftBorderX();
            rightBorderXOfList = listOfStrokes.get(0).getRightBorderX();

            if (listOfStrokes.size() > 1) {
                for (int i = 1; i < listOfStrokes.size(); i++) {
                    Stroke s = listOfStrokes.get(i);
                    if (topBorderYOfList > s.getTopBorderY())
                        topBorderYOfList = s.getTopBorderY();
                    if (bottomBorderYOfList < s.getBottomBorderY())
                        bottomBorderYOfList = s.getBottomBorderY();
                    if (leftBorderXOfList > s.getLeftBorderX())
                        leftBorderXOfList = s.getLeftBorderX();
                    if (rightBorderXOfList < s.getRightBorderX())
                        rightBorderXOfList = s.getRightBorderX();
                }
            }

            var height = Math.ceil(Math.abs(bottomBorderYOfList - topBorderYOfList));
            var width = Math.ceil(Math.abs(rightBorderXOfList - leftBorderXOfList));

            image = new BufferedImage(
                    (int) Math.ceil(width + (2 * imgOffset)),
                    (int) Math.ceil(height + (2 * imgOffset)),
                    BufferedImage.TYPE_BYTE_GRAY);

            Graphics2D graphics2D = image.createGraphics();

            // Smoothing the image
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // White background
            graphics2D.setPaint(java.awt.Color.WHITE);
            graphics2D.fillRect(0, 0, image.getWidth(), image.getHeight());

            // INK Color
            graphics2D.setPaint(new GradientPaint(0, 0, java.awt.Color.BLACK, 100, 100, java.awt.Color.BLACK));
            graphics2D.setStroke(new BasicStroke((float) 3.0));

            for (Stroke s : listOfStrokes) {
                for (int i = 1; i < s.getPathPoints().size(); i++) {
                    Portal.Storage.Point p1 = s.getPathPoints().get(i - 1);
                    Point p2 = s.getPathPoints().get(i);
                    graphics2D.drawLine((int) Math.ceil(p1.getX() - leftBorderXOfList) + imgOffset,
                            (int) Math.ceil(p1.getY() - topBorderYOfList) + imgOffset,
                            (int) Math.ceil(p2.getX() - leftBorderXOfList) + imgOffset,
                            (int) Math.ceil(p2.getY() - topBorderYOfList) + imgOffset);
                }
            }

            graphics2D.dispose();
        }
        else{
            System.out.println("Image not created. You provided an empty list of Stroke!");
        }
        return image;
    }

    public static String[] getListOfFileNamesFromGivenFolder(String folderPath){
        //System.out.println("PATH: "+folderPath);
        ArrayList<String> listOfFileNames = new ArrayList<>();
        // TODO: Implement
        File folderPathFile = new File(folderPath);
        var result = folderPathFile.list();
        return result;
    }

    public static ArrayList<String> getListOfValidTemplates(){
        ArrayList<String> listOfValidTemplates = new ArrayList<>();
        // TODO: Implement
        var namesOfAllFiles = getListOfFileNamesFromGivenFolder(templateFolderPath);
        if(namesOfAllFiles.length>0) {
            for (int i = 0; i < namesOfAllFiles.length; i++) {
                if (!stringContainsCharacters(namesOfAllFiles[i])) {
                    listOfValidTemplates.add(namesOfAllFiles[i].split("_")[1].replace(".xlsx",""));
                    //System.out.println(namesOfAllFiles[i].split("_")[1].replace(".xlsx",""));
                }
            }
        }
        return listOfValidTemplates;
    }

    public static boolean stringContainsCharacters(String testedString){
        if(testedString.length()>0){
            for(char c : testedString.toCharArray()){
                if(Character.isDigit(c)){
                  return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param templateFileName is the entire name of the file: e.g. Circle.xlsx
     * @return
     */
    public static  ArrayList<ArrayList<Stroke>> getAllTemplatesFromATemplateFile(String templateFileName){
        ArrayList<ArrayList<Stroke>> allTemplates = new ArrayList<>();
        // TODO: IMPLEMENT
        if(templateFileName.contains(".xlsx")){
            String filePath = templateFolderPath+"\\"+templateFileName;

            File file = new File(filePath);
            // It exists, as the file names are taken from the folder itself.
            // However, it is an error prevention method: in case a user removes the Excel files while the software is running.
            if (file.exists()) {
                FileInputStream fileInputStream = null;
                XSSFWorkbook workbook = null;
                try {
                    fileInputStream = new FileInputStream(file);

                    workbook = new XSSFWorkbook(fileInputStream);

                    XSSFSheet sheet = workbook.getSheetAt(0);

                    Iterator<Row> rowIterator = sheet.iterator();

                    int rowNumber = -1;

                    while (rowIterator.hasNext()) {
                        rowNumber++;

                        Row row = rowIterator.next();

                        // if the row contains Data, and not the file's title row
                        if(rowNumber > 0){
                            //String SymbolName = row.getCell(0).toString();

                            int incomingStroke = (int) row.getCell(1).getNumericCellValue();
                            ArrayList<Stroke> currentTemplate = new ArrayList<>();
                            for (int strokeNumber = 0; strokeNumber < incomingStroke; strokeNumber++) {
                                Stroke newStroke = new Stroke();
                                newStroke.updateLineThickness(4.0);
                                newStroke.updateColor(CanvasUI.COMMAND_COLOR);
                                //ArrayList<Point> strokePoints = new ArrayList<>();
                                row = rowIterator.next();
                                int numberOfPointsInStroke = (int) row.getCell(2).getNumericCellValue();
                                for (int i = 0; i < numberOfPointsInStroke; i++) {
                                    row = rowIterator.next();
                                    double x = row.getCell(3).getNumericCellValue(),
                                            y = row.getCell(4).getNumericCellValue();
                                    Point currentPoint = new Point(x, y);
                                    if(i == 0){
                                        newStroke.beginPath(currentPoint);
                                    }
                                    else{
                                        newStroke.addPointToPath(currentPoint);
                                    }
                                }
                                newStroke.finishPath();
                                currentTemplate.add(newStroke);
                            }
                            allTemplates.add(currentTemplate);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("ERROR!!");
            }
        }
        return allTemplates;
    }

    public static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }

        return new ImageView(wr).getImage();
    }
}