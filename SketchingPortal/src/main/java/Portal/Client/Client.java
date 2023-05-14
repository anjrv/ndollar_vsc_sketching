package Portal.Client;

import Portal.Storage.JSONData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A class that helps communicate with the VS Code Server.
 * It sends HTTP Requests
 */
public class Client {

    /**
     * PORT represents the Port Number of the localhost server hosted by our VS Code extension.
     */
    private static final String VSCodeServer_PORT = "3000";

    /* Instance of the 3RD party software (OkHttp3) */
    private static final OkHttpClient httpClient = new OkHttpClient();

    /**
     * MediaType of the JSON
     */
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    /**
     * Sends a GET request to the VS Code Server.
     * @throws IOException -
     */
    public static void sendHttp3GetRequestToVSCodeServer() throws IOException {
        //https://mkyong.com/java/okhttp-how-to-send-http-requests/

        /* Builds the request. */
        Request request = new Request.Builder()
                .url("http://localhost:"+VSCodeServer_PORT)
                .addHeader("User-Agent", "OkHttp3 Bot - GET")
                .build();
        /* Sends the request. */
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            /* Retrieve response headers. */
            Headers responseHeaders = response.headers();
            // Prints response body. */
            System.out.println(response.body().string());
            response.body().close();
        }
    }

    /**
     * Sends a POST Request to the VS Code Express server.
     * The data that is sent represents a list of Strokes. The input might get changed in the future.
     * @param recognizedStrokes The list of Strokes that is to be send in a JSON Format.
     * @throws IOException Any encountered Input-Output Exception
     */
    public static void sendHttp3PostRequestToVSCodeServer(ArrayList<JSONData> recognizedStrokes) throws IOException {
        /* Class used on JSON - Stringifies the input data */
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Stringify the data
            String jsonData = mapper.writeValueAsString(recognizedStrokes).toString();

            // Add it to the body;
            RequestBody body = RequestBody.create(jsonData, JSON);

            // Create the request with that data set on the body
            Request request = new Request.Builder()
                    .url("http://localhost:"+VSCodeServer_PORT)
                    .addHeader("User-Agent", "OkHttp3 Bot - POST")
                    .post(body)
                    .build();

            // Send the Request and receive the Response
            Response response = httpClient.newCall(request).execute();
            response.body().close();
        }
        catch(JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void sendSignalToExtension(String signalName) throws IOException {
        /* Class used on JSON - Stringifies the input data */
        ObjectMapper mapper = new ObjectMapper();

        try {
            ArrayList<JSONData> signalStroke = new ArrayList<>();
            JSONData signal = new JSONData();
            signal.updateRecognizedAs("Signal");
            signal.updateText(signalName);
            signalStroke.add(signal);

            // Stringify the data
            String jsonData = mapper.writeValueAsString(signalStroke).toString();

            // Add it to the body;
            RequestBody body = RequestBody.create(jsonData, JSON);

            // Create the request with that data set on the body
            Request request = new Request.Builder()
                    .url("http://localhost:" + VSCodeServer_PORT)
                    .addHeader("User-Agent", "OkHttp3 Bot - POST")
                    .post(body)
                    .build();

            // Send the Request and receive the Response
            Response response = httpClient.newCall(request).execute();
            response.body().close();
        }
        catch(JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param responseOfUser either "Yes" or "No"
     */
    public static void sendUserResponseToVSCode(String responseOfUser) throws IOException{
        /* Class used on JSON - Stringifies the input data */
        ObjectMapper mapper = new ObjectMapper();

        try {
            ArrayList<JSONData> signalStroke = new ArrayList<>();
            JSONData signal = new JSONData();
            signal.updateRecognizedAs("User Response");
            signal.updateText(responseOfUser);
            signalStroke.add(signal);

            // Stringify the data
            String jsonData = mapper.writeValueAsString(signalStroke).toString();

            // Add it to the body;
            RequestBody body = RequestBody.create(jsonData, JSON);

            // Create the request with that data set on the body
            Request request = new Request.Builder()
                    .url("http://localhost:" + VSCodeServer_PORT)
                    .addHeader("User-Agent", "OkHttp3 Bot - POST")
                    .post(body)
                    .build();

            // Send the Request and receive the Response
            Response response = httpClient.newCall(request).execute();
            response.body().close();
        }
        catch(JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
