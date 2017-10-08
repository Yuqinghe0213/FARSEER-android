package com.example.farseer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by YuqingHe on 2017/10/6.
 * This class is used to get response to google API Geocoder Tool. This API can convert a address
 * to its altitude and longitude.
 */

public class GooglePlaces {
    public GooglePlaces(){

    }
    public String getHTTPData(String  requeastURL)
    {
        URL url;
        String srespond = "";
        InputStream inputStream = null;
        HttpURLConnection conn = null;
        try{

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(requeastURL)
                    .get()
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "02e728d2-9571-df1b-95cb-4375864df23d")
                    .build();

            Response response = client.newCall(request).execute();

            srespond = response.body().string();

        }  catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.d("DownloadURL","Returning data= "+ srespond);

        return srespond;
    }
}
