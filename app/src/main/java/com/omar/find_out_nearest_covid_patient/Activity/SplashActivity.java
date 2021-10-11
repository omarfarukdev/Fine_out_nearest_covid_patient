package com.omar.find_out_nearest_covid_patient.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


public class SplashActivity extends AppCompatActivity {

    private LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING=1001;
    private static final int REQUEST_CODE_LOCATION_PERMISSION=1;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        /*findViewById(R.id.checkBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SplashActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                }
                else {
                    startLocationService();
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                }
            }
        });
*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        auth=FirebaseAuth.getInstance();
        firebaseUser=auth.getCurrentUser();

        if (firebaseUser!=null){
            takeDisitionToStartAC();
        }
        else {
            Intent intent=new Intent(SplashActivity.this,PhoneAuthenticationActivity.class);

            finish();
            startActivity(intent);
        }

    }

    private void takeDisitionToStartAC() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getPhoneNumber());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    int count = (int) snapshot.getChildrenCount();
                    if (!isLocationEnabled()){

                        turnOnLocation();
                    }
                    else if (isLocationEnabled()  && snapshot.hasChild("Full Name") && snapshot.hasChild("NID No") && snapshot.hasChild("BirthDate")) {
                        Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(intent);

                    }
                    else if (isLocationEnabled() && !snapshot.hasChild("Full Name") && !snapshot.hasChild("NID No") && !snapshot.hasChild("BirthDate")){
                        Intent intent=new Intent(SplashActivity.this,CreateAccountActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(intent);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            }
            else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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
    private void turnOnLocation(){

        locationRequest=LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);


        Task<LocationSettingsResponse> result= LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response=task.getResult(ApiException.class);

                    Toast.makeText(SplashActivity.this, "GPS is On", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {


                    switch (e.getStatusCode()){

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {

                                ResolvableApiException resolvableApiException= (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(SplashActivity.this,REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTING){
            Toast.makeText(SplashActivity.this, "Call", Toast.LENGTH_SHORT).show();
            switch (requestCode){


                case Activity.RESULT_OK:
                    Toast.makeText(SplashActivity.this, "GPS is Turned on", Toast.LENGTH_SHORT).show();
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(SplashActivity.this, "GPS is required yo be turned on", Toast.LENGTH_SHORT).show();
                    break;
            }
            onStart();
        }
    }

    private void startLocationService(){
        if (!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Cont.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopLocationService(){
        if (isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Cont.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void startService(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }
        else {
            startLocationService();
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
        }
    }
}