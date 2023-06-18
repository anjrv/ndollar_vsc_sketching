package is.nsn.sketching.nDollar;

import is.nsn.sketching.templates.Templates;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class Recognizer {

    /**
     * Represents the Minimum temporal distance between 2 strokes that defines if those are considered [temporally] close enough.
     * Used in the GROUPING algorithm
     * Decided to be 650 after repeatedly testing the grouping algorithm
     */
    public static final int MAX_TIME_BETWEEN_STROKES_IN_A_GROUP = 650;
    /**
     * Variables used throughout the recognition process
     */
    private static final double DX = 250.0;
    /**
     * Lower limit of a shape.
     * If the recognition score is above this value, the shape is a good candidate for the recognized shape.
     * Otherwise, the group will automatically be recognized as TEXT
     */
    private static final double BARE_MINIMUM_SCORE_TO_BE_CONSIDERED_A_SHAPE = 0.65;
    /**
     * Resampling Scale [250 x 250 pixels]
     */
    public static SizeR ResampleScale = new SizeR(DX, DX);
    /**
     * origin point of the resampling box
     * Meaning that the group that will be recognized will shifted so its root will be moved in this point
     */
    public static PointR ResampleOrigin = new PointR(0, 0);
    /**
     * threshold for the ratio between short-side and long-side of a gesture;
     * empirically determined
     */
    public static double _1DThreshold = 0.30;

    /*
     * End of Variables
     */
    /**
     * Singleton Instance
     */
    private static Recognizer instance;
    /**
     * List of all the Templates used for the recognition
     */
    private final Hashtable<String, MultistrokeR> _gestures;

    /**
     * Constructor
     */
    private Recognizer() {
        _gestures = new Hashtable<>(100);

        try {
            Hashtable<String, ArrayList<ArrayList<PointR>>> templates = Templates.getTemplates();
            for (Map.Entry<String, ArrayList<ArrayList<PointR>>> template : templates.entrySet()) {
                _gestures.put(template.getKey(), new MultistrokeR(template.getKey(), template.getValue(), true));
            }

            System.out.println("Recognizer loaded " + _gestures.size() + " templates");
        } catch (Exception e) {
            System.out.println("Could not get templates");
            throw new RuntimeException(e);
        }

        // File currentDirFile = new File(".");
        // String absPathToDirectory="";

        // try{
        //     absPathToDirectory = currentDirFile.getAbsolutePath();

        //     absPathToDirectory = absPathToDirectory.substring(0, absPathToDirectory.length() - 1);

        // }
        // catch (Exception e){
        //     System.out.println(e);
        // }

        // if(absPathToDirectory.equals("")){
        //     absPathToDirectory = System.getProperty("user.dir") + "\\";
        // }

        // String dataPath = absPathToDirectory+"\\resource\\tessdata\\";

        // dataPath = "./resource/tessdata/";
        // try {
        //     /* Set data path - for training data */
        //     tesseract.setLanguage("eng");
        //     tesseract.setDatapath(dataPath);
        // }
        // catch (Exception e){
        //     System.out.println("Failed to instantiate the training data for tesseract (from "+dataPath+")");
        //     System.out.println(e);
        // }
    }

    /**
     * getter for the Instance
     *
     * @return the Singleton distance
     */
    public static Recognizer getInstance() {
        if (instance == null)
            instance = new Recognizer();
        return instance;
    }

    /**
     * Function checking if the distance from two different groups is small
     * That includes both their intersection, overlapping, and not intersecting but being just spatially within a short range
     *
     * @param group1 the first group
     * @param group2 the second group
     * @return whether the groups are close or not
     */
    private static boolean distanceBetweenExtremitiesOfGroupsIsSmall(ArrayList<StrokeR> group1, ArrayList<StrokeR> group2) {
        for (StrokeR r : group1) {
            for (StrokeR strokeR : group2) {
                if (Utils.strokesHaveExtremitiesClose(r, strokeR)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Given the initial grouping (one stroke per group), it shifts the strokes between groups, and creates the final groups
     * The process of doing so looks like this:
     * - initially, it finds the strokes which have extremities close to one another (Box made out of 4 strokes, for example)
     * - finally, if groups are within a short temporal AND spatial distance, they are merged together
     *
     * @param initialGrouping the initial groups, from the Storage, forming an Array of Arrays of Strokes, where there is exactly one Stroke per group (Array)
     */
    private static void createTheGroupsOfStrokes(ArrayList<ArrayList<StrokeR>> initialGrouping) {
        // the idea for the new approach is to first connect the groups that are physically close to one another
        //System.out.println("Grouping started");
        // variable confirming that shifts happened in the previous stage
        boolean shiftsHappened = true;

        while (shiftsHappened) {
            // it is assumed that no shift will happen in this stage.
            shiftsHappened = false;
            if (initialGrouping.size() == 1) {
                //System.out.println("Skip the grouping. only 1 group left");
                break;
            }

            // groups the strokes which have their extremities close to one another
            for (int i = 0; i < initialGrouping.size() - 1; i++) {
                for (int j = i + 1; j < initialGrouping.size(); j++) {
                    // if the distance between the extremities of any stroke from these 2 groups is small enough
                    // or if the groups intersect in any way, for more than 50% of a size
                    if (distanceBetweenExtremitiesOfGroupsIsSmall(initialGrouping.get(i), initialGrouping.get(j))) {
                        //shift group J into group I
                        initialGrouping.get(i).addAll(initialGrouping.get(j));
                        // remove group j
                        initialGrouping.remove(j);
                        // decrease j
                        j--;
                        // and set shiftsHappened to true;
                        shiftsHappened = true;

                        //System.out.println("Group "+j+": moved in group "+i);
                    }
                }
            }
        }

        // next, connect the groups that are within each other's proximity, what are temporally close together
        for (int i = 0; i < initialGrouping.size() - 1; i++) {
            for (int j = i + 1; j < initialGrouping.size(); j++) {
                // The temporal distance is needed to avoid cases such: text within a box. if the temporal check isn't added, the text would be included inside the box's group, and will result into a misclassification (and only one result, instead of 2)
                if (groupsAreInSpatialProximity(initialGrouping.get(i), initialGrouping.get(j)) &&
                        groupsAreInTemporalProximity(initialGrouping.get(i), initialGrouping.get(j))) {
                    //shift group J into group I
                    initialGrouping.get(i).addAll(initialGrouping.get(j));
                    // remove group j
                    initialGrouping.remove(j);
                    // decrease J
                    j--;

                    //System.out.println("Group "+j+": moved in group "+i);
                }
            }
        }
    }

    /**
     * Visualization purposes
     * <p>
     * For a given group, it finds its root, height and width
     */
    private static Pair<PointR, Pair<Double, Double>> getBoundingBoxOfGroup(ArrayList<StrokeR> providedGroup) {
        PointR root = new PointR(0, 0);
        double height, width;

        double leftMostLimit = Utils.getTypeMostLimitOfGroup(providedGroup, "Left"),
                rightMostLimit = Utils.getTypeMostLimitOfGroup(providedGroup, "Right"),
                topMostLimit = Utils.getTypeMostLimitOfGroup(providedGroup, "Top"),
                bottomMostLimit = Utils.getTypeMostLimitOfGroup(providedGroup, "Bottom");

        root = new PointR(leftMostLimit, topMostLimit);
        height = bottomMostLimit - topMostLimit;
        width = rightMostLimit - leftMostLimit;

        return new Pair<>(root, new Pair<>(width, height));
    }

    /**
     * Function returning whether the two groups are physically in each other's proximity
     *
     * @param group1 the first group
     * @param group2 the second group
     * @return TRUE if the groups are in their physical proximity, FALSE otherwise
     */
    private static boolean groupsAreInSpatialProximity(ArrayList<StrokeR> group1, ArrayList<StrokeR> group2) {
        for (StrokeR r : group1) {
            for (StrokeR strokeR : group2) {
                // if the groups are within a short distance, or if they intersect
                if (Utils.strokesAreInProximity(r, strokeR, -1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Function returning whether the two groups are temporally in each other's proximity  aaa
     *
     * @param group1 the first group
     * @param group2 the second group
     * @return TRUE if the groups are in their temporal proximity, FALSE otherwise
     */
    private static boolean groupsAreInTemporalProximity(ArrayList<StrokeR> group1, ArrayList<StrokeR> group2) {
        for (StrokeR r : group1) {
            for (StrokeR strokeR : group2) {
                if (Math.abs(strokeR.getTimeStopDrawing().getTime() - r.getTimeStartDrawing().getTime()) <= (MAX_TIME_BETWEEN_STROKES_IN_A_GROUP) ||
                        Math.abs(strokeR.getTimeStartDrawing().getTime() - r.getTimeStopDrawing().getTime()) <= (MAX_TIME_BETWEEN_STROKES_IN_A_GROUP)) {
                    return true;
                }
            }
        }
        return false;
    }

    // public ArrayList<JSONData> startRecognitionOf(ArrayList<StrokeR> listOfStrokesFromCanvas){
    //     ArrayList<JSONData> result = new ArrayList<>();

    //     if (listOfStrokesFromCanvas.size() > 0) {
    //         /*
    //          *  If there is at least one provided stroke, the recognition algorithm follows the following structure:
    //          *  1. Groups the strokes, based on Temporal (time) and Spatial (physical distance on the Canvas) Distances
    //          *  2. For every group, call the Recognize function.
    //          */

    //         /*  1. The Grouping
    //          *   First, create two empty groups of strokes, for Shape and for Text
    //          */
    //         ArrayList<ArrayList<Stroke>> groupedSHAPEList = new ArrayList<>();

    //         /* Initially, put all the strokes into one group */
    //         // old:
    //         //groupedSHAPEList.add(listOfStrokesFromCanvas);
    //         // new:
    //         for(int i = 0 ; i < listOfStrokesFromCanvas.size() ; i++){
    //             ArrayList<Stroke> newStrokeGroup = new ArrayList<>();
    //             newStrokeGroup.add(listOfStrokesFromCanvas.get(i));

    //             groupedSHAPEList.add(newStrokeGroup);
    //         }

    //         createTheGroupsOfStrokes(groupedSHAPEList);

    //         /*
    //          * 2. Recognition Part.
    //          */
    //         for(ArrayList<Stroke> currentGroupOriginal : groupedSHAPEList) {

    //             // creating a representative, according to the $N
    //             ArrayList<ArrayList<PointR>> currentListOfListOfPoints = new ArrayList<>();
    //             for (Stroke str : currentGroupOriginal) {
    //                 ArrayList<PointR> listOFPointsInStroke = new ArrayList<>();
    //                 for (Point p : str.getPathPoints()) {
    //                     listOFPointsInStroke.add(p.toPointR());
    //                 }
    //                 currentListOfListOfPoints.add(listOFPointsInStroke);
    //             }

    //             // Object that will be recognized
    //             Multistroke currentMultiStroke = new Multistroke("Group to be Tested", currentListOfListOfPoints, false);

    //             // timestamp Start
    //             final long startTime = System.currentTimeMillis();

    //             // recognition
    //             NBestList resultDollarN = Recognize(currentMultiStroke.OriginalGesture.Points, currentMultiStroke.NumStrokes);

    //             // timestamp End
    //             final long endTime = System.currentTimeMillis();

    //             // the recognition Result
    //             NBestResult topResult = resultDollarN.getNBestList().get(0);

    //             // TO BE REMOVED - just to see how much time the recognition took
    //             double timeSpentOnDollarNRecognition = (endTime - startTime)/1000.00; // milliseconds -> seconds
    //             System.out.println("$N (Best Result) in " + timeSpentOnDollarNRecognition + " seconds: " + topResult.getName()+" "+topResult.getScore());

    //             // Add the results as a JSONData data
    //             JSONData newRecognizedShape = new JSONData();

    //             // recognition result
    //             newRecognizedShape.updateRecognizedAs(topResult.getName());

    //             // root & dimensions
    //             double  leftMostBorderX = Utils.getTypeMostLimitOfGroup(currentGroupOriginal, "Left"),
    //                     rightMostBorderX = Utils.getTypeMostLimitOfGroup(currentGroupOriginal, "Right"),
    //                     topMostBorderY = Utils.getTypeMostLimitOfGroup(currentGroupOriginal, "Top"),
    //                     bottomMostBorderY = Utils.getTypeMostLimitOfGroup(currentGroupOriginal, "Bottom");
    //             newRecognizedShape.updateRoot(new Point(leftMostBorderX, topMostBorderY));
    //             newRecognizedShape.updateDimensions(Math.abs(bottomMostBorderY - topMostBorderY),
    //                                                 Math.abs(rightMostBorderX - leftMostBorderX));

    //             // computing the Centroid / Middle point
    //             ArrayList<PointR> completeInitialPoints = new ArrayList<>();
    //             for(int i = 0 ; i < currentListOfListOfPoints.size() ; i++) {
    //                 completeInitialPoints.addAll(currentListOfListOfPoints.get(i));
    //             }
    //             PointR centroidR = Utils.Centroid(completeInitialPoints);
    //             newRecognizedShape.updateMiddlePoint(new Point(centroidR.x, centroidR.y));

    //             // start point & end point
    //             newRecognizedShape.updateStartingPoint(currentGroupOriginal.get(0).getStartPoint());
    //             newRecognizedShape.updateEndPoint(currentGroupOriginal.get(currentGroupOriginal.size()-1).getEndPoint());

    //             // pointing at
    //             if(topResult.getName().toLowerCase().contains("line")){
    //                 // For the line, the direction should represent the most common direction found in its path; and the tip should be the last point
    //                 var lineVariables = Utils.getLinePointingVariables(currentGroupOriginal);
    //                 newRecognizedShape.updatePointingAt(lineVariables.getValue(), lineVariables.getKey());
    //                 System.out.println("Line having the direction: "+lineVariables.getKey());
    //                 CanvasUI.drawBox(lineVariables.getValue(), 5,5, Color.CYAN);
    //             }
    //             else if(topResult.getName().toLowerCase().contains("arrow")) {
    //                 // For the arrow, the direction should be given by the last 25% of the points from the merged version of that group (in $N - resampled points).
    //                 // in other words, it should be given by the point #72 to #73 (from those 96 points).
    //                 // The tip should be a point, found after that #72 position, which would have the highest distance.

    //                 var arrowVariables = Utils.getArrowPointingVariables(currentGroupOriginal);

    //                 System.out.println("Arrow having the direction: "+arrowVariables.getKey()+" and the tip on ("+arrowVariables.getValue().getValue().getX()+", "+arrowVariables.getValue().getValue().getY()+")");
    //                 //CanvasUI.drawBox(arrowVariables.getValue(),5,5, Color.CYAN);
    //                 newRecognizedShape.updateStartingPoint(arrowVariables.getValue().getKey());
    //                 newRecognizedShape.updatePointingAt(arrowVariables.getValue().getValue(), arrowVariables.getKey());
    //             }
    //             else { // the other shapes will not use these fields
    //                 newRecognizedShape.updatePointingAt(new Point(0, 0), 0.0);
    //             }

    //             // timestamp variables: Start & End
    //             newRecognizedShape.updateTimeOfStartingDrawing(currentGroupOriginal.get(0).getTimeStartDrawing());
    //             newRecognizedShape.updateTimeOfStopDrawing(currentGroupOriginal.get(currentGroupOriginal.size()-1).getTimeStopDrawing());

    //             // OCR part - in case it represents text
    //             BufferedImage bufferedImage = Utils.getImageDataOfGroup(currentGroupOriginal);

    //             String resultOfOCR = callOCR(bufferedImage);

    //             newRecognizedShape.updateText(resultOfOCR);
    //             newRecognizedShape.updateTimeOfRecognition(timeSpentOnDollarNRecognition);

    //             newRecognizedShape.updateScore(topResult.getScore());

    //             // in case the Text is a better representative, update the Recognition result & Score
    //             // the very large shapes are excluded
    //             if( (!newRecognizedShape.getRecognizedAs().equals("Watch") &&
    //                     !newRecognizedShape.getRecognizedAs().equals("Split Screen") &&
    //                     resultOfOCR.length() >= 2 && Utils.isText(currentGroupOriginal)) /**/||
    //                 // the bad results are taken, by default, as text
    //                 (topResult.getScore() < BARE_MINIMUM_SCORE_TO_BE_CONSIDERED_A_SHAPE) ||
    //                 // also arrows composed by more than 4 strokes
    //                 (newRecognizedShape.getRecognizedAs().equals("Arrow") && currentGroupOriginal.size() > 4)/**/) {
    //                 newRecognizedShape.updateRecognizedAs("Text");
    //                 newRecognizedShape.updateScore(1.0);
    //                 System.out.println("Shape switched to text: /*"+resultOfOCR+"*/");
    //             }

    //             // Finally, add the result to the current result list (from the canvas)
    //             result.add(newRecognizedShape);
    //         }

    //         // Sort the data by timeOfSTartDrawing before sending it to VS Code
    //         result.sort(Comparator.comparing(JSONData::getTimeOfStartDrawing));
    //     }

    //     // TO BE REMOVED
    //     System.out.println("\n\n");

    //     // return the result
    //     return result;
    // }

    /**
     * Function which recognizes a provided list of points (representing a Multi-Stroke/List-of-Strokes added by the user)
     *
     * @param points     List of Points representing the Strokes drawn by the user (in a 'merged' version)
     * @param numStrokes Number of strokes in the initial drawing. Now used tho
     * @return the List of results (with their scores), based on already-existing templates.
     */
    public BestListR recognize(ArrayList<PointR> points, int numStrokes) {
        // removed the candidate transformations by creating a Gesture here
        // of the input points
        // this helps keep the transformations done to templates and candidates the same
        // and we won't have to edit code in two places
        GestureR candidate = new GestureR(points);

        // Instantiates the result as an empty list of Results
        BestListR nbest = new BestListR();

        // added to check how many savings we are getting out of the Utils.AngleBetweenVUnitVectors() check

        // we have to compare the current gesture to all candidates,
        // each sub-gesture in our set of MultiStrokes

        /* For every template in the Template DataBase*/
        for (MultistrokeR ms : _gestures.values()) {
            // initiate the local list of results with an empty list
            // this Multi Stroke nbest
            BestListR thisMSnbest = new BestListR(); // store the best list for just this MS

            // for each combination of ordering/direction of the strokes created within every template
            for (GestureR p : ms.subGestures) {
                double score;
                double best;

                best = OptimalCosineDistance(p.VectorVersion, candidate.VectorVersion); //candidate.Points, p.Points);
                score = 1d - best;

                // keep track of what sub-gesture was best match for this multistroke
                // and only add that particular template's score to the nbest list
                // Adds the result to the local list of Result
                thisMSnbest.AddResult(ms.Name, score); // name, score, distance, angle
                thisMSnbest.AddResult(ms.Name, score); // name, score, distance, angle
            }

            // Adds only the highest result.
            thisMSnbest.SortDescending();
            // add the one that was best of those sub-gestures
            // these properties return the property of the top result
            String nameOfResult = thisMSnbest.getNameOfTopResult();
            if (nameOfResult.contains("_")) {
                nameOfResult = nameOfResult.split("_")[0];
            }
            double scoreOfResult = thisMSnbest.getScoreOfTopResult();
            nbest.AddResult(nameOfResult, scoreOfResult); // name, score, distance, angle
        }

        // adding the Heuristic Enclosure recognition;
        if (numStrokes == 1) {
            // create a stroke with the points
            StrokeR equivalentStroke = new StrokeR();
            equivalentStroke.beginPath(new PointR(points.get(0).x, points.get(0).y));
            for (int i = 1; i < points.size(); i++) {
                equivalentStroke.addPointToPath(new PointR(points.get(i).x, points.get(i).y));
            }
            equivalentStroke.finishPath();
            ArrayList<StrokeR> equivalentListOfStrokes = new ArrayList<>();
            equivalentListOfStrokes.add(equivalentStroke);

            double enclosureScoreHeuristics = Utils.getEnclosureScoreHeuristics(equivalentListOfStrokes);
            nbest.AddResult("Enclosure", enclosureScoreHeuristics);
        }
        // Sort the Actual results and return them
        nbest.SortDescending();
        return nbest;
    }

    /**
     * I think it refers to the Cosine Similarity (<a href="https://en.wikipedia.org/wiki/Cosine_similarity">...</a>)
     * <p>
     * From: <a href="http://yangl.org/pdf/protractor-chi2010.pdf">...</a>
     * <p>
     * Given two different vectors, it finds Optimal Angular distanceBetweenPointRs between these vectors.
     * The similarity score is given (based on the formula from the paper)
     */
    private Double OptimalCosineDistance(ArrayList<Double> v1, ArrayList<Double> v2) {
        double a = 0;
        double b = 0;

        for (int i = 0; i < v1.size(); i = i + 2) {
            a += v1.get(i) * v2.get(i) + v1.get(i + 1) * v2.get(i + 1);
            b += v1.get(i) * v2.get(i + 1) - v1.get(i + 1) * v2.get(i);
        }

        double angle = Math.atan(b / a);

        return Math.acos(a * Math.cos(angle) + b * Math.sin(angle));
    }
}