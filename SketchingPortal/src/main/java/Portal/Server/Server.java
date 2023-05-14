package Portal.Server;

import Portal.CanvasLogic;
import Portal.CanvasUI;
import Portal.Storage.Point;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * The server will listen to the specified port, and will do actions based on the requests that are received.
 */
public class Server {

    /**
     * the Port of our Server in Java
     */
    private int JavaServer_PORT = 3910;

    /* The server itself */
    private HttpServer server;

    /* Constructor of the server */
    public Server() throws IOException {
        server = HttpServer.create(new InetSocketAddress(JavaServer_PORT), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    /* Handler for requests */
    private class MyHandler implements HttpHandler {
        //private int numberOfProcessedRequests = 0;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            //System.out.println(++numberOfProcessedRequests);
            String requestParamValue = null;
            if("GET".equals(httpExchange.getRequestMethod())) {
                requestParamValue = handleGetRequest(httpExchange);
            }else if("POST".equals(httpExchange.getRequestMethod())) {
                requestParamValue = handlePostRequest(httpExchange);
            }
            handleResponse(httpExchange, requestParamValue);
        }

        private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
            System.out.println(requestParamValue);
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();

            htmlBuilder.append("<html>").
            append("<body>").
            append("<h1>").
            append("Hello ")
                    .append(requestParamValue)
                    .append("</h1>")
                    .append("</body>")
                    .append("</html>");

            // encode HTML content
            String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());;

            // this line is a must
            httpExchange.sendResponseHeaders(200, htmlResponse.length());

            outputStream.write(htmlResponse.getBytes());
            outputStream.flush();
            outputStream.close();
        }

        /* Handle a GET Request */
        private String handleGetRequest(HttpExchange httpExchange) {
            System.out.println("GET Request received! "+" "+httpExchange.getRequestMethod()+" "+httpExchange.getRequestURI());
            return httpExchange.
                    getRequestURI()
                    .toString()
                    .split("\\?")[1]
                    .split("=")[1];
        }

        /*
        * Handle a POST Request
        * Usually, the Signals are being received here
        */
        private String handlePostRequest(HttpExchange httpExchange) {
            try {
                //transform the encoded signal into humanly-readable text
                String reqBody = new String(httpExchange.getRequestBody().readAllBytes());

                System.out.println("POST Request received: "+reqBody);
                String[] commandReceived = reqBody.split("_");

                //if the user sent "Signal_SIGNAL-NAME_SIGNAL-MORE-PARAMS", call the appropriate functions
                if(commandReceived.length >= 2){
                    if(commandReceived[0].contains("Signal")) {
                        // Signal: Clear all INK
                        if (commandReceived[1].contains("Clear All INK")) {
                            // by firing the button, it will send a signal to VS Code's extension, and it will glitch out [due to overlapping signals]
                            //CanvasUI.removeAllInkButtonInCollapsableMenu.fire();
                            CanvasUI.removeAllInk();
                        }
                        else if(commandReceived[1].contains("Clear All Command INK")){
                            CanvasUI.removeAllCommandStrokes();
                        }
                        // Signal: Undo
                        else if (commandReceived[1].contains("Undo")) {
                            //CanvasUI.callUndo();
                        } else {
                            System.out.println("Signal not defined yet!");
                        }
                    }
                    else if(commandReceived[0].contains("Confirm Box")){
                        double TOP_LEFT_CORNER_FOR_BOX_WITH_TWO_OPTIONS_X = 550.0,
                                TOP_LEFT_CORNER_FOR_BOX_WITH_TWO_OPTIONS_Y = 150.0,
                                WIDTH_FOR_BOX_WITH_TWO_OPTIONS = 250.0,
                                HEIGHT_WITH_TWO_OPTIONS = 200;

                        CanvasUI.addTextWithTwoOptions(new Point(TOP_LEFT_CORNER_FOR_BOX_WITH_TWO_OPTIONS_X,TOP_LEFT_CORNER_FOR_BOX_WITH_TWO_OPTIONS_Y),
                                WIDTH_FOR_BOX_WITH_TWO_OPTIONS,HEIGHT_WITH_TWO_OPTIONS,commandReceived[1]+"?");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return httpExchange.
                    getRequestURI()
                    .toString()
                    .split("\\?")[1]
                    .split("=")[1];
        }
    }
}
