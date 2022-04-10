package com.example.computacionmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView)findViewById(R.id.main_Text_ValueTotalMeasurement)).setText(String.valueOf(Arrays.stream(Objects.requireNonNull(getApplicationContext().getExternalFilesDir("").listFiles())).count()));
    }

    public void openLocationActivity(View w){
        Intent intent=new Intent(this, LocationActivity.class);
        startActivity(intent);
    }

    public void openMapsActivity(View w){
        Intent intent=new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void openSettingActivity(View w){
        Intent intent=new Intent(this, SettingActivity.class);
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