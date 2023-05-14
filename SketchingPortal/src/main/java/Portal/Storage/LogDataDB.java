package Portal.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Portal.CanvasUI;
import Portal.Helper.Utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;

/** TO BE REMOVED AFTER THE USABILITY STUDY IS COMPLETED
 * Temporal class that keeps track of all the events that happened during a session, and than creates an excel logFile based on it.
 */
public class LogDataDB {
    public static String saveLocationPath = Utils.templateFolderPath.replace("resource\\templateData","resource\\logData"); //"./resource/logData";

    public static void updateSaveLocationPath(String newPath){
        saveLocationPath = newPath;
    }
    /**
     * Singleton instance
     */
    private static LogDataDB instance;

    /**
     * Excel columns names for the log
     */
    private static final String[] columns = {"Type", "Info", "Start Time", "End Time", "Point.x", "Point.y", "Recognition result", "Score", "Recognition Time", "Stroke ID", "Stroke in Java"};

    /**
     * The log: List of LogData events
     */
    private static ArrayList<LogData> logDataList;

    /**
     * @return The singleton instance of the LogDataDB
     */
    public static LogDataDB getInstance() {
        if(instance == null) {
            instance = new LogDataDB();
            logDataList = new ArrayList<>();
        }
        return instance;
    }

    /**
     * Adds an event to the log
     * @param logData the event
     */
    public void addEventToDatabase(LogData logData){
        logDataList.add(logData);
    }

    /**
     * Function that saves the log as Excel
     * @throws IOException Exception thrown in case there was a problem creating the log file
     */
    public void saveAsExcel() throws IOException {

        long currentTime = new Timestamp(System.currentTimeMillis()).getTime();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log File");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Other rows and cells with contacts data
        int rowNum = 1;

        for (LogData logData : logDataList) {
            Row row = sheet.createRow(rowNum++);
            // Info, Button
            row.createCell(0).setCellValue(logData.getType());
            row.createCell(1).setCellValue(logData.getAdditionalInfo());
            //row.createCell(3).setCellValue(new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss.S").format(new Date(logData.getEventEnd().getTime())));

            // Result
            if(logData.getType().equals("Result")){
                // recognition results
                if(logData.getJsonData().getRecognizedAs() != "Text"){
                    row.createCell(6).setCellValue(logData.getJsonData().getRecognizedAs()+"(Text: "+logData.getJsonData().getText()+")");
                }
                else {
                    row.createCell(6).setCellValue("("+logData.getJsonData().getRecognizedAs()+") "+logData.getJsonData().getText());
                }
                row.createCell(7).setCellValue(logData.getJsonData().getScore());
                row.createCell(8).setCellValue(logData.getJsonData().getTimeOfRecognition());

            }
            else if(logData.getType().contains("Mouse")) {
                // Buttons / other interactions
                row.createCell(1).setCellValue(logData.getAdditionalInfo());
                row.createCell(3).setCellValue(new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss.S").format(new Date(logData.getEventEnd().getTime())));
                row.createCell(4).setCellValue(logData.getStroke().getPathPoints().get(0).getX());
                row.createCell(5).setCellValue(logData.getStroke().getPathPoints().get(0).getY());
            }
            else if(logData.getType().contains("Touch")) {
                // Touch events [actual log results]
                row.createCell(1).setCellValue(" ");
                row.createCell(2).setCellValue(new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss.S").format(new Date(logData.getStroke().getTimeStartDrawing().getTime())));
                row.createCell(3).setCellValue(new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss.S").format(new Date(logData.getStroke().getTimeStopDrawing().getTime())));
                row.createCell(4).setCellValue(logData.getStroke().getPathPoints().size());
                row.createCell(5).setCellValue("\\/");
                row.createCell(9).setCellValue(logData.getStroke().getId().toString());

                String dataToJavaString = "";
                dataToJavaString += "Stroke s = new Stroke();";
                dataToJavaString += "s.beginPath(new Point(" + logData.getStroke().getStartPoint().getX() + ", " + logData.getStroke().getStartPoint().getY() + "));";
                for(int i =  1 ; i < logData.getStroke().getPathPoints().size() ; i++) {
                    dataToJavaString += "s.addPointToPath(new Point(" + logData.getStroke().getPathPoints().get(i).getX() + ", " + logData.getStroke().getPathPoints().get(i).getY() + "));";
                }
                dataToJavaString += "s.finishPath();";

                row.createCell(10).setCellValue(dataToJavaString);

                int groupStartRow = sheet.getLastRowNum() + 1;
                // add the path points to the path points column;
                for(int i = 0; i < logData.getStroke().getPathPoints().size() ; i++){
                    row = sheet.createRow(rowNum++);
                    row.createCell(4).setCellValue(logData.getStroke().getPathPoints().get(i).getX());
                    row.createCell(5).setCellValue(logData.getStroke().getPathPoints().get(i).getY());
                }
                sheet.groupRow(groupStartRow,sheet.getLastRowNum());
                sheet.setRowGroupCollapsed(groupStartRow,true);

            }
        }

        // Resize all columns to fit the content size
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        Path pathOfLogData = Paths.get(saveLocationPath);
        Files.createDirectories(pathOfLogData);

        String fileName = "";
        String typeToggleGroup = CanvasUI.toggleGroupType.getSelectedToggle().toString().split("'")[1];
        String handToggleGroup = CanvasUI.toggleGroupHand.getSelectedToggle().toString().split("'")[1];
        fileName = CanvasUI.textFieldUserName.getText() + "_" + typeToggleGroup + "_" + handToggleGroup + "_" +  currentTime;
        // Write the output to a file
        FileOutputStream finalFileOutput = new FileOutputStream(saveLocationPath+"/"+fileName + ".xlsx");

        boolean anyTouchInput = false;
        for (LogData logData : logDataList) {
            if(logData.getType().contains("Touch")){
                // Creating the image representative of the touch events
                if(!anyTouchInput) {
                    anyTouchInput = true;
                    Path path = Paths.get(saveLocationPath+"/"+fileName);
                    Files.createDirectories(path);
                }

                ArrayList<Stroke> myList = new ArrayList<>();
                myList.add(logData.getStroke());

                File outputfile = new File(saveLocationPath+"/"+ fileName + "/" + logData.getStroke().getId().toString() + ".jpg");
                ImageIO.write(Utils.getImageDataOfGroup(myList), "jpg", outputfile);
            }
        }

        workbook.write(finalFileOutput);
        finalFileOutput.close();
    }
}
