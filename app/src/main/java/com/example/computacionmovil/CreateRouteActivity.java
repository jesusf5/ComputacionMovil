package com.example.computacionmovil;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class CreateRouteActivity extends AppCompatActivity {

    //Declaramos los elementos relacionados con el nombre de la ruta
    private EditText name;
    private TextView nameError;

    //Declaramos los elementos relacionados con las antenas de la ruta
    private CheckBox checkBox2G,checkBox3G,checkBox4G; //CheckBox para seleccionar una de las antenas(4G,3G o 2G)
    private TextView antennaError; //Texto para posibles implementaciones de errores en la selección de las antenas
    private int antennaSelected = 4; //Por defecto marcada la antena de 4G

    //Declaramos los elementos relacionados con el intervalo de la ruta
    private EditText interval; //Intervalo de metros entre mediciones
    private TextView intervalError; //TextView para mostrar los errores derivados del intervalo introducido


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        //Inicializamos los elementos que permiten introducir el nombre de la ruta
        name = findViewById(R.id.createRoute_Text_InputName);
        nameError = findViewById(R.id.createRoute_Text_NameError);

        //Inicializamos los checkBox que permiten seleccionar las antenas
        checkBox2G = findViewById(R.id.createRoute_CheckBox_Antenna2G);
        checkBox3G = findViewById(R.id.createRoute_CheckBox_Antenna3G);
        checkBox4G = findViewById(R.id.createRoute_CheckBox_Antenna4G);

        checkBox2G.setOnClickListener(checkboxListenerAntenna);
        checkBox3G.setOnClickListener(checkboxListenerAntenna);
        checkBox4G.setOnClickListener(checkboxListenerAntenna);

        antennaError = findViewById(R.id.createRoute_Text_AntennaError);

        //Inicializamos los valores necesarios para gestionar el intervalo
        interval = findViewById(R.id.createRoute_Text_InputInterval);
        intervalError = findViewById(R.id.createRoute_Text_IntervalError);

    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    public void cancelMainActivity(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void createRoute(View w){
        //Antes de crear la ruta comprobamos que vaya a surgir errores con los valores introducidos
        //Para ello creamos un variable booleana de error para cada uno de los datos introducidos(inicialmente a false todas)
        boolean errorName = false, errorInterval = false, errorSIM = false;

        //Comprobamos si existen errores para el nombre introducido
        if(name.getText().toString().length()==0 || name.getText().toString().length()>100){
            nameError.setText(R.string.createRoute_error_name);
            name.setBackgroundTintList(ColorStateList.valueOf(0xFFFF0000));
            errorName=true;
        }else if(comprobarRepetido(name.getText().toString())){
            nameError.setText(R.string.createRoute_error_nameRepeat);
            name.setBackgroundTintList(ColorStateList.valueOf(0xFFFF0000));
            errorName=true;
        } else{
            nameError.setText("");
            name.setBackgroundTintList(ColorStateList.valueOf(0xFF000000));
        }

        //Comprobamos si existen errores para el intervalo introducido
        int valorIntervalo = (Integer.parseInt(String.valueOf(interval.getText())));
        if( valorIntervalo < 1 || valorIntervalo>100){
            intervalError.setText(R.string.createRoute_error_interval);
            interval.setBackgroundTintList(ColorStateList.valueOf(0xFFFF0000));
            errorInterval=true;
        }else{
            intervalError.setText("");
            interval.setBackgroundTintList(ColorStateList.valueOf(0xFF000000));
        }

        //Comprobamos que no se haya producido ningun error antes de pasar a crear la nueva ruta
        if((!errorName)&&(!errorInterval)&&(!errorSIM)){
            Intent intent=new Intent(this, MapsRoutesActivity.class);

            //Pasamos los parametros seleccionados en la creación de la ruta
            Bundle b = new Bundle();
            b.putString("name", name.getText().toString());
            b.putInt("antennaSelected", antennaSelected);
            b.putInt("interval", valorIntervalo);

            intent.putExtras(b);

            startActivity(intent);
        }
    }

    private boolean comprobarRepetido(String name) {
        //Obtenemos la lista de ficheros en el directorio que almacena los datos de la aplicación y por ende, los ficheros JSON con las mediciones guardadas.
        File[] files = getApplicationContext().getExternalFilesDir("").listFiles();

        //Recorremos todos los fichero y comprobamos si coincide el nombre con alguno de los ya existentes
        assert files != null;
        for(File f : files){
            //Si coincide, devolvemos true indicando que si esta repetido
            if(f.getName().equals(name+".json")){
                return true;
            }
        }
        //Si no coincide con ningun fichero, devolvemos false indicando que no esta repetido
        return false;
    }


    //Para asegurarnos de que solo se selecciona una SIM y una antena, implementamos unos Listener para cada uno de estos valores.
    View.OnClickListener checkboxListenerAntenna = new View.OnClickListener() {
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
