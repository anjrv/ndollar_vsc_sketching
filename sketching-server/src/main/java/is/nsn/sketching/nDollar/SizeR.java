package is.nsn.sketching.nDollar;

/**
 * Size used mainly for Rescaling (in Utils), but the constant is stored within the Recognizer
 */
public class SizeR {
    /**
     * Height and Width for the Size
     */
    private final double width;
    private final double height;

    /**
     * Constructor
     *
     * @param width  Width
     * @param height Height
     */
    public SizeR(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * getter of the Width
     *
     * @return Width of the Size
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * getter of the Height
     *
     * @return Height of the Size
     */
    public double getHeight() {
        return this.height;
    }
}
