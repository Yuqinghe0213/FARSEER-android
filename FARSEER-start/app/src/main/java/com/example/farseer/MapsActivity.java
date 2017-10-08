package com.example.farseer;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
This class is use to implement the main features of this app.
The features includes
1. Search the latitude and longitude by address via Google map API.
2. Get near by creatures by sending current location to server.
3.Get report context from server based on current location
4.Search for the nearby safe area, which mean there is no or little creatures in this area.
5 Write a report to server about the creature the user find.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    EditText tf_location;

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Location searchLocation= new Location("search location");
    private Marker currentLocationMarker;
    private Marker searchMarker;
    private static final int REQUEST_LOCATION_CODE = 99;
    private static boolean currentRequest = true;
    private String userEmail;

    // record the data get from server
    private List<HashMap<String, String>> nearcreatures = new ArrayList<HashMap<String, String>>();
    private List<HashMap<String, String>> reportcreatures = new ArrayList<HashMap<String, String>>();
    private List<HashMap<String, String>> safetyarea = new ArrayList<HashMap<String, String>>();
    private List<HashMap<String,String>> reportcontext = new ArrayList<HashMap<String, String>>();
    private ArrayList<Marker> reportMarkers = new ArrayList<Marker>();
    private ArrayList<Marker> creatureMarkers = new ArrayList<Marker>();
    private ArrayList<Marker> safeMarkers = new ArrayList<Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        tf_location = (EditText) findViewById(R.id.location);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Load the information of current user.
        Authorized();

    }


    // write the permission configurattion
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);

                    }
                    else {
                        Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // Build the information window to show the basic information about marker
        // and gei the detailed information by click the information window.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                // when the clicked marker is a creature marker.
                if (creatureMarkers.contains(marker)){
                    View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                    ImageView image = (ImageView) v.findViewById(R.id.image);
                    TextView title = (TextView) v.findViewById(R.id.title);
                    TextView dis = (TextView) v.findViewById(R.id.distance);

                    //Calculate the distace between clicked marker and your location
                    double disbetween = distance(marker.getPosition().latitude, marker.getPosition().longitude,
                            currentLocationMarker.getPosition().latitude,
                            currentLocationMarker.getPosition().longitude) * 1000;
                    title.setText("Species: " + marker.getTitle());
                    dis.setText("Distance: " + String.valueOf(disbetween));
                    String name = marker.getTitle()+"c";
                    int resId = getResources().getIdentifier(name, "drawable", getPackageName());
                    image.setImageResource(resId);
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        public void onInfoWindowClick(Marker marker) {

                            // Jump to the creatures' profile
                            String creatureid = marker.getSnippet();
                            Log.d("CreatureId","creature"+ creatureid);
                            Intent newactivity = new Intent(MapsActivity.this,NextActivity.class);
                            newactivity.putExtra("creatureId", creatureid);
                            startActivity(newactivity);
                            //finish()

                        }
                    });
                    // Returning the view containing InfoWindow contentsreturn v;
                    return v;
                }

                // when the clicked marker is a report marker
                else if (reportMarkers.contains(marker)){
                    View v = getLayoutInflater().inflate(R.layout.info_report_layout, null);
                    TextView dis2 = (TextView) v.findViewById(R.id.distance2);
                    double disbetween = distance(marker.getPosition().latitude, marker.getPosition().longitude,
                            currentLocationMarker.getPosition().latitude,
                            currentLocationMarker.getPosition().longitude) * 1000;
                    dis2.setText("Distance of your location is: " + String.valueOf(disbetween));

                    // show all report in this location
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        public void onInfoWindowClick(Marker marker) {
                            Datatransfer datatransfer = new Datatransfer();
                            datatransfer.execute(String.valueOf(5), String.valueOf(marker.getPosition().latitude),
                                    String.valueOf(marker.getPosition().longitude));
                        }
                    });
                    return v;
                }

                // when click the human marker.
                else if( marker.equals(currentLocationMarker))
                {
                    View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                    ImageView image = (ImageView) v.findViewById(R.id.image);
                    TextView title = (TextView) v.findViewById(R.id.title);
                    TextView dis = (TextView) v.findViewById(R.id.distance);

                    double disbetween = distance(marker.getPosition().latitude, marker.getPosition().longitude,
                            currentLocationMarker.getPosition().latitude,
                            currentLocationMarker.getPosition().longitude) * 1000;
                    title.setText("Name: " + marker.getTitle());
                    dis.setText("Distance: " + String.valueOf(disbetween));
                    int resId = getResources().getIdentifier(marker.getTitle(), "drawable", getPackageName());
                    image.setImageResource(resId);

                    // show the detail of the user
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        public void onInfoWindowClick(Marker marker) {

                            // Jump to user profile
                            Intent newactivity = new Intent(MapsActivity.this,UserProfileActivity.class);
                            newactivity.putExtra("email", userEmail);
                            startActivity(newactivity);
                            //finish();
                        }
                    });
                    // Returning the view containing InfoWindow contentsreturn v;
                    return v;
                }
                else {
                    View v = getLayoutInflater().inflate(R.layout.info_report_layout, null);
                    TextView dis2 = (TextView) v.findViewById(R.id.distance2);
                    double disbetween = distance(marker.getPosition().latitude, marker.getPosition().longitude,
                            currentLocationMarker.getPosition().latitude,
                            currentLocationMarker.getPosition().longitude) * 1000;
                    dis2.setText("Distance of your location is: " + String.valueOf(disbetween));
                    return v;

                }
            }
        });
    }

    // Build client for google API
    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    //trace the change of user's location and update user location marker
    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions =  new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("human");
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("human", 135, 135)));
        currentLocationMarker = mMap.addMarker(markerOptions);
        if(currentRequest) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            searchLocation = lastLocation;
        }
        //mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
        if(client == null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    // connect to google API
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        // The interval between two request is 100ms
        //This interval is uncertained, it depends on the speed of network and mobile
        locationRequest.setInterval(100);
        //The shorted interval betwwen two request is 20ms
        locationRequest.setFastestInterval(20);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }


    }

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
            return false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Resize the size of all icon.
    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    // when the search button is clicked, it will send a request to google API based on the input address
    public void onSearchClick(View v) {

        String location = tf_location.getText().toString().replace(" ", "+");
        if(searchMarker!=null) {
            searchMarker.remove();
        }
        if (reportMarkers.size() != 0) {
            for (Marker m : reportMarkers) {
                // m.setVisible(false);
                m.remove();
            }
            reportMarkers.clear();
            reportcreatures.clear();
        }
        if (creatureMarkers.size() != 0) {
            for (Marker m : creatureMarkers) {
                // m.setVisible(false);
                m.remove();
            }
            creatureMarkers.clear();
            nearcreatures.clear();
        }
        if (safeMarkers.size() != 0) {
            for (Marker m : safeMarkers) {
                // m.setVisible(false);
                m.remove();
            }
            safeMarkers.clear();
            safetyarea.clear();
        }
        Log.d("InputData ", "address =  "+ location);
        if (!location.equals("")) {
            Datatransfer datatransfer = new Datatransfer();
            datatransfer.execute(String.valueOf(4), location);
            currentRequest = false;
        }
        tf_location.setText("");
    }

    // When the Menu button is clicked, it will show a feature list
    public void onMenuClick(View v) {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        View mview = getLayoutInflater().inflate(R.layout.menu_dialog, null);
        mBuilder.setView(mview);
        final AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();

        // If user not search any address, it will return the creatures near by users' current
        //location.However, if user search for a address, farseer will show the creatures near
        // //the serch point.
        Button mcreature = (Button) mview.findViewById(R.id.B_creatures);
        mcreature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Ask for data from server
                Datatransfer datatransfer = new Datatransfer();
                datatransfer.execute(String.valueOf(2), String.valueOf(searchLocation.getLatitude()),
                        String.valueOf(searchLocation.getLongitude()));
                alertDialog.cancel();
            }
        });


        //There are a lot of report data in the database. The report data is all from users and record
        //when and where a user see a special creatures. One loction may include multiple reports.
        //Each user can not report same specise in the same location at one day.
        Button mreport = (Button) mview.findViewById(R.id.B_report);
        mreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Datatransfer datatransfer = new Datatransfer();
                datatransfer.execute(String.valueOf(1), String.valueOf(searchLocation.getLatitude()),
                        String.valueOf(searchLocation.getLongitude()));
                alertDialog.cancel();
            }
        });

        // The server will geive uer a safe area where has not or little creatures.
        Button marea = (Button) mview.findViewById(R.id.B_safety);
        marea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Datatransfer datatransfer = new Datatransfer();
                datatransfer.execute(String.valueOf(3), String.valueOf(searchLocation.getLatitude()),
                        String.valueOf(searchLocation.getLongitude()));
                alertDialog.cancel();
            }
        });

        //User can write a report to server about which creature they finf in certain are.
        Button mwrite = (Button) mview.findViewById(R.id.B_reportCreature);
        mwrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder nBuilder = new AlertDialog.Builder(MapsActivity.this);
                View nView = getLayoutInflater().inflate(R.layout.rtoserver,null);
                final Spinner nspinner = (Spinner)nView.findViewById(R.id.spinner);
                final EditText description = (EditText)nView.findViewById(R.id.editText);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.speciesList));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                nspinner.setAdapter(adapter);
                nBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // choose the species of the creature.
                        if(!nspinner.getSelectedItem().toString().equalsIgnoreCase("Choose a species.....")
                                && !description.getText().toString().isEmpty())
                        {
                            String str = description.getText().toString();
                            Datatransfer datatransfer = new Datatransfer();
                            datatransfer.execute(String.valueOf(6), String.valueOf(lastLocation.getLatitude()),
                                    String.valueOf(lastLocation.getLongitude()), str, nspinner.getSelectedItem().toString());
                            dialogInterface.dismiss();
                        }
                        else
                        {
                            Toast.makeText(MapsActivity.this, "Please fill all blanks", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                nBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                });
                nBuilder.setView(nView);
                AlertDialog dialog = nBuilder.create();
                dialog.show();

                alertDialog.cancel();
            }
        });


    }

   //load the information of user, its identity is email.
    public boolean Authorized(){
        //reading text from file
        String s="";
        try {
            FileInputStream fileIn=openFileInput("LoginInfo.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[1000000];

            int charRead;
            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            String[] nstr = s.split("\n");
            userEmail = nstr[0];
            InputRead.close();
            return !s.equals("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Calculate the distance between two location beased on their latitudes and longitude.

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    //Send the request to server and get the result from server.

    public class Datatransfer extends AsyncTask<String, String, String> {

        //   private  ProgressDialog progressDialog = new ProgressDialog(MapsActivity.);
        private int  type;
        private String location_X;
        private String location_Y;
        private String l_result;
        //Parser data, read the json object to a list.
        private DataParser parser = new DataParser();

        protected String doInBackground(String... params) {

            // Send data to server
            type = Integer.parseInt(params[0]);
            switch (type)
            {
                // check the nearby report based on search loction or user's location
                case 1: {
                    location_X = params[1];
                    location_Y = params[2];
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Log.d("DataFromServer ", "locationX = "+ location_X + " locationY" + location_Y);

                        Request request = new Request.Builder()
                                .url("http://13.73.108.122/getReportLocation?User_LocationX="+location_X+"&User_LocationY="+location_Y)
                                .get()
                                .addHeader("cache-control", "no-cache")
                                .addHeader("postman-token", "2c1a5cab-862d-2b0c-5f07-f92160fb0ee6")
                                .build();

                        Response response = client.newCall(request).execute();
                        final String sResponse = response.body().string();
                        String origin = sResponse.substring(sResponse.indexOf('{'), sResponse.indexOf('}') + 1);
                        final String sResult = origin.replace("\\", "");
                        Log.d("DataFromServer ", "Data = "+ sResult);
                        JSONObject result = new JSONObject(sResult);
                        reportcreatures = parser.getList(2,result);
                        l_result = result.getString("result");
                    } catch (final IOException e) {
                        e.printStackTrace();

                    }

                    catch (final JSONException e) {
                        e.printStackTrace();

                    }
                    break;
                }
                case 2:{
                    // check nearby creatures based on search loction or user's location
                    location_X = params[1];
                    location_Y = params[2];
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Log.d("DataFromServer ", "locationX = "+ location_X + " locationY" + location_Y);

                        Request request = new Request.Builder()
                                .url("http://13.73.108.122/getCreatureList?User_LocationX="+location_X+"&User_LocationY="+location_Y)
                                .get()
                                .addHeader("cache-control", "no-cache")
                                .addHeader("postman-token", "2c1a5cab-862d-2b0c-5f07-f92160fb0ee6")
                                .build();

                        Response response = client.newCall(request).execute();
                        final String sResponse = response.body().string();
                        if(sResponse.equals(null))
                        {
                            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                            alertDialog.setTitle("Alert");
                            alertDialog.setMessage("There is not Creatures near");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        }
                        String origin = sResponse.substring(sResponse.indexOf('{'), sResponse.indexOf('}') + 1);
                        final String sResult = origin.replace("\\", "");
                        JSONObject result = new JSONObject(sResult);
                        nearcreatures = parser.getList(1,result);

                        Log.d("DataFromServer ", "Data = "+ nearcreatures.toString());
                        l_result = result.getString("result");
                    } catch (final IOException e) {
                        e.printStackTrace();

                    }

                    catch (final JSONException e) {
                        e.printStackTrace();

                    }
                    break;
                }
                case 3:{
                    //search the safe area
                    location_X = params[1];
                    location_Y = params[2];
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Log.d("DataFromServer ", "locationX = "+ location_X + " locationY" + location_Y);

                        Request request = new Request.Builder()
                                .url("http://13.73.108.122/getSafeArea?User_LocationX="+location_X+"&User_LocationY="+location_Y)
                                .get()
                                .addHeader("cache-control", "no-cache")
                                .addHeader("postman-token", "2c1a5cab-862d-2b0c-5f07-f92160fb0ee6")
                                .build();

                        Response response = client.newCall(request).execute();
                        final String sResponse = response.body().string();
                        String origin = sResponse.substring(sResponse.indexOf('{'), sResponse.indexOf('}') + 1);
                        final String sResult = origin.replace("\\", "");
                        JSONObject result = new JSONObject(sResult);
                        safetyarea = parser.getList(4,result);
                        Log.d("DataFromServer ", "Data = "+ safetyarea.toString());

                        l_result = result.getString("result");
                    } catch (final IOException e) {
                        e.printStackTrace();

                    }

                    catch (final JSONException e) {
                        e.printStackTrace();

                    }
                    break;
                }
                case 4:{
                    // Encode Address to latitude and longitude using google map API
                    String address = params[1];
                    GooglePlaces http = new GooglePlaces();
                    String url = getUrl(address);
                    Log.d("ADDRESS", "ADDRESS = "+address);
                    l_result = http.getHTTPData(url);
                    Log.d("MapsActivity", "RESULT = "+l_result);

                    break;

                }
                case 5:
                {
                    // Get report context for a certain location.
                    location_X = params[1];
                    location_Y = params[2];
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Log.d("DataFromServer ", "locationX = "+ location_X + " locationY" + location_Y);

                        Request request = new Request.Builder()
                                .url("http://13.73.108.122/getReport?LocationX="+location_X+"&LocationY="+location_Y)
                                .get()
                                .addHeader("cache-control", "no-cache")
                                .addHeader("postman-token", "2c1a5cab-862d-2b0c-5f07-f92160fb0ee6")
                                .build();

                        Response response = client.newCall(request).execute();
                        final String sResponse = response.body().string();
                        if(sResponse.equals(null))
                        {
                            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                            alertDialog.setTitle("Alert");
                            alertDialog.setMessage("There is not report near");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        }
                        String origin = sResponse.substring(sResponse.indexOf('{'), sResponse.indexOf('}') + 1);
                        final String sResult = origin.replace("\\", "");
                        Log.d("DataFromServer ", "Data = "+ sResult);
                        JSONObject result = new JSONObject(sResult);
                        reportcontext = parser.getList(3,result);
                        l_result = result.getString("result");
                    } catch (final IOException e) {
                        e.printStackTrace();

                    }

                    catch (final JSONException e) {
                        e.printStackTrace();

                    }
                    break;

                }

                case 6:
                {
                    // writer the report and post it to server.
                    location_X = params[1];
                    location_Y = params[2];
                    String description = params[3];
                    String species = params[4];
                    String date = getCurrentDate();
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("someParam", "someValue").build();
                    Log.d("MainActivity", "Url = "+ "http://13.73.108.122/report?User_Email="+ userEmail +"&User_LocationX="+location_X+
                            "&User_LocationY="+location_Y+"&User_Description="+ description +
                            "&Input_Specises="+species+"&Create_Date="+ date);

                    Request request = new Request.Builder()
                            .url("http://13.73.108.122/report?User_Email="+ "karen@gmail.com" +"&User_LocationX="+location_X+
                                    "&User_LocationY="+location_Y+"&User_Description="+ description +
                                    "&Input_Specises="+species+"&Create_Date="+ date)
                            .method("POST",RequestBody.create(null, new byte[0]))
                            .post(requestBody)
                            .addHeader("Connetion", "close")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        final String sResponse = response.body().string();
                        Log.d("DataFromServer ", "Data = "+ sResponse);
                        String origin = sResponse.substring(sResponse.indexOf('{'), sResponse.indexOf('}') + 1);
                        final String sResult = origin.replace("\\", "");
                        JSONObject result = new JSONObject(sResult);
                        l_result = result.getString("result");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                break;
            }

            return  l_result;
        }

        //show the result in the map
        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            switch (type)
            {
                case 1:
                {//server return a list of report location, add marker for each creatures.
                    // remove anther markers
                    if (creatureMarkers.size() != 0) {
                        for (Marker m : creatureMarkers) {
                            // m.setVisible(false);
                            m.remove();
                        }
                        creatureMarkers.clear();
                        nearcreatures.clear();
                    }
                    //remove other marker on the Map
                    if (safeMarkers.size() != 0) {
                        for (Marker m : safeMarkers) {
                            // m.setVisible(false);
                            m.remove();
                        }
                        safeMarkers.clear();
                        safetyarea.clear();
                    }
                    //show the marker for each report location
                    if (reportcreatures.size() != 0) {
                        for (int i = 0; i < reportcreatures.size(); i++) {
                            HashMap<String, String> report = reportcreatures.get(i);
                            Double lat = Double.parseDouble(report.get("lat"));
                            Double lng = Double.parseDouble(report.get("long"));
                            LatLng latLng = new LatLng(lat, lng);
                            reportMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("reporticon", 135, 135)))));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.2f));
                        }
                    }
                    break;

                }
                case 2:
                {//server return a list of creature location
                    //remove other useless markers
                    if (reportMarkers.size() != 0) {
                        for (Marker m : reportMarkers) {
                            // m.setVisible(false);
                            m.remove();
                        }
                        reportMarkers.clear();
                        reportcreatures.clear();
                    }
                    if (safeMarkers.size() != 0) {
                        for (Marker m : safeMarkers) {
                            // m.setVisible(false);
                            m.remove();
                        }
                        safeMarkers.clear();
                        safetyarea.clear();
                    }
                    //Add the data to creatures list and show marker for each creatures in each location.
                    if (nearcreatures.size() != 0) {
                        for (int i = 0; i < nearcreatures.size(); i++) {
                            HashMap<String, String> creature = nearcreatures.get(i);
                            Double lat = Double.parseDouble(creature.get("lat"));
                            Double lng = Double.parseDouble(creature.get("long"));
                            LatLng latLng = new LatLng(lat, lng);
                            String species = creature.get("species");
                            String id = creature.get("id");

                            String name = species+"c";
                            creatureMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(species).snippet(id)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(name, 135, 135)))));
                        }
                    }
                    break;

                }
                case 3:
                {//Show safe area location
                    if (result.equals("you are in a safe area"))
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                        alertDialog.setTitle("Message");
                        alertDialog.setMessage("You are in a safe area. Enjoy your safe journey!");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    else {
                        if (reportMarkers.size() != 0) {
                            for (Marker m : reportMarkers) {
                                // m.setVisible(false);
                                m.remove();
                            }
                            reportMarkers.clear();
                            reportcreatures.clear();
                        }
                        if (safetyarea.size() != 0) {
                            for (int i = 0; i < safetyarea.size(); i++) {
                                Log.d("MapsActivity", "safety = " + safetyarea.size());
                                HashMap<String, String> creature = safetyarea.get(i);
                                Double lat = Double.parseDouble(creature.get("lat"));
                                Double lng = Double.parseDouble(creature.get("long"));
                                LatLng latLng = new LatLng(lat, lng);
                                safeMarkers.add(mMap.addMarker(new MarkerOptions().position(latLng).title("This is a safety area")
                                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("safety", 135, 135)))));
                            }
                        }
                        else {
                            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                            alertDialog.setTitle("Alert");
                            alertDialog.setMessage("You are surrounding by many dangerous animals, Farseer suggests you to be careful!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        }
                    }

                    break;

                }
                case 4:
                {// google return a json including all information about the address inputted. Here,we just
                    //need to find the the "location" item the get the latitude and longitude of the address.
                    try {
                        Log.d("POSTACTIVITY", "RESULT = "+l_result);
                        JSONObject jsonObject = new JSONObject(result);
                        //get latitude
                        location_X = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                                .getJSONObject("location").get("lat").toString();
                        //get longitude
                        location_Y = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                                .getJSONObject("location").get("lng").toString();
                        Log.d("MARKER LOCATION", "X = "+location_X + "Y=" + location_Y);
                        LatLng latLng = new LatLng(Double.parseDouble(location_X), Double.parseDouble(location_Y));
                        searchLocation.setLatitude(Double.parseDouble(location_X));
                        searchLocation.setLongitude(Double.parseDouble(location_Y));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("I want go there");
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("search", 135, 135)));
                        searchMarker = mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                }
                case 5:{//show all report for a specific location

                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    LayoutInflater inflater = (LayoutInflater) MapsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View row = inflater.inflate(R.layout.row_item,null);
                    ListView lv = (ListView)row.findViewById(R.id.list_item);
                    lv.setAdapter(new ReportAdapter(MapsActivity.this, reportcontext));
                    builder.setView(row);
                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
                }
                case 6:
                {
                    //if the report which is written by user adding in database successfully, server will
                    //reture ok
                    if(result.equals("OK"))
                    {
                        Toast.makeText(getApplicationContext(), "Report add sucessfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "You have reported it today", Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
            }
        }

        //write the url which is used to request data from google API, the key need to apply in advance
        private String getUrl(String address)
        {

            StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?");
            googlePlaceUrl.append("address="+address);;
            googlePlaceUrl.append("&key="+"AIzaSyCP0uI98UTfqQJcF9M93XTfyun1lFheT4k");

            Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

            return googlePlaceUrl.toString();
        }

        //Get current data from system
        public String getCurrentDate() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy - MM - dd ");
            String strDate = mdformat.format(calendar.getTime());

            return strDate;
        }



    }

}
