package Portal.DolarN;

/**
 * Result class
 */
public class NBestResult {
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
     * @param name Name of the Result
     * @param score Score of the Result
     */
    public NBestResult(String name, double score)
    {
        this._name = name;
        this._score = score;
    }

    /**
     * Copying Constructor
     * @param newNBestResult given Result
     */
    public NBestResult(NBestResult newNBestResult){
        this._name = newNBestResult.getName();
        this._score = newNBestResult.getScore();
    }

    /**
     * Getter of the Name of the Result
     * @return Name of Result
     */
    public String getName() { return this._name;  }

    /**
     * Getter of the Score of the Result
     * @return Score of Result
     */
    public double getScore() { return this._score; }

    /**
     * Setter for the Name of the Result
     * @param value new Name Value
     */
    public void setName(String value){ this._name = value; }

    /**
     * Setter for the Score of the Result
     * @param value new Score Value
     */
    public void setScore(double value){ this._score = value; }
}