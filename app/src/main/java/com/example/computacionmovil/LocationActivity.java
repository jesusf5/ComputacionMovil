package com.example.computacionmovil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    LocationCallback callback;
    LocationRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        client = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();

        request.setInterval(6000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult){
                super.onLocationResult(locationResult);
                TextView textView=findViewById(R.id.textViewLocation);
                for (Location location:locationResult.getLocations()){
                    double alt=location.getAltitude();
                    double lon=location.getLongitude();
                    textView.setText(alt+" - "+lon+"\n");
                }
            }
        };
    }

    public void click(View v) { requestLocationUpdates(); }

    private void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            client.requestLocationUpdates(request,callback, null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.removeLocationUpdates(callback);
    }

    /*private void requestLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            client.getLastLocation().addOnSuccessListener(this, location -> {

            });
        }
    }
     */
}