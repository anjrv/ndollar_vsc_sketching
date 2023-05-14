package Portal;

import Portal.Client.Client;
import Portal.DolarN.TemplateLocalStorage;
import Portal.Helper.Utils;
import Portal.Storage.*;
import Portal.Storage.Point;

import Portal.Storage.Stroke;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import static Portal.CanvasLogic.logDataDB;

/**
 * Class containing all the Visual Elements of this Sketching Software
 * Additional with the basic functionalities of some
 */
public class CanvasUI {

    /*
     * The screen size of the current machine
     * Used to instantiate the Canvas.
     * Future feature: If the monitor is changed, this size shall be updated.
     */
    public static final Dimension size = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();

    DirectoryChooser directoryChooser = new DirectoryChooser();

    /**
     * Instance of the Singleton Storage Class
     */
    private static SingletonStorage singletonStorage = SingletonStorage.getInstance();

    /*
     * Line Thickness variables
     */
    private static double   THICKNESS_STEP = 1.0,
                            THICKNESS_DEFAULT = 4.0,
                            MAX_THICKNESS = 7.0,
                            MIN_THICKNESS = 1.0;

    /**
     * Current global Thickness value
     * It is referring to the thickness of the stroke.
     */
    public static double GLOBAL_LINE_THICKNESS;

    // Default values for the HELPING DISPLAY.
    // preFileExtension is needed to avoid an error - while loading the image;
    // helpDirectoryPath = path of the folder including all helping images
    private String  preFileExtension = "file:///",
                    helpDirectoryPath = System.getProperty("user.dir")+"\\resource\\help\\";

    // the current index, used by the Carousel
    private int indexOfHelpImage = 0;

    private int indexOfVisualizationImage = 0;
    // list of the images (for the Carousel).
    private String[] helpImageNames;

    /*
     * Collapsable menu - size of un-collapsed menu
     */
    private static double   COLLAPSABLE_MENU_FULL_HEIGHT = 444,//419,// 395 = up to Recognition Speed-up
                            COLLAPSABLE_MENU_FULL_WIDTH = 165;// 165 = Primary menu

    /*
     * Collapsable menu - size of collapsed menu
     */
    private static double   COLLAPSABLE_MENU_COLLAPSED_HEIGHT = 26,
                            COLLAPSABLE_MENU_COLLAPSED_WIDTH = 100;

    private static double HELP_MENU_HEIGHT = 500,
                          HELP_MENU_WIDTH = 500;

    private static double   HELP_IMAGE_WIDTH = 500.0,
                            HELP_IMAGE_HEIGHT = 450.0;

    /**
     * Time of last interaction.
     * This variable is used to determine weather a Recognition call must be made or not.
     */
    public static Timestamp TIME_OF_LAST_INTERACTION;

    /*
     * Class used to draw on canvas
     */
    public static GraphicsContext graphicsContextGlobal;

    /*
     * Status text - displays the current type of ink [between COMMAND, ANNOTATION and ERASER]
     * the Current_Ink_Status will be displayed on the top-right part of the Canvas
     *  as well as on the upper part of the Collapsable Menu (but without the "STATUS:" part, due to the lack of space).
     */
    public static String TYPE_OF_INK_STATUS = "STATUS: Command INK";

    /*
     * Different Colors for the types of INK that are going to be displayed on the Canvas
     */
    public static Color COMMAND_COLOR = Color.web("0x032DC7",1.0),
                        ANNOTATION_COLOR = Color.web("0xD40226",0.8),
                        ERASE_COLOR = Color.web("0xFFA500",0.9);

    /**
     * The ink color that is currently selected on canvas. - either via pressing the COMMAND/ANNOTATION ink button, or via the color_picker
     */
    public static Color CANVAS_CURRENT_COLOR;

    /**
     * Height - of the title bar.
     * Usually it is around 29 pixels [if the resolution is the recommended one].
     */
    private final static double TITLE_BAR_VSCODE_HEIGHT = 29;

    /**
     * Represents the horizontal portion of the Title bar from VS CODE, where multiple options are found.
     * Because the robot cannot click on a currently hovered option (because the click on the Canvas process will trigger an automatic unfocus on the IDE),
     *      the previous click made here must be duplicated after the user decided what he/she wants to do.
     */
    private static double TITLE_BAR_VSCODE_WIDTH = 410;


    /**
     * Font of the text that will be added on Canvas through the function drawText
     */
    private static final double textFontSize = 12;

    /* Instance of the Canvas. */
    public static Canvas canvas;

    /**
     * SVG string representing the 'Draggable ICON'
     */
    public static String SVGDraggablePath = "M11.2,8.8l2.22-2.22a0,0,0,0,0,0-.06L11.2,4.3a0,0,0,0,0-.07,0V5.81a0,0,0,0,1-.05.05h-2a0,0,0,0,0,0,0v.59H8.33V6.32a.65.65,0,0,0,0-.22A1.32,1.32,0,0,0,7,5.12H7V4.38h.59a0,0,0,0,0,0,0v-2a0,0,0,0,1,0-.05H9.15a0,0,0,0,0,0-.07L7,0H6.9L4.68,2.23a0,0,0,0,0,0,.07H6.2a0,0,0,0,1,0,.05v2a0,0,0,0,0,0,0h.59v.74A1.35,1.35,0,0,0,5.56,6.41v.08H4.81V5.9a0,0,0,0,0,0,0h-2a0,0,0,0,1,0-.05V4.33s-.05-.06-.07,0L.45,6.52a0,0,0,0,0,0,.06L2.67,8.8a0,0,0,0,0,.07,0V7.28a0,0,0,0,1,0,0h2a0,0,0,0,0,0,0V6.61h.75v3a1.33,1.33,0,0,0-1.69-.39c-1,.54-3.22,1.88-3.22,1.88A1.34,1.34,0,0,0,0,12.21v2.53a4.39,4.39,0,0,0,1.89,3.43,4.5,4.5,0,0,0,5.7-.4L10,15.39l2.23-2.28s.3-.2.3-.4,0-.5-.79-.74-2-.55-3.33,1.14c0,0-.05.05-.05-.2V6.61h.72V7.2a0,0,0,0,0,0,0h2a0,0,0,0,1,.05,0V8.77A0,0,0,0,0,11.2,8.8Z";

    /**
     * All Stage(s)
     */
    public static Stage primaryStage;

    /**
     * All Scene(s)
     */
    private static Scene canvasScene;

    /**
     * All AnchorPane(s)
     */
    public static AnchorPane collapsableMenuRoot = new AnchorPane(),
                             helpRoot = new AnchorPane(),
                             visualizeTemplatesRoot = new AnchorPane();

    /**
     * All Group(s)
     */
    private static Group canvasRoot = new Group();

    /**
     * All SVGPath(s)
     */
    public static SVGPath svgDraggableIcon = new SVGPath();

    /**
     * All CheckBox(es)
     * The Data Collection Mode
     */
    public static CheckBox dataCollectionModeCheckbox = new CheckBox("ON");

    /**
     * All ComboBox (es)
     */
    public static ComboBox comboBoxUserIntendedToDrawSymbol = new ComboBox<String>(),
            comboBoxUserIntendedToDrawTemplateFor = new ComboBox<String>(),
            comboBoxTemplateFileNames = new ComboBox<String>();

    /**
     * All TextField(s)
     */
    public static TextField textFieldUserName = new TextField(),
                            newSymbolName = new TextField();

    /**
     * All HBox(es)
     */
    private static HBox topBarSectionOnCollapsableMenuHBox = new HBox(),
                        topBarOfCollapsableMenuHBox = new HBox(),
                        lineThicknessButtonsSectionHBox = new HBox(),
                        closeButtonHBox = new HBox(),
                        topBarSectionOnHelpDisplayHBox = new HBox(),
                        photoSectionOnHelpDisplayHBox = new HBox(),
                        buttonSectionOnHelpDisplayHBox = new HBox(),
                        topBarSectionOnVisualizationOfTemplatesDisplayHBox = new HBox(),
                        photoSectionOnVisualizationOfTemplatesDisplayHBox = new HBox(),
                        buttonSectionOnVisualizationOfTemplatesDisplayHBox = new HBox();

    ArrayList<BufferedImage> templatesAsImages = new ArrayList<>();

    /**
     * All VBox(es)
     */
    private static VBox vBoxLineThicknessModifierSectionOnCollapsableMenu = new VBox(),
                        vBoxInkChangeTypeSectionInCollapsableMenu = new VBox(),
                        vBoxFunctionalitySectionInCollapsableMenu = new VBox(),
                        vBoxExtra = new VBox(),
                        vBoxParticipant = new VBox(),
                        vBoxTemplates = new VBox(),
                        radioButtonsVBox = new VBox(),
                        radioButtonsHandVBox = new VBox(),
                        radioButtonsTypeVBox = new VBox();

    /**
     * All [private] Button(s)
     * the buttons requiring more logics are being defined by the CanvasLogic.
     */
    public static Button buttonClose = new Button("Close Extension");
    private static Button   collapseTheCollapsableMenuButton = new Button("-"),

                            regularInkInCollapsableMenuButton = new Button("COMMAND"),
                            eraserModeInCollapsableMenuButton = new Button("ERASER"),

                            increaseLineThicknessButton = new Button("+"),
                            decreaseLineThicknessButton = new Button("-"),

                            removeExtraInkButtonInCollapsableMenu = new Button("Remove Annotation INK"),
                            undoButtonInCollapsableMenu = new Button("UNDO"),

                            testButton = new Button("Test functionality"),
                            userChooseNewPath = new Button("Choose new 'save' path"),
                            addNewSymbolButton = new Button("Add new symbol"),
                            closeHelpMenuButton = new Button("Close HELP Menu"),
                            previousHintHelpMenuButton = new Button("< Previous"),
                            nextHintHelpMenuButton = new Button(" Next >"),
                            closeVisualizationMenuButton = new Button("Close VISUALIZATION Menu"),
                                    previousImageVisualizationMenuButton = new Button("< Previous Template"),
                            nextImageVisualizationMenuButton = new Button("Next Template >");

    /**
     * All [Public] Button(s)
     */
    public static Button    extraInkInCollapsableMenuButton = new Button("ANNOTATION"),
                            removeAllInkButtonInCollapsableMenu = new Button("Remove All INK"),
                            restartCanvasButton = new Button("Restart Canvas"),
                            helpButton = new Button("Help"),
                            forcePokingButton = new Button("Recognition Speed-Up"),
                            addTemplateButton = new Button("Add as Template"),
                            toggleSecondaryParticipantInfoMenuButton = new Button("Display Participant Menu"),
                            toggleSecondaryTemplateInfoMenuButton = new Button("Display Template Menu"),
                            visualizeTemplatesButton = new Button("Visualize Templates");

    public boolean displaySecondaryParticipantInfoMenu = false,
                    displaySecondaryTemplateInfoMenu = false;

    /**
     * All Text(s)
     */
    private static Text typeOfCurrentInkText = new Text(),
                        currentStatusText = new Text(),
                        inkTypeTextInCollapsableMenu = new Text("INK Type:"),
                        lineThicknessModifierText = new Text(),
                        functionalityTextInCollapsableMenu = new Text("Functionalities:"),
                        userIntendedToDrawSymbolText = new Text("User is drawing:"),
                        userIntendsToAddTemplateForText = new Text("Add template for:"),
                        userAddsNewSymbol = new Text("New Symbol:"),
                        userText = new Text("Participant:"),
                        clearCanvasText = new Text("Data Collection Mode:"),
                        participantTitleText = new Text("PARTICIPANT MENU"),
                        templateTitleText = new Text("TEMPLATE MENU"),
                        visualizeTemplateTitleText = new Text("VISUALIZATION"),
                        visualizeTemplateText = new Text("Select template file");

    /**
     * All RadioButton(s)
     */
    public static RadioButton   radioButtonHand1 = new RadioButton("Right-handed"),
                                radioButtonHand2 = new RadioButton("Left-handed"),

                                radioButtonType1 = new RadioButton("Finger"),
                                radioButtonType2 = new RadioButton("Pen");

    /**
     * All ToggleGroup(s)
     */
    public static ToggleGroup   toggleGroup = new ToggleGroup(),
                                toggleGroupHand = new ToggleGroup(),
                                toggleGroupType = new ToggleGroup();

    /**
     * All ColorPicker(s)
     */
    public static ColorPicker colorPicker = new ColorPicker();

    private static Image helpImage,
                        visualizationImage;

    private static ImageView helpImageView = new ImageView(),
                            visualizationImageView = new ImageView();
    /**
     * All PauseTransition(s)
     */
    public static PauseTransition transitionForButtons = new PauseTransition(Duration.seconds(0.075));

    /**
     * Styles of Buttons
     */
    public final static String
        forcePokingButtonOriginalStyle = "-fx-background-color: rgba(0, 230, 64, 0.6);" +
            "-fx-alignment: center;" +
            "-fx-text-fill: white;" +
            "-fx-min-width: 165",
        forcePokingButtonClickedStyle = "-fx-background-color: rgba(18, 184, 64, 0.6);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: white;" +
                "-fx-min-width: 165",
        restartCanvasButtonOriginalStyle = "-fx-background-color: rgba(245, 185, 20, 0.6);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: white;" +
                "-fx-min-width: 165",
        resetCanvasButtonClickedStyle = "-fx-background-color: rgba(204, 156, 22, 0.6);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: white;" +
                "-fx-min-width: 165",
        helpButtonOriginalStyle = "-fx-background-color: rgba(41, 227, 221, 0.6);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: white;" +
                "-fx-min-width: 165",
        helpButtonClickedStyle = "-fx-background-color: rgba(31, 191, 186);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: white;" +
                "-fx-min-width: 165",
        toggleSecondaryMenuOriginalStyle = "-fx-background-color: rgba(145, 61, 136, 0.6);" +
            "-fx-alignment: center;" +
            "-fx-text-fill: black;" +
            "-fx-min-width: 165",
        collapseTheCollapsableMenuButtonOriginalStyle = "-fx-background-color: rgba(211,211,211, 0.6);"+
            "-fx-alignment: center;" +
            "-fx-text-fill: white;" +
            "-fx-min-width: 25px;",
        regularInkInCollapsableMenuButtonOriginalStyle = "-fx-background-color: rgba(30, 139, 195, 0.6);" +
            "-fx-alignment: center;" +
            "-fx-text-fill: white;",
        extraInkInCollapsableMenuButtonOriginalStyle = "-fx-background-color: rgba(214, 69, 65, 0.6);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: white;",
        eraserModeInCollapsableMenuButtonOriginalStyle = "-fx-background-color: rgba(255, 165, 0, 0.6);" +
            "-fx-alignment: center;" +
            "-fx-text-fill: white;",
        decreaseLineThicknessButtonOriginalStyle = "-fx-background-color: rgba(177, 156, 217, 0.6);"+
            "-fx-alignment: center;" +
            "-fx-text-fill: white;",
        increaseLineThicknessButtonOriginalStyle = "-fx-background-color: rgba(148, 0, 211, 0.6);"+
            "-fx-alignment: center;" +
            "-fx-text-fill: white;",
        removeExtraInkButtonInCollapsableMenuOriginalStyle = regularInkInCollapsableMenuButtonOriginalStyle,
        removeAllInkButtonInCollapsableMenuOriginalStyle = regularInkInCollapsableMenuButtonOriginalStyle,
        undoButtonOriginalStyle = regularInkInCollapsableMenuButtonOriginalStyle,
        testButtonOriginalStyle = "-fx-background-color: rgba(241, 90, 34, 0.6);" +
            "-fx-alignment: center;" +
            "-fx-text-fill: white;",
        COLLAPSABLE_MENU_ROOT_STYLE_NOT_COLLAPSED = "-fx-background-color: rgba(232, 232, 232, 0.5);",
            HELP_ROOT_STYLE_DISPLAYED = "-fx-background-color: rgba(232, 232, 232, 1.0);",
        COLLAPSABLE_MENU_ROOT_STYLE_COLLAPSED = "-fx-background-color: rgba(232, 232, 232, 0.1);";

    /**
     * Function returning the width of the title bar [which is 410 pixels]
     * @return the width of the title bar [of VS Code]
     */
    public static double getTitleBarVscodeWidth(){
        return TITLE_BAR_VSCODE_WIDTH;
    }

    /**
     * Function returning the height of the title bar [which is 29 pixels]
     * @return the height of the title bar [of VS Code]
     */
    public static double getTitleBarVscodeHeight(){
        return TITLE_BAR_VSCODE_HEIGHT;
    }

    /**
     * Updates the TIME_OF_LAST_INTERACTION, and thus stop the Recognition calls as long as the user uses mouse,
     *     which is a sign that the interaction is not done yet.
     * @param myRoot root of the Draggable Menu
     */
    private static void updateTimeOnAnyActionHandler(AnchorPane myRoot){
        myRoot.addEventHandler(MouseEvent.ANY,
                event -> TIME_OF_LAST_INTERACTION = new Timestamp(System.currentTimeMillis()));

        myRoot.addEventHandler(TouchEvent.ANY,
                event -> TIME_OF_LAST_INTERACTION = new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Updates the TIME_OF_LAST_INTERACTION, thus stopping the Recognition Layer calls
     * as long as the user uses mouse which is a sign that the interaction is not done yet.
     *
     * @param myRoot root of the Canvas
     */
    private static void updateTimeOnAnyActionHandler(Group myRoot){
        myRoot.addEventHandler(MouseEvent.ANY,
                event -> TIME_OF_LAST_INTERACTION = new Timestamp(System.currentTimeMillis()));

        myRoot.addEventHandler(TouchEvent.ANY,
                event -> TIME_OF_LAST_INTERACTION = new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Sets the Display value of the canvas
     *  True = It cannot be unfocused
     *  False = It can be unfocused
     * @param booleanValue new boolean Value.
     */
    public static void setPrimaryStageAlwaysOnTop(boolean booleanValue){
        primaryStage.setAlwaysOnTop(booleanValue);
    }

    /**
     * This function updates the current color in the Canvas - for the strokes that are to be added
     */
    private static void updateCanvasColor(Color newColor){
        CANVAS_CURRENT_COLOR = newColor;
        graphicsContextGlobal.setStroke(CANVAS_CURRENT_COLOR);
    }

    /**
     * Function called by the Command/Annotation/Eraser buttons.
     * It will switch to the specified INK Type
     * @param s either "COMMAND", "ANNOTATION" or "ERASER"
     */
    private void switchToColor(String s){
        if(s.equals("COMMAND")) {
            CanvasLogic.eraserMode = false;
            updateCanvasColor(COMMAND_COLOR);
            TYPE_OF_INK_STATUS = "STATUS: Command INK";
        }
        else if(s.equals("ANNOTATION")){
            CanvasLogic.eraserMode = false;
            updateCanvasColor(ANNOTATION_COLOR);
            TYPE_OF_INK_STATUS = "STATUS: Annotation INK";
        }
        else{ // ERASER
            CanvasLogic.eraserMode = true;
            updateCanvasColor(ERASE_COLOR);
            TYPE_OF_INK_STATUS = "STATUS: ERASER";
        }

        typeOfCurrentInkText.setText(TYPE_OF_INK_STATUS);
        currentStatusText.setText(typeOfCurrentInkText.getText().replace("STATUS: ",""));
    }

    /**
     * Duplicates a stroke, so it will be visible on the canvas.
     * @param s The stroke that will be added.
     */
    public static void addStrokeToCanvas(Stroke s){
        graphicsContextGlobal.beginPath();
        graphicsContextGlobal.moveTo(s.getStartPoint().getX() + (double) 0, s.getStartPoint().getY() + (double) 0);

        /*
        // the Blurring effect - unused due to the fact that it makes the stoke not opaque anymore. Also, the thickness should have been dropped to half due to the the blurring distorsion
        double currentLineWidth = graphicsContextGlobal.getLineWidth();
        graphicsContextGlobal.setLineWidth(currentLineWidth/2);
        graphicsContextGlobal.setEffect(new BoxBlur());
        */

        // applying a filter (removing points that are really close) over the path
        Stroke sFiltered = new Stroke(s);
        ArrayList<Stroke> arrayListStrokes = new ArrayList<>();
        arrayListStrokes.add(sFiltered);
        sFiltered = Utils.removeConsecutiveClosePointsFilter(arrayListStrokes).get(0);

        // Between every 2 consecutive points, the "drawLineByUsingOvals" function adds equidistant points, and on every point, it draws a circle.
        // An alternative would have been the LineTo function, but the "Ovals" seem to smooth the path by a bit.
        for(int i = 1 ; i < sFiltered.getPathPoints().size() ; i++){
            drawLineByUsingOvals(new Point(  sFiltered.getPathPoints().get(i-1).getX(),
                            sFiltered.getPathPoints().get(i-1).getY()),
                    new Point(  sFiltered.getPathPoints().get(i).getX(),
                            sFiltered.getPathPoints().get(i).getY()));
            graphicsContextGlobal.stroke();
        }

        /*
        // Removing the Blurring effect
        graphicsContextGlobal.setEffect(null);
        graphicsContextGlobal.setLineWidth(currentLineWidth);
        */

        graphicsContextGlobal.closePath();
    }

    /**
     * Function used for visualization and debug.
     *
     * Similar to addStrokeToCanvas. The only change is that now the stroke is added with a specified Color
     *
     * @param s Stroke that is to be added
     * @param strokeColor Color that will be displayed
     */
    public static void addStrokeToCanvas(Stroke s, Color strokeColor){
        graphicsContextGlobal.setStroke(strokeColor);
        graphicsContextGlobal.beginPath();
        graphicsContextGlobal.moveTo(s.getStartPoint().getX() + (double) 0, s.getStartPoint().getY() + (double) 0);

        Stroke sFiltered = new Stroke(s);
        ArrayList<Stroke> arrayListStrokes = new ArrayList<>();
        arrayListStrokes.add(sFiltered);
        sFiltered = Utils.removeConsecutiveClosePointsFilter(arrayListStrokes).get(0);

        for(int i = 1 ; i < sFiltered.getPathPoints().size() ; i++){
            drawLineByUsingOvals(new Point(  sFiltered.getPathPoints().get(i-1).getX(),
                            sFiltered.getPathPoints().get(i-1).getY()),
                    new Point(  sFiltered.getPathPoints().get(i).getX(),
                            sFiltered.getPathPoints().get(i).getY()));
            graphicsContextGlobal.stroke();
        }

        graphicsContextGlobal.closePath();
        graphicsContextGlobal.setStroke(CANVAS_CURRENT_COLOR);
    }

    /**
     * Function used when drawing/redrawing
     *
     * This function will create an imaginary line between the points p1 and p2, by drawing consecutive "Circles/Ovals"
     * it will than find equidistant points on that line, found at a distance of x (radiusOfOval = 0.5)
     * and will make those points as Circles (or Ovals having the same width and height)
     *
     * @param p1 starting point
     * @param p2 end point
     */
    public static void drawLineByUsingOvals(Point p1, Point p2){
        // defining the radius
        double radiusOfOval = 0.5;
        // make the first point an oval
        graphicsContextGlobal.strokeOval(p1.getX(), p1.getY(), radiusOfOval*1.5, radiusOfOval*1.5);

        // compute the distance between p1 and p2
        double distanceBetweenPoints = Utils.getDistanceBetweenTwoPoints(p1,p2);
        // compute the number of points that will be added to that line: distance / radius of oval
        int numPoints = (int)Math.floor(distanceBetweenPoints/radiusOfOval);

        // height and width sizes
        double  widthP1P2 = p2.getX()-p1.getX(),
                heightP1P2 = p2.getY()-p1.getY();

        // if there is a need to adding points:
        if(numPoints > 0) {
            // for every point that will be added
            for (int i = 1; i <= numPoints; i++) {

                // compute the corresponding coordinates, and add the oval
                double currentX = p1.getX() + (double)((double)i / (double)numPoints) * widthP1P2,
                        currentY = p1.getY() + (double)((double)i / (double)numPoints) * heightP1P2;
                Point p = new Point(currentX, currentY);
                graphicsContextGlobal.strokeOval(p.getX(), p.getY(), radiusOfOval*1.5, radiusOfOval*1.5);
            }
        }

        // add an oval over end point also
        graphicsContextGlobal.strokeOval(p2.getX(), p2.getY(), radiusOfOval*1.5, radiusOfOval*1.5);
    }

    /**
     * Function used mainly to display the tip, or to show the bounding box
     *
     * Draw a box based on given parameters
     *
     * @param topLeftCorner root of the box
     * @param width width of the box
     * @param height height of the box
     */
    public static void drawBox(Point topLeftCorner, double width, double height, Color newColor){
        setLineThickness(MIN_THICKNESS);
        graphicsContextGlobal.setStroke(newColor);
        graphicsContextGlobal.strokeRect(topLeftCorner.getX(), topLeftCorner.getY(), width, height);
        graphicsContextGlobal.setStroke(CANVAS_CURRENT_COLOR);
        setLineThickness(GLOBAL_LINE_THICKNESS);
    }

    /**
     * Adds TEXT on canvas, having the root on a specified point.
     *
     * @param topLeftCorner Root where the Text will be drawn
     * @param text The text that will be added
     * @param maxWidth maximum width of the text that is to be added
     */
    public static void drawText(Point topLeftCorner, String text, double maxWidth){
        setLineThickness(MIN_THICKNESS);

        graphicsContextGlobal.setStroke(Color.DARKORANGE);
        graphicsContextGlobal.strokeText(text, topLeftCorner.getX() ,topLeftCorner.getY()+textFontSize,maxWidth);
        graphicsContextGlobal.setStroke(CANVAS_CURRENT_COLOR);

        setLineThickness(GLOBAL_LINE_THICKNESS);
    }

    /**
     * Adds a section on Canvas which has a text and two boxes representing both a YES and NO area
     * Will be use to check with the user if the 'text' is what was intended by the previous drawings
     * @param topLeftCorner Root of this element
     * @param width width of element
     * @param height height of element
     * @param textFromInterpretationLayer text that is displayed as a label
     */
    public static void addTextWithTwoOptions(Point topLeftCorner, double width, double height, String textFromInterpretationLayer){

        if(!CanvasLogic.interpretationLayerRequestResponseFLAG) {
            CanvasLogic.instantiateTextWithTwoOptions(topLeftCorner, width, height, textFromInterpretationLayer);
        }
        if(CanvasLogic.textWithTwoOptionsIsDisplayed) {
            drawText(new Point(topLeftCorner.getX(), topLeftCorner.getY()), textFromInterpretationLayer, width);

            // Yes
            drawText(new Point(topLeftCorner.getX() + 1, topLeftCorner.getY() + height / 2), "Yes", width / 2);
            drawBox(new Point(topLeftCorner.getX(), topLeftCorner.getY() + height / 2), width / 2 - 2, height / 2, Color.ORANGE);

            // No
            drawText(new Point(topLeftCorner.getX() + width / 2 + 5, topLeftCorner.getY() + height / 2), "No", width / 2);
            drawBox(new Point(topLeftCorner.getX() + width / 2 + 2, topLeftCorner.getY() + height / 2), width / 2 - 2, height / 2, Color.ORANGE);
        }

    }

    /**
     * Function that cleans everything from canvas and immediately adds the stored strokes.
     * Used as a 'force refresh' by the canvas after removing a stroke, or after the recognition started
     */
    public static void reDraw(){
        graphicsContextGlobal.clearRect(0,0, graphicsContextGlobal.getCanvas().getWidth(), graphicsContextGlobal.getCanvas().getHeight());
        Color strokeColor;
        // draw the Command Strokes
        for(int i = 0; i < singletonStorage.getCommandStrokes().size() ; i++) {
            Stroke s = singletonStorage.getCommandStrokes().get(i);
            strokeColor = s.getColor();
            graphicsContextGlobal.setStroke(strokeColor);
            setLineThickness(s.getLineThickness());
            addStrokeToCanvas(s);
        }

        // draw the Annotation Strokes
        for(int i = 0; i < singletonStorage.getAnnotationStrokes().size() ; i++) {
            Stroke s = singletonStorage.getAnnotationStrokes().get(i);
            strokeColor = s.getColor();
            graphicsContextGlobal.setStroke(strokeColor);
            setLineThickness(s.getLineThickness());
            addStrokeToCanvas(s);
        }

        graphicsContextGlobal.setStroke(CANVAS_CURRENT_COLOR);
        setLineThickness(GLOBAL_LINE_THICKNESS);
        if(CanvasLogic.interpretationLayerRequestResponseFLAG){
            addTextWithTwoOptions(CanvasLogic.topLeftCornerOfFLAG, CanvasLogic.widthOfFLAG, CanvasLogic.heightOfFLAG, CanvasLogic.textOfFLAG);
        }
    }

    /** Removes all the ink from the storage, and so, from the canvas. */
    public static void removeAllInk(){
        // resets the history of the shapes which were sent to VS Code
        CanvasLogic.resetHistory();

        singletonStorage.removeAllStrokesFromCanvas();
        graphicsContextGlobal.clearRect(0,0, graphicsContextGlobal.getCanvas().getWidth(), graphicsContextGlobal.getCanvas().getHeight());

        if(CanvasLogic.interpretationLayerRequestResponseFLAG){
            addTextWithTwoOptions(CanvasLogic.topLeftCornerOfFLAG, CanvasLogic.widthOfFLAG, CanvasLogic.heightOfFLAG, CanvasLogic.textOfFLAG);
        }
        reDraw();
    }

    /** Removes all the Annotation Strokes from canvas and from the storage. */
    private void removeAllAnnotationStrokes(){
        singletonStorage.removeAllAnnotationStrokes();
        reDraw();
    }

    /**
     * Removes all the Command Strokes from the canvas, and from the storage.
     */
    public static void removeAllCommandStrokes(){
        singletonStorage.removeAllCommandStrokes();
        reDraw();
    }

    /** Undo function - removes the last drawn Command stroke. */
    private static void undoCommandStroke(){
        if(singletonStorage.getCommandStrokes().size() > 0) {
            singletonStorage.removeCommandStrokeAt(singletonStorage.getCommandStrokes().size() - 1);
        }
        reDraw();
    }

    /** Undo function - removes the last drawn Annotation stroke. */
    private static void undoAnnotationStroke(){
        if(singletonStorage.getAnnotationStrokes().size() > 0){
            singletonStorage.removeAnnotationStrokeAt(singletonStorage.getAnnotationStrokes().size()-1);
        }
        reDraw();
    }

    /**
     * Function called by the UNDO Button.
     */
    public static void callUndo(){
        if(singletonStorage.getCommandStrokes().size() > 0 && singletonStorage.getAnnotationStrokes().size() > 0) {
            if (singletonStorage.getCommandStrokes().get(singletonStorage.getCommandStrokes().size() - 1).getTimeStopDrawing().getTime() > singletonStorage.getAnnotationStrokes().get(singletonStorage.getAnnotationStrokes().size() - 1).getTimeStopDrawing().getTime()) {
                undoCommandStroke();
            }
            else{
                undoAnnotationStroke();
            }
        }
        else if(singletonStorage.getCommandStrokes().size() == 0 && singletonStorage.getAnnotationStrokes().size()>0){
            undoAnnotationStroke();
        }
        else if(singletonStorage.getAnnotationStrokes().size() == 0 && singletonStorage.getCommandStrokes().size()>0){
            undoCommandStroke();
        }

        CanvasLogic.addButtonLogToDB("Undo");
    }

    /**
     * Set up the Graphics context
     */
    private void initDraw(){
        graphicsContextGlobal.setStroke(COMMAND_COLOR);
        graphicsContextGlobal.setLineWidth(THICKNESS_DEFAULT);
        GLOBAL_LINE_THICKNESS = graphicsContextGlobal.getLineWidth();
        lineThicknessModifierText.setText("Stroke Thickness: "+GLOBAL_LINE_THICKNESS);
        graphicsContextGlobal.setFill(Color.DARKGRAY);

        graphicsContextGlobal.setFont(Font.font("Verdana", textFontSize));
    }

    /**
     * Increase the thickness of strokes that will be added
     */
    private void increaseLineThickness(){
        if(graphicsContextGlobal.getLineWidth() < MAX_THICKNESS){
            graphicsContextGlobal.setLineWidth(graphicsContextGlobal.getLineWidth() + THICKNESS_STEP);
            GLOBAL_LINE_THICKNESS = graphicsContextGlobal.getLineWidth();
            lineThicknessModifierText.setText("Stroke Thickness: "+GLOBAL_LINE_THICKNESS);
        }
    }

    /**
     * Decrease the thickness of strokes that will be added
     */
    public void decreaseLineThickness(){
        if(graphicsContextGlobal.getLineWidth() > MIN_THICKNESS){
            graphicsContextGlobal.setLineWidth(graphicsContextGlobal.getLineWidth() - THICKNESS_STEP);
            GLOBAL_LINE_THICKNESS = graphicsContextGlobal.getLineWidth();
            lineThicknessModifierText.setText("Stroke Thickness: "+GLOBAL_LINE_THICKNESS);
        }
    }

    /**
     * Set the thickness of the lines to a certain value
     * @param newValue given value.
     */
    public static void setLineThickness(double newValue){
        if(newValue >= MIN_THICKNESS && newValue <= MAX_THICKNESS)
            graphicsContextGlobal.setLineWidth(newValue);
        else if(newValue>MAX_THICKNESS) {
            graphicsContextGlobal.setLineWidth(MAX_THICKNESS);
        }
        else {
            graphicsContextGlobal.setLineWidth(MIN_THICKNESS);
        }
    }

    public static void displayHelpMenu(){
        helpRoot.setVisible(true);
    }

    public static void hideHelpMenu(){
        helpRoot.setVisible(false);
    }

    public static void displayVisualizationMenu(){
        visualizeTemplatesRoot.setVisible(true);
    }

    public static void hideVisualizationMenu(){
        visualizeTemplatesRoot.setVisible(false);
    }
    /**
     * Constructor
     * @param startingStage the Stage
     */
    public CanvasUI(Stage startingStage){

        directoryChooser.setInitialDirectory(new File(logDataDB.saveLocationPath));
        // set the primary stage
        primaryStage = startingStage;

        /* Set the default colo of the canvas, which is the Regular Color (= Shape Color)*/
        CANVAS_CURRENT_COLOR = COMMAND_COLOR;

        /* Size of canvas is set */
        canvas = new Canvas(size.width - 3, size.height);

        /* Instantiate the context. It helps us with the drawings. */
        graphicsContextGlobal = canvas.getGraphicsContext2D();

        /* set up the Graphics Context */
        initDraw();

        /* positioning of the primary stage */
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setHeight(canvas.getHeight());
        primaryStage.setWidth(canvas.getWidth());

        /* get the list of names for the photos in the resource/help folder */
        indexOfHelpImage = 0;

        helpImageNames = Utils.getListOfFileNamesFromGivenFolder(helpDirectoryPath);

        /* slightly gray background */
        collapsableMenuRoot.setStyle(COLLAPSABLE_MENU_ROOT_STYLE_NOT_COLLAPSED);

        helpRoot.setStyle(HELP_ROOT_STYLE_DISPLAYED);

        visualizeTemplatesRoot.setStyle(HELP_ROOT_STYLE_DISPLAYED);

        /* Draggable and collapsable menu - variables */
        final double[] xCollapsableMenuOffset = {0};
        final double[] yCollapsableMenuOffset = {0};

        /* Draggable functionality #1 - get the current coordinates */
        collapsableMenuRoot.setOnMousePressed(event -> {
            xCollapsableMenuOffset[0] = collapsableMenuRoot.getTranslateX() - event.getScreenX();
            yCollapsableMenuOffset[0] = collapsableMenuRoot.getTranslateY() - event.getScreenY();
        });

        /* Draggable functionality #2 - actual modify the position - draggable */
        collapsableMenuRoot.setOnMouseDragged(event -> {
            collapsableMenuRoot.setTranslateX(event.getScreenX() + xCollapsableMenuOffset[0]);
            collapsableMenuRoot.setTranslateY(event.getScreenY() + yCollapsableMenuOffset[0]);
        });

        final double[] xHelpRootOffset = {0};
        final double[] yHelpRootOffset = {0};

        helpRoot.setOnMousePressed(event -> {
            xHelpRootOffset[0] = helpRoot.getTranslateX() - event.getScreenX();
            yHelpRootOffset[0] = helpRoot.getTranslateY() - event.getScreenY();
        });

        helpRoot.setOnMouseDragged(event -> {
            helpRoot.setTranslateX(event.getScreenX() + xHelpRootOffset[0]);
            helpRoot.setTranslateY(event.getScreenY() + yHelpRootOffset[0]);
        });

        final double[] xVisualizeTemplatesRootOffset = {0};
        final double[] yVisualizeTemplatesRootOffset = {0};

        visualizeTemplatesRoot.setOnMousePressed(event -> {
            xVisualizeTemplatesRootOffset[0] = visualizeTemplatesRoot.getTranslateX() - event.getScreenX();
            yVisualizeTemplatesRootOffset[0] = visualizeTemplatesRoot.getTranslateY() - event.getScreenY();
        });

        visualizeTemplatesRoot.setOnMouseDragged(event -> {
            visualizeTemplatesRoot.setTranslateX(event.getScreenX() + xVisualizeTemplatesRootOffset[0]);
            visualizeTemplatesRoot.setTranslateY(event.getScreenY() + yVisualizeTemplatesRootOffset[0]);
        });



        /* functions which updates a variable that keeps the last 'interaction' with the canvas - either physical interaction (actual click/touch) or non-physical (drag the mouse over the canvas) */
        updateTimeOnAnyActionHandler(canvasRoot);
        updateTimeOnAnyActionHandler(collapsableMenuRoot);
        updateTimeOnAnyActionHandler(helpRoot);
        updateTimeOnAnyActionHandler(visualizeTemplatesRoot);

        /* Set the size of the HELP MENU  */
        helpRoot.setPrefHeight(HELP_MENU_HEIGHT);
        helpRoot.setPrefWidth(HELP_MENU_WIDTH);

        visualizeTemplatesRoot.setPrefHeight(HELP_MENU_HEIGHT);
        visualizeTemplatesRoot.setPrefWidth(HELP_MENU_WIDTH);

        /* Collapse the Menu */
        collapseTheCollapsableMenuButton.setStyle(collapseTheCollapsableMenuButtonOriginalStyle);

        currentStatusText.setStyle("" +
                "-fx-font-size: 16px;" +
                "-fx-fill: LIGHTGOLDENRODYELLOW;"+
                "-fx-stroke: black;"+
                "-fx-stroke-width: 0.5px;");

        svgDraggableIcon.setContent(SVGDraggablePath);

        svgDraggableIcon.setStyle(  "-fx-stroke:  rgba(0, 0, 0, 0.6);" +
                                    "-fx-fill: rgba(255,255,255, 0.6);" +
                                            "-fx-translate-x: 5");

        topBarOfCollapsableMenuHBox.getChildren().addAll(collapseTheCollapsableMenuButton, svgDraggableIcon, currentStatusText);

        topBarOfCollapsableMenuHBox.setAlignment(Pos.CENTER);

        topBarSectionOnCollapsableMenuHBox.getChildren().addAll(topBarOfCollapsableMenuHBox);

        /* Memorize if weather the menu is collapsed or not */
        final boolean[] collapsableMenuIsCollapsed = {false};

        /* Collapse/Extend the menu on button press */
        collapseTheCollapsableMenuButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                collapseTheCollapsableMenuButton.setStyle(collapseTheCollapsableMenuButtonOriginalStyle);
            });
            collapseTheCollapsableMenuButton.setStyle("-fx-background-color: rgba(168, 168, 168, 0.6);"+
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;" +
                    "-fx-min-width: 25px;");

            if(!collapsableMenuIsCollapsed[0]){
                collapsableMenuIsCollapsed[0] = true;
                collapseTheCollapsableMenuButton.setText("+");

                collapsableMenuRoot.setMaxHeight(COLLAPSABLE_MENU_COLLAPSED_HEIGHT);
                collapsableMenuRoot.setPrefHeight(COLLAPSABLE_MENU_COLLAPSED_HEIGHT);
                collapsableMenuRoot.setMinHeight(COLLAPSABLE_MENU_COLLAPSED_HEIGHT);
                collapsableMenuRoot.setPrefWidth(COLLAPSABLE_MENU_COLLAPSED_WIDTH);
                collapsableMenuRoot.setStyle(COLLAPSABLE_MENU_ROOT_STYLE_COLLAPSED);
            }
            else{
                collapsableMenuIsCollapsed[0] = false;
                collapseTheCollapsableMenuButton.setText("-");

                collapsableMenuRoot.setMaxHeight(COLLAPSABLE_MENU_FULL_HEIGHT);
                collapsableMenuRoot.setPrefHeight(COLLAPSABLE_MENU_FULL_HEIGHT);
                collapsableMenuRoot.setMinHeight(COLLAPSABLE_MENU_FULL_HEIGHT);
                collapsableMenuRoot.setPrefWidth(COLLAPSABLE_MENU_FULL_WIDTH);
                collapsableMenuRoot.setStyle(COLLAPSABLE_MENU_ROOT_STYLE_NOT_COLLAPSED);
            }

            vBoxInkChangeTypeSectionInCollapsableMenu.setVisible(!vBoxInkChangeTypeSectionInCollapsableMenu.isVisible());
            vBoxLineThicknessModifierSectionOnCollapsableMenu.setVisible(!vBoxLineThicknessModifierSectionOnCollapsableMenu.isVisible());
            vBoxFunctionalitySectionInCollapsableMenu.setVisible(!vBoxFunctionalitySectionInCollapsableMenu.isVisible());
            vBoxExtra.setVisible(!vBoxExtra.isVisible());
            transitionForButtons.playFromStart();

            if(displaySecondaryParticipantInfoMenu) {
                vBoxParticipant.setVisible(vBoxInkChangeTypeSectionInCollapsableMenu.isVisible());
            }
            else{
                vBoxParticipant.setVisible(false);
            }

            if (displaySecondaryTemplateInfoMenu) {
                vBoxTemplates.setVisible(vBoxInkChangeTypeSectionInCollapsableMenu.isVisible());
            }
            else{
                vBoxTemplates.setVisible(false);
            }

            CanvasLogic.addButtonLogToDB("Collapse");
        });

        toggleSecondaryParticipantInfoMenuButton.setOnAction(e -> {
            displaySecondaryParticipantInfoMenu = !displaySecondaryParticipantInfoMenu;
            if(!displaySecondaryParticipantInfoMenu){
                vBoxParticipant.setVisible(false);
            }
            else{
                vBoxParticipant.setVisible(vBoxInkChangeTypeSectionInCollapsableMenu.isVisible());
            }

            if(vBoxParticipant.isVisible()){
                toggleSecondaryParticipantInfoMenuButton.setText("Hide Participant Menu");
            }
            else{
                toggleSecondaryParticipantInfoMenuButton.setText("Display Participant Menu");
            }
        });

        toggleSecondaryTemplateInfoMenuButton.setOnAction(e -> {
            displaySecondaryTemplateInfoMenu = !displaySecondaryTemplateInfoMenu;
            if(!displaySecondaryTemplateInfoMenu){
                vBoxTemplates.setVisible(false);
            }
            else{
                vBoxTemplates.setVisible(vBoxInkChangeTypeSectionInCollapsableMenu.isVisible());
            }

            if(vBoxTemplates.isVisible()){
                toggleSecondaryTemplateInfoMenuButton.setText("Hide Template Menu");
            }
            else{
                toggleSecondaryTemplateInfoMenuButton.setText("Display Template Menu");
            }
        });

        previousHintHelpMenuButton.setOnAction(e -> {
            indexOfHelpImage = (indexOfHelpImage - 1);
            if(indexOfHelpImage < 0){
                indexOfHelpImage =  helpImageNames.length - 1;
            }
            helpImage = new Image(preFileExtension + helpDirectoryPath + helpImageNames[indexOfHelpImage], HELP_IMAGE_WIDTH, HELP_IMAGE_HEIGHT,true,true);
            helpImageView.setImage(helpImage);
            helpImageView.setPreserveRatio(true);
        });

        previousImageVisualizationMenuButton.setOnAction(e -> {
            indexOfVisualizationImage = (indexOfVisualizationImage - 1);
            if(indexOfVisualizationImage < 0){
                indexOfVisualizationImage =  templatesAsImages.size() - 1;
            }
            visualizationImage = Utils.convertToFxImage(templatesAsImages.get(indexOfVisualizationImage));
            visualizationImageView.setImage(visualizationImage);
            visualizationImageView.setPreserveRatio(true);
        });

        nextHintHelpMenuButton.setOnAction(e -> {
            indexOfHelpImage = (indexOfHelpImage + 1) % helpImageNames.length;
            helpImage = new Image(preFileExtension + helpDirectoryPath + helpImageNames[indexOfHelpImage], HELP_IMAGE_WIDTH, HELP_IMAGE_HEIGHT,true,true);
            helpImageView.setImage(helpImage);
            helpImageView.setPreserveRatio(true);
        });

        nextImageVisualizationMenuButton.setOnAction(e -> {
            indexOfVisualizationImage = (indexOfVisualizationImage + 1) % templatesAsImages.size();

            visualizationImage = Utils.convertToFxImage(templatesAsImages.get(indexOfVisualizationImage));
            visualizationImageView.setImage(visualizationImage);
            visualizationImageView.setPreserveRatio(true);
        });

        vBoxParticipant.setStyle("-fx-background-color: rgba(232, 232, 232, 0.5);"+
                "-fx-min-height: "+COLLAPSABLE_MENU_FULL_HEIGHT+";");

        vBoxTemplates.setStyle("-fx-background-color: rgba(232, 232, 232, 0.5);"+
                "-fx-min-height: "+COLLAPSABLE_MENU_FULL_HEIGHT+";");

        inkTypeTextInCollapsableMenu.setStyle("" +
                "-fx-font-size: 14px;" +
                "-fx-fill: LIGHTGOLDENRODYELLOW;"+
                "-fx-stroke: black;"+
                "-fx-stroke-width: 0.35px;");

        regularInkInCollapsableMenuButton.setStyle(regularInkInCollapsableMenuButtonOriginalStyle);

        regularInkInCollapsableMenuButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                regularInkInCollapsableMenuButton.setStyle(regularInkInCollapsableMenuButtonOriginalStyle);
            });
            regularInkInCollapsableMenuButton.setStyle("-fx-background-color: rgba(40, 92, 119, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            switchToColor("COMMAND");

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Switch to: Command Ink");
            });

        extraInkInCollapsableMenuButton.setStyle(extraInkInCollapsableMenuButtonOriginalStyle);
        extraInkInCollapsableMenuButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                extraInkInCollapsableMenuButton.setStyle(extraInkInCollapsableMenuButtonOriginalStyle);
            });
            extraInkInCollapsableMenuButton.setStyle("-fx-background-color: rgba(160, 18, 13, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            switchToColor("ANNOTATION");

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Switch to: Annotation Ink");
        });

        eraserModeInCollapsableMenuButton.setStyle(eraserModeInCollapsableMenuButtonOriginalStyle);
        eraserModeInCollapsableMenuButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                eraserModeInCollapsableMenuButton.setStyle(eraserModeInCollapsableMenuButtonOriginalStyle);
            });
            eraserModeInCollapsableMenuButton.setStyle("-fx-background-color: rgba(189, 123, 0, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            switchToColor("ERASER");

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Switch to: Eraser Ink");
        });

        addNewSymbolButton.setOnAction(e -> {
            // add the symbol to the TemplateLocalStorage
            if(TemplateLocalStorage.getInstance().addNewSymbolToList(newSymbolName.getText())) {
                // also add the symbol to the dropdown list, if it was added successfully to the template storage
                comboBoxUserIntendedToDrawTemplateFor.getItems().add(newSymbolName.getText());
            }
        });

        colorPicker.setValue(ANNOTATION_COLOR);
        colorPicker.setStyle(   "-fx-background-color: rgba(255,255,255, 0.01);"+
                                "-fx-max-width: 40; ");

        vBoxInkChangeTypeSectionInCollapsableMenu.getChildren().addAll(inkTypeTextInCollapsableMenu, regularInkInCollapsableMenuButton, extraInkInCollapsableMenuButton, colorPicker, eraserModeInCollapsableMenuButton);
        vBoxInkChangeTypeSectionInCollapsableMenu.setAlignment(Pos.CENTER);

        lineThicknessModifierText.setStyle(inkTypeTextInCollapsableMenu.getStyle());

        decreaseLineThicknessButton.setStyle(decreaseLineThicknessButtonOriginalStyle);
        decreaseLineThicknessButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                decreaseLineThicknessButton.setStyle(decreaseLineThicknessButtonOriginalStyle);
            });
            decreaseLineThicknessButton.setStyle("-fx-background-color: rgba(139, 107, 199, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            decreaseLineThickness();

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Decrease Thickness");
        });

        increaseLineThicknessButton.setStyle(increaseLineThicknessButtonOriginalStyle);
        increaseLineThicknessButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                increaseLineThicknessButton.setStyle(increaseLineThicknessButtonOriginalStyle);
            });
            increaseLineThicknessButton.setStyle("-fx-background-color: rgba(118, 0, 168, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            increaseLineThickness();

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Increase Thickness");
        });

        lineThicknessButtonsSectionHBox.getChildren().addAll(decreaseLineThicknessButton, increaseLineThicknessButton);
        vBoxLineThicknessModifierSectionOnCollapsableMenu.getChildren().addAll(lineThicknessModifierText, lineThicknessButtonsSectionHBox);
        vBoxLineThicknessModifierSectionOnCollapsableMenu.setAlignment(Pos.CENTER);


        functionalityTextInCollapsableMenu.setStyle(inkTypeTextInCollapsableMenu.getStyle());

        removeExtraInkButtonInCollapsableMenu.setStyle(removeExtraInkButtonInCollapsableMenuOriginalStyle);
        removeExtraInkButtonInCollapsableMenu.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                removeExtraInkButtonInCollapsableMenu.setStyle(removeExtraInkButtonInCollapsableMenuOriginalStyle);
            });
            removeExtraInkButtonInCollapsableMenu.setStyle("-fx-background-color: rgba(40, 92, 119, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            removeAllAnnotationStrokes();

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Remove all RED INK");
        });

        removeAllInkButtonInCollapsableMenu.setStyle(removeAllInkButtonInCollapsableMenuOriginalStyle);

        removeAllInkButtonInCollapsableMenu.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            CanvasLogic.recognizerPoked = false;
            transitionForButtons.setOnFinished(event -> {
                removeAllInkButtonInCollapsableMenu.setStyle(removeAllInkButtonInCollapsableMenuOriginalStyle);
            });
            removeAllInkButtonInCollapsableMenu.setStyle("-fx-background-color: rgba(40, 92, 119, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            removeAllInk();

            transitionForButtons.playFromStart();

            // This part will send a signal to VS Code, saying that the ink was removed.
            try {
                Client.sendSignalToExtension("Remove All INK Button Pressed");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            CanvasLogic.addButtonLogToDB("Remove all INK");
        });

        undoButtonInCollapsableMenu.setStyle(undoButtonOriginalStyle);


        undoButtonInCollapsableMenu.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                undoButtonInCollapsableMenu.setStyle(undoButtonOriginalStyle);
            });

            undoButtonInCollapsableMenu.setStyle("-fx-background-color: rgba(40, 92, 119, 0.6);" +
                                                "-fx-alignment: center;" +
                                                "-fx-text-fill: white;");

            callUndo();

            transitionForButtons.playFromStart();
        });


        vBoxFunctionalitySectionInCollapsableMenu.getChildren().addAll(functionalityTextInCollapsableMenu, removeExtraInkButtonInCollapsableMenu, removeAllInkButtonInCollapsableMenu, undoButtonInCollapsableMenu);
        vBoxFunctionalitySectionInCollapsableMenu.setAlignment(Pos.CENTER);

        closeHelpMenuButton.setStyle("-fx-background-color:  rgba(255, 69, 0, 0.6);" +
                                    "-fx-alignment: center;" +
                                    "-fx-text-fill: aliceblue;");

        closeVisualizationMenuButton.setStyle(closeHelpMenuButton.getStyle());

        previousHintHelpMenuButton.setStyle("-fx-background-color: rgba(177, 156, 217, 0.6);"+
                                            "-fx-alignment: center;" +
                                            "-fx-text-fill: white;");

        nextHintHelpMenuButton.setStyle("-fx-background-color: rgba(148, 0, 211, 0.6);"+
                                        "-fx-alignment: center;" +
                                        "-fx-text-fill: white;");

        closeHelpMenuButton.setOnAction(e -> {
            hideHelpMenu();
        });

        closeVisualizationMenuButton.setOnAction(e -> {
            hideVisualizationMenu();
        });

        testButton.setStyle(testButtonOriginalStyle);
        testButton.setOnAction(e -> {
            CanvasLogic.focusOnIDE();
            transitionForButtons.setOnFinished(event -> {
                testButton.setStyle(testButtonOriginalStyle);
            });

            testButton.setStyle("-fx-background-color: rgba(211, 66, 13, 0.6);" +
                    "-fx-alignment: center;" +
                    "-fx-text-fill: white;");

            addTextWithTwoOptions(new Point(550,150),250,200,"Are you sure about this?");

            transitionForButtons.playFromStart();

            CanvasLogic.addButtonLogToDB("Yes/No Test");
        });

        forcePokingButton.setStyle(forcePokingButtonOriginalStyle);
        restartCanvasButton.setStyle(restartCanvasButtonOriginalStyle);
        helpButton.setStyle(helpButtonOriginalStyle);
        toggleSecondaryParticipantInfoMenuButton.setStyle(toggleSecondaryMenuOriginalStyle);
        toggleSecondaryTemplateInfoMenuButton.setStyle(toggleSecondaryMenuOriginalStyle);

        vBoxExtra.getChildren().addAll(testButton, restartCanvasButton, helpButton, forcePokingButton, toggleSecondaryParticipantInfoMenuButton, toggleSecondaryTemplateInfoMenuButton);
        vBoxExtra.setAlignment(Pos.CENTER);


        // TODO: set to FALSE to hide it; or TRUE for displaying it
        testButton.setVisible(false);

        var validTemplateList = Utils.getListOfValidTemplates();
        comboBoxUserIntendedToDrawSymbol.getItems().addAll(validTemplateList);
        comboBoxUserIntendedToDrawSymbol.setValue(validTemplateList.get(0));

        radioButtonsVBox.getChildren().addAll(clearCanvasText, dataCollectionModeCheckbox);

        radioButtonsHandVBox.getChildren().addAll(radioButtonHand1, radioButtonHand2);
        radioButtonHand1.setToggleGroup(toggleGroupHand);
        radioButtonHand2.setToggleGroup(toggleGroupHand);

        radioButtonsTypeVBox.getChildren().addAll(radioButtonType1, radioButtonType2);
        radioButtonType1.setToggleGroup(toggleGroupType);
        radioButtonType2.setToggleGroup(toggleGroupType);

        vBoxParticipant.getChildren().addAll(participantTitleText, userText, textFieldUserName, radioButtonsVBox, radioButtonsHandVBox, radioButtonsTypeVBox, userIntendedToDrawSymbolText, comboBoxUserIntendedToDrawSymbol, userChooseNewPath);
        vBoxParticipant.setVisible(false);
        vBoxParticipant.setAlignment(Pos.TOP_CENTER);

        comboBoxUserIntendedToDrawTemplateFor.getItems().addAll(validTemplateList);
        comboBoxUserIntendedToDrawTemplateFor.setValue(validTemplateList.get(0));

        var listOfExcelFilesContainingTemplates = Utils.getListOfFileNamesFromGivenFolder(Utils.templateFolderPath);
        comboBoxTemplateFileNames.getItems().addAll(listOfExcelFilesContainingTemplates);
        comboBoxTemplateFileNames.setValue(listOfExcelFilesContainingTemplates[0]);
        comboBoxTemplateFileNames.setMaxWidth(photoSectionOnHelpDisplayHBox.getWidth());



        vBoxTemplates.setAlignment(Pos.TOP_CENTER);

        vBoxTemplates.getChildren().addAll(templateTitleText, userIntendsToAddTemplateForText, comboBoxUserIntendedToDrawTemplateFor, addTemplateButton, userAddsNewSymbol, newSymbolName, addNewSymbolButton, visualizeTemplateTitleText, visualizeTemplateText, comboBoxTemplateFileNames, visualizeTemplatesButton);
        vBoxTemplates.setVisible(false);

        // add all elements to the Menu
        collapsableMenuRoot.getChildren().addAll(topBarSectionOnCollapsableMenuHBox, vBoxInkChangeTypeSectionInCollapsableMenu, vBoxLineThicknessModifierSectionOnCollapsableMenu, vBoxFunctionalitySectionInCollapsableMenu, vBoxExtra, vBoxParticipant, vBoxTemplates);
        collapsableMenuRoot.setPrefHeight(COLLAPSABLE_MENU_FULL_HEIGHT);
        collapsableMenuRoot.setPrefWidth(COLLAPSABLE_MENU_FULL_WIDTH);

        topBarSectionOnHelpDisplayHBox.getChildren().addAll(closeHelpMenuButton);
        topBarSectionOnHelpDisplayHBox.setAlignment(Pos.CENTER);

        topBarSectionOnVisualizationOfTemplatesDisplayHBox.getChildren().addAll(closeVisualizationMenuButton);
        topBarSectionOnVisualizationOfTemplatesDisplayHBox.setAlignment(Pos.CENTER);

        buttonSectionOnHelpDisplayHBox.getChildren().addAll(previousHintHelpMenuButton, nextHintHelpMenuButton);
        buttonSectionOnHelpDisplayHBox.setAlignment(Pos.CENTER);

        buttonSectionOnVisualizationOfTemplatesDisplayHBox.getChildren().addAll(previousImageVisualizationMenuButton, nextImageVisualizationMenuButton);
        buttonSectionOnVisualizationOfTemplatesDisplayHBox.setAlignment(Pos.CENTER);

        // topBarSectionOnHelpDisplayHBox.getHeight() and buttonSectionOnHelpDisplayHBox.getHeight() both have 25
        helpImage = new Image(preFileExtension + helpDirectoryPath + helpImageNames[indexOfHelpImage], HELP_IMAGE_WIDTH, HELP_IMAGE_HEIGHT, true,true);
        helpImageView.setImage(helpImage);
        helpImageView.setPreserveRatio(true);
        photoSectionOnHelpDisplayHBox.getChildren().addAll(helpImageView);
        photoSectionOnHelpDisplayHBox.setAlignment(Pos.CENTER);

        helpRoot.getChildren().addAll(topBarSectionOnHelpDisplayHBox, photoSectionOnHelpDisplayHBox, buttonSectionOnHelpDisplayHBox);
        hideHelpMenu();

        visualizationImage = new Image(preFileExtension + helpDirectoryPath + helpImageNames[indexOfHelpImage], HELP_IMAGE_WIDTH, HELP_IMAGE_HEIGHT,true,true);
        visualizationImageView.setImage(visualizationImage);
        visualizationImageView.setPreserveRatio(true);
        photoSectionOnVisualizationOfTemplatesDisplayHBox.getChildren().addAll(visualizationImageView);
        photoSectionOnVisualizationOfTemplatesDisplayHBox.setAlignment(Pos.CENTER);

        visualizeTemplatesRoot.getChildren().addAll(topBarSectionOnVisualizationOfTemplatesDisplayHBox, photoSectionOnVisualizationOfTemplatesDisplayHBox, buttonSectionOnVisualizationOfTemplatesDisplayHBox);
        hideVisualizationMenu();

        /* Handles for the interactions with the Canvas: Touch and Mouse interactions - done in the CanvasLogic class */

        /* Handler for the SCROLL event - replicates it over the IDE */
        canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
            // if the last event has the root in a Mouse Event
            if(CanvasLogic.typeOfLastInteractionBeforeScroll == 1) {
                CanvasLogic.robotScroll(event.getX(), event.getY(), event.getDeltaY(),1);
            }
        });

        buttonClose.setStyle("" +
                "-fx-background-color:  rgba(255, 69, 0, 0.6);" +
                "-fx-alignment: center;" +
                "-fx-text-fill: aliceblue;");

        buttonClose.setOnAction(e -> {

            if(!CanvasLogic.IDEClosedPrematurely) {
                primaryStage.hide();
                primaryStage.close();
            }

            CanvasLogic.addButtonLogToDB("Close");

            // Save the templates as an Excel file
            try {
                TemplateLocalStorage.getInstance().saveAsExcel();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            // Send a signal to VS Code, saying that the extension was closed.
            if(!CanvasLogic.IDEClosedPrematurely) {
                // If the IDE closed normally, send the Closing Signal to VS Code
                try {
                    Client.sendSignalToExtension("Closing Button Pressed");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            // Closing the App
            System.exit(0);
        });

        visualizeTemplatesButton.setOnAction(e -> {
            String templateToBeVisualized = comboBoxTemplateFileNames.getValue().toString();

            var templatesToBeVisualized = Utils.getAllTemplatesFromATemplateFile(templateToBeVisualized);

            templatesAsImages = new ArrayList<>();
            if(templatesToBeVisualized.size() > 0){
                // Visualizing the templates!
                for(int i = 0 ; i < templatesToBeVisualized.size() ; i++){
                    ArrayList<Stroke> currentTemplateToBeVisualized = templatesToBeVisualized.get(i);
                    BufferedImage bufferedImageOfTemplate = Utils.getImageDataOfGroup(currentTemplateToBeVisualized);
                    templatesAsImages.add(bufferedImageOfTemplate);
                }
            }

            // Instead of using the convertToFxImage() function, a more elegant approach would have been
            // to use the SwingFXUtils.toFXImage(templatesAsImages.get(0), null); function.
            // However, I couldn't make it work through gradle, so this is the solution that works for now
            // Source: https://stackoverflow.com/questions/30970005/bufferedimage-to-javafx-image
            visualizationImage = Utils.convertToFxImage(templatesAsImages.get(0));
            visualizationImageView.setImage(visualizationImage);
            visualizationImageView.setPreserveRatio(true);
            displayVisualizationMenu();
        });

        userChooseNewPath.setOnAction(e -> {
            directoryChooser.setInitialDirectory(new File(logDataDB.saveLocationPath));
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            System.out.println("Initial Saving Directory: "+logDataDB.saveLocationPath);
            logDataDB.updateSaveLocationPath(selectedDirectory.getAbsolutePath());
            System.out.println("New Saving Directory: "+ logDataDB.saveLocationPath);
        });

        closeButtonHBox.getChildren().addAll(buttonClose);

        typeOfCurrentInkText.setStyle("" +
                "-fx-font-size: 20px;" +
                "-fx-fill: LIGHTGOLDENRODYELLOW;"+
                "-fx-stroke: black;"+
                "-fx-stroke-width: 0.5px;");
        typeOfCurrentInkText.setText(TYPE_OF_INK_STATUS);

        currentStatusText.setText(typeOfCurrentInkText.getText().replace("STATUS: ",""));

        /* Adds the Canvas and the buttons to the root. */
        canvasRoot.getChildren().addAll(canvas, closeButtonHBox, typeOfCurrentInkText, collapsableMenuRoot, helpRoot, visualizeTemplatesRoot);
        canvasScene = new Scene(canvasRoot, canvas.getWidth(), canvas.getHeight());

        TYPE_OF_INK_STATUS = "STATUS: Command INK";
        typeOfCurrentInkText.setText(TYPE_OF_INK_STATUS);
        currentStatusText.setText(typeOfCurrentInkText.getText().replace("STATUS: ",""));

        removeAllInk();

        setPrimaryStageAlwaysOnTop(false);

        try {
            Thread.sleep(2);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        setPrimaryStageAlwaysOnTop(true);

        /* Options for the stage - Transparent */
        primaryStage.setTitle("Canvas Portal");
        primaryStage.setScene(canvasScene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        canvasScene.setFill(Color.web("FFFFFF",0.01));
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();

        //setTranslateX and setTranslateY !!!

        photoSectionOnHelpDisplayHBox.setTranslateY(closeHelpMenuButton.getTranslateY()+closeHelpMenuButton.getHeight());
        buttonSectionOnHelpDisplayHBox.setTranslateY(photoSectionOnHelpDisplayHBox.getTranslateY() + photoSectionOnHelpDisplayHBox.getHeight());
        closeHelpMenuButton.setTranslateX(topBarSectionOnHelpDisplayHBox.getTranslateX() + photoSectionOnHelpDisplayHBox.getWidth() - closeHelpMenuButton.getWidth());
        previousHintHelpMenuButton.setPrefWidth(photoSectionOnHelpDisplayHBox.getWidth() / 2);
        nextHintHelpMenuButton.setPrefWidth(photoSectionOnHelpDisplayHBox.getWidth() / 2);

        closeVisualizationMenuButton.setTranslateX(topBarSectionOnVisualizationOfTemplatesDisplayHBox.getTranslateX() + photoSectionOnVisualizationOfTemplatesDisplayHBox.getWidth() - closeVisualizationMenuButton.getWidth());
        buttonSectionOnVisualizationOfTemplatesDisplayHBox.setTranslateY(photoSectionOnVisualizationOfTemplatesDisplayHBox.getTranslateY()+ photoSectionOnVisualizationOfTemplatesDisplayHBox.getHeight());
        photoSectionOnVisualizationOfTemplatesDisplayHBox.setTranslateY(topBarSectionOnVisualizationOfTemplatesDisplayHBox.getTranslateY() + topBarSectionOnVisualizationOfTemplatesDisplayHBox.getHeight());

        closeButtonHBox.setTranslateX(canvas.getWidth() - buttonClose.getWidth() - 5);
        closeButtonHBox.setTranslateY(35 + TITLE_BAR_VSCODE_HEIGHT + 2);

        typeOfCurrentInkText.setTranslateX(canvas.getWidth() - 325);
        typeOfCurrentInkText.setTranslateY(closeButtonHBox.getTranslateY() + 20);

        collapsableMenuRoot.setTranslateX(canvas.getWidth()/2 - collapsableMenuRoot.getLayoutBounds().getWidth()/2);
        collapsableMenuRoot.setTranslateY(canvas.getHeight()*0.25);

        currentStatusText.setTranslateX(10);
        vBoxInkChangeTypeSectionInCollapsableMenu.setTranslateY(topBarSectionOnCollapsableMenuHBox.getHeight());
        vBoxLineThicknessModifierSectionOnCollapsableMenu.setTranslateY(vBoxInkChangeTypeSectionInCollapsableMenu.getTranslateY() + vBoxInkChangeTypeSectionInCollapsableMenu.getHeight() + 20);
        vBoxFunctionalitySectionInCollapsableMenu.setTranslateY(vBoxLineThicknessModifierSectionOnCollapsableMenu.getTranslateY() + vBoxLineThicknessModifierSectionOnCollapsableMenu.getHeight() + 20);
        vBoxExtra.setTranslateY(vBoxFunctionalitySectionInCollapsableMenu.getTranslateY() + vBoxFunctionalitySectionInCollapsableMenu.getHeight() + 20);

        userText.setTranslateY(participantTitleText.getTranslateY()+20);
        textFieldUserName.setTranslateY(userText.getTranslateY()+10);
        textFieldUserName.setText("Username");
        newSymbolName.setText("Symbol");

        radioButtonsVBox.setTranslateY(textFieldUserName.getTranslateY()+20);

        radioButtonsHandVBox.setTranslateY(radioButtonsVBox.getTranslateY() + 20);
        radioButtonsTypeVBox.setTranslateY(radioButtonsHandVBox.getTranslateY() + 10);


        userIntendedToDrawSymbolText.setTranslateY(radioButtonsTypeVBox.getTranslateY() + 20);
        comboBoxUserIntendedToDrawSymbol.setTranslateY(userIntendedToDrawSymbolText.getTranslateY() + 5);

        userChooseNewPath.setTranslateY(comboBoxUserIntendedToDrawSymbol.getTranslateY() + 40);

        userIntendsToAddTemplateForText.setTranslateY(templateTitleText.getTranslateY() + 20);
        comboBoxUserIntendedToDrawTemplateFor.setTranslateY(userIntendsToAddTemplateForText.getTranslateY() + 5);
        addTemplateButton.setTranslateY(comboBoxUserIntendedToDrawTemplateFor.getTranslateY() + 5);
        userAddsNewSymbol.setTranslateY(addTemplateButton.getTranslateY() + 20);
        newSymbolName.setTranslateY(userAddsNewSymbol.getTranslateY() + 10);
        addNewSymbolButton.setTranslateY(newSymbolName.getTranslateY() + 10);
        visualizeTemplateTitleText.setTranslateY(addNewSymbolButton.getTranslateY() + 40);
        visualizeTemplateText.setTranslateY(visualizeTemplateTitleText.getTranslateY() + 20);
        comboBoxTemplateFileNames.setTranslateY(visualizeTemplateText.getTranslateY() + 10);
        visualizeTemplatesButton.setTranslateY(comboBoxTemplateFileNames.getTranslateY() + 10);

        decreaseLineThicknessButton.setMinWidth(forcePokingButton.getWidth()/2);
        increaseLineThicknessButton.setMinWidth(forcePokingButton.getWidth()/2);

        removeExtraInkButtonInCollapsableMenu.setMinWidth(forcePokingButton.getWidth());
        regularInkInCollapsableMenuButton.setMinWidth(forcePokingButton.getWidth());
        extraInkInCollapsableMenuButton.setMinWidth(forcePokingButton.getWidth());
        removeAllInkButtonInCollapsableMenu.setMinWidth(forcePokingButton.getWidth());
        undoButtonInCollapsableMenu.setMinWidth(forcePokingButton.getWidth());
        eraserModeInCollapsableMenuButton.setMinWidth(forcePokingButton.getWidth());
        testButton.setMinWidth(forcePokingButton.getWidth());
        restartCanvasButton.setMinWidth(forcePokingButton.getWidth());
        helpButton.setMinWidth(forcePokingButton.getWidth());
        vBoxParticipant.setTranslateX(topBarSectionOnCollapsableMenuHBox.getWidth()+50);
        vBoxTemplates.setTranslateX(vBoxParticipant.getTranslateX()+vBoxParticipant.getWidth()+50);
    }
}