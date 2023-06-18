package is.nsn.sketching.nDollar;

import java.util.ArrayList;

/**
 * MultiStroke is the equivalent of a Group of Strokes
 */
public class MultistrokeR {
    /**
     * Name of the Group / MultiStroke
     */
    public String Name;

    /**
     * Number of strokes composing the MultiStroke
     */
    public int NumStrokes; // how many strokes this MultiStroke has

    /**
     * List of "Merged" versions of all the possible ordering & directions of the strokes (from this group/multi-stroke)
     * their number is n! * 2^n, where n is the number of contributing strokes
     */
    public ArrayList<GestureR> subGestures;  // all possible orderings/directions of this multistroke gesture

    /**
     * the "Original Look" of a gesture, as a merged version of all the points used there
     */
    public GestureR OriginalGesture; // the original gesture used to instantiate this Multistroke

    // when a new MultiStroke is made, it handles pre-processing the points given
    // so that all possible orderings and directions of the points are handled.
    // this allows $N to receive 1 template for a MultiStroke gesture such as "="
    // without limiting future recognition to users writing that template with the
    // strokes in the same order and the same direction.

    /**
     * Constructor creating the Multistroke object, including the original gesture, and all possible sub-gestures.
     *
     * @param name                          name of the new object
     * @param strokes                       initial List of strokes (List of <List of Points>)
     * @param createAllPossibleCombinations true for templates, false for the recognition part.
     */
    public MultistrokeR(String name, ArrayList<ArrayList<PointR>> strokes, boolean createAllPossibleCombinations) {
        this.Name = name;

        // combine the strokes into one unistroke gesture to save the original gesture
        ArrayList<PointR> points = new ArrayList<>();
        for (ArrayList<PointR> pts : strokes) {
            points.addAll(pts);
        }
        this.OriginalGesture = new GestureR(points);

        this.NumStrokes = strokes.size();

        // Computes all possible stroke orderings/stroke direction combinations of the
        // given Gesture.  This is done in two steps:
        // 1. Use the algorithm HeapPermute(n) to find all possible orderings (permutations)
        // 2. For each ordering,
        //    Use the binary enumeration technique to enumerate all possible combinations of stroke directions.
        ArrayList<Integer> defaultOrder = new ArrayList<>(strokes.size()); // array of integer indices
        for (int i = 0; i < strokes.size(); i++) {
            defaultOrder.add(i); // initialize
        }

        // if the Multi-Stroke is a Template, and not a recognizable group, than compute all its sub-gestures
        // otherwise, just skip these steps
        if (createAllPossibleCombinations) {
            ArrayList<ArrayList<Integer>> allOrderings = new ArrayList<>();
            // HeapPermute operates on the indices
            HeapPermute(this.NumStrokes, defaultOrder, allOrderings);
            // now allOrderings should contain all possible permutations of the stroke indices

            // now enumerate each ordering with all possible stroke directions
            // (forward/backward)
            // operates directly on the strokes
            ArrayList<ArrayList<PointR>> unistrokes = MakeUnistrokes(strokes, allOrderings);

            this.subGestures = new ArrayList<>(unistrokes.size());
            for (ArrayList<PointR> entry : unistrokes) {
                GestureR newG = new GestureR(entry);
                this.subGestures.add(newG);
            }
        } else {
            this.subGestures = new ArrayList<>();
        }
    }

    /**
     * Recursive function, which creates all possible combinations of order/direction of a Template
     * <p>
     * Old note: this algorithm is given by B. Heap
     * A. Levitin, Introduction to The Design & Analysis of Algorithms, Addison Wesley, 2003
     * <a href="http://www.cut-the-knot.org/do_you_know/AllPerm.shtml">...</a>
     * <p>
     * NOTE: this will side effect into allOrders
     *
     * @param n            number of strokes
     * @param currentOrder current indexing order
     * @param allOrders    all possible orderings
     */
    public void HeapPermute(int n, ArrayList<Integer> currentOrder, ArrayList<ArrayList<Integer>> allOrders) {
        if (n == 1) {
            // base case
            // build return value to be an ArrayList containing 1 ArrayList (strokes) of ArrayLists (points)
            allOrders.add(new ArrayList<>(currentOrder)); // copy
        } else {
            for (int i = 0; i < n; i++) {
                // recurse here, building up set of lists
                HeapPermute(n - 1, currentOrder, allOrders);
                if ((n % 2) == 1) { // odd n
                    SwapStrokes(0, n - 1, currentOrder);
                } else { // even n
                    SwapStrokes(i, n - 1, currentOrder);
                }
            }
        }
    }

    /**
     * Switches the Int values within a list, between the index 'first' and 'second'
     * <p>
     * Old note: swap the strokes given by the indices "first" and "second" in the
     * "order" argument; this DOES change the ArrayList sent as an argument.
     * used by HeapPermute
     *
     * @param first  index of the first item to be interChanged
     * @param second index of the second item to be interChanged
     * @param order  given list of items (Integer, in this case)
     */
    private void SwapStrokes(int first, int second, ArrayList<Integer> order) {
        int temp = order.get(first);
        order.set(first, order.get(second));
        order.set(second, temp);
    }

    /**
     * Function creating all subTemplates, based on the ordering resulted from HeapPermute
     * <p>
     * Old note: now swap stroke directions within all possible permutations
     * this can be done by treating the strokes as binary variables (F=0, B=1)
     * therefore, for each ordering, iterate 2^(num strokes) and extract bits of
     * that # to determine which stroke is forward and which is backward
     * allOrderings has indices in it
     *
     * @param originalStrokes initial aspect of Points (of a Template)
     * @param allOrderings    list of all possible combinations of ordering/direciton
     * @return the subTemplates
     */
    public ArrayList<ArrayList<PointR>> MakeUnistrokes(ArrayList<ArrayList<PointR>> originalStrokes, ArrayList<ArrayList<Integer>> allOrderings) {
        ArrayList<ArrayList<PointR>> allUnistrokes = new ArrayList<>(); // will contain all possible orderings/direction enumerations of this gesture
        for (ArrayList<Integer> ordering : allOrderings) {
            for (int b = 0; b < Math.pow(2d, ordering.size()); b++) { // decimal value b
                ArrayList<PointR> unistroke = new ArrayList<>(); // we're building a unistroke instead of multistroke now for ease of processing
                for (int i = 0; i < ordering.size(); i++) { // examine b's bits
                    // copy the correct UniStroke
                    ArrayList<PointR> stroke = new ArrayList<>(originalStrokes.get(ordering.get(i)));
                    if (((b >> i) & 1) == 1) { // if (BitAt(b, i) == 1), i.e., is b's bit at index i on?
                        stroke = getReverseOfArrayListOfPoints(stroke); // reverse the strokes
                    }
                    unistroke.addAll(stroke); // add stroke to current strokePermute
                }
                // add completed strokePermute to set of strokePermutes (aka Multistrokes)
                allUnistrokes.add(unistroke);
            }
        }
        return allUnistrokes;
    }

    /**
     * Returns the reverse of a given list of PointR's
     * <p>
     * Used by MakeUnistrokes
     *
     * @param stroke the initial ordering
     * @return the reverse of the list
     */
    public ArrayList<PointR> getReverseOfArrayListOfPoints(ArrayList<PointR> stroke) {
        ArrayList<PointR> resultingStroke = new ArrayList<>();
        for (int i = stroke.size() - 1; i >= 0; i--) {
            resultingStroke.add(stroke.get(i));
        }
        return resultingStroke;
    }
}