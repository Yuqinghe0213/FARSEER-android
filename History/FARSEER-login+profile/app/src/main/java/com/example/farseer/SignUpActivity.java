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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Request;

public class SignUpActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private EditText email;
    private EditText phone;
    private Button sign_up,sign_up_back;
    private String SignUpResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //Get sign up an back button
        sign_up = (Button)findViewById(R.id.sign_up);
        sign_up_back = (Button)findViewById(R.id.sign_up_back);
        SignUpResult=null;

        //Get EditTexts
        username = (EditText)findViewById(R.id.sign_up_username);
        password = (EditText)findViewById(R.id.sign_up_password);
        email = (EditText)findViewById(R.id.sign_up_email);
        phone = (EditText)findViewById(R.id.sign_up_phone);

        // Set action for clicking sign up button
        // Send information to database
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_username = username.getText().toString();
                String ip_pw = password.getText().toString();
                String ip_email = email.getText().toString();
                String ip_phone = phone.getText().toString();
                // Check whether the input of each field is valid
                if(!CheckBlank(ip_username,ip_pw,ip_email,ip_phone)&&!Username_invalid(ip_username)
                        &&!Password_invalid(ip_pw)&&!Email_invalid(ip_email)&&!Phone_invalid(ip_phone)){
                    new SendSignUp().execute(ip_username,ip_pw,ip_email,ip_phone);
                }
            }
        });

        sign_up_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUpActivity.this,StartActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    public boolean CheckBlank(String username,String password,String email,String phone){
        boolean is_blank = true;
        //Check blank
        if(username.equals("")){
            Warn_blank("Username");
        }else if(password.equals("")){
            Warn_blank("Password");
        }else if(email.equals("")){
            Warn_blank("Email");
        }else if(phone.equals("")){
            Warn_blank("Phone");
        }else{
            is_blank = false;
        }
        return is_blank;
    }

    public void Warn_blank(String field){
        AlertDialog.Builder warning = new AlertDialog.Builder(SignUpActivity.this);
        warning.setMessage(field+" cannot be blank");
        warning.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        warning.show();
    }
    // Return whether the username is valid
    public boolean Username_invalid(String username){
        // Check username format
        boolean is_invalid = true;

        String not_contain = "[^a-zA-Z'_'0-9]";
        String contain_reg = "[a-z0-9A-Z]";
        Pattern nt_ct  = Pattern.compile(not_contain);
        Pattern ct = Pattern.compile(contain_reg);
        Matcher match_nt_ct = nt_ct.matcher(username);
        Matcher match_ct =ct.matcher(username);
        if(match_nt_ct.lookingAt()){
            Warning("Username can only contain letter, number and underscore");
        }else if(!match_ct.lookingAt()){
            Warning("Username must contain at least one number or letter");

        }else{
            is_invalid = false;
        }
        return is_invalid;
    }

    public boolean Password_invalid(String password){
        boolean is_invalid = true;
        String not_contain = "[^a-zA-Z0-9]";
        Pattern nt_ct  = Pattern.compile(not_contain);
        Matcher match_nt_ct = nt_ct.matcher(password);
        if(password.toCharArray().length < 6){
            Warning("Please set your password at a minimum of 6 characters");
        }else if (match_nt_ct.lookingAt()){
            Warning("Password can only contain letter or number");
        }else{
            is_invalid = false;
        }
        return is_invalid;
    }

    public boolean Email_invalid(String email){
        boolean is_invalid = true;
        String reg = "[a-zA-Z0-9'_'\\-]+@[A-Za-z0-9]+(\\.[a-zA-Z]{2,3}){1,2}";
        Pattern email_pt = Pattern.compile(reg);
        Matcher email_mt = email_pt.matcher(email);
        if(!email_mt.matches()){
            Warning("Please input valid email account");
        }else{
            is_invalid=false;
        }
        return  is_invalid;
    }

    public boolean Phone_invalid(String phone){
        boolean is_invalid = true;
        String reg = "[0-9\\-]*";
        Pattern phone_pt = Pattern.compile(reg);
        Matcher phone_mt = phone_pt.matcher(phone);
        if(!phone_mt.matches()||phone.toCharArray().length<10){
            Warning("Please input valid phone number");
        }else{
            is_invalid=false;
        }
        return  is_invalid;
    }

    public void Warning(String message){
        AlertDialog.Builder warn_format = new AlertDialog.Builder(SignUpActivity.this);
        warn_format.setMessage(message);
        warn_format.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        warn_format.show();
    }


    private class SendSignUp extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String signUp_email;
        private String signUp_ps;
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SignUpActivity.this);
            progressDialog.setMessage("Please wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("somParam", "someValue")
                        .build();

                signUp_email=params[2];
                signUp_ps = params[1];
                Request request = new Request.Builder()
                        .url("http://13.73.108.122/register?User_Name="+ params[0]+
                                "&User_PSW="+params[1]+"&User_Email="+params[2]
                                +"&User_Mobile="+params[3])
                        .method("POST", RequestBody.create(null, new byte[0]))
                        .addHeader("Connection","close")
                        .post(requestBody)
                        .build();


                Response response = client.newCall(request).execute();
                final String sResponse=response.body().string();
                String origin = sResponse.substring(sResponse.indexOf('{'),sResponse.indexOf('}')+1);
                final String sResult = origin.replace("\\", "");
                JSONObject result = new JSONObject(sResult);
                SignUpResult = result.getString("result");

            } catch (final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){

                    @Override
                    public void run(){
                        Toast.makeText(SignUpActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

            }

            catch (final JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(SignUpActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
            return  SignUpResult;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(result.equals("OK")){
                WriteLoginInfo(signUp_email,signUp_ps);
                Toast.makeText(SignUpActivity.this,"Welcome to Farseer!",Toast.LENGTH_LONG).show();
                Intent i = new Intent(SignUpActivity.this,NextActivity.class);
                startActivity(i);
                finish();
            }else{
                Warning("Email already exist!");
                username.setText("");
                password.setText("");
                email.setText("");
                phone.setText("");
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

