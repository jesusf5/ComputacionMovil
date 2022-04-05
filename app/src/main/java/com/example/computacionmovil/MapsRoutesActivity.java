package com.example.computacionmovil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.List;

public class MapsRoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Creamos una serie de variables que se extraen directamente desde la pantalla de creación de la ruta
    private String name; //Nombre de la ruta que se esta creando, necesario para al cargar cada ruta saber cual es cada una.
    private int antenna; //TODO de momento se deja al usuario especificar si quiere realizar mediciones en 4G,3G o 2G, pero hay que ver si es correcto o si nos interesa permitir en varias a la vez
    private int interval = 10000; //Permitimos al usuario especificar un intervalo de actualizaciones pero por defecto ponemos un intervalo de 10 segundos
    private int selectedSim = 0; //TODO Podemos permitir al usuario seleccionar en que SIM quiere realizar las lecturas si esque tiene más de una SIM
    // Por ejemplo entre 5 y 30 segundos
    private TextView valueNameRoute, valueSelectedAntennas, valueReadAntennas, valueNamePhase, valueNameSIMs;

    private MapView mMapView;

    //Creamos la variables necesarias para obtener la latitud y longitud de nuestra ubicación actual
    private FusedLocationProviderClient client;
    LocationCallback callback;
    LocationRequest request;
    private Location locationActual;

    //TODO SUPER PROVISIONAL PARA COMPROBAR SI FUNCIONA
    boolean newUbication = false;
    private GoogleMap gM;

    //Valor leido por la antena correspondiente
    private int valueAntenna = 0;

    //Creamos una variable para definir el número de la fase en la que nos encotramos
    private int phase = 1;

    //Creamos una lista para mostrar en tiempo real las medidas
    private ListView listMeasures;
    private ArrayAdapter<String> arrayLecturas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(null);

        mMapView.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            name = getIntent().getStringExtra("name");
            antenna = getIntent().getIntExtra("antennaSelected", 4);
            interval = getIntent().getIntExtra("interval", 10000);
            selectedSim = getIntent().getIntExtra("selectedSim", 0);
        }

        valueNameRoute = findViewById(R.id.valueNameRoute);
        valueSelectedAntennas = findViewById(R.id.valueSelectedAntennas);
        valueReadAntennas = findViewById(R.id.valueReadAntennas);
        valueNamePhase = findViewById(R.id.valueNamePhase);
        valueNameSIMs = findViewById(R.id.valueNameSIMs);

        listMeasures = findViewById(R.id.listViewMeasure);
        arrayLecturas = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);


        valueNameRoute.setText(name);
        valueSelectedAntennas.setText(antenna + "G");

        valueNamePhase.setText(String.valueOf(phase));

        //Creamos la solicitudes periodicas de la ubicación cada tiempo especificado por el usuario
        client = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();

        request.setInterval(interval);
        request.setFastestInterval(interval - 2000);//Por establecer un límite más que nada
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    locationActual = location;
                    newUbication = true;
                }
                if (newUbication) {
                    valueAntenna = readValueAntenna(selectedSim);
                    String newLine = (String) ("Phase " + phase + ", " + antenna + "G: " + valueAntenna + " dbm");
                    arrayLecturas.add(newLine);
                    listMeasures.setAdapter(arrayLecturas);
                    valueReadAntennas.setText(newLine);
                    gM.addMarker(new MarkerOptions().position(new LatLng(locationActual.getLatitude(), locationActual.getLongitude())).title(newLine)).setIcon(obtenerTipoMarcador(valueAntenna, antenna));
                    newUbication = false;
                }
            }
        };

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gM = googleMap;
        startShowingLocation();
    }

    @SuppressLint("NewApi")
    private int readValueAntenna(int selectedSim) {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)&&(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        }
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //Obtiene información sobre todas las SIMs del teléfono movil
        String datosSims = "";
        if(cellInfos!=null && selectedSim==0){
            int strength = -200;
            for (int i = 0; i<cellInfos.size(); i++){
                if (cellInfos.get(i).isRegistered()){
                    if(cellInfos.get(i) instanceof CellInfoWcdma){
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = cellSignalStrengthWcdma.getDbm();
                        datosSims = datosSims + "\n" + cellInfoWcdma.getCellIdentity().getOperatorAlphaShort().toString();
                    }else if(cellInfos.get(i) instanceof CellInfoGsm){
                        CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = cellSignalStrengthGsm.getDbm();
                        datosSims = datosSims + "\n" + cellInfogsm.getCellIdentity().getOperatorAlphaShort().toString();
                    }else if(cellInfos.get(i) instanceof CellInfoLte){
                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = cellSignalStrengthLte.getDbm();
                        datosSims = datosSims + "\n" + cellInfoLte.getCellIdentity().getOperatorAlphaShort().toString();
                    }

                }
            }
            valueNameSIMs.setText(datosSims);
            return strength;
        }//En caso de que se haya seleccionado una tarjeta SIM solo leemos las señales de dicha targeta
        else if(cellInfos!=null && cellInfos.get(selectedSim-1).isRegistered()){
            int strength = -200;
            if(cellInfos.get(selectedSim-1) instanceof CellInfoWcdma){
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                strength = cellSignalStrengthWcdma.getDbm();
                datosSims = datosSims + "\n" + cellInfoWcdma.getCellIdentity().getOperatorAlphaShort().toString();
            }else if(cellInfos.get(selectedSim-1) instanceof CellInfoGsm){
                CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                strength = cellSignalStrengthGsm.getDbm();
                datosSims = datosSims + "\n" + cellInfogsm.getCellIdentity().getOperatorAlphaShort().toString();
            }else if(cellInfos.get(selectedSim-1) instanceof CellInfoLte){
                CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                strength = cellSignalStrengthLte.getDbm();
                datosSims = datosSims + "\n" + cellInfoLte.getCellIdentity().getOperatorAlphaShort().toString();
            }
            valueNameSIMs.setText(datosSims);
            return strength;
        }
        return 0;
    }

    //Método para saber de que color corresponde el marcador en función de la cobertura y la antena sobre la que se este midiendo.
    private BitmapDescriptor obtenerTipoMarcador(int valueAntenna, int antenna) {
        //TODO Fuente de los datos(4G y 3G): https://www.xatakandroid.com/productividad-herramientas/como-saber-intensidad-senal-movil-android-que-significan-valores-dbm
        //TODO Fuente de los datos(2G): https://norfipc.com/redes/intensidad-nivel-senal-redes-moviles-2g-3g-4g.php
        if(antenna==4){
            //El rango del 4G se establece entre 30 dbm de diferencia, mientras que la escala de color va desde 0(ROJO)-120(VERDE)
            //Es decir, por cada decibelio de diferencia debemos aumentar en 4 la escala de color
            //Por ejemplo:
            // -90dmp -> 120 (Verde)
            //-91dbm(120-91=29) -> 116 (Menos verde)
            //-92dbm(120-92=28) -> 112 (Aun menos verde)
            //....
            //-119(120-119=1) -> 4 (Muy rojo)
            //-120 -> 0 (Muy rojo)
            //Y así seguimos, podemos operar más facil si hacemos ((-)MinValue)+(valueAntenna)
            int valorRealDBM = 120+valueAntenna;
            if (-90 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-91 >= valueAntenna && -119 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(valorRealDBM*4);
            } else if (-120 >= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
        } else if(antenna==3){
            //Para el rango del 3G se establece entre 40 dbm de diferencia, mientras que la escala de color va desde 0(ROJO)-120(VERDE)
            //Es decir, por cada decibelio de diferencia debemos aumentar en 3 la escala de color
            //Por ejemplo:
            // -70dmp -> 120 (Verde)
            //-71dbm(110-91=39) -> (39*3)117 (Menos verde)
            //-72dbm(110-92=38) -> (38*3)114 (Aun menos verde)
            //....
            //-109(110-109=1) -> (1*3)3 (Muy rojo)
            //-110 -> 0 (Muy rojo)
            //Y así seguimos, podemos operar más facil si hacemos ((-)MinValue)+(valueAntenna)
            int valorRealDBM = 110+valueAntenna;
            if (-70 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-71 >= valueAntenna && -109 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(valorRealDBM*3);
            } else if (-110 >= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
        } else if(antenna==2){

            //Para el rango del 3G se establece entre 40 dbm de diferencia, mientras que la escala de color va desde 0(ROJO)-120(VERDE)
            //Es decir, por cada decibelio de diferencia debemos aumentar en 3 la escala de color
            //Por ejemplo:
            // -80dmp -> 120 (Verde)
            //-81dbm(120-91=39) -> (39*3)117 (Menos verde)
            //-82dbm(120-92=38) -> (38*3)114 (Aun menos verde)
            //....
            //-119(120-119=1) -> (1*3)3 (Muy rojo)
            //-120 -> 0 (Muy rojo)
            //Y así seguimos, podemos operar más facil si hacemos ((-)MinValue)+(valueAntenna)
            int valorRealDBM = 120+valueAntenna;
            if (-80 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-81 >= valueAntenna && -119 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(valorRealDBM*3);
            } else if (-120 >= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
        }
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
    }

    //Método para mostrar nuestra ubicación actual en el mapa mostrado
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
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void newPhase(View w){
        phase=phase+1;
        valueNamePhase.setText(String.valueOf(phase));
    }
}