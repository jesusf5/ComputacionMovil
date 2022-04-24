package com.example.computacionmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtenemos los parámetros pasados desde la actividad de creación de ruta
        String error="";
        Bundle extras = getIntent().getExtras();
        if (extras!=null && !extras.isEmpty() && extras.containsKey("error")) {
            error = getIntent().getStringExtra("error");
            Snackbar.make(findViewById(android.R.id.content), error, BaseTransientBottomBar.LENGTH_LONG).show();
        }

        File[] files = getApplicationContext().getExternalFilesDir("").listFiles();
        String distances = "";

        assert files!=null;
        for(File f : files){
            //Buscamos si el fichero que almacena las distancias totales recorridas con la aplicación abierta sigue existiendo y leemos su valor si existe
            if(f!=null && f.getName().equals(getApplicationContext().getString(R.string.fileDistances))){
                try {
                    distances = StorageHelper.readStringFromFile(getString(R.string.fileDistances),getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }//Si el fichero existe y se trata de un error en la apertura del load route, buscamos aquellos ficheros corruptos o que no se pueden abrir
            else if(f!=null && error.equals(getApplicationContext().getString(R.string.main_text_errorLecturaFichero))){
                    try {
                        //Si el fichero retorna null es porque no ha podido ser leido de manera correcta, por lo que lo eliminamos
                        if(StorageHelper.readRecorridoFromFile(f.getName(),getApplicationContext())==null){
                            StorageHelper.eliminarFichero(f.getName(),getApplicationContext());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }

        //Obtenemos el número de ficheros existentes en la carpeta con todos los recorridos guardados
        long nFicheros = Arrays.stream(Objects.requireNonNull(getApplicationContext().getExternalFilesDir("").listFiles())).count();

        //Si el fichero distances existe, restamos 1 al total de los ficheros presentes
        if(!distances.equals("")){
            ((TextView)findViewById(R.id.main_Text_ValueTotalMeasurement)).setText(String.valueOf(nFicheros-1));
        }else{
            ((TextView)findViewById(R.id.main_Text_ValueTotalMeasurement)).setText(String.valueOf(nFicheros));
        }

        //Establecemos los valores del texto de las distancias
        ((TextView)findViewById(R.id.main_Text_ValueTotalMeters)).setText(distances);
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