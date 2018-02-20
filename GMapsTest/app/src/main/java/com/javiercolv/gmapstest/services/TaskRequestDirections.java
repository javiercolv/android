package com.javiercolv.gmapstest.services;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Javier Cruz on 17/02/2018.
 */

public class TaskRequestDirections extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... strings) {
        String response = "";
        try {
            response = requestDirections(strings[0]);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private String requestDirections(String urlRequest) throws IOException {
        String response = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(urlRequest);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
            response = stringBuilder.toString();
            bufferedReader.close();
            inputStreamReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return response;
    }
}
