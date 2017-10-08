package com.example.farseer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.farseer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ThreeFragment extends Fragment{

    private TextView habittitle;
    private TextView description;

    public ThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_three, container, false);
        final Bundle args = getArguments();
        final String creatureid = args.getString("creature");
        habittitle = (TextView)v.findViewById(R.id.habittitle);
        description = (TextView)v.findViewById(R.id.habitdescription);

        new ThreeFragment.getCreature().execute("1");
//        new UserProfileActivity.getCreature().execute(creatureid);
        return v;

    }

    private class getCreature extends AsyncTask<String, String, String> {
        String title;
        String des;

        protected String doInBackground(String... params) {
            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://13.73.108.122/getCreature?Creature_ID=1")
                        .addHeader("cache-control","no-cache")
                        .addHeader("postman-token","55d1aa01-810e-d677-340d-4c17c0f261cd")
                        .get()
                        .build();


                Response response = client.newCall(request).execute();
                final String sResponse=response.body().string();

                String origin = sResponse.substring(sResponse.indexOf('{'),sResponse.indexOf('}')+1);
                final String sResult = origin.replace("\\", "");
                JSONObject result = new JSONObject(sResult);
                title = result.getString("habitcategory");
                des = result.getString("habitdescription");
            } catch (final IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
//                        Toast.makeText(OneFragment.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            catch (final JSONException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
//                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
            return title;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            habittitle.setText(title);
            description.setText(des);
        }
    }

}