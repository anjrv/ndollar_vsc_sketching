package Portal;

import Portal.Client.Client;
import Portal.DolarN.NDolarRecognizer;
import Portal.DolarN.TemplateLocalStorage;
import Portal.Helper.Utils;
import Portal.Server.Server;
import Portal.Storage.JSONData;
import Portal.Storage.*;
import Portal.Storage.Point;

import Portal.Storage.Stroke;
import javafx.application.Application;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.InputEvent;

import java.io.IOException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sun.jna.Native.*;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.*;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.WinDef.HWND;

public class CanvasLogic extends Application {

    /*
     * Reference to the Singleton Class
     */
    public static SingletonStorage singletonStorage;

    /**
     * The Server
     */
    private Server myServer;
    {
        try {
            myServer = new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Variable containing a local history of the sent items (recognition items).
     *
     * Useful because the already-recognized items should not be sent again to VS Code
     */
    private static ArrayList<JSONData> JSONHistory;

    /**
     * Log Data class - initialized
     * Class used to save the events that happened during the usability study.
     */
    public static LogDataDB logDataDB = LogDataDB.getInstance();

    /**
     * Flag indicating if the IDE (VS Code) closed prematurely
     * and that the Canvas is still running. However, without no process to sent the data towards.
     */
    public static boolean IDEClosedPrematurely = false;

    /**
     * local instance of the Recognizer
     */
    private static NDolarRecognizer myDollarNRecognizer;

    /*
     * The Robot
     */
    public static Robot robot;

    /**
     * Max distance, in pixels, between the strokes representing the scrolling gesture
     * Together with other Scrolling variables - useful for the 2-finger stroll
     */
    private final double twoFingerGestureDistance = 80.0;
    private boolean scrollGestureTriggered = false,
            scrollingGestureID1Released = true,
            scrollingGestureID2Released = true,
            strokeID1RepresentsAScrollingGesture = false,
            strokeID2RepresentsAScrollingGesture = false;
    private double  scrollGestureDirection = 100,
            scrollGestureID1Height = 0.0,
            scrollGestureID2Height = 0.0;

    /**
     * There are 2 types of interactions, coming from the Mouse:
     * 0 - left click (Primary button)
     * 1 - right click (Secondary button)
     */
    private int typeOfLastMouseInteraction;

    /**
     * This variable shows if the last right click event was consumed or not.
     */
    private boolean mouseRightClickEventConsumed = true;

    /**
     * Point of the last clicked position on the special section of the Title Bar (the section containing more options)
     */
    private final Point mouseLastClickedPositionOnTitleBarSection = new Point(0,0);

    /**
     * Boolean variable holding weather the last Title Bar interaction was consumed or not
     */
    private boolean titleBarClickConsumed = true;

    /**
     * There are 2 possible types of interactions with the canvas:
     * 0 - normal I/O operations = Mouse
     * 1 - touch I/O operations = Touch/ *PEN
     *
     * * the specific PEN is not available, due to JavaFX
     * * Keyboard interactions are treated by the IDE
     */
    private int typeOfLastInteraction = 0;

    /**
     * Represents the type of interaction the user last had with the canvas
     * If it was a TOUCH Event, it is set to 0;
     * If it was a MOUSE Event, it is set to 1;
     */
    public static int typeOfLastInteractionBeforeScroll;

    /*
     * Holds the time when the touch event was released.
     * It is used for the Scroll Functionality.
     */
    private Timestamp timeWhenTouchEventWasReleased;

    /**
     * Upon pressing a specific (eraser) button, the mode will be shifted to Eraser Mode.
     * This mean that any stroke that is withing the bounding box of the Following Eraser-Stroke(s) will be removed from the canvas (and the storage).
     */
    public static boolean eraserMode = false;

    /**
     * Represents the Cartesian Coordinate of the position where the user last (PRIMARY) clicked with the canvas.
     */
    private final Point mouseLastLeftClicked = new Point(0,0);

    /**
     * Represents the Cartesian Coordinate of the position where the user last (Secondary) right clicked with the canvas.
     */
    private final Point mouseLastRightClicked = new Point(0,0);

    /**
     *  Instantiates the JNA.
     */
    static User32 user32 = User32.instance;

    /**
     * JNA
     * User32 is used for changing the focus between opened windows through JNA.
     *   Here, the process will be between the opened Canvas and the running IDE
     *   It allows the Robot the mimic the behaviour of each user.
     */
    private interface User32 extends W32APIOptions {

        // loading the JNI
        User32 instance = loadLibrary("user32", User32.class, DEFAULT_OPTIONS);

        /**
         * Focuses the specified window
         */
        void ShowWindow(HWND hWnd, int nCmdShow);

        /**
         * Sets the process having the specified hWnd in foreground
         * @param hWnd
         */
        void SetForegroundWindow(WinDef.HWND hWnd);
        /**
         *
         * @param winClass null in our case
         * @param title the title (from Task Bar) of the targeted IDE
         * @return the requested window
         */
        WinDef.HWND FindWindow(String winClass, String title);

        /* List of SHOW COMMANDS - Predefined by JNA. */
        // list can be found here: https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-showwindow
        int SW_HIDE = 0;
        int SW_SHOWNORMAL = 1;
        int SW_SHOWMINIMIZED = 2;
        int SW_MAXIMIZE = 3;
        int SW_SHOWNOACTIVATE = 4;
        int SW_SHOW = 5;
        int SW_MINIMIZE = 6;
        int SW_SHOWMINNOACTIVE = 7;
        int SW_SHOWNA = 8;
        int SW_RESTORE = 9;
        int SW_SHOWDEFAULT = 10;
        int SW_FORCEMINIMIZE = 11;
    }

    // Holds a Unique identifier representing the thread of the Chosen IDE
    public static HWND CHOSEN_IDE_HWND;
    // and its Name/Title
    public static String CHOSEN_IDE_HWND_STRING;

    // List of all running IDEs
    public static ArrayList<HWND> HWND_List = new ArrayList<>();

    /**
     * Variable showing if the recognizer was poked - by whether a button press, or timeout
     */
    public static boolean recognizerPoked = false;

    /**
     * Amount of time that must pass after the last drawing was completed - in order to poke to recognizer.
     * it will be set to 1 second, by default.
     * Meaning that the recognition will start after ~ 1 second after the user stops interacting with the canvas (no touch inputs, no mouse movement
     */
    private static final int TIME_UNTIL_POKING_STARTS = 1000;

    // The next few variables are used to add an option for the user to test a Yes/No message received from VS Code.
    public static boolean interpretationLayerRequestResponseFLAG = false,
            textWithTwoOptionsIsDisplayed = false;
    public static Point topLeftCornerOfFLAG = new Point(0,0);
    public static double   widthOfFLAG = 0,
            heightOfFLAG = 0;
    public static String textOfFLAG = "",
            resultOfFLAG = "";
    public static Stroke yesBox = new Stroke(),
            noBox = new Stroke();

    /**
     * Instance of the CanvasUI
     */
    private static CanvasUI myCanvasUI;

    /**
     * Function resetting the History
     * called when all the ink was removed
     *    it makes sense, as, after removing the ink, what will be drawn again is guaranteed to be new information
     */
    public static void resetHistory(){
        JSONHistory = new ArrayList<>();
    }

    /**
     * Emulating the Left Click [by using the robot]
     * @param clickPressPositionX x position of Touch point
     * @param clickPressPositionY y position of Touch point
     * @param clickReleasePositionX x position of Release point
     * @param clickReleasePositionY y position of Release point
     */
    public static void robotLeftClick(double clickPressPositionX, double clickPressPositionY, double clickReleasePositionX, double clickReleasePositionY){
        robot.mouseMove((int) clickPressPositionX, (int) clickPressPositionY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseMove((int) clickReleasePositionX, (int) clickReleasePositionY);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Emulating the Right Click [by using the robot]
     * @param clickPressPositionX x position of Touch point
     * @param clickPressPositionY y position of Touch point
     */
    public static void robotRightClick(double clickPressPositionX, double clickPressPositionY){
        robot.mouseMove((int) clickPressPositionX, (int) clickPressPositionY);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        robot.delay(2);
    }

    /**
     * Calls the robot to mimic a scroll event;
     * @param X the x coordinate of your current mouse (or touch) position
     * @param Y the y coordinate of your current mouse (or touch) position
     * @param direction towards the user have scrolled:
     *             either downwards (negative value) or
     *                      upwards (positive value)
     * @param numberOfScrolls how many times the scrolling event should be replicated
     */
    public static void robotScroll(double X, double Y, double direction, int numberOfScrolls){
        if(numberOfScrolls<1){
            numberOfScrolls = 1;
        }
        if(numberOfScrolls > 5){
            numberOfScrolls = 3;//5;
        }
        CanvasLogic.robot.mouseMove(CanvasUI.size.width - 2, (int) Y);
        for(int i = 0 ; i < numberOfScrolls ; i++) {
            robot.mouseWheel(direction > 0 ? -1 : 1);
        }
        CanvasLogic.robot.mouseMove((int) X, (int) Y);
    }

    /**
     * Updates the last position that was clicked on the special section of the Title Bar
     *
     * @param x Width position
     * @param y Height position
     */
    private void updateLastClickedPositionOnTitleBarSection(double x, double y){
        mouseLastClickedPositionOnTitleBarSection.updateX(x);
        mouseLastClickedPositionOnTitleBarSection.updateY((y));
    }

    /**
     * Updates the position of the last PRIMARY Click interaction with the canvas.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    private void updateLastLeftClickedPosition(double x, double y){
        mouseLastLeftClicked.updateX(x);
        mouseLastLeftClicked.updateY(y);
    }

    /**
     * Updates the position of the last SECONDARY Right Click interaction with the canvas.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    private void updateLastRightClickedPosition(double x, double y){
        mouseLastRightClicked.updateX(x);
        //mouseLastRightClicked.updateY((y + myCanvasUI.getTitleBarVscodeHeight()));
        mouseLastRightClicked.updateY(y);
    }

    // Focuses on the IDE
    public static void focusOnIDE(){
        user32.ShowWindow(CHOSEN_IDE_HWND, User32.SW_MAXIMIZE);
        user32.SetForegroundWindow(CHOSEN_IDE_HWND);
    }

    // TEST Method - prints out all running processes (their ID and Title)
    public void printAllRunningProcesses(){
        for(var runningProcess : WindowUtils.getAllWindows(true)){
            System.out.println(runningProcess.getHWND()+" "+runningProcess.getTitle());
        }
    }

    /**
     * Detects the HWND list of all running processes
     *
     * This function finds the very first process with a non-null name (OR not a background process).
     * the .JAR file will be called from within VSCode, so it should be the IDE that is captured by this function
     *
     * However, there is an additional check done after the canvas starts, checking the name of the focused process,
     * as well as checking if the IDE is still running.
     * In any of these 2 cases, if the response is negative, it will force shut down the Canvas.
     */
    private void detectTheRunningIDE(){
        for(var runningProcess : WindowUtils.getAllWindows(true)){
            if(!runningProcess.getTitle().equals("")){
                HWND_List.add(runningProcess.getHWND());
                CHOSEN_IDE_HWND = runningProcess.getHWND();
                CHOSEN_IDE_HWND_STRING = CHOSEN_IDE_HWND.toString();
                break;
            }
        }
    }

    /**
     * Function checking if the IDE (by process) is still running or not
     * @return TRUE if the process representing the IDE is still running, and FALSE otherwise
     */
    private static boolean theCorrectIDEIsRunning(){
        for(var runningProcess : WindowUtils.getAllWindows(true)){
            if(!runningProcess.getTitle().equals("")) {
                if (runningProcess.getHWND().toString().equals(CHOSEN_IDE_HWND_STRING)) {
                    if(runningProcess.getTitle().contains("Visual Studio Code")) {
                        return true;
                    }
                    else{
                        System.out.println("WARNING: The focused IDE is not Visual Studio Code.\nMake sure to open the canvas from within VS Code!\nProcess is terminating...\n\n");
                        return false;
                    }

                }
            }
        }
        return false;
    }

    /**
     * This function decides if the recognition process should be called or not.
     */
    public static void checkIfPokingIsNeeded(){
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            // If the recognition was not poked, and there are regular strokes in the storage
            if(!recognizerPoked && singletonStorage.getCommandStrokes().size() > 0) {
                focusOnIDE();
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                // If enough time passed since the user 'finished' interacting with the touch screen
                if (currentTime.getTime() - CanvasUI.TIME_OF_LAST_INTERACTION.getTime() > TIME_UNTIL_POKING_STARTS) {
                    recognizerPoked = true;

                    CanvasUI.reDraw();
                    ArrayList<Stroke> listOfStrokesFromCanvas = singletonStorage.getCommandStrokes();

                    //calls the Recognizer -> The recognition process has started
                    ArrayList<JSONData> recognizedShapesOnCanvas = myDollarNRecognizer.startRecognitionOf(listOfStrokesFromCanvas);

                    logDataDB.addEventToDatabase(new LogData("Info","Recognition Started (Automatic)", new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));

                    // if the "Data Collection Mode" is ON, set the intended shape before the result in the logDataDB

                    if(CanvasUI.dataCollectionModeCheckbox.isSelected()){
                        logDataDB.addEventToDatabase(new LogData("Button","Intended shape: "+CanvasUI.comboBoxUserIntendedToDrawSymbol.getValue().toString(), new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));
                    }

                    for (JSONData json_data : recognizedShapesOnCanvas) {
                        logDataDB.addEventToDatabase(new LogData("Result", "", new Timestamp(System.currentTimeMillis()), new Stroke(), json_data));
                        if(json_data.getRecognizedAs().equals("Arrow")){
                            Point p_tip = json_data.getTip();
                            Point p_start = json_data.getStartingPoint();

                            CanvasUI.drawBox(p_tip, 5,5, Color.RED);
                            CanvasUI.drawBox(p_start,5,5,Color.GREEN);

                            // TBA?: Show the pointing area!
                        }
                        //System.out.println(json_data.getRoot().getX()+" "+json_data.getRoot().getY()+"   "+json_data.getHeight()+" "+json_data.getWidth()+" "+json_data.getScore());
                    }

                    // the actual results that will be sent (excluding duplicates
                    ArrayList<JSONData> JSONListToBeSent = new ArrayList<>();
                    for (JSONData json_data : recognizedShapesOnCanvas) {
                        // if the data was not already sent, include it in the list that will be sent.
                        if (!Utils.listOfJSONContainsItem(JSONHistory, json_data)) {
                            JSONHistory.add(json_data);
                            JSONListToBeSent.add(json_data);
                        }
                    }

                    if(JSONListToBeSent.size()>=1 && !CanvasUI.dataCollectionModeCheckbox.isSelected()) {
                        try {
                            Client.sendHttp3PostRequestToVSCodeServer(JSONListToBeSent);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }

                    focusOnIDE();

                    if(CanvasUI.dataCollectionModeCheckbox.isSelected()){
                        CanvasUI.removeAllInk();
                    }
                }
            }
        }, 200,500,TimeUnit.MILLISECONDS);
    }

    /**
     * Function checking whether the IDE is still running, or if it was 'prematurely' closed - before the Canvas
     */
    public static void checkIfIDEIsStillRunning(){
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            if(theCorrectIDEIsRunning() == false){
                System.out.println("IDE process not running anymore. Canvas closing soon...");
                IDEClosedPrematurely = true;
                CanvasUI.buttonClose.fire();
            }
        }, 4000,4000,TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a Yes/No option
     *
     * @param topLeftCorner root of element
     * @param width width of element
     * @param height height of element
     * @param textFromInterpretationLayer Text that will be displayed
     */
    public static void instantiateTextWithTwoOptions(Point topLeftCorner, double width, double height, String  textFromInterpretationLayer){
        interpretationLayerRequestResponseFLAG = true;
        textWithTwoOptionsIsDisplayed = true;
        topLeftCornerOfFLAG = new Point(topLeftCorner.getX(),topLeftCorner.getY());
        widthOfFLAG = width;
        heightOfFLAG = height;
        textOfFLAG = textFromInterpretationLayer;
        resultOfFLAG = "";

        yesBox = new Stroke();
        yesBox.beginPath(     new Point((topLeftCorner.getX()),              (topLeftCorner.getY() + height/2)));
        yesBox.addPointToPath(new Point((topLeftCorner.getX()),              (topLeftCorner.getY() + height)));
        yesBox.addPointToPath(new Point((topLeftCorner.getX() + width/2 - 2),(topLeftCorner.getY() + height)));
        yesBox.addPointToPath(new Point((topLeftCorner.getX() + width/2 - 2),(topLeftCorner.getY() + height/2)));
        yesBox.finishPath();

        noBox = new Stroke();
        noBox.beginPath(     new Point((topLeftCorner.getX() + width/2 + 2), (topLeftCorner.getY() + height/2)));
        noBox.addPointToPath(new Point((topLeftCorner.getX() + width/2 + 2), (topLeftCorner.getY() + height)));
        noBox.addPointToPath(new Point((topLeftCorner.getX() + width),       (topLeftCorner.getY() + height)));
        noBox.addPointToPath(new Point((topLeftCorner.getX() + width),       (topLeftCorner.getY() + height/2)));
        noBox.finishPath();
    }

    /**
     * Function that defines the functionality of the buttons found on CanvasUI
     */
    private void defineCanvasUIActionsForTheButtons(){
        CanvasUI.collapsableMenuRoot.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    focusOnIDE();
                });

        CanvasUI.helpButton.setOnAction(e -> {
            focusOnIDE();
            CanvasUI.transitionForButtons.setOnFinished(event -> {
                CanvasUI.helpButton.setStyle(CanvasUI.helpButtonOriginalStyle);
            });

            CanvasUI.helpButton.setStyle(CanvasUI.helpButtonClickedStyle);

            CanvasUI.reDraw();

            CanvasUI.transitionForButtons.playFromStart();

            // Do the intended action -> Display the other mini window
            CanvasUI.displayHelpMenu();
        });

        CanvasUI.restartCanvasButton.setOnAction(e -> {
            focusOnIDE();
            CanvasUI.transitionForButtons.setOnFinished(event -> {
                CanvasUI.restartCanvasButton.setStyle(CanvasUI.restartCanvasButtonOriginalStyle);
            });

            CanvasUI.restartCanvasButton.setStyle(CanvasUI.resetCanvasButtonClickedStyle);

            CanvasUI.reDraw();

            CanvasUI.transitionForButtons.playFromStart();

            // Send a Signal to VS Code : Reset Canvas.
            try {
                Client.sendSignalToExtension("Restart Canvas");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            CanvasUI.buttonClose.fire();
        });

        CanvasUI.forcePokingButton.setOnAction(e -> {
            focusOnIDE();
            CanvasUI.transitionForButtons.setOnFinished(event -> {
                CanvasUI.forcePokingButton.setStyle(CanvasUI.forcePokingButtonOriginalStyle);
            });

            CanvasUI.forcePokingButton.setStyle(CanvasUI.forcePokingButtonClickedStyle);

            CanvasUI.reDraw();

            CanvasUI.transitionForButtons.playFromStart();

            // a poking is needed here. This part is a duplicate of checkIfPokingIsNeeded
            ArrayList<Stroke> listOfStrokesFromCanvas = singletonStorage.getCommandStrokes();
            ArrayList<JSONData> resultsOfPoking = myDollarNRecognizer.startRecognitionOf(listOfStrokesFromCanvas);

            logDataDB.addEventToDatabase(new LogData("Button","Force Poking", new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));

            for (JSONData json_data : resultsOfPoking) {
                logDataDB.addEventToDatabase(new LogData("Recognized Shape", "", new Timestamp(System.currentTimeMillis()), new Stroke(), json_data));
            }

            recognizerPoked = true;
            logDataDB.addEventToDatabase(new LogData("Button","Force Poking", new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));
            logDataDB.addEventToDatabase(new LogData("Info","Recognition started (Manually)", new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));

            if(CanvasUI.dataCollectionModeCheckbox.isSelected()){
                CanvasUI.removeAllInkButtonInCollapsableMenu.fire();
            }

            focusOnIDE();
        });

        CanvasUI.colorPicker.setOnAction(e -> {
            CanvasUI.ANNOTATION_COLOR = CanvasUI.colorPicker.getValue();
            CanvasUI.extraInkInCollapsableMenuButton.fire();
        });

        CanvasUI.addTemplateButton.setOnAction(e -> {
            focusOnIDE();

            if(singletonStorage.getCommandStrokes().size()>=1) {
                ArrayList<Stroke> currentTemplate = singletonStorage.getCommandStrokes();
                String currentSelectedName = CanvasUI.comboBoxUserIntendedToDrawTemplateFor.getValue().toString();
                TemplateLocalStorage.getInstance().addTemplateToLocalStorage(currentSelectedName, currentTemplate);
                System.out.println("Template containing "+currentTemplate.size()+" strokes was saved as "+currentSelectedName);
            }

            CanvasUI.removeAllInkButtonInCollapsableMenu.fire();
        });
    }

    /**
     * Function adding the lately-pressed button event to the Log database
     * @param buttonName name of the button
     */
    public static void addButtonLogToDB(String buttonName){
        logDataDB.addEventToDatabase(new LogData("Button", buttonName, new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));
        if(buttonName.equals("Close")){
            try {
                logDataDB.saveAsExcel();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Launches the JAVA Application.
     * @param args arguments provided when the program runs
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Actual start function of the Software.
     * @param startingStage the stage containing the canvas and the other elements.
     *                      By default, it is called when the software starts.
     * @throws AWTException Any error interfering with JavaFX's Canvas.
     */
    public void start(Stage startingStage) throws AWTException {

        // instantiating the history of sent recognition results.
        JSONHistory = new ArrayList<>();

        // instantiating the recognizer;
        myDollarNRecognizer = NDolarRecognizer.getInstance();

        /*
         * Instantiating the robot that is going to be used to click through the canvas.
         * It mimics the behaviour of the user
         */
        robot = new Robot();

        // giving some time (5 milliseconds, or 0.005 seconds) to the Recognizer and Robot to load.
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        detectTheRunningIDE();
        checkIfIDEIsStillRunning();

        // Instantiating the CanvasUI
        myCanvasUI = new CanvasUI(startingStage);

        defineCanvasUIActionsForTheButtons();

        // Getting an instance of the Singleton Storage
        singletonStorage = SingletonStorage.getInstance();

        // Last interaction was a Touch interaction
        typeOfLastInteractionBeforeScroll = 0;

        timeWhenTouchEventWasReleased = new Timestamp(System.currentTimeMillis());

        // local variable used to add the newly created strokes - via touch interaction
        final Stroke[] myStrokes = new Stroke[11];

        // Next, the Handlers for Canvas:
        //      Mouse events usually imply triggering the robot to do the same mouse events;
        //      Touch events imply the processing of those touch Points, so a Stroke could be created.

        CanvasUI.canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    Timestamp timeRightNow = new Timestamp(System.currentTimeMillis());

                    // if the last interaction was actually a mouse interaction, and not a false mouse interaction triggered by the touch-detecting system:
                    if(timeRightNow.getTime() - timeWhenTouchEventWasReleased.getTime() > 200)
                        typeOfLastInteractionBeforeScroll = 1;

                    // if the last interaction came from a mouse
                    if (typeOfLastInteraction == 0) {
                        Stroke mouseStroke = new Stroke();
                        mouseStroke.beginPath(new Point(event.getX(), event.getY()));
                        mouseStroke.finishPath();
                        logDataDB.addEventToDatabase(new LogData("Mouse","Pressed", new Timestamp(System.currentTimeMillis()), mouseStroke, new JSONData()));

                        if (event.getButton() == MouseButton.PRIMARY) {
                            /* If the last click was Left Click */
                            updateLastLeftClickedPosition(event.getX(), event.getY());
                            typeOfLastMouseInteraction = 0;

                        } else if (event.getButton() == MouseButton.SECONDARY) {
                            /* If the last click was Right Click */
                            typeOfLastMouseInteraction = 1;
                            mouseRightClickEventConsumed = false;
                            updateLastRightClickedPosition(event.getX(), event.getY());
                        }
                    }

                });

        CanvasUI.canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    // if the last interaction came from a mouse
                    if (typeOfLastInteraction == 0){
                        // Further commands could be added here
                    }
                });

        CanvasUI.canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    // if the last interaction came from a mouse
                    if (typeOfLastInteraction == 0) {
                        Stroke mouseStroke = new Stroke();
                        mouseStroke.beginPath(new Point(event.getX(), event.getY()));
                        mouseStroke.finishPath();
                        logDataDB.addEventToDatabase(new LogData("Mouse","Released", new Timestamp(System.currentTimeMillis()), mouseStroke, new JSONData()));
                        try {
                            // Take the canvas on the background
                            CanvasUI.setPrimaryStageAlwaysOnTop(false);

                            // Shift the focus to the IDE
                            focusOnIDE();

                            // Let the robot click on the specified area
                            if(typeOfLastMouseInteraction == 0) {
                                // PRIMARY (Left) Click
                                if(mouseRightClickEventConsumed) {
                                    // Click and drag = select
                                    if(titleBarClickConsumed == true) {
                                        robot.mouseMove((int) mouseLastLeftClicked.getX(), (int) mouseLastLeftClicked.getY());
                                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                        robot.mouseMove((int) event.getX(), (int) event.getY());
                                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                    }
                                    else {  // topBarClick was not consumed yet
                                        titleBarClickConsumed = true;
                                        robot.mouseMove((int) mouseLastClickedPositionOnTitleBarSection.getX(), (int) mouseLastClickedPositionOnTitleBarSection.getY());
                                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                        robot.delay(5);
                                        robot.mouseMove((int) event.getX(), (int) event.getY());

                                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    }

                                } else {
                                    // A right click was pressed before. It needs to be consumed
                                    robot.mouseMove((int) mouseLastRightClicked.getX(), (int) mouseLastRightClicked.getY());
                                    robot.delay(5);

                                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                    robot.delay(5);
                                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

                                    robot.mouseMove((int) mouseLastLeftClicked.getX(), (int) mouseLastLeftClicked.getY());
                                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    //robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); - This one disables the Second Layer in Context Menu
                                    mouseRightClickEventConsumed = true;
                                }

                                if(event.getY() < CanvasUI.getTitleBarVscodeHeight() && event.getX() < CanvasUI.getTitleBarVscodeWidth()){
                                    updateLastClickedPositionOnTitleBarSection(event.getX(),event.getY());
                                    titleBarClickConsumed = false;
                                }

                            } else if (typeOfLastMouseInteraction == 1) {
                                // SECONDARY (Right) Click
                                robot.mouseMove((int) mouseLastRightClicked.getX(), (int) mouseLastRightClicked.getY());
                                robot.delay(5);
                                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                robot.delay(5);

                                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                                //robot.mouseMove((int) event.getX(), (int) event.getY() + myCanvasUI.getTitleBarVscodeHeight());
                                robot.mouseMove((int) event.getX(), (int) event.getY());
                                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                            }


                            // Bring the canvas back on front
                            Thread.sleep(20);

                            CanvasUI.setPrimaryStageAlwaysOnTop(true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // it was a touch input.
                        typeOfLastInteraction = 0;
                    }
                    CanvasUI.TIME_OF_LAST_INTERACTION = new Timestamp(System.currentTimeMillis());
                });


        CanvasUI.canvas.addEventHandler(TouchEvent.TOUCH_PRESSED, event -> {
            if(event.getTouchPoint().getId() <= 10) {
                timeWhenTouchEventWasReleased = new Timestamp(System.currentTimeMillis());
                typeOfLastInteractionBeforeScroll = 0;

                typeOfLastInteraction = 1;
                CanvasUI.graphicsContextGlobal.beginPath();
                CanvasUI.graphicsContextGlobal.moveTo(event.getTouchPoint().getX(), event.getTouchPoint().getY());

                myStrokes[event.getTouchPoint().getId() - 1] = new Stroke();
                myStrokes[event.getTouchPoint().getId() - 1].beginPath(new Point(event.getTouchPoint().getX(), event.getTouchPoint().getY()));
            }
        });

        CanvasUI.canvas.addEventHandler(TouchEvent.TOUCH_MOVED, event -> {
            if(event.getTouchPoint().getId() <= 10) {
                // TO BE NOTED: the SCROLLING GESTURE must be created by the first 2 (ongoing) strokes. On other words, make sure that there is no other finger in contact with the touch screen before you are doing the gesture

                // If there are exactly 2 gestures
                if(event.getTouchPoint().getId() == 2){
                    // take the ID's (indexes) of these 2 gestures (index 0 [currentStroke] and 1 [previousStroke])
                    int currentStrokeID = event.getTouchPoint().getId()-1;
                    int previousStrokeID = currentStrokeID - 1;

                    //the strokes must have at least 5 points added up to this point - to determine the direction of scrolling;
                    int defaultScrollingIndex = 5;

                    // if both strokes constructing the gesture contain at least those 5 points:
                    if(myStrokes[currentStrokeID].getPathPoints().size()>defaultScrollingIndex &&
                            myStrokes[previousStrokeID].getPathPoints().size()>defaultScrollingIndex){

                        // take their first (initial) points
                        Point currentInitialPoint = myStrokes[currentStrokeID].getPathPoints().get(0),
                              previousInitialPoint = myStrokes[previousStrokeID].getPathPoints().get(0);

                        // if these points are within a short distance. ~ 80px
                        if(Math.abs(currentInitialPoint.getX()-previousInitialPoint.getX())<=twoFingerGestureDistance){

                            // if the gesture hasn't triggered a scrolling event yet
                            if(scrollGestureTriggered == false) {
                                // set the 2 strokes as representing a scrolling event
                                scrollGestureTriggered = true;
                                strokeID1RepresentsAScrollingGesture = true;
                                strokeID2RepresentsAScrollingGesture = true;

                                // set the 'release' variables as false [stroke on-going]
                                scrollingGestureID1Released = false;
                                scrollingGestureID2Released = false;

                                // determine the scrolling direction: positive for going upwards, negative for going downwards.
                                if( currentInitialPoint.getY() > myStrokes[currentStrokeID].getPathPoints().get(defaultScrollingIndex).getY() &&
                                    previousInitialPoint.getY() > myStrokes[previousStrokeID].getPathPoints().get(defaultScrollingIndex).getY() ){
                                    // initial point is BELOW the last point - for both strokes -> fingers are going up;
                                    scrollGestureDirection = -100.0;
                                }
                                else if(currentInitialPoint.getY() <= myStrokes[currentStrokeID].getPathPoints().get(defaultScrollingIndex).getY() &&
                                        previousInitialPoint.getY() <= myStrokes[previousStrokeID].getPathPoints().get(defaultScrollingIndex).getY()){
                                    // initial point is ABOVE the last point - for both strokes -> fingers are going down;
                                    scrollGestureDirection = 100.0;
                                }

                                // The last step (done when both strokes will be finished) would be to check how long (height-wise = in pixels) the gesture is, in order to determine how many scrolling events to call
                                // step 1 on HIDING THE SCROLLING GESTURE'S STROKES
                                CanvasUI.reDraw();
                            }
                        }

                    }

                }


                // if the stroke doesn't represent a scrolling gesture, than let the user draw
                // step 2 on HIDING THE SCROLLING GESTURE'S STROKES: the IF statement
                if( event.getTouchPoint().getId() > 2 ||
                        (   event.getTouchPoint().getId() == 1 &&
                                strokeID1RepresentsAScrollingGesture == false) ||
                        (   event.getTouchPoint().getId() == 2 &&
                                strokeID2RepresentsAScrollingGesture == false)
                ) {
                    CanvasUI.graphicsContextGlobal.moveTo(myStrokes[event.getTouchPoint().getId() - 1].getEndPoint().getX(), myStrokes[event.getTouchPoint().getId() - 1].getEndPoint().getY());
                    CanvasUI.drawLineByUsingOvals( new Point(  myStrokes[event.getTouchPoint().getId() - 1].getEndPoint().getX(),
                                                                    myStrokes[event.getTouchPoint().getId() - 1].getEndPoint().getY()),
                                                        new Point(  event.getTouchPoint().getX(),
                                                                    event.getTouchPoint().getY()));
                    CanvasUI.graphicsContextGlobal.stroke();
                }

                myStrokes[event.getTouchPoint().getId() - 1].addPointToPath(new Point(event.getTouchPoint().getX(), event.getTouchPoint().getY()));
            }
        });

        CanvasUI.canvas.addEventHandler(TouchEvent.TOUCH_RELEASED, event -> {
            if(event.getTouchPoint().getId() <= 10) {
                timeWhenTouchEventWasReleased = new Timestamp(System.currentTimeMillis());

                // this IF STATEMENT was added to allow the scrolling gesture to be removed/ignored right away, without being processed
                if( event.getTouchPoint().getId() > 2 ||
                    (   event.getTouchPoint().getId() == 1 &&
                        strokeID1RepresentsAScrollingGesture == false) ||
                    (   event.getTouchPoint().getId() == 2 &&
                        strokeID2RepresentsAScrollingGesture == false)
                ) {
                    // regular behaviour - not scrolling gesture
                    try {
                        CanvasUI.graphicsContextGlobal.closePath();

                        myStrokes[event.getTouchPoint().getId() - 1].finishPath();
                        myStrokes[event.getTouchPoint().getId() - 1].updateLineThickness(CanvasUI.GLOBAL_LINE_THICKNESS);
                        myStrokes[event.getTouchPoint().getId() - 1].updateColor(CanvasUI.CANVAS_CURRENT_COLOR);

                        logDataDB.addEventToDatabase(new LogData("Touch", "", myStrokes[0].getTimeStopDrawing(), myStrokes[0], new JSONData()));

                        if (!eraserMode) {

                            // if the flag was triggered by VS CODE, check if the last Stroke intersect either the YES or NO box
                            if (interpretationLayerRequestResponseFLAG) {
                                boolean checkIfYesTicked = Utils.isInsideOf(myStrokes[event.getTouchPoint().getId() - 1], yesBox),
                                        checkIfNoTicked = Utils.isInsideOf(myStrokes[event.getTouchPoint().getId() - 1], noBox);
                                if (checkIfYesTicked) {
                                    resultOfFLAG = "Yes";
                                    System.out.println("User Chose: " + resultOfFLAG);
                                    interpretationLayerRequestResponseFLAG = false;
                                    textWithTwoOptionsIsDisplayed = false;
                                    Client.sendUserResponseToVSCode("Yes");
                                } else if (checkIfNoTicked) {
                                    resultOfFLAG = "No";
                                    System.out.println("User Chose: " + resultOfFLAG);
                                    interpretationLayerRequestResponseFLAG = false;
                                    textWithTwoOptionsIsDisplayed = false;
                                    Client.sendUserResponseToVSCode("No");
                                }
                                logDataDB.addEventToDatabase(new LogData("Info", "User has chosen: " + resultOfFLAG, new Timestamp(System.currentTimeMillis()), new Stroke(), new JSONData()));
                                CanvasUI.reDraw();
                            }
                            // Add the stroke to the Storage, as nothing happened
                            // the ELSE statement prevent the user from drawing while a box with two options appears
                            else if (myStrokes[event.getTouchPoint().getId() - 1].getPathPoints().size() > 1) {
                                //if (myCanvasUI.getCanvasCurrentColor() != myCanvasUI.ANNOTATION_COLOR) {
                                if (CanvasUI.TYPE_OF_INK_STATUS.equals("STATUS: Command INK")) {
                                    SingletonStorage.addCommandStroke(myStrokes[event.getTouchPoint().getId() - 1]);
                                } else {
                                    SingletonStorage.addAnnotationStroke(myStrokes[event.getTouchPoint().getId() - 1]);
                                }
                            }

                            // if the stroke actually intersected either the YES or NO box, s
                            // imply remove the stroke (as it should not be taken as a regular stroke).
                            if (!resultOfFLAG.equals("")) {
                                // instead of printing it out, send the response back to the Interpretation Layer!
                                resultOfFLAG = "";
                                // An UNDO call is needed, if the previous ELSE statement is removed.
                            }
                        } else {
                            // eraser mode: ON
                            // remove intersecting red strokes
                            for (int i = 0; i < singletonStorage.getAnnotationStrokes().size(); i++) {
                                if (Utils.overlap(myStrokes[event.getTouchPoint().getId() - 1], singletonStorage.getAnnotationStrokeAt(i))) {
                                    singletonStorage.removeAnnotationStrokeAt(i);
                                    i--;
                                }
                            }

                            //remove intersecting regular strokes
                            for (int i = 0; i < singletonStorage.getCommandStrokes().size(); i++) {
                                if (Utils.overlap(myStrokes[event.getTouchPoint().getId() - 1], singletonStorage.getCommandStrokeAt(i))) {
                                    singletonStorage.removeCommandStrokeAt(i);
                                    i--;
                                }
                            }

                            CanvasUI.reDraw();
                        }
                    } catch (Exception e) {
                        System.out.println("Touch Event Release - exception: " + e);
                    }
                }

                // for the scrolling gesture
                if(scrollGestureTriggered == true){
                    // scrolling gesture - stroke1 was lifted
                    if(event.getTouchPoint().getId() == 1){
                        scrollingGestureID1Released = true;
                        myStrokes[event.getTouchPoint().getId()-1].finishPath();
                        scrollGestureID1Height = myStrokes[event.getTouchPoint().getId()-1].getBottomBorderY()- myStrokes[event.getTouchPoint().getId()-1].getTopBorderY();
                    }

                    // scrolling gesture - stroke2 was lifted
                    if(event.getTouchPoint().getId() == 2){
                        scrollingGestureID2Released = true;
                        myStrokes[event.getTouchPoint().getId()-1].finishPath();
                        scrollGestureID2Height = myStrokes[event.getTouchPoint().getId()-1].getBottomBorderY()- myStrokes[event.getTouchPoint().getId()-1].getTopBorderY();
                    }

                    // if both strokes representing the scrolling gesture were lifted
                    if(scrollingGestureID1Released && scrollingGestureID2Released){

                        // determine how long the gesture is:
                        double scrollGestureLongestHeight = scrollGestureID1Height > scrollGestureID2Height ? scrollGestureID1Height : scrollGestureID2Height;

                        // determine the number of scrolling events that will be replicated
                        int numberOfScrollEventsToBeReplicated = (int)scrollGestureLongestHeight / 100;

                        // there must be at least 1 scroll (usually the case for a short scrolling gesture), and at most 5 scrolls
                        if(numberOfScrollEventsToBeReplicated < 1){
                            numberOfScrollEventsToBeReplicated = 1;
                        }
                        if(numberOfScrollEventsToBeReplicated > 5){
                            numberOfScrollEventsToBeReplicated = 5;
                        }

                        // replicate the scroll
                        CanvasLogic.robotScroll(event.getTouchPoint().getX(), event.getTouchPoint().getY(), scrollGestureDirection, numberOfScrollEventsToBeReplicated);

                        // also, make sure that that specific stroke was not added to the storage.
                        scrollGestureTriggered = false;
                        strokeID1RepresentsAScrollingGesture = false;
                        strokeID2RepresentsAScrollingGesture = false;
                    }
                }
            }
            recognizerPoked = false;
            CanvasUI.TIME_OF_LAST_INTERACTION = new Timestamp(System.currentTimeMillis());
        });

        // MODE: Remove drawings or not after recognition
        CanvasUI.dataCollectionModeCheckbox.setSelected(false);

        // Hand: LEFT / RIGHT
        CanvasUI.radioButtonHand1.setSelected(true);
        //myCanvasUI.radioButtonHand2.setSelected(true);

        // Type of test: FINGER / PEN
        CanvasUI.radioButtonType1.setSelected(true);
        //myCanvasUI.radioButtonType2.setSelected(true);

        focusOnIDE();
        checkIfPokingIsNeeded();
    }
}