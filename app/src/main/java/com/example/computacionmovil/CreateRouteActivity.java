package com.example.computacionmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class CreateRouteActivity extends AppCompatActivity {

    private EditText name;

    private CheckBox checkBox2G,checkBox3G,checkBox4G;

    private int antennaSelected = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        checkBox2G = findViewById(R.id.createRouteAntenna2GCheck);
        checkBox3G = findViewById(R.id.createRouteAntenna3GCheck);
        checkBox4G = findViewById(R.id.createRouteAntenna4GCheck);

        checkBox2G.setOnClickListener(checkboxListener);
        checkBox3G.setOnClickListener(checkboxListener);
        checkBox4G.setOnClickListener(checkboxListener);

        name = findViewById(R.id.createRouteTextInputName);

        findViewById(R.id.createRouteButtonCreate);

    }

    public void cancelMainActivity(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void createRoute(View w){
        Intent intent=new Intent(this, MapsRoutesActivity.class);

        //Pasamos los parametros seleccionados en la creación de la ruta
        Bundle b = new Bundle();
        b.putString("name", name.getText().toString());
        b.putInt("antennaSelected", antennaSelected);
        intent.putExtras(b);

        startActivity(intent);


    }

    View.OnClickListener checkboxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == checkBox4G){
                checkBox2G.setChecked(false);
                checkBox3G.setChecked(false);
                checkBox4G.setChecked(true);
                antennaSelected=4;
            } else   if (v == checkBox3G){
                checkBox4G.setChecked(false);
                checkBox3G.setChecked(true);
                checkBox2G.setChecked(false);
                antennaSelected=3;
            } else   if (v == checkBox2G){
                checkBox4G.setChecked(false);
                checkBox3G.setChecked(false);
                checkBox2G.setChecked(true);
                antennaSelected=2;
            }
        }
    };

}
