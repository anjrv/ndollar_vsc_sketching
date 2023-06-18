package is.nsn.sketching.nDollar;

import java.util.ArrayList;

public class GestureR {

    private static final int StartAngleIndex = 8;

    /**
     * List of initial, raw/unchanged Points
     */
    public ArrayList<PointR> rawPointRS;
    /**
     * List of Resampled Points
     * Instead of having N (Raw)Points it contains 96 points. [96 is a constant variable within the Parameters of the $N Recognizer]
     */
    public ArrayList<PointR> pointRS;
    /**
     * Flag indicating if it is a 1D (Straight) or 2D (e.g. arc) gesture
     * Important for Rescaling (as 1D and 2D strokes are rescaled differently
     */
    public boolean Is1D;
    /**
     * Unit vector representing the Angle between the 1st point and the 8th point in the list of Points (Restructured)
     */
    public PointR StartUnitVector;

    /* Process notes:
       When a new prototype is made, its raw points are resampled into n equidistantly spaced
       points, then it is scaled to a preset size and translated to a preset origin. This is
       the same treatment applied to each candidate stroke, and it allows us to thereafter
       simply step through each point in each stroke and compare those points' distances.
       In other words, it removes the challenge of determining corresponding points in each gesture.
       after resampling, scaling, and translating, we compute the "indicative angle" of the
       stroke as defined by the angle between its centroid point and first point.
     */
    /**
     * For the Protractor algorithm
     * Vector representative of the List of Points (having the size of 192, or 2x96; because both x and y from each point is contributing for this vector)
     */
    public ArrayList<Double> VectorVersion;
    /**
     * Private const, having the options: 16, 64, 96
     * Represents the number of Points, after resampling, of the RawPoints
     */
    public int NumResamplePoints = 96;

    /**
     * Constructor
     *
     * @param pointRS List of RAW Points
     */
    public GestureR(ArrayList<PointR> pointRS) {
        this.rawPointRS = new ArrayList<>(pointRS); // copy (saved for drawing)

        this.pointRS = rawPointRS;

        /* new order (as of 8/31/2009) is
         1. resample
         2. rotate (all); save amount
         3. check for 1D
         4. scale
         5. rotate back (if NOT rot-invariant)
         6. translate
         7. calculate start angle
         */

        // first, resample the points into N (96) equidistantly spaced points
        this.pointRS = Utils.Resample(this.pointRS, NumResamplePoints);

        // then, if we are rotation-invariant, rotate to a common reference angle
        // otherwise skip that step

        // rotate so that the centroid-to-1st-point is at zero degrees
        double radians = Utils.AngleInRadians(Utils.Centroid(this.pointRS), this.pointRS.get(0), false);
        this.pointRS = Utils.RotateByRadians(this.pointRS, -radians);

        // then, resize to a square
        // check for 1D vs 2D (because we resize differently)
        this.Is1D = Utils.Is1DGesture(rawPointRS);

        // scale to a common (square of 250.0 x 250.0) dimension
        // moved determination of scale method to within the scale() method for less branching here
        this.pointRS = Utils.Scale(this.pointRS, Recognizer.ResampleScale);

        // next, translate to a common origin
        this.pointRS = Utils.TranslateCentroidTo(this.pointRS, Recognizer.ResampleOrigin);

        // finally, save the start angle
        // store the start unit vector after post-processing steps
        this.StartUnitVector = Utils.CalcStartUnitVector(this.pointRS, StartAngleIndex);

        // make the simple vector-based version for Protractor testing
        this.VectorVersion = Vectorize(this.pointRS);
    }

    /**
     * Vector Representative of the List of Points
     * will have twice the size of it (192 instead of 96)
     * e.g. {(2.0, 3.0), (4.0, 4.0)}
     * -> {0.305769212268421, 0.4420340232622131, 0.6064246414167132, 0.5859695089361983}
     * Not sure exactly why, but it is needed by the Protractor's OptimalCosineDistance().
     * <p>
     * <p>
     * From <a href="http://yangl.org/protractor/Protractor%20Gesture%20Recognizer.pdf">...</a>
     * Given a list of PointR's this can translate them into a flat list of x,y coordinates,
     * a Vector, which is needed by Protractor's OptimalCosineDistance().
     */
    private ArrayList<Double> Vectorize(ArrayList<PointR> pts) {
        // skip the resampling, translation because $N already did this in pre-processing
        // re-do the rotation though
        // (note: doing rotation  on the pre-processed points is ok because $N rotates it back to the
        // original orientation if !RotationInvariant, e.g., it is rotation sensitive)

        // extract indicative angle (delta)
        double delta = -Math.atan2(pts.get(0).y, pts.get(0).x);


        // find the match
        double sum = 0;
        ArrayList<Double> vector = new ArrayList<>();
        for (PointR p : pts) {
            double newX = p.x * Math.cos(delta) - p.y * Math.sin(delta);
            double newY = p.y * Math.cos(delta) + p.x * Math.sin(delta);
            vector.add(newX);
            vector.add(newY);
            sum += newX * newX + newY * newY;
        }
        double magnitude = Math.sqrt(sum);
        vector.replaceAll(aDouble -> aDouble / magnitude);

        return vector;
    }
}