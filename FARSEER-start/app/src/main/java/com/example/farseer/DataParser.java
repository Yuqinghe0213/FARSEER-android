package com.example.farseer;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yuqing on 27/9/17.
 * This class is used to deal with the data from server and change the json object to list.
 * We use hash map to store the data from server, and add it to a arraylist.
 */

public class DataParser {

    //Store the information about creatures
    private HashMap<String, String> getCreature (String latitude, String longtitude, String species, String id){
        HashMap<String, String> creatureMap = new HashMap<>();
        creatureMap.put("lat", latitude);
        creatureMap.put("long", longtitude);
        creatureMap.put("species", species);
        creatureMap.put("id", id);

        return creatureMap;
    }

    //store the context of one report in one location
    private HashMap<String, String> getReportcontext(String name, String date, String species, String context){

        HashMap<String, String> reportMap = new HashMap<>();
        reportMap.put("name", name);
        reportMap.put("date",date);
        reportMap.put("species", species);
        reportMap.put("context", context);

        return reportMap;
    }

    // store the infromation about location of report and creatures
    private HashMap<String, String> getReport (String latitude, String longtitude){
        HashMap<String, String> creatureMap = new HashMap<>();
        creatureMap.put("lat", latitude);
        creatureMap.put("long", longtitude);

        return creatureMap;

    }

    public List<HashMap<String, String>> getList(int type,JSONObject jsonObject){

        List<HashMap<String, String>> List = new ArrayList<>();
        HashMap<String, String> creaMap = null;
        switch (type) {
            case 1: {
                try {//store the location of creatures.
                    JSONArray list = ((JSONArray) jsonObject.get("locations"));
                    for (int i = 0; i < list.length(); i++) {
                        JSONArray element = list.getJSONArray(i);
                        String latitude = element.getString(0);
                        String longitude = element.getString(1);
                        String species = element.getString(2);
                        String id = element.getString(3);
                        creaMap = getCreature(latitude, longitude, species, id);
                        List.add(creaMap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
            case 2: {
                try {//store the location of report
                    JSONArray list = ((JSONArray) jsonObject.get("locations"));
                    for (int i = 0; i < list.length(); i++) {
                        JSONArray element = list.getJSONArray(i);
                        String latitude = element.getString(0);
                        String longitude = element.getString(1);
                        creaMap = getReport(latitude, longitude);
                        List.add(creaMap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            }
            case 3: {
                try {//store the report context
                    JSONArray list = ((JSONArray) jsonObject.get("reports"));
                    for (int i = 0; i < list.length(); i++) {
                        JSONArray element = list.getJSONArray(i);
                        String name = element.getString(0);
                        String date = element.getString(1);
                        String species = element.getString(2);
                        String context = element.getString(3);
                        creaMap = getReportcontext(name, date, species, context);
                        Log.d("DataParser", "creMap = " + creaMap.toString());
                        List.add(creaMap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            }
            case 4:{
                try {//get the safe area location
                    JSONArray list = ((JSONArray) jsonObject.get("locations"));
                    for (int i = 0; i < list.length(); i++) {
                        JSONArray element = list.getJSONArray(i);
                        String latitude = element.getString(0);
                        String longitude = element.getString(1);
                        creaMap = getReport(latitude, longitude);
                        List.add(creaMap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return List;
    }



}
