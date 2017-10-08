package com.example.farseer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.farseer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserProfileActivity extends AppCompatActivity {


    private TextView username;
    private TextView mobile;
    private TextView mail;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        username = (TextView)findViewById(R.id.usrname);
        mobile = (TextView)findViewById(R.id.mobile);
        mail = (TextView)findViewById(R.id.maill);
        img = (ImageView)findViewById(R.id.usrimg);

        String useremail = getIntent().getStringExtra("email");

        new UserProfileActivity.getUser().execute("karen@gmail.com");
        Button back = (Button)findViewById(R.id.btn_back1);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this,SignInActivity.class);
                startActivity(i);
                finish();
            }
        });

        Button logout = (Button)findViewById(R.id.btn_logout1);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmptyLoginInfo();
                Intent i = new Intent(UserProfileActivity.this,StartActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    public void EmptyLoginInfo(){
        // Create a new output file stream
        try {
            FileOutputStream fileout = openFileOutput("LoginInfo.txt", Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write("");
            outputWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class getUser extends AsyncTask<String, String, String> {
        String name;
        String number;

        protected String doInBackground(String... params) {
            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://13.73.108.122/getUser?User_Email="+ params[0])
                        .addHeader("cache-control","no-cache")
                        .addHeader("postman-token","55d1aa01-810e-d677-340d-4c17c0f261cd")
                        .get()
                        .build();


                Response response = client.newCall(request).execute();
                final String sResponse=response.body().string();

                String origin = sResponse.substring(sResponse.indexOf('{'),sResponse.indexOf('}')+1);
                final String sResult = origin.replace("\\", "");
                JSONObject result = new JSONObject(sResult);
                name = result.getString("username");
                number = result.getString("usermobile");

            } catch (final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(UserProfileActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            catch (final JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(UserProfileActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
            return name;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            username.setText(name);
            mobile.setText(number);
            mail.setText("karen@gmail.com");
            img.setImageResource(R.drawable.user2);
        }
    }
}
