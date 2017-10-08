package com.example.farseer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SignInActivity extends AppCompatActivity {


    private Button sign_in,back;
    private String SignInResult;
    private EditText email;
    private EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password_text);

        sign_in = (Button)findViewById(R.id.sign_in);
        back = (Button)findViewById(R.id.back);

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ip_email = email.getText().toString();
                final String ip_pw = password.getText().toString();


                if(ip_email.equals("")){
                    Warning("Email field cannot be black!");
                }else if (ip_pw.equals("")){
                    Warning("Password field cannot be black!");
                }else{
                    new SignInActivity.SendSignIn().execute(ip_email,ip_pw);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignInActivity.this,StartActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    public void Warning(String message){
        AlertDialog.Builder warn_format = new AlertDialog.Builder(SignInActivity.this);
        warn_format.setMessage(message);
        warn_format.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        warn_format.show();
    }

    private class SendSignIn extends AsyncTask<String, String, String> {
        String paraEmail;
        String paraPw;
        ProgressDialog progressDialog;

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SignInActivity.this);
            progressDialog.setMessage("Please wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            try {

                paraEmail = params[0];
                paraPw = params[1];
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("somParam", "someValue")
                        .build();

                Request request = new Request.Builder()
                        .url("http://13.73.108.122/login?User_Email="+ params[0]+
                                "&User_PSW="+params[1])
                        .method("POST", RequestBody.create(null, new byte[0]))
                        .addHeader("Connection","close")
                        .post(requestBody)
                        .build();


                Response response = client.newCall(request).execute();
                final String sResponse=response.body().string();
//                runOnUiThread(new Runnable(){
//                    @Override
//                    public void run(){
//                        Toast.makeText(SignInActivity.this,sResponse,Toast.LENGTH_LONG).show();
//                    }
//                });
                String origin = sResponse.substring(sResponse.indexOf('{'),sResponse.indexOf('}')+1);
                final String sResult = origin.replace("\\", "");
                JSONObject result = new JSONObject(sResult);
                SignInResult = result.getString("result");

            } catch (final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(SignInActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            catch (final JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(SignInActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
            return  SignInResult;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(result.equals("OK")){
                WriteLoginInfo(paraEmail,paraPw);
                Toast.makeText(SignInActivity.this,"Welcome to Farseer!",Toast.LENGTH_LONG).show();

                Intent i = new Intent(SignInActivity.this,NextActivity.class);
                startActivity(i);
                finish();

            }else if(result.equals("failed, password is incorrect")){
                Warning("Password is incorrect!");
                email.setText("");
                password.setText("");
            }else if(result.equals("failed, user is not exist")){
                Warning("User is not exist!");
                email.setText("");
                password.setText("");
            }else{
                Warning("User already logined!");
                email.setText("");
                password.setText("");
            }
        }
    }

    public void WriteLoginInfo(String email,String password){
        String fileName = "LoginInfo.txt";
        // Create a new output file stream
        try {
            FileOutputStream fileout = openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(email+'\n'+password);
            outputWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
