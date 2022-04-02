package com.example.computacionmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class LoadRouteActivity extends AppCompatActivity {

    //Variable para especificar mediante nombre o id la ruta seleccionada que queremos cargar(Habrá que ver que nos conviene)
    private String name = "Prueba";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_route);
    }

    public void cancelMainActivity(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void loadRoute(View w){
        Intent intent=new Intent(this, MapsActivity.class);

        //Pasamos los parametros seleccionados en la carga de la ruta
        Bundle b = new Bundle();
        b.putString("name", name);
        intent.putExtras(b);

        startActivity(intent);
    }


}
