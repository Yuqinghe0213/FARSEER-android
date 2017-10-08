package com.example.farseer;

/**
 * Created by Administrator on 2017/10/8.
 * The class is used to build structured data for showing in a list view.
 */

public class Report {

    String username;
    String date;
    String description;
    String species;

    public Report(String name,String date, String description, String species){
        this.username = name;
        this.date = date;
        this.description = description;
        this.species = species;
    }
}
