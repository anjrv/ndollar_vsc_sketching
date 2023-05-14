package Portal.Storage;

import java.sql.Timestamp;

/** TO BE REMOVED AFTER THE USABILITY STUDY IS COMPLETED
 * Class representing an event that will be part of the log file
 */
public class LogData {
    /**
     * Type of event:
     *     Mouse
     *     Info
     *     Button
     *     Touch
     *     Result
     */
    private final String type;

    /**
     * Additional info.
     * e.g. Name of the button that was pressed (if pressed)
     *      Response form used (Yes/No)
     *      Type of Mouse interaction: Press/Release
     */
    private final String additionalInfo;

    /**
     * The stroke (if any)
     */
    private final Stroke stroke;

    /**
     * Start and End time of the event
     */
    private Timestamp eventStart,
            eventEnd;

    /**
     * (JSON) Data that was sent to the IDE (in any)
     */
    private final JSONData jsonData;

    /**
     * @return The type of event
     */
    public String getType(){
        return type;
    }

    /**
     * @return The name of the button
     */
    public String getAdditionalInfo(){
        return additionalInfo;
    }

    /**
     * @return the stroke
     */
    public Stroke getStroke(){
        return stroke;
    }

    /**
     * @return the end time
     */
    public Timestamp getEventEnd(){
        return eventEnd;
    }

    public Timestamp getEventStart(){
        return eventStart;
    }

    public void updateEventStart(Timestamp newEventStart){
        eventStart = newEventStart;
    }

    public void updateEventEnd(Timestamp newEventEnd){
        eventEnd = newEventEnd;
    }

    /**
     * @return the (JSON) Data
     */
    public JSONData getJsonData(){
        return jsonData;
    }

    /**
     * Constructor
     *
     * @param newType type of event
     * @param newButtonName name of button (if any)
     * @param newEventEnd end time of event
     * @param newStroke the stoke (if any)
     * @param newJsonData the (JSON) Data (if any)
     */
    public LogData(String newType,
                   String newButtonName,
                   Timestamp newEventEnd,
                   Stroke newStroke,
                   JSONData newJsonData){
        type = newType;
        eventEnd = newEventEnd;
        additionalInfo = newButtonName;
        stroke = newStroke;
        jsonData = newJsonData;
        if(type == "Touch"){
            updateEventStart(jsonData.getTimeOfStartDrawing());
            updateEventEnd(jsonData.getTimeOfStopDrawing());
        }
        else if(type == "Recognized Shape"){
            updateEventStart(jsonData.getTimeOfStartDrawing());
            updateEventEnd(jsonData.getTimeOfStopDrawing());
        }
    }
}