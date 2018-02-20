package com.javiercolv.gmapstest.services;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Javier Cruz on 17/02/2018.
 */

public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
        JSONObject jsonObject = null;
        List<List<HashMap<String, String>>> routes = null;
        try {
            jsonObject = new JSONObject(strings[0]);
            DirectionsParser directionsParser = new DirectionsParser();
            routes = directionsParser.parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }

}

