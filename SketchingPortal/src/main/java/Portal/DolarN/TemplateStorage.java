package Portal.DolarN;

import Portal.CanvasUI;
import Portal.Helper.Utils;
import Portal.Storage.Point;
import Portal.Storage.Stroke;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

/**
 * Singleton Class holding all the initial Templates, loaded via the method "instantiateTemplates".
 */
public class TemplateStorage {

    /**
     * List of all the templates
     */
    public static Hashtable<String, Multistroke> allTemplates = new Hashtable<>(){
        {} // Instantiates it with Empty
    };

    /**
     * Names of the columns within the EXCEL files (containing the data for the templates
     */
    private static final ArrayList<String> columnNames = new ArrayList<>(){{
                                                                            add("Name of Gesture");
                                                                            add("#Incoming Strokes");
                                                                            add("#Incoming Points");
                                                                            add("Point.x");
                                                                            add("Point.y");
                                                                            }};

    /**
     * List of the Objects represented by templates.
     */
    private static ArrayList<String> gestureList = new ArrayList<>();

    /**
     * Singleton instance
     */
    private static TemplateStorage instance;

    /**
     * Function called only ONCE for instantiating
     *
     * It goes through all the EXCEL files for every category, retrieves and instantiates the templates
     */
    private static void instantiateTemplates(){

        gestureList = Utils.getListOfValidTemplates();

        allTemplates = new Hashtable<String, Multistroke>();

        // for each templateName given
        for(String gestureName : gestureList) {
            try {
                // go within its Excel file to retrieve data
                String excelFilePath = "./resource/templateData/templates_"+gestureName+".xlsx";
                File file = new File(excelFilePath);

                if (file.exists()) {
                    // if the file exists, get the first sheet of the workbook, and create the templates
                    FileInputStream fileInputStream = new FileInputStream(file);

                    XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

                    XSSFSheet sheet = workbook.getSheetAt(0);

                    Iterator<Row> rowIterator = sheet.iterator();

                    int rowNumber = -1;

                    // Create the Template as long as there are templates.
                    while (rowIterator.hasNext()) {
                        rowNumber++;

                        // take current raw
                        Row row = rowIterator.next();

                        if (rowNumber == 0) {
                            //this one contains the names of the columns
                            columnNames.add(row.getCell(0).toString());
                            columnNames.add(row.getCell(1).toString());
                            columnNames.add(row.getCell(2).toString());
                            columnNames.add(row.getCell(3).toString());
                            columnNames.add(row.getCell(4).toString());
                        } else {
                            // from now on, it contains the Data representing Templates
                            String templateName = row.getCell(0).toString();
                            int incomingStroke = (int) row.getCell(1).getNumericCellValue();
                            ArrayList<ArrayList<PointR>> msPoints = new ArrayList<>();

                            for (int strokeNumber = 0; strokeNumber < incomingStroke; strokeNumber++) {
                                ArrayList<PointR> strokePoints = new ArrayList<>();
                                row = rowIterator.next();
                                int numberOfPointsInStroke = (int) row.getCell(2).getNumericCellValue();
                                for (int i = 0; i < numberOfPointsInStroke; i++) {
                                    row = rowIterator.next();
                                    double x = row.getCell(3).getNumericCellValue(),
                                            y = row.getCell(4).getNumericCellValue();
                                    PointR currentPoint = new PointR(x, y);
                                    strokePoints.add(currentPoint);
                                }
                                msPoints.add(strokePoints);
                            }
                            if (!allTemplates.containsKey(templateName)) {
                                // if the template is a first (e.g. first Line)
                                allTemplates.put(templateName, new Multistroke(templateName, msPoints, true));
                            } else {
                                // there is already a template with this name. A new name must be found
                                // this else branch will add templates to already existing gestures (in the future)
                                while (allTemplates.containsKey(templateName)) {
                                    templateName = templateName + "_" + UUID.randomUUID().toString();
                                }
                                allTemplates.put(templateName, new Multistroke(templateName, msPoints, true));
                            }
                        }
                    }
                    fileInputStream.close();

                } else {
                    System.out.println("The file including the templates for "+gestureName+" is missing! (tried to open "+excelFilePath+")");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructor
     */
    private TemplateStorage(){
        this.instantiateTemplates();
    }

    /**
     * Getter for the instance.
     * Making sure that the initialization of the templates is done only ONCE
     * @return the Singleton Instance of the templates
     */
    public static TemplateStorage getInstance() {
        if(instance == null)
            instance = new TemplateStorage();
        return instance;
    }
}