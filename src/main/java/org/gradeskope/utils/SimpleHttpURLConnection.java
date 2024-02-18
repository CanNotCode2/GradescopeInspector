package org.gradeskope.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttpURLConnection {
    private String USER_AGENT = "";
    private String GET_URL = "";
    private String POST_URL = "";
    private String POST_PARAMS = "";
    public SimpleHttpURLConnection(String USER_AGENT, String GET_URL) {
        this.USER_AGENT = USER_AGENT;
        this.GET_URL = GET_URL;
    }


    public String sendGET() throws IOException {
        if (GET_URL.isEmpty()) {
           throw new RuntimeException("GET URL is Empty");
        }

        URL obj = new URL(GET_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            throw new RuntimeException("GET request did not work.");
        }

    }

    public String sendPOST() throws IOException {
        if (POST_URL.isEmpty()) {
            throw new RuntimeException("POST URL is Empty");
        }

        URL obj = new URL(POST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            throw new RuntimeException("POST request did not work.");
        }
    }

}