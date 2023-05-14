package Portal.DolarN;

import Portal.CanvasUI;
import Portal.Helper.Utils;
import Portal.Storage.Point;
import Portal.Storage.Stroke;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Class used (in the additional menu of the Canvas) to store (& save) templates.
 * The way it works is that, if a group is observed to be miss-classified, its data must be saved:
 *  - go to the Second Menu
 *  - select the shape on the dropdown section ("Template represents:")
 *  - press the button "Save as Template"
 *
 * NOTE THAT:
 *  - the current group representing the template will be removed from the canvas, and stored, temporally, in this class
 *  - before closing the application, set the name of the file in the field from the secondary menu, named "User"
 *      it will be saved as "template_NAME-GIVEN-BY-YOU_SOME-TIMESTAMP"
 *      do not worry about the name - it will be original (not similar to other files form that folder)
 *      the .xlsx file will be saved under "...\SketchingPortal\resource\templateData\"
 *  - all the templates are automatically saved when the button "Close Extension" is pressed
 *    (avoid closing the Canvas in other way)
 *  - to add this template to the representative .xlsx (with those types of templates) by copying the template from the newly created .xlsx file, and pasting that data to the end of the representative .xlsx file
 */
public class TemplateLocalStorage {

    /**
     * List of Strokes representing the template
     * Every symbol will have a list of templates, some of which will be empty.
     */
    private static ArrayList<ArrayList<ArrayList<Stroke>>> templates;

    /**
     * Name/Category of the template (e.g. Line, Arrow, Step Into)
     * Should be ready-to-be-added to the .xlsx file, but make sure to double-check the name from the representative file
     */
    private static ArrayList<String> nameListOfSymbols;

    private int initialNumberOfSymbols = 0;

    /**
     * Singleton instance
     */
    private static TemplateLocalStorage instance;

    /**
     * first line within the .xlsx file
     */
    private static String[] columns = {"Name of Gesture", "#Incoming Strokes", "#Incoming Points", "Point.x", "Point.y"};

    /**
     * Constructor
     */
    private TemplateLocalStorage() {
        templates = new ArrayList<>();
        nameListOfSymbols = new ArrayList<>();

        nameListOfSymbols = Utils.getListOfValidTemplates();
        initialNumberOfSymbols = nameListOfSymbols.size();
        for(int i = 0 ; i < nameListOfSymbols.size() ; i++){
            // every symbol will have an empty initial local template storage
            templates.add(new ArrayList<>());
        }

    }

    /**
     * returns the Singleton Instance
     *
     * @return the Instance
     */
    public static TemplateLocalStorage getInstance() {
        if (instance == null)
            instance = new TemplateLocalStorage();
        return instance;
    }

    /**
     * Adds a template to the storage
     *
     * @param symbolName name of the symbol, to which the template will be added
     * @param newTemplate  the Template itself
     */
    public void addTemplateToLocalStorage(String symbolName, ArrayList<Stroke> newTemplate) {
        //this.nameListOfSymbols.add(templateName);
        int indexOfSymbol = 0;
        // a more elegant version would be nameListOfSymbols.indexOf(symbolName)
        // Try it later - although it will not improve the speed significantly
        for(int i = 0 ; i < nameListOfSymbols.size() ; i++){
            if(nameListOfSymbols.get(i).contains(symbolName)){
                indexOfSymbol = i;
            }
        }

        this.templates.get(indexOfSymbol).add(newTemplate);
    }

    public boolean addNewSymbolToList(String newSymbolName){
        // if the symbol is already on the list, do nothing.
        // otherwise, add it at the end.
        if(!nameListOfSymbols.contains(newSymbolName)) {
            this.nameListOfSymbols.add(newSymbolName);
            this.templates.add(new ArrayList<>());
            System.out.println("Symbol " + newSymbolName + " was added!");
            return true;
        }
        return false;
    }

    /**
     * Function called at the end of the session, which saves the entire local database to an .xlsx file
     *
     * @throws IOException
     */
    public void saveAsExcel() throws IOException {
        for(int symbolIndex = 0 ; symbolIndex < nameListOfSymbols.size() ; symbolIndex++){
            var templateListOfSymbol = templates.get(symbolIndex);

            // if there is at least one symbol which was added by the user
            if(templateListOfSymbol.size() > 0){
                String symbolName = nameListOfSymbols.get(symbolIndex);

                long currentTime = new Timestamp(System.currentTimeMillis()).getTime();

                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("SavedTemplates");

                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 14);
                headerFont.setColor(IndexedColors.RED.getIndex());

                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(0);

                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                int rowNum = 1;

                for (int i = 0; i < templateListOfSymbol.size(); i++) {
                    ArrayList<Stroke> currentGroup = templateListOfSymbol.get(i);
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(symbolName);
                    row.createCell(1).setCellValue(currentGroup.size());

                    for (Stroke currentStroke : currentGroup) {
                        row = sheet.createRow(rowNum++);
                        row.createCell(2).setCellValue(currentStroke.getPathPoints().size());
                        for (Point currentPoint : currentStroke.getPathPoints()) {
                            row = sheet.createRow(rowNum++);
                            row.createCell(3).setCellValue(currentPoint.getX());
                            row.createCell(4).setCellValue(currentPoint.getY());
                        }
                    }

                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                Path pathOfLogData = Paths.get("./resource/templateData/");
                Files.createDirectories(pathOfLogData);
                String fileName = "templates_" + symbolName;
                if(symbolIndex < initialNumberOfSymbols) {
                    fileName += "_" + currentTime;
                }

                FileOutputStream finalFileOutput = new FileOutputStream("./resource/templateData/" + fileName + ".xlsx");

                workbook.write(finalFileOutput);
                finalFileOutput.close();
            }
        }
    }
}