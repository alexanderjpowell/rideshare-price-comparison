package com.applications.alexanderpowell.findmyride;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;

public class UberApiHandler {

    public String TAG = "log";

    private String product_id_uber_x;
    private String product_id_uber_xl;
    private String product_id_uber_pool;
    private String product_id_uber_black;

    public HashMap<String, String> product_id_map = new HashMap<String, String>();

    public UberApiHandler(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);
            Object array = jsonObject.get("products");
            JSONArray jsonArray = new JSONArray(array.toString());
            String product_id, short_description;

            for (int i = 0; i < jsonArray.length(); i++) {
                short_description = ((JSONObject) jsonArray.get(i)).getString("short_description");
                product_id = ((JSONObject) jsonArray.get(i)).getString("product_id");
                product_id_map.put(short_description, product_id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (HashMap.Entry<String, String> entry : product_id_map.entrySet()) {
            //Log.i(TAG, "Key/Value: " + entry.getKey() + ":" + entry.getValue());
            if (entry.getKey().equals("Pool"))
                product_id_uber_pool = entry.getValue();
            if (entry.getKey().equals("UberX"))
                product_id_uber_x = entry.getValue();
            if (entry.getKey().equals("UberXL"))
                product_id_uber_xl = entry.getValue();
            if (entry.getKey().equals("Black"))
                product_id_uber_black = entry.getValue();
        }
    }

    // Getters
    public String getProduct_id_uber_x() {
        return product_id_uber_x;
    }

    public String getProduct_id_uber_xl() {
        return product_id_uber_xl;
    }

    public String getProduct_id_uber_pool() {
        return product_id_uber_pool;
    }

    public String getProduct_id_uber_black() {
        return product_id_uber_black;
    }
}
