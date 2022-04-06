package com.example.computacionmovil;

import android.Manifest;
import android.annotation.SuppressLint;
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

import java.util.List;

public class CreateRouteActivity extends AppCompatActivity {

    private EditText name;
    private TextView nameError;

    private EditText interval;
    private TextView intervalError;

    private CheckBox checkBox2G,checkBox3G,checkBox4G;
    private TextView antennaError;

    private CheckBox sim1,sim2;
    private TextView simError;

    private int antennaSelected = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        name = findViewById(R.id.createRouteTextInputName);
        nameError = findViewById(R.id.createRouteTextInputNameError);

        checkBox2G = findViewById(R.id.createRouteAntenna2GCheck);
        checkBox3G = findViewById(R.id.createRouteAntenna3GCheck);
        checkBox4G = findViewById(R.id.createRouteAntenna4GCheck);

        checkBox2G.setOnClickListener(checkboxListenerAntenna);
        checkBox3G.setOnClickListener(checkboxListenerAntenna);
        checkBox4G.setOnClickListener(checkboxListenerAntenna);

        antennaError = findViewById(R.id.createRouteAntennaCheckError);

        interval = findViewById(R.id.createRouteTextInputInterval);
        intervalError = findViewById(R.id.createRouteTextInputIntervalError);

        sim1 = findViewById(R.id.createRouteCheckBoxSIM1);
        sim2 = findViewById(R.id.createRouteCheckBoxSIM2);

        sim1.setOnClickListener(checkboxListenerSIM);
        sim2.setOnClickListener(checkboxListenerSIM);

        simError = findViewById(R.id.createRouteSimCheckError);

        findViewById(R.id.createRouteButtonCreate);

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
        boolean errorName = false, errorInterval = false, errorSIM = false;

        //Comprobamos si existen errores para el nombre introducido
        if(name.getText().toString().length()==0 || name.getText().toString().length()>100){
            nameError.setText(R.string.createRoute_error_name);
            name.setBackgroundTintList(ColorStateList.valueOf(0xFFFF0000));
            errorName=true;
        }else{
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

        if((!errorName)&&(!errorInterval)&&(errorSIM==false)){
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

    private void setNameSIMs() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)&&(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        }
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //Obtiene información sobre todas las SIMs del teléfono movil

        if(cellInfos!=null){
            int registeredIndex = 0;
            for (int i = 0; i<cellInfos.size(); i++){
                if (cellInfos.get(i).isRegistered()){
                    registeredIndex++;
                    if(registeredIndex==1){
                        //Método solo válido para versiones altas de android
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            sim1.setText(cellInfos.get(i).getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        }
                    }else{
                        //Método solo válido para versiones altas de android
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            sim2.setText(cellInfos.get(i).getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        }
                    }
                }
            }

            if(registeredIndex>1){
                findViewById(R.id.CreateRouteTittleSelectedSIM).setVisibility(View.VISIBLE);

                sim1.setVisibility(View.VISIBLE);

                sim2.setVisibility(View.VISIBLE);
            }
        }
    }

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
