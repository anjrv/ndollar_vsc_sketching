package Portal.Storage;

import java.util.*;

/**
 * The Storage Class, holding all Strokes
 * follows the Singleton design pattern
 * It contains two list of strokes: Command Strokes and Annotation Strokes
 */
public class SingletonStorage {

    /**
     * List of all Command Strokes from the Canvas;
     * Either Shapes or Text/Characters.
     */
    private static ArrayList<Stroke> CommandStrokes;

    /**
     * List of all Annotation Strokes from the Canvas;
     * The Extra text: Ideas, Blobs.
     */
    private static ArrayList<Stroke> AnnotationStrokes;

    /**
     * Global instance of the Storage
     */
    private static SingletonStorage instance;

    /**
     * Constructor
     * Instantiate both lists - CommandStrokes and AnnotationStrokes
     */
    private SingletonStorage() {
        CommandStrokes = new ArrayList<Stroke>();
        AnnotationStrokes = new ArrayList<Stroke>();
    }

    /**
     * @return the Singleton instance of the Storage;
     */
    public static SingletonStorage getInstance() {
        if(instance == null)
            instance = new SingletonStorage();
        return instance;
    }

    /**
     * @return a list of all Command Strokes.
     */
    public ArrayList<Stroke> getCommandStrokes() { return CommandStrokes; }

    /**
     * @return a list of all Annotation Strokes
     */
    public ArrayList<Stroke> getAnnotationStrokes(){ return AnnotationStrokes; }

    /**
     * @param index requested position.
     * @return one Command Stroke found at a specific position.
     */
    public Stroke getCommandStrokeAt(int index){
        return CommandStrokes.get(index);
    }

    /**
     * @param index requested position.
     * @return an Annotation Stroke found at a specific position.
     */
    public Stroke getAnnotationStrokeAt(int index){
        return AnnotationStrokes.get(index);
    }

    /**
     * Adds one Command Stroke to the current list.
     * @param stroke the stroke that will be added.
     */
    public static void addCommandStroke(Stroke stroke) {
        // update time since last drawing
        if(CommandStrokes.size() > 0){
            long endTimeOfLastStroke = CommandStrokes.get(CommandStrokes.size()-1).getTimeStopDrawing().getTime();
            long timeSinceLastDrawing = stroke.getTimeStartDrawing().getTime()- endTimeOfLastStroke;
            stroke.updateTimeSinceLastDrawing(timeSinceLastDrawing);
        }
        else{
            stroke.updateTimeSinceLastDrawing(0);
        }

        CommandStrokes.add(stroke);
    }

    /**
     * Adds one Annotation Stroke to the current list.
     * @param stroke the stroke that will be added.
     */
    public static void addAnnotationStroke(Stroke stroke) {
        stroke.updateTimeSinceLastDrawing(0);

        AnnotationStrokes.add(stroke);
    }

    /**
     * Removes a Command Stroke from a specified position.
     * @param index the position.
     */
    public void removeCommandStrokeAt(int index){
        CommandStrokes.remove(index);
    }

    /**
     * Removes an Annotation Stroke from a specified position.
     * @param index the position.
     */
    public void removeAnnotationStrokeAt(int index){
        AnnotationStrokes.remove(index);
    }

    /**
     * Removes all Command Strokes
     */
    public void removeAllCommandStrokes(){
        CommandStrokes = new ArrayList<Stroke>();
    }

    /**
     * Removes all Annotation Strokes
     */
    public void removeAllAnnotationStrokes(){
        AnnotationStrokes = new ArrayList<Stroke>();
    }

    /**
     * Removes every Stroke from both lists.
     */
    public void removeAllStrokesFromCanvas(){
        removeAllCommandStrokes();
        removeAllAnnotationStrokes();
    }
}