package com.example.computacionmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*public void openSecondActivity(View w){
        long clickTime=System.currentTimeMillis();
        Intent intent=new Intent(this, SecondActivity.class);
        intent.putExtra("clickTime", clickTime);
        startActivity(intent);
    }
    */


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