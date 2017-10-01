package com.example.yuqing.mymap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yuqing on 27/9/17.
 */

public class DataParser {

    private HashMap<String, String> getCreature (){
        HashMap<String, String> creatureMap = new HashMap<>();
        String name = "-NA-";
        String latitude = "";
        String longtitude = "";

        name = "snake";
        latitude = "";
        longtitude = "";
        creatureMap.put("name", name);
        creatureMap.put("lat", latitude);
        creatureMap.put("long", longtitude);

        return creatureMap;

    }

    private List<HashMap<String, String>> getCreatureList(){

        List<HashMap<String, String>> creatureList = new ArrayList<>();
        HashMap<String, String> creaMap = null;
        creaMap = getCreature();
        creatureList.add(creaMap);

        return creatureList;
    }



}
