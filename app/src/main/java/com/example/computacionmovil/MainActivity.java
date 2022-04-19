package com.example.computacionmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long nFicheros = Arrays.stream(Objects.requireNonNull(getApplicationContext().getExternalFilesDir("").listFiles())).count();
        if(nFicheros>0){
            ((TextView)findViewById(R.id.main_Text_ValueTotalMeasurement)).setText(String.valueOf(nFicheros-1));
        }else{
            ((TextView)findViewById(R.id.main_Text_ValueTotalMeasurement)).setText(String.valueOf(0));
        }


        String distanceReader = "0";
        try {
            distanceReader = StorageHelper.readStringFromFile(getString(R.string.fileDistances),getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((TextView)findViewById(R.id.main_Text_ValueTotalMeters)).setText(distanceReader);
    }

    public void openLocationActivity(View w){
        Intent intent=new Intent(this, LocationActivity.class);
        startActivity(intent);
    }

    public void openMapsActivity(View w){
        Intent intent=new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void openCreateRouteActivity(View w){
        Intent intent=new Intent(this, CreateRouteActivity.class);
        startActivity(intent);
    }

    public void openLoadRouteActivity(View w){
        Intent intent=new Intent(this, LoadRouteActivity.class);
        startActivity(intent);
    }
}