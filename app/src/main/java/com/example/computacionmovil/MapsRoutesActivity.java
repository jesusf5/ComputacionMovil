package com.example.computacionmovil;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsRoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Creamos una serie de variables que se extraen directamente desde la pantalla de creación de la ruta
    private String name; //Nombre de la ruta que se esta creando, necesario para al cargar cada ruta saber cual es cada una.
    private int antenna; //Se permite al usuario especificar con que antena realizar las mediciones, pero por limitaciones de los propios dispositivos el uso de las antenas deberá ser forzado desde los ajustes del teléfono
    private int intervalMetersMeaures; //Permitimos al usuario especificar un intervalo de actualizaciones pero por defecto ponemos un intervalo de 5 metros

    // Declaramos una serie de TextView para mostrar datos en tiempo real
    private TextView valueNameRoute, valueSelectedAntennas, valueReadAntennas, valueNameStage, valueNameSIM;

    //Creamos una variable para el mapa
    private MapView mMapView;
    private int intervalSecondsMeaures = 5000; //Intervalo de milisegundos entre las peticiones de la ubicación

    //Creamos la variables necesarias para obtener la latitud y longitud de nuestra ubicación actual y para mostrar los mapas
    private FusedLocationProviderClient client;
    LocationCallback callback;
    LocationRequest request;
    private Location locationActual;
    private Location locationAnterior;
    private GoogleMap gM;

    //Valor leido por la lectura de la antena correspondiente (la del movil)
    private int valueAntenna = 0;

    //Creamos una variable para definir el número de la etapa en la que nos encotramos
    private int stage = 1;

    //Creamos una lista para mostrar en tiempo real las medidas
    private ListView listMeasures;
    private ArrayAdapter<String> arrayLecturas;

    //Guardamos la posición de la antena actual y anterior
    private LatLng antenaActual,antenaAnterior;

    //Variable para comprobar si conocemos la antena a la que estamos conectados
    private boolean antenaReady = false;

    //Array de las distintas medidas realizadas
    //Establecemos un límite de 100 mediciones por cada etapa
    // Y un límite de 100 etapas como mucho por recorrido
    Medida[] arrayDeMedidas = new Medida[100];
    Etapa[] arrayDeEtapas = new Etapa[100];

    //Declaramos un array con todas la antenas que se van encontrando
    ArrayList<LatLng> arrayAntenas;


    //Creamos una serie de variables para posteriormente crear una variable de tipo recorrido que almacenar en un fichero
    private int nMedidas = 0;
    private int minMedida=1000;
    private int medMedida=0;
    private int maxMedida=-1000;

    //Creamos una serie de variables para posteriormente crear una o varias variables de tipo etapa que almacenar en un fichero
    private int nMedidasEtapa = 0;
    private int minMedidaEtapa=1000;
    private int medMedidaEtapa=0;
    private int maxMedidaEtapa=-1000;
    private int indexEtapa = 0;

    //Parametros para torre telefonia
    private int lac, cellid = 0;
    private String mcc, mnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);

        //Inicializamos el mapa
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

        //Obtenemos los parámetros pasados desde la actividad de creación de ruta
        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            name = getIntent().getStringExtra("name");
            antenna = getIntent().getIntExtra("antennaSelected", 4);
            intervalMetersMeaures = getIntent().getIntExtra("interval", 10);
        }

        //Establecemos la relación entre las variables y sus respectivas zonas de la interfaz
        valueNameRoute = findViewById(R.id.mapsRoute_Text_ValueName);
        valueSelectedAntennas = findViewById(R.id.mapsRoute_Text_ValueSelectedAntennas);
        valueReadAntennas = findViewById(R.id.mapsRoute_Text_ValueReadAntennas);
        valueNameStage = findViewById(R.id.mapsRoute_Text_ValueNameStage);
        valueNameSIM = findViewById(R.id.mapsRoute_Text_ValueNameSIMs);

        //Inicializamos la lista que muestra las medidas tomadas en tiempo real
        listMeasures = findViewById(R.id.LoadRouteData_Text_listViewStages);
        arrayLecturas = new ArrayAdapter<>(this, R.layout.list_element);

        //Establecemos algunos valores para mostrar al usuario
        valueNameRoute.setText(name);
        valueSelectedAntennas.setText(antenna + "G");
        valueNameStage.setText(String.valueOf(stage));

        //Inicializamos nuestro array de antenas
        arrayAntenas = new ArrayList<>();

        //Creamos la solicitudes periodicas de la ubicación cada tiempo especificado por el usuario
        client = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();

        request.setInterval(intervalSecondsMeaures);
        request.setFastestInterval(intervalSecondsMeaures - 500);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //Recibimos la nueva localización
                for (Location location : locationResult.getLocations()) {
                    //Para la primera ubicación, hacemos un zoom que centre la camara en el usuario
                    if(locationActual==null){
                        gM.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 16));
                    }
                    locationActual = location;
                }

                //Medición inicial para conocer la antena
                readValueAntenna(antenna);

                //Retrofit para enviar a la API una solicitud con los parametros obtenidos
                mostrarTorresTelefonia();

                //Comprobamos que la distancia sea mayor a la especificada o que sea la primera antes de realizar una nueva medición
                if(antenaReady && (locationAnterior==null || locationAnterior.distanceTo(locationActual)>intervalMetersMeaures)){

                    //Obtenemos la distancia entre la medición anterior y la actual
                    float distance = 0;
                    if(locationAnterior!=null){
                        distance = locationAnterior.distanceTo(locationActual);
                    }

                    //Cada vez que obtenemos una ubicación realizamos una serie de lecturas y nos quedamos con la media de estas lecturas
                    valueAntenna=0;
                    for(int l=0; l<10;l++){
                        valueAntenna = valueAntenna + readValueAntenna(antenna);
                    }
                    valueAntenna = (valueAntenna/10);

                    //Comprobamos si se produce algun error en la lectura del valor de la antena
                    if(valueAntenna==0){
                        valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                        valueReadAntennas.setText(R.string.mapsRoute_error_SIM);
                    }else if(valueAntenna==-200){
                        valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                        valueReadAntennas.setText(R.string.mapsRoute_error_Antenna);
                    }else{

                        //Comprobamos si la antena ha variado respecto a la anterior medición
                        if(antenaActual!=null && antenaAnterior!=null && (antenaActual.longitude!=antenaAnterior.longitude || antenaActual.latitude!=antenaAnterior.latitude)){
                            antenaAnterior=antenaActual;
                            arrayAntenas.add(antenaActual);
                        }else if(antenaActual!=null && antenaAnterior==null){
                            antenaAnterior=antenaActual;
                            arrayAntenas.add(antenaActual);
                        }

                        //Si va bien, aumentamos el número de lecturas que llevamos, la almacenamos en nuestro array y mostramos la lectura por pantalla en un texto.
                        nMedidas++;
                        nMedidasEtapa++;
                        valueReadAntennas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                        String newLine = "Nº" + nMedidas + ": " + getApplicationContext().getString(R.string.medida_textDescription1) + " " + stage + getApplicationContext().getString(R.string.medida_textDescription2) + " " +  antenna + "G " + getApplicationContext().getString(R.string.medida_textDescription4) + " " + valueAntenna + " dbm";
                        if(nMedidasEtapa<100){
                            arrayDeMedidas[nMedidasEtapa] = new Medida(getApplicationContext(), stage,locationActual.getLongitude(),locationActual.getLatitude(),antenna,valueAntenna,arrayAntenas.size());

                            //Guardamos los valores que posteriormente utilizaremos para nuestro objeto recorrido
                            if(minMedida>valueAntenna){
                                minMedida=valueAntenna;
                            }
                            if(maxMedida<valueAntenna){
                                maxMedida=valueAntenna;
                            }
                            medMedida=medMedida+valueAntenna;

                            //Guardamos los valores que posteriormente utilizaremos para nuestro objeto etapa
                            if(minMedidaEtapa>valueAntenna){
                                minMedidaEtapa=valueAntenna;
                            }
                            if(maxMedidaEtapa<valueAntenna){
                                maxMedidaEtapa=valueAntenna;
                            }
                            medMedidaEtapa=medMedidaEtapa+valueAntenna;

                            //Actualizamos la lista de lecturas
                            arrayLecturas.add(newLine);
                            listMeasures.setAdapter(arrayLecturas);
                            valueReadAntennas.setText(newLine);

                            //Establecemos un marcador en la posición desde la que se ha realizado la lectura con la imagen y color correspondiente segun el valor leido y la antena seleccionada
                            Objects.requireNonNull(gM.addMarker(new MarkerOptions().position(new LatLng(locationActual.getLatitude(), locationActual.getLongitude())).title(newLine))).setIcon(BitmapDescriptorFactory.defaultMarker(Auxiliar.obtenerTipoMarcador(valueAntenna, antenna)));

                        }else{
                            valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                            valueReadAntennas.setText(R.string.mapsRoute_error_limitMeasurement);
                            newStage();
                        }

                        //Añadimos la distancia a un fichero para tenerla después y mostrarla, a modo de curiosidad
                        String distanciaGuardada="0";
                        try {
                            distanciaGuardada=StorageHelper.readStringFromFile(getString(R.string.fileDistances),getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            float distValue = Float.parseFloat(distanciaGuardada)+distance;
                            StorageHelper.saveStringToFile(getString(R.string.fileDistances),String.valueOf(distValue),getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Añadimos una línea entre los dos puntos para conocer el recorrido que hemos hecho, y entre la antena y la medición para saber a que antena estamos conectados
                        if(locationAnterior!=null && nMedidas<100){
                            Polyline line = gM.addPolyline(new PolylineOptions()
                                    .add(new LatLng(locationAnterior.getLatitude(), locationAnterior.getLongitude()), new LatLng(locationActual.getLatitude(), locationActual.getLongitude()))
                                    .width(5)
                                    .color(Auxiliar.getColorStage(stage)));
                        }
                        if(antenaActual!=null && antenna==4){
                            Polyline lineAntenna = gM.addPolyline(new PolylineOptions()
                                    .add(new LatLng(antenaActual.latitude, antenaActual.longitude), new LatLng(locationActual.getLatitude(), locationActual.getLongitude()))
                                    .width(2)
                                    .color(Color.BLACK));
                        }
                        if(nMedidas<100){
                            locationAnterior = locationActual;
                        }
                    }
                }//En caso de no alcanzar la distancia especificada, esperamos a la siguiente ubicación
                else if(!antenaReady){
                    valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                    valueReadAntennas.setText(R.string.mapsRoute_error_readyAntena);
                } else {
                    valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                    valueReadAntennas.setText(R.string.mapsRoute_error_intervalDistance);
                }

            }
        };

        //Solicitamos el comienzo de las actualizaciones de nuestra ubicación
        requestLocationUpdates();

        //Establecemos que mientras que estemos en esta actividad, no se pueda apagar la pantalla
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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


    private int readValueAntenna(int antenna) {

        //Declaramos el manager para solicitarle información sobre el servicio de telefonía
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //Comprobamos los permisos necesarios y en caso de no tenerlos, los solicitamos
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
            for (int i = 0; i<cellInfos.size(); i++){
                if (cellInfos.get(i).isRegistered()){
                    if(antenna==2 && cellInfos.get(i) instanceof CellInfoGsm){
                        CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = cellSignalStrengthGsm.getDbm();
                        valueNameSIM.setText(cellInfogsm.getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        mcc = cellInfogsm.getCellIdentity().getMccString();
                        mnc = cellInfogsm.getCellIdentity().getMncString();
                        lac = cellInfogsm.getCellIdentity().getLac();
                        cellid = cellInfogsm.getCellIdentity().getCid();
                    }else if(antenna==3 && cellInfos.get(i) instanceof CellInfoWcdma){
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = cellSignalStrengthWcdma.getDbm();
                        valueNameSIM.setText(cellInfoWcdma.getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        mcc = cellInfoWcdma.getCellIdentity().getMccString();
                        mnc = cellInfoWcdma.getCellIdentity().getMncString();
                        lac = cellInfoWcdma.getCellIdentity().getLac();
                        cellid = cellInfoWcdma.getCellIdentity().getCid();
                    }else if( antenna==4 && cellInfos.get(i) instanceof CellInfoLte){
                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = cellSignalStrengthLte.getDbm();
                        valueNameSIM.setText(cellInfoLte.getCellIdentity().getOperatorAlphaShort().toString().toUpperCase());
                        mcc = cellInfoLte.getCellIdentity().getMccString();
                        mnc = cellInfoLte.getCellIdentity().getMncString();
                        lac = cellInfoLte.getCellIdentity().getTac();
                        cellid = cellInfoLte.getCellIdentity().getCi();
                    }
                    return strength;
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

    private void mostrarTorresTelefonia() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.mylnikov.org/geolocation/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CellsPosition service = retrofit.create(CellsPosition.class);

        Call<CellsPositionRes> call = service.listLocation(1.1, "open", Integer.parseInt(mcc), Integer.parseInt(mnc), lac, cellid);

        call.enqueue(new Callback<CellsPositionRes>() {
            @Override
            public void onResponse(@NonNull Call<CellsPositionRes> call, @NonNull Response<CellsPositionRes> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "Error" + response.code());
                } else {
                    antenaReady=true;
                    CellsPositionRes cellsPositions = response.body();
                    assert cellsPositions != null;
                    Log.d("Resultado latitud: ", String.valueOf(cellsPositions.getData().getLat()));
                    Log.d("Resultado longitud: ", String.valueOf(cellsPositions.getData().getLon()));

                    LatLng location = new LatLng(cellsPositions.getData().getLat(), cellsPositions.getData().getLon());
                    Objects.requireNonNull(gM.addMarker(new MarkerOptions().position(location).title(getApplicationContext().getString(R.string.mapsRoute_text_Tower)))).setIcon(Auxiliar.getBitmapDescriptor(getApplicationContext(),R.drawable.antena));
                    antenaActual = location;
                }
            }

            @Override
            public void onFailure(@NonNull Call<CellsPositionRes> call, @NonNull Throwable t) {
                Log.e("error", t.toString());
            }
        });
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
        //Declaramos la clase Main a la que volveremos
        Intent intent = new Intent(this, MainActivity.class);

        //La primera medida se guarda en la posición 1, así pues, comprobamos si existe alguna medida guardada antes de proceder a guardar el recorrido en un fichero
        if(arrayDeMedidas[1] != null) {
            //Comprobamos que esta etapa no haya sido guardada con antrioridad y la guardamos
            if (arrayDeEtapas[stage] == null) {
                if (nMedidasEtapa > 0) {
                    Medida[] arrayDePaso = new Medida[nMedidasEtapa];
                    int i = 0;
                    for (Medida m : arrayDeMedidas) {
                        if (m != null && m.getEtapa() == stage) {
                            arrayDePaso[i] = m;
                            i++;
                        }
                    }
                    arrayDeEtapas[stage] = new Etapa(getApplicationContext(), stage, minMedidaEtapa, (medMedidaEtapa / nMedidasEtapa), maxMedidaEtapa, nMedidasEtapa, arrayDePaso);
                    arrayDeMedidas = new Medida[100];
                }
            }
            Recorrido r = new Recorrido(getApplicationContext(), minMedida, (medMedida / nMedidas), maxMedida, nMedidas, arrayAntenas, arrayDeEtapas);
            //Creamos un array JSON para guardar todas nuestras medicioens
            JsonObject res = new JsonObject();

            res.add("recorrido", r.toJson());

            //Guardamos el array JSON en un fichero con el nombre pasado como parámetro
            try {
                StorageHelper.saveStringToFile(name + ".json", res.toString(), this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //En caso de que no tengamos mediciones en la etapa actual y estemos en una etapa posterior a la primera, sabemos que hemos llegado hasta ese punto porque en la primera etapa, al menos, se guardo una medición, por lo que simplemente guardamos todas las etapas anteriores en el recorrido.
        else if(stage>1){
            Recorrido r = new Recorrido(getApplicationContext(), minMedida, (medMedida / nMedidas), maxMedida, nMedidas, arrayAntenas, arrayDeEtapas);
            //Creamos un array JSON para guardar todas nuestras medicioens
            JsonObject res = new JsonObject();

            res.add("recorrido", r.toJson());

            //Guardamos el array JSON en un fichero con el nombre pasado como parámetro
            try {
                StorageHelper.saveStringToFile(name + ".json", res.toString(), this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            //En caso de no haber medidas guardads notificamos al usuario de que no se ha guardado nada
            //Pasamos el parámetro que indica un error en la lectura de un fichero y notificamos al usuario del problema
            Bundle b = new Bundle();
            b.putString("error", getApplicationContext().getString(R.string.main_text_errorSaveRecorr));
            intent.putExtras(b);
        }
        //Volvemos a la actividad principal
        startActivity(intent);
    }
    private void newStage(){
        //Guardamos los valores para la etapa actual
        if(nMedidasEtapa>0 && stage<99){
            Medida[] arrayDePaso = new Medida[nMedidasEtapa];
            int i = 0;
            for(Medida m : arrayDeMedidas){
                if(m!=null && m.getEtapa()==stage){
                    arrayDePaso[i] = m;
                    i++;
                }
            }

            arrayDeEtapas[stage]=new Etapa(getApplicationContext(),stage,minMedidaEtapa,(medMedidaEtapa/nMedidasEtapa),maxMedidaEtapa,nMedidasEtapa, arrayDePaso);
            arrayDeMedidas = new Medida[100];

            //Reiniciamos las variables de la etapa
            nMedidasEtapa = 0;
            minMedidaEtapa=1000;
            medMedidaEtapa=0;
            maxMedidaEtapa=-1000;
            indexEtapa = nMedidas;

            //Iniciamos una nueva etapa sumando uno al número de etapa y mostrando el valor por pantalla
            stage = stage +1;
            valueNameStage.setText(String.valueOf(stage));
        }else if(stage<99){
            valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
            valueReadAntennas.setText(R.string.mapsRoute_error_Stage);
        }else{
            valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
            valueReadAntennas.setText(R.string.mapsRoute_error_MaximunStage);
        }
    }

    public void newStageButton(View w){
        newStage();
    }

}