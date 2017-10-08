package com.example.farseer;



import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.farseer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by KailunZhang.

 This class is used to continue the creature profile feature.
 The feature includes:
 1. get creature id from NextActivity through bundle;
 2. search in the database with creature id and return dangerous information of the corresponding creature id.
 */

public class TwoFragment extends Fragment{

    private TextView dangerlevel;
    private TextView description;
    private TextView dangertitle;
    private String creatureid;

    public TwoFragment() {
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

        View v = inflater.inflate(R.layout.fragment_two, container, false);
        final Bundle args = getArguments();
        creatureid = args.getString("creature");
        dangerlevel = (TextView)v.findViewById(R.id.dangerlevel);
        description = (TextView)v.findViewById(R.id.dangerdescription);
        dangertitle = (TextView)v.findViewById(R.id.dangertitle);

        new TwoFragment.getCreature().execute(creatureid);
        return v;

    }

    private class getCreature extends AsyncTask<String, String, String> {
        String level;
        String des;
        String title;

        protected String doInBackground(String... params) {
            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://13.73.108.122/getCreature?Creature_ID="+params[0])
                        .addHeader("cache-control","no-cache")
                        .addHeader("postman-token","55d1aa01-810e-d677-340d-4c17c0f261cd")
                        .get()
                        .build();


                Response response = client.newCall(request).execute();
                final String sResponse=response.body().string();

                String origin = sResponse.substring(sResponse.indexOf('{'),sResponse.indexOf('}')+1);
                final String sResult = origin.replace("\\", "");
                JSONObject result = new JSONObject(sResult);
                level = result.getString("dangerlevel");
                des = result.getString("dangerdescription");
                if (level.equals("5")){
                    title = "! Extreme Dangerous Region !";
                } else if (level.equals("4")){
                    title = "High Dangerous Region";
                } else if (level.equals("3")){
                    title = "Quite Dangerous Region";
                } else {
                    title = "enjoy your journey";
                }

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
            return level;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            dangerlevel.setText(level);
            description.setText(des);
            dangertitle.setText(title);
        }
    }

}