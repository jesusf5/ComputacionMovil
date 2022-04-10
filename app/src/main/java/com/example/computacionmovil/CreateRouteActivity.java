package com.example.computacionmovil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.List;

public class CreateRouteActivity extends AppCompatActivity {

    //Declaramos los elementos relacionados con el nombre de la ruta
    private EditText name;
    private TextView nameError;

    //Declaramos los elementos relacionados con las antenas de la ruta
    private CheckBox checkBox2G,checkBox3G,checkBox4G;
    private TextView antennaError; //TODO QUITAR, PROVISIONAMENTE, POR SI IMPLEMENTAMOS ALGUN ERROR PARA LAS ANTENAS
    private int antennaSelected = 4; //Por defecto marcada la antena de 4G

    //Declaramos los elementos relacionados con el intervalo de la ruta
    private EditText interval; //TODO REVISAR COMO VAMOS A IMPLEMENTAR EL TEMA DE LOS INTERVALOS FINALMENTE, POR DISTANCIA?, TIEMPO?, BOTÓN?
    private TextView intervalError;

    //Declaramos los elementos relacionados con las sims de la ruta
    private CheckBox sim1,sim2;
    private TextView simError;


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

        //Inicializamos los valores necesarios para introducir las SIMs
        sim1 = findViewById(R.id.createRoute_CheckBox_SIM1);
        sim2 = findViewById(R.id.createRoute_CheckBox_SIM2);

        sim1.setOnClickListener(checkboxListenerSIM);
        sim2.setOnClickListener(checkboxListenerSIM);

        simError = findViewById(R.id.createRoute_Text_SimError);

        //Establecemos las sims que deben aparecer como seleccionables en función de las disponibles en el dispositivo
        setNameSIMs();

    }

    @Override
    protected void onResume(){
        super.onResume();
        setNameSIMs();
    }

    @Override
    protected void onStart(){
        super.onStart();
        setNameSIMs();
    }

    public void cancelMainActivity(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void createRoute(View w){
        //TODO No voy a implementar un error para las antenas pero queda hecho a medida por si queremos implementarlo

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

        //TODO En este momento no vamos a comprobar si existen errores para las antenas

        //Comprobamos si existen errores para el intervalo introducido
        int valorIntervalo = (Integer.parseInt(String.valueOf(interval.getText())))*1000;
        if( valorIntervalo <= 4999 || valorIntervalo>30000){
            intervalError.setText(R.string.createRoute_error_interval);
            interval.setBackgroundTintList(ColorStateList.valueOf(0xFFFF0000));
            errorInterval=true;
        }else{
            intervalError.setText("");
            interval.setBackgroundTintList(ColorStateList.valueOf(0xFF000000));
        }

        //TODO Con la nueva filosofía de solo leer de una tarjeta no tiene mucho sentido

        // Comprobamos si existen errores para la/las SIMs seleccionadas
        if( !sim1.isChecked() && !sim2.isChecked() ){
            simError.setText(R.string.createRoute_error_SIM);
            errorSIM=true;
        }else{
            simError.setText("");
        }

        //Comprobamos que no se haya producido ningun error antes de pasar a crear la nueva ruta
        if((!errorName)&&(!errorInterval)&&(!errorSIM)){
            Intent intent=new Intent(this, MapsRoutesActivity.class);

            //Pasamos los parametros seleccionados en la creación de la ruta
            Bundle b = new Bundle();
            b.putString("name", name.getText().toString());
            b.putInt("antennaSelected", antennaSelected);
            b.putInt("interval", valorIntervalo);

            int selectedSim = 0;
            if(sim1.isChecked()){
                selectedSim=1;
            }else if(sim2.isChecked()){
                selectedSim =2;
            }
            b.putInt("selectedSim", selectedSim);
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

    //TODO REVISAR, FUNCIONA MAL
    private void setNameSIMs() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //Comprobamos si tenemos acceso a todos los permisos necesarios
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        }


        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //Obtiene información sobre todas las SIMs del teléfono movil


        if(cellInfos!=null){
            int registeredIndex = 0;

            //Si el sistema operativo es ANDROID 11 o superior, comprobamos si existe la posibilidad e MULTI SIM permitimos seleccionar entre las distintas SIMs disponibles.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && telephonyManager.getActiveModemCount() == 2) {
                for (int i = 0; i<cellInfos.size(); i++){
                    if (cellInfos.get(i).isRegistered()){
                        //Comprobamos la cantidad de targetas SIM registradas
                        registeredIndex++;
                        if(registeredIndex==1){
                            //Método solo válido para versiones altas de android
                            sim1.setText(cellInfos.get(i).getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        }else{
                            //Método solo válido para versiones altas de android
                            sim2.setText(cellInfos.get(i).getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        }
                    }
                }
                //Si se han encontrado varias SIM registradas, las mostramos por pantalla para permitir la selección
                if(registeredIndex>1) {
                    findViewById(R.id.CreateRoute_Tittle_SelectedSIM).setVisibility(View.VISIBLE);
                    sim1.setVisibility(View.VISIBLE);
                    sim2.setVisibility(View.VISIBLE);
                }
            }
            //En caso de que no sea ANDROID 11 o superior, o bien si hemos detectado que no hay MULTI SIM
            //Nos quedamos con la SIM identificada en la primera posición(La única o la del primer SLOT)
        }
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

    View.OnClickListener checkboxListenerSIM = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == sim1){
                sim2.setChecked(false);
                sim1.setChecked(true);
            } else   if (v == sim2){
                sim2.setChecked(true);
                sim1.setChecked(false);
            }
        }
    };


}
