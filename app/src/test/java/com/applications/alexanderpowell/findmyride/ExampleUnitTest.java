package com.applications.alexanderpowell.findmyride;

import org.junit.Test;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.json.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println("printing from command line");

        try {
            URL url = new URL("https://api.uber.com/v1.2/products?latitude=38.899752&longitude=-76.997337");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            // Set headers
            connection.setRequestProperty("Accept-Language", "en_US");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer KA.eyJ2ZXJzaW9uIjoyLCJpZCI6IktaVnVOSnNEUWRPcHN3ZVdzYWFRNmc9PSIsImV4cGlyZXNfYXQiOjE1MzM2MDI2MDIsInBpcGVsaW5lX2tleV9pZCI6Ik1RPT0iLCJwaXBlbGluZV9pZCI6MX0.0CM-AA1pRaMQPgFrsQIlCvQzWtT9zR1GlUBx7oELyEA");

            int responseCode = connection.getResponseCode();

            System.out.println("Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();

            String output = response.toString();
            System.out.println("Output: ");
            System.out.println(output);

            JSONObject json = new JSONObject(output);

            String products = (String)json.get("short_description");
            System.out.println("Products: " + products);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        assertEquals(4, 2 + 2);
    }
}