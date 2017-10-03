package com.example.farseer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class StartActivity extends AppCompatActivity {
    private final int READ_BLOCK_SIZE =100;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private int[] layouts;
    private TextView[] dots;
    private LinearLayout dotsLayout;
    private Button sign_in, sign_up;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Authorized()){
            Toast.makeText(StartActivity.this,"Welcome to Farseer!",Toast.LENGTH_LONG).show();

            Intent i = new Intent(StartActivity.this,NextActivity.class);
            startActivity(i);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //Get view pager
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        // Get dot's layout
        dotsLayout=(LinearLayout) findViewById(R.id.layout_dots);
        // Get sign in and sign up button
        sign_in = (Button)findViewById(R.id.btn_sign_in);
        sign_up = (Button)findViewById(R.id.btn_sign_up);
        // Set up different page for user guide
        layouts = new int[]{R.layout.activity_screen1,R.layout.activity_screen2,R.layout.activity_screen3};
        // Get a ViewPagerAdapter
        viewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(viewListener);

        // Set on click action for sign in button
        // Go to sign in page
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sign_in = new Intent(StartActivity.this,SignInActivity.class);
                startActivity(sign_in);
                finish();
            }
        });
        // Set on click action for sign up button
        // Go to sign up page
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sign_up = new Intent(StartActivity.this,SignUpActivity.class);
                startActivity(sign_up);
                finish();
            }
        });
        // Add User guide dots
        addBottomDots(0);
    }

    // This mathod is used to add user guide dots on the bottom of the screen
    // It creates a list of dots according to the position of the userguide
    private void addBottomDots(int position){
        dots = new TextView[layouts.length];
        // Set the dot of current page to light color
        int[] colorActive = getResources().getIntArray(R.array.dot_active);
        // Others are set to dark color
        int[] colorInActive = getResources().getIntArray(R.array.dot_inactive);
        // Remove the previous dots
        dotsLayout.removeAllViews();
        for(int i = 0; i< dots.length;i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(50);
            dots[i].setTextColor(colorInActive[position]);
            dotsLayout.addView(dots[i]);
        }
        if(dots.length>0){
            dots[position].setTextColor(colorActive[position]);
        }
    }

    ViewPager.OnPageChangeListener viewListener = new  ViewPager.OnPageChangeListener(){
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
//            if(position==layouts.length-1){
//                //////////////////////////
//            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    //    private void changeStatusBarColor(){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }
//    }
    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        @Override
        public int getCount() {
            return layouts.length;
        }

        public Object instantiateItem(ViewGroup container, int position){
            layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = layoutInflater.inflate(layouts[position],container,false);
            container.addView(v);
            return v;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        public void destroyItem(ViewGroup container,int position, Object object){
            View v = (View)object;
            container.removeView(v);
        }
    }

    public boolean Authorized(){
        //reading text from file
        try {
            FileInputStream fileIn=openFileInput("LoginInfo.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;
            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            return !s.equals("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
