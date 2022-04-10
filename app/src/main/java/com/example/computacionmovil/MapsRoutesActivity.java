package com.example.computacionmovil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MapsRoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Creamos una serie de variables que se extraen directamente desde la pantalla de creación de la ruta
    private String name; //Nombre de la ruta que se esta creando, necesario para al cargar cada ruta saber cual es cada una.
    private int antenna; //TODO de momento se deja al usuario especificar si quiere realizar mediciones en 4G,3G o 2G, pero hay que ver si es correcto o si nos interesa permitir en varias a la vez
    private int interval; //Permitimos al usuario especificar un intervalo de actualizaciones pero por defecto ponemos un intervalo de 10 segundos
    private int selectedSim; //TODO Podemos permitir al usuario seleccionar en que SIM quiere realizar las lecturas si esque tiene más de una SIM

    // Declaramos una serie de TextView para mostrar datos en tiempo real
    private TextView valueNameRoute, valueSelectedAntennas, valueReadAntennas, valueNameStage, valueNameSIM;

    //Creamos una variable para el mapa
    private MapView mMapView;

    //Creamos la variables necesarias para obtener la latitud y longitud de nuestra ubicación actual
    private FusedLocationProviderClient client;
    LocationCallback callback;
    LocationRequest request;
    private Location locationActual;

    //TODO SUPER PROVISIONAL PARA COMPROBAR SI FUNCIONA
    private GoogleMap gM;

    //Valor leido por la antena correspondiente
    private int valueAntenna = 0;

    //Creamos una variable para definir el número de la fase en la que nos encotramos
    private int stage = 1;

    //Creamos una lista para mostrar en tiempo real las medidas
    private ListView listMeasures;
    private ArrayAdapter<String> arrayLecturas;

    //Array de las distintas medidas realizadas
    //TODO De momento esta establecido un límite de 100 mediciones
    Medida[] arrayDeMedidas = new Medida[100];
    private int nMedidas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);

        //Inicializamos el mapa
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

        //Obtenemos los parámetros pasados desde la actividad de creación de ruta
        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            name = getIntent().getStringExtra("name");
            antenna = getIntent().getIntExtra("antennaSelected", 4);
            interval = getIntent().getIntExtra("interval", 10000);
            selectedSim = getIntent().getIntExtra("selectedSim", 1);
        }

        //Establecemos la relación entre las variables y sus respectivas zonas de la interfaz
        valueNameRoute = findViewById(R.id.mapsRoute_Text_ValueName);
        valueSelectedAntennas = findViewById(R.id.mapsRoute_Text_ValueSelectedAntennas);
        valueReadAntennas = findViewById(R.id.mapsRoute_Text_ValueReadAntennas);
        valueNameStage = findViewById(R.id.mapsRoute_Text_ValueNamePhase);
        valueNameSIM = findViewById(R.id.mapsRoute_Text_ValueNameSIMs);

        //Inicializamos la lista que muestra las medidas tomadas en tiempo real
        listMeasures = findViewById(R.id.mapsRoute_Text_listViewMeasure);
        arrayLecturas = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        //Establecemos algunos valores para mostrar al usuario
        valueNameRoute.setText(name);
        valueSelectedAntennas.setText(antenna + "G");
        valueNameStage.setText(String.valueOf(stage));

        //Creamos la solicitudes periodicas de la ubicación cada tiempo especificado por el usuario
        client = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();

        request.setInterval(interval);
        request.setFastestInterval(interval - 2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    locationActual = location;
                }
                //Cada vez que obtenemos una ubicación realizamos una lectura
                //TODO ESTO ES LO QUE DEBEMOS CAMBIAR SI QUEREMOS HACER QUE LAS LECTURAS SE HAGAN CADA X TIEMPO, O CADA VEZ QUE PULSEMOS UN BOTÓN
                //TENDREMOS QUE SACARLO DE AQUÍ O BIEN MODIFICARLO
                valueAntenna = readValueAntenna(selectedSim,antenna);
                //Comprobamos si se produce algun error en la lectura
                if(valueAntenna==0){
                    valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                    valueReadAntennas.setText(R.string.mapsRoute_error_SIM);
                }else if(valueAntenna==-200){
                    valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                    valueReadAntennas.setText(R.string.mapsRoute_error_Antenna);
                }else{
                    //Si va bien, aumentamos el número de lecturas que llevamos, la almacenamos en nuestro array y mostramos la lectura por pantalla en un texto.
                    nMedidas++;
                    valueReadAntennas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                    String newLine = (String) ("Phase " + stage + ", " + antenna + "G: " + valueAntenna + " dbm");
                    //TODO Array de medidas limitado a 100 medidas
                    if(nMedidas<100){
                        arrayDeMedidas[nMedidas] = new Medida(getApplicationContext(), stage,locationActual.getLongitude(),locationActual.getLatitude(),antenna,valueAntenna);
                    }else{
                        valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                        valueReadAntennas.setText(R.string.mapsRoute_error_limitMeasurement);
                    }

                    //Actualizamos la lista de lecturas
                    arrayLecturas.add(newLine);
                    listMeasures.setAdapter(arrayLecturas);
                    valueReadAntennas.setText(newLine);

                    //Establecemos un marcador en la posición desde la que se ha realizado la lectura con la imagen y color correspondiente segun el valor leido y la antena seleccionada
                    Objects.requireNonNull(gM.addMarker(new MarkerOptions().position(new LatLng(locationActual.getLatitude(), locationActual.getLongitude())).title(newLine))).setIcon(Auxiliar.obtenerTipoMarcador(valueAntenna, antenna));
                }
            }
        };

        //Solicitamos el comienzo de las actualizaciones de nuestra ubicación
        requestLocationUpdates();
    }

    private void requestLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            client.requestLocationUpdates(request,callback, null);
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gM = googleMap;
        startShowingLocation();
    }


    private int readValueAntenna(int selectedSim, int antenna) {

        //Declaramos el manager para solicitarle información sobre el servicio de telefonía
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //Comprobamos los permisos necesarios y en caso de no tenerlos, los solicitamos
        //TODO REVISAR POR A VECES HACE COSAS RARAS AL PEDIR LOS PERMISOS LA PRIMERA VEZ
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }else  if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        }

        //Obtenemos la lista de todas las celdas del dispositivo
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

        //Comprobamos que el dispositivo cuente con alguna celda
        if(cellInfos!=null){
            int strength = -200;
            int registeredIndex = 0;
            for (int i = 0; i<cellInfos.size(); i++){
                if (cellInfos.get(i).isRegistered() && selectedSim==(registeredIndex+1)){
                    if(antenna==2 && cellInfos.get(i) instanceof CellInfoGsm){
                        CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = cellSignalStrengthGsm.getDbm();
                        valueNameSIM.setText(cellInfogsm.getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                    }else if(antenna==3 && cellInfos.get(i) instanceof CellInfoWcdma){
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = cellSignalStrengthWcdma.getDbm();
                        valueNameSIM.setText(cellInfoWcdma.getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                    }else if( antenna==4 && cellInfos.get(i) instanceof CellInfoLte){
                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = cellSignalStrengthLte.getDbm();
                        valueNameSIM.setText(cellInfoLte.getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                    }
                    return strength;
                }else if(cellInfos.get(i).isRegistered()){
                    registeredIndex++;
                }
            }
            return strength;
        }
        return 0;
    }

    //Método para mostrar nuestra ubicación actual en el mapa
    private void startShowingLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            gM.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        client.removeLocationUpdates(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    //Funciones de los botones de empezar nueva fase, salir o guardar.

    public void exitRoute(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveRoute(View w){
        //Creamos un array JSON para guardar todas nuestras medicioens
        JsonObject res=new JsonObject();
        JsonArray arrayMedidaJSON=new JsonArray();

        //Añadimos cada medición al array JSON
        for(Medida m : arrayDeMedidas){
            if(m!=null){
                arrayMedidaJSON.add(m.toJson());
            }
        }

        res.add("medida",arrayMedidaJSON);

        //Guardamos el array JSON en un fichero con el nombre pasado como parámetro
        try {
            StorageHelper.saveStringToFile(name + ".json",res.toString(),this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Volvemos a la actividad principal
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void newStage(View w){
        //Iniciamos una nueva etapa sumando uno al número de etapa y mostrando el valor por pantalla
        stage = stage +1;
        valueNameStage.setText(String.valueOf(stage));
    }
}