package com.omar.find_out_nearest_covid_patient.Service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.omar.find_out_nearest_covid_patient.Cont;
import com.omar.find_out_nearest_covid_patient.Model.Constants;
import com.omar.find_out_nearest_covid_patient.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    DatabaseReference databaseReference;
    private LocationManager mLocationManager;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                //String address =locationResult.getLastLocation();

                Log.d("UPDETE_Location", "" + latitude + "      " + longitude);
                Toast.makeText(LocationService.this, "Call "+latitude, Toast.LENGTH_SHORT).show();
            }
            for (Location location : locationResult.getLocations()) {
                // Update UI with location data
                // ...
                Geocoder geocoder = new Geocoder(LocationService.this, Locale.getDefault());
                List<Address> addresses;
                ArrayList<String> addressList = new ArrayList<>();

                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Address obj = addresses.get(0);


                    Log.d("Address","  ");

                    String fullAddress = obj.getAddressLine(0);

                    String[] sp = fullAddress.split(",");
                    if(sp.length != 0){

                        for(int i=0;i<sp.length;i++){
                            addressList.add(sp[i]);

                        }
                        try{
                            databaseReference.child("Country").setValue(addressList.get(addressList.size()-1).trim());

                            Log.d("Country","  "+addressList.get(addressList.size()-1).trim());
                            Log.d("City","  "+addressList.get(addressList.size()-2).trim());
                            Log.d("Status","  "+addressList.get(addressList.size()-3).trim());
                            Log.d("Road","  "+addressList.get(addressList.size()-4).trim());
                        }catch (Exception e){}
                        try{
                            databaseReference.child("City").setValue(addressList.get(addressList.size()-2).trim());
                        }catch (Exception e){}
                        try{
                            databaseReference.child("State").setValue(addressList.get(addressList.size()-3).trim());
                        }catch (Exception e){}
                        try{
                            databaseReference.child("Full address").setValue(fullAddress);
                        }catch (Exception e){}
                        try{
                            databaseReference.child("Latitude").setValue(location.getLatitude());
                        }catch (Exception e){}
                        try{
                            databaseReference.child("Longitude").setValue(location.getLongitude());
                        }catch (Exception e){}
                        try{
                            Calendar cal = Calendar.getInstance();
                            DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                            String currentDateandTime = sdf.format(cal.getTime());
                            databaseReference.child("Active time").setValue(currentDateandTime);
                        }catch (Exception e){}
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void startLocationService() {
        /*String channelId = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resenlIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resenlIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (notificationManager != null &&
                    notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is user by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }*/

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        registerReceiver(broadcastReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));


    }
    private void staopLocationService(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        if (intent!=null){
            String action = intent.getAction();
            if (action !=null){
                if (action.equals(Cont.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                }
                else if (action.equals(Cont.ACTION_STOP_LOCATION_SERVICE))
                {
                    staopLocationService();
                }
            }

        }

        if (isLocationEnabled()) {
            databaseReference.child("Location").setValue("On");
        }

        return super.onStartCommand(intent, flags, startId);
    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isLocationEnabled()) {
                databaseReference.child("Location").setValue("On");
            }else if(intent.getAction().matches("android.location.PROVIDERS_CHANGED") && !isLocationEnabled()){
                databaseReference.child("Location").setValue("Off");
            }
        }
    };

    private boolean isLocationEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
