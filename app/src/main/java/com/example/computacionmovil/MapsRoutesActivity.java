package com.example.computacionmovil;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Arrays;
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
    private int antenna; //TODO de momento se deja al usuario especificar si quiere realizar mediciones en 4G,3G o 2G, pero hay que ver si es correcto o si nos interesa permitir en varias a la vez
    private int intervalMetersMeaures; //Permitimos al usuario especificar un intervalo de actualizaciones pero por defecto ponemos un intervalo de 10 segundos
    private int selectedSim; //TODO Podemos permitir al usuario seleccionar en que SIM quiere realizar las lecturas si esque tiene más de una SIM

    // Declaramos una serie de TextView para mostrar datos en tiempo real
    private TextView valueNameRoute, valueSelectedAntennas, valueReadAntennas, valueNameStage, valueNameSIM;

    //Creamos una variable para el mapa
    private MapView mMapView;
    private int intervalSecondsMeaures = 5000; //Intervalo de milisegundos entre las peticiones de la ubicación

    //Creamos la variables necesarias para obtener la latitud y longitud de nuestra ubicación actual
    private FusedLocationProviderClient client;
    LocationCallback callback;
    LocationRequest request;
    private Location locationActual;
    private Location locationAnterior;

    //TODO SUPER PROVISIONAL PARA COMPROBAR SI FUNCIONA
    private GoogleMap gM;

    //Valor leido por la antena correspondiente
    private int valueAntenna = 0;

    //Creamos una variable para definir el número de la etapa en la que nos encotramos
    private int stage = 1;

    //Creamos una lista para mostrar en tiempo real las medidas
    private ListView listMeasures;
    private ArrayAdapter<String> arrayLecturas;

    //Array de las distintas medidas realizadas
    //TODO De momento esta establecido un límite de 100 mediciones y 1 phase por medición como mucho
    Medida[] arrayDeMedidas = new Medida[100];
    Etapa[] arrayDeEtapas = new Etapa[100];


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
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

        //Obtenemos los parámetros pasados desde la actividad de creación de ruta
        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            name = getIntent().getStringExtra("name");
            antenna = getIntent().getIntExtra("antennaSelected", 4);
            intervalMetersMeaures = getIntent().getIntExtra("interval", 10);
            selectedSim = getIntent().getIntExtra("selectedSim", 1);
        }

        //Establecemos la relación entre las variables y sus respectivas zonas de la interfaz
        valueNameRoute = findViewById(R.id.mapsRoute_Text_ValueName);
        valueSelectedAntennas = findViewById(R.id.mapsRoute_Text_ValueSelectedAntennas);
        valueReadAntennas = findViewById(R.id.mapsRoute_Text_ValueReadAntennas);
        valueNameStage = findViewById(R.id.mapsRoute_Text_ValueNameStage);
        valueNameSIM = findViewById(R.id.mapsRoute_Text_ValueNameSIMs);

        //Inicializamos la lista que muestra las medidas tomadas en tiempo real
        listMeasures = findViewById(R.id.LoadRouteData_Text_listViewStages);
        arrayLecturas = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        //Establecemos algunos valores para mostrar al usuario
        valueNameRoute.setText(name);
        valueSelectedAntennas.setText(antenna + "G");
        valueNameStage.setText(String.valueOf(stage));

        //Creamos la solicitudes periodicas de la ubicación cada tiempo especificado por el usuario
        client = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();

        request.setInterval(intervalSecondsMeaures);
        request.setFastestInterval(intervalSecondsMeaures - 2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //Recibimos la nueva localización
                for (Location location : locationResult.getLocations()) {
                    locationActual = location;
                }
                //Comprobamos que la distancia sea mayor a la especificada o que sea la primera antes de realizar una nueva medición
                //TODO DE MOMENTO NO FUNCIONA, PERO LO HE DEJADO PARA QUE PUDEA SEGUIR PROBANDO COSAS DESDE MI CASA
                if(locationAnterior==null || locationAnterior.distanceTo(locationActual)>intervalMetersMeaures){

                    float distance = 0;
                    if(locationAnterior!=null){
                        distance = locationAnterior.distanceTo(locationActual);
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
                        nMedidasEtapa++;
                        valueReadAntennas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                        String newLine = (String) ("Stage " + stage + ", " + antenna + "G: " + valueAntenna + " dbm");
                        //TODO Array de medidas limitado a 100 medidas, pero no se porque en algunas pruebas que he hecho me ha guardado menos, seguia registrandolas pero al darle a guardar no se guardaban todas
                        if(nMedidas<100){
                            arrayDeMedidas[nMedidas] = new Medida(getApplicationContext(), stage,locationActual.getLongitude(),locationActual.getLatitude(),antenna,valueAntenna);

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

                        }else{
                            valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                            valueReadAntennas.setText(R.string.mapsRoute_error_limitMeasurement);
                        }

                        //Actualizamos la lista de lecturas
                        arrayLecturas.add(newLine);
                        listMeasures.setAdapter(arrayLecturas);
                        valueReadAntennas.setText(newLine);

                        //Retrofit para enviar a la API una solicitud con los parametros obtenidos
                        mostrarTorresTelefonia();

                        //Establecemos un marcador en la posición desde la que se ha realizado la lectura con la imagen y color correspondiente segun el valor leido y la antena seleccionada
                        Objects.requireNonNull(gM.addMarker(new MarkerOptions().position(new LatLng(locationActual.getLatitude(), locationActual.getLongitude())).title(newLine))).setIcon(BitmapDescriptorFactory.defaultMarker(Auxiliar.obtenerTipoMarcador(valueAntenna, antenna)));

                        //Añadimos la distancia a un fichero para tenerla después
                        String distanciaGuardada="0";
                        try {
                            distanciaGuardada=StorageHelper.readStringFromFile(getString(R.string.fileDistances),getApplicationContext());
                        } catch (IOException e) {
                            distanciaGuardada="0";
                            e.printStackTrace();
                        }
                        try {
                            float distValue = Float.parseFloat(distanciaGuardada)+distance;
                            StorageHelper.saveStringToFile(getString(R.string.fileDistances),String.valueOf(distValue),getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Añadimos una línea entre los dos puntos para conocer el recorrido que hemos hecho
                        if(locationAnterior!=null){
                            Polyline line = gM.addPolyline(new PolylineOptions()
                                    .add(new LatLng(locationAnterior.getLatitude(), locationAnterior.getLongitude()), new LatLng(locationActual.getLatitude(), locationActual.getLongitude()))
                                    .width(5)
                                    .color(Auxiliar.getColorStage(stage)));
                        }

                        locationAnterior = locationActual;
                    }
                }else{
                    valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
                    valueReadAntennas.setText(R.string.mapsRoute_error_intervalDistance);
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

    private void mostrarTorresTelefonia() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.mylnikov.org/geolocation/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CellsPosition service = retrofit.create(CellsPosition.class);

        Call<CellsPositionRes> call = service.listLocation(1.1, "open", Integer.parseInt(mcc), Integer.parseInt(mnc), lac, cellid);

        call.enqueue(new Callback<CellsPositionRes>() {
            @Override
            public void onResponse(Call<CellsPositionRes> call, Response<CellsPositionRes> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "Error" + response.code());
                } else {
                    CellsPositionRes cellsPositions = response.body();
                    Log.d("Resultado latitud: ", String.valueOf(cellsPositions.getData().getLat()));
                    Log.d("Resultado longitud: ", String.valueOf(cellsPositions.getData().getLon()));

                    LatLng location = new LatLng(cellsPositions.getData().getLat(), cellsPositions.getData().getLon());
                    gM.addMarker(new MarkerOptions().position(location).title(getApplicationContext().getString(R.string.mapsRoute_text_Tower))).setIcon(getBitmapDescriptor(R.drawable.antena));
                }
            }

            @Override
            public void onFailure(Call<CellsPositionRes> call, Throwable t) {
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
        if(arrayDeMedidas.length>0) {
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
                }
            }
            Recorrido r = new Recorrido(getApplicationContext(), minMedida, (medMedida / nMedidas), maxMedida, nMedidas, arrayDeEtapas);
            //Creamos un array JSON para guardar todas nuestras medicioens
            JsonObject res = new JsonObject();

            res.add("recorrido", r.toJson());

            //Guardamos el array JSON en un fichero con el nombre pasado como parámetro
            try {
                StorageHelper.saveStringToFile(name + ".json", res.toString(), this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Volvemos a la actividad principal
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{

        }
    }

    public void newStage(View w){
        //Guardamos los valores para la etapa actual
        if(nMedidasEtapa>0){

            Medida[] arrayDePaso = new Medida[nMedidasEtapa];
            int i = 0;
            for(Medida m : arrayDeMedidas){
                if(m!=null && m.getEtapa()==stage){
                    arrayDePaso[i] = m;
                    i++;
                }
            }

            arrayDeEtapas[stage]=new Etapa(getApplicationContext(),stage,minMedidaEtapa,(medMedidaEtapa/nMedidasEtapa),maxMedidaEtapa,nMedidasEtapa, arrayDePaso);

            //Reiniciamos las variables de la etapa
            nMedidasEtapa = 0;
            minMedidaEtapa=1000;
            medMedidaEtapa=0;
            maxMedidaEtapa=-1000;
            indexEtapa = nMedidas;

            //Iniciamos una nueva etapa sumando uno al número de etapa y mostrando el valor por pantalla
            stage = stage +1;
            valueNameStage.setText(String.valueOf(stage));
        }else{
            valueReadAntennas.setTextColor(ColorStateList.valueOf(0xFFFF0000));
            valueReadAntennas.setText(R.string.mapsRoute_error_Stage);
        }
    }

    //Método para comvertir un drawable en un bitMapDescriptor
    private BitmapDescriptor getBitmapDescriptor(int id) {
        Context context = getApplicationContext();
        Drawable vectorDrawable = context.getDrawable(id);
        int h = ((int) vectorDrawable.getIntrinsicHeight());
        int w = ((int) vectorDrawable.getIntrinsicWidth());
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }
}