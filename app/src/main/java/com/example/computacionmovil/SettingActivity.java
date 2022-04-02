package com.example.computacionmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public void saveAndOpenMainActivity(View w){
        openMainActivity(w);
    }

    public void openMainActivity(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}