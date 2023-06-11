package is.nsn.sketching.nDollar;

public class BestResultR {
    /**
     * Name of the Result Template
     */
    private String _name;

    /**
     * Score (between 0 and 1) of the Result Template
     */
    private double _score;

    /**
     * Constructor
     *
     * @param name  Name of the Result
     * @param score Score of the Result
     */
    public BestResultR(String name, double score) {
        this._name = name;
        this._score = score;
    }

    /**
     * Copying Constructor
     *
     * @param newBestResult given Result
     */
    public BestResultR(BestResultR newBestResult) {
        this._name = newBestResult.getName();
        this._score = newBestResult.getScore();
    }

    /**
     * Getter of the Name of the Result
     *
     * @return Name of Result
     */
    public String getName() {
        return this._name;
    }

    /**
     * Setter for the Name of the Result
     *
     * @param value new Name Value
     */
    public void setName(String value) {
        this._name = value;
    }

    /**
     * Getter of the Score of the Result
     *
     * @return Score of Result
     */
    public double getScore() {
        return this._score;
    }

    /**
     * Setter for the Score of the Result
     *
     * @param value new Score Value
     */
    public void setScore(double value) {
        this._score = value;
    }
}