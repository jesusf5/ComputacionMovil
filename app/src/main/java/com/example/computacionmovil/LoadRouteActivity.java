package com.example.computacionmovil;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LoadRouteActivity extends AppCompatActivity {
    //Texto para mostrar posibles errores
    private TextView errorText;

    //Variable para especificar mediante nombre o id la ruta seleccionada que queremos cargar
    private String selectedName = "";
    private int selectedIndex = -1;

    //Variables para mostrar la lista de las distintas rutas que podemos cargar
    private ArrayList<String> arrayRoutesNames;
    private ArrayAdapter<String> arrayRoutes;
    private ListView listRoutes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_route);
        //Inicializamos el texto para mostrar errores
        errorText = findViewById(R.id.loadRoute_ErrorText);

        //Inicializamos lista con las medidas que podemos cargar
        listRoutes = findViewById(R.id.loadRoute_ListRoutes);
        listRoutes.setClickable(true);
        //Establecemos un listener para poder seleccionar los distintos elementos de la lista
        listRoutes.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            listRoutes.setSelector(R.drawable.selecteditem);
            listRoutes.setSelection(position);
            listRoutes.setSelected(true);
            selectedName = arrayRoutesNames.get(position);
            selectedIndex=position;
        });
        //Establecemos las distintas rutas que se pueden cargar en la lista mostrada en la activity
        setRoutes();
    }

    private void setRoutes() {

        //Obtenemos la lista de ficheros en el directorio que almacena los datos de la aplicación y por ende, los ficheros JSON con las mediciones guardadas.
        File[] files = getApplicationContext().getExternalFilesDir("").listFiles();

        //Reiniciamos la lista de medidas guardadas y despues añadimos y mostramos la lista actualizada con los ficheros disponibles
        arrayRoutes = new ArrayAdapter<>(this, R.layout.list_element);
        arrayRoutesNames = new ArrayList<>();
        assert files != null;
        for(File f : files){
            if(!f.getName().equals(getApplicationContext().getString(R.string.fileDistances))){
                Recorrido r;
                try {
                    r = StorageHelper.readRecorridoFromFile(f.getName(),getApplicationContext());
                    arrayRoutes.add(f.getName()+"(" + getApplicationContext().getString(R.string.loadRoute_text_min) + r.getMinMedida() + getApplicationContext().getString(R.string.loadRoute_text_med) + r.getMedMedida() + getApplicationContext().getString(R.string.loadRoute_text_max) + r.getMaxMedida() + getApplicationContext().getString(R.string.loadRoute_text_nmeasures) + r.getnMedidas() + ")");
                    arrayRoutesNames.add(f.getName());
                } catch (NullPointerException e) {
                    Log.i(TAG, "El fichero " + f.getName() + " no ha podido ser cargado correctamente");

                    //Declaramos la clase main para salir
                    Intent intent=new Intent(this, MainActivity.class);
                    //Pasamos el parámetro que indica un error en la lectura de un fichero y notificamos al usuario del problema
                    Bundle b = new Bundle();
                    b.putString("error", getApplicationContext().getString(R.string.main_text_errorLecturaFichero));
                    intent.putExtras(b);
                    //Salimos e iniciamos la activity del Main
                    startActivity(intent);
                }catch (IOException e){
                    e.printStackTrace();
                    Intent intent=new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }
        listRoutes.setAdapter(arrayRoutes);

    }

    public void exitLoadRoute(View w){
        //Salimos e iniciamos la activity del Main
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void deleteLoadRoute(View w){
        //Comprobamos que realmente haya algun elemento seleccionado
        if(selectedIndex!=-1 && !selectedName.equals("") && listRoutes.isSelected()){
            //Eliminamos el elemento seleccionado del array de valores mostrado en la lista de rutas
            arrayRoutes.remove(selectedName);
            arrayRoutesNames.remove(selectedIndex);
            //Eliminamos el fichero que almacena los datos de la ruta seleccionada
            StorageHelper.eliminarFichero(selectedName, getApplicationContext());
            Snackbar.make(findViewById(android.R.id.content), selectedName + " " + getApplicationContext().getString(R.string.loadRoute_text_deleted), BaseTransientBottomBar.LENGTH_LONG).show();
            //Establecemos como ningun elemento seleccionado
            selectedName ="";
            selectedIndex = -1;
            listRoutes.setSelected(false);
            errorText.setText("");
            listRoutes.setSelector(R.drawable.transparente);
            setRoutes();
        }else{
            //En caso de no haber elemento seleccionado lo comunicamos
            errorText.setText(R.string.loadRoute_text_errorDelete);
        }
    }

    public void loadRoute(View w){
        //Comprobamos si hay algun elemento seleccionado
        if(!selectedName.equals("")){
            //Selecionamos la actividad a cargar
            Intent intent=new Intent(this, MapsLoadRouteActivity.class);

            //Pasamos los parametros seleccionados en la carga de la ruta(En nuestro caso solo el nombre del fichero)
            Bundle b = new Bundle();
            b.putString("name", selectedName);
            intent.putExtras(b);

            //Iniciamos la nueva actividad
            startActivity(intent);
        }else{
            //En caso de no haber elemento seleccionado lo comunicamos
            errorText.setText(R.string.loadRoute_text_errorLoad);
        }
    }


}
