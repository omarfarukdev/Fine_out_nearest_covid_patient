package com.omar.find_out_nearest_covid_patient.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.omar.find_out_nearest_covid_patient.Cont;
import com.omar.find_out_nearest_covid_patient.R;
import com.omar.find_out_nearest_covid_patient.Service.LocationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING=1001;
    private static final int REQUEST_CODE_LOCATION_PERMISSION=1;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference,dataReference;
    private FirebaseAuth mAuth;
    boolean isPermissionGranted;
    GoogleMap mGoogleMap;
    private FusedLocationProviderClient mLocationClient;
    Intent intentThatCalled;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;
    Location mylocation;
    LatLng latLng;
    double distance;
    DatePickerDialog.OnDateSetListener mDateListener;
    String covidtestDat;
    TextView covidtestDate;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    TextView textView;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();
        //fab=findViewById(R.id.fab);
        //checkMyPermission();
        //initMap();
        sharedpreferences=getSharedPreferences("Date", Context.MODE_PRIVATE);
        editor=sharedpreferences.edit();

        SupportMapFragment supportMapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        supportMapFragment.getMapAsync(MainActivity.this);

        mLocationClient=new FusedLocationProviderClient(this);
        mylocation = new Location("locationA");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /*if (notificationManager != null &&
                    notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is user by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }*/


            NotificationChannel channel=new NotificationChannel("My Notification","My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        /*if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }
        else {
            startLocationService();
            //startActivity(new Intent(MainActivity.this,MainActivity.class));
        }*/

        //startService();

        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //notification();
            }
        });*/

        mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker , int year , int month , int day) {
                month = month + 1;


                covidtestDat=(new StringBuilder().append( day ).append( "-" )
                        .append( month ).append( "-" ).append( year )).toString();

                Log.d( "onDateSet" , month + "/" + day + "/" + year +"  " +covidtestDat);
                covidtestDate.setText( covidtestDat );
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
       /* Intent intent1=new Intent(MainActivity.this, LocationService.class);
        startService(intent1);*/
        startServiceOn();
    }
    private void startLocationService(){
        if (!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Cont.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    private void getCurrentLoca() {

        mLocationClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                Location location=task.getResult();
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                //gotoLocation(latitude,longitude);
            }
        });

       /* LocationCallback locationCallback=new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null && locationResult.getLastLocation() != null){
                     latitude = locationResult.getLastLocation().getLatitude();
                     longitude = locationResult.getLastLocation().getLongitude();
                    gotoLocation(latitude,longitude);
                }

            }
        };*/



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;

    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option){

            mAuth.signOut();
            SendUserToLoginActivity();
        }
        else if (item.getItemId() == R.id.covid_status){
            /* if (!sharedpreferences.getString("First",null).equals("true") || !sharedpreferences.getString("First",null).equals("false")){
                 editor.putString("First","test");
                 editor.apply();
             }*/

            Dialog dialog=new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_covid_status);
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            //dialog.setTitle("Added Your Covid Status");
            dialog.show();
            Spinner covid_status_sp=dialog.findViewById(R.id.covidstatus);
            Spinner covid_test_Sp=dialog.findViewById(R.id.covidteststatus);
            Button add_covid_status_bt=dialog.findViewById(R.id.add_covid_status);
            covidtestDate=dialog.findViewById(R.id.covidtestDate);


            covidtestDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar calendar=Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
                    String currentdate= format.format(calendar.getTime());
                    //birthdateTv.setText(currentdate);

                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dialog =  new DatePickerDialog(
                            MainActivity.this,
                            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                            mDateListener,
                            year,month,day
                    );
                    dialog.setTitle("Set Date");
                    dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                    dialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );

                    dialog.show();
                }
            });
            //covidtestDate.setText(sharedpreferences.getString("First",null));
            add_covid_status_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataReference= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getPhoneNumber());

                    //Toast.makeText(MainActivity.this, ""+sharedpreferences.getString("First",null), Toast.LENGTH_SHORT).show();

                    if (covid_test_Sp.getSelectedItem().toString().equals("First")) {
                        dataReference.child("Covid_Status").setValue(covid_status_sp.getSelectedItem().toString());
                        dataReference.child("First_test_date").setValue(covidtestDat);
                         /*editor.putString("First","true");
                         editor.apply();*/
                        Toast.makeText(MainActivity.this, "Added First Test Result", Toast.LENGTH_SHORT).show();
                    }
                    else if (covid_test_Sp.getSelectedItem().toString().equals("Second"))
                    {
                        dataReference.child("Covid_Status").setValue(covid_status_sp.getSelectedItem().toString());
                        dataReference.child("Second_test_date").setValue(covidtestDat);
                        /* editor.putString("First","false");
                         editor.apply();*/
                        Toast.makeText(MainActivity.this, "Added Second Test Result", Toast.LENGTH_SHORT).show();
                    }
                    else if (covid_test_Sp.getSelectedItem().toString().equals("Third")) {
                        dataReference.child("Covid_Status").setValue(covid_status_sp.getSelectedItem().toString());
                        dataReference.child("Third_test_date").setValue(covidtestDat);
                         /*editor.putString("First","true");
                         editor.apply();*/
                        Toast.makeText(MainActivity.this, "Added First Test Result", Toast.LENGTH_SHORT).show();
                    }
                    else if (covid_test_Sp.getSelectedItem().toString().equals("Fourth")) {
                        dataReference.child("Covid_Status").setValue(covid_status_sp.getSelectedItem().toString());
                        dataReference.child("Fourth_test_date").setValue(covidtestDat);
                         /*editor.putString("First","true");
                         editor.apply();*/
                        Toast.makeText(MainActivity.this, "Added First Test Result", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                }
            });



        }
        else if (item.getItemId() == R.id.profile){

            Intent intent=new Intent(MainActivity.this,ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        return true;
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent=new Intent(MainActivity.this, PhoneAuthenticationActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private boolean isLocationServiceRunning(){

        ActivityManager activityManager=(ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager !=null){
            for (ActivityManager.RunningServiceInfo service:
                    activityManager.getRunningServices(Integer.MAX_VALUE)){
                if (LocationService.class.getName().equals(service.service.getClassName())){
                    if (service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
    private void startServiceOn(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }
        else {
            startLocationService();
            //startActivity(new Intent(SplashActivity.this,MainActivity.class));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mGoogleMap=googleMap;
        // mGoogleMap.setMyLocationEnabled(true);
       /* if (mGoogleMap != null) {

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }*/
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                Location location=task.getResult();
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                latLng = new LatLng(latitude,longitude);

                mylocation.setLatitude(latitude);
                mylocation.setLongitude(longitude);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng).title("").snippet("")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(false).visible(true)).showInfoWindow();

            }
        });

      /* LatLng sydney = new LatLng(23.7958396, 90.38451);
       mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,18));*/

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                    Location dest_location = new Location("locationA");

                    dest_location.setLatitude(Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString()));
                    dest_location.setLongitude(Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString()));

                    LatLng latLn = new LatLng(Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString()));
                    distance = mylocation.distanceTo(dest_location);
                    Log.d("Dis",dataSnapshot.getKey().toString()+" ooo "+distance/1000);

                    if (!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {

                        if (distance<=100){
                           /* mGoogleMap.addMarker(new MarkerOptions()
                                    .position(latLn).title("").snippet("")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                    .draggable(false).visible(true)).showInfoWindow();*/
                            if (dataSnapshot.child("Covid_Status").exists()){
                                if (dataSnapshot.child("Covid_Status").getValue().toString().trim().equals("Positive"))
                                {
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .position(latLn).title("")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.covidpositive))
                                            .draggable(false).visible(true)).showInfoWindow();
                                    notification();
                                }
                                else {
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .position(latLn).title("")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.covidnegative60))
                                            .draggable(false).visible(true)).showInfoWindow();
                                }
                            }
                            else {
                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(latLn).title("")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.covidnegative60))
                                        .draggable(false).visible(true)).showInfoWindow();
                            }


                        }
                        else {
                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(latLn).title("")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.alluser60))
                                    .draggable(false).visible(true)).showInfoWindow();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void notification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "My Notification")
                .setSmallIcon(R.drawable.ic_baseline_dangerous_24)
                .setContentTitle("Covid Patient")
                .setContentText("There is Covid Patient Nearby")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);

        notificationManager.notify(1, builder.build());
    }


    private void gotoLocation(double latitude, double longitude) {

        latLng = new LatLng(latitude,longitude);
        mylocation.setLatitude(latitude);
        mylocation.setLongitude(longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng).title("")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(false).visible(true)).showInfoWindow();
       /* LatLng latLng=new LatLng(latitude,longitude);

        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng,15);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationService();
    }
    private void stopLocationService(){
        if (isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Cont.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }
}