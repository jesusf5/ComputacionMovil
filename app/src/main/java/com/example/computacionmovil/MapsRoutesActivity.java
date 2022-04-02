package com.example.computacionmovil;

import android.Manifest;
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

import java.util.List;

public class MapsRoutesActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Creamos una serie de variables que se extraen directamente desde la pantalla de creación de la ruta
    private String name; //Nombre de la ruta que se esta creando, necesario para al cargar cada ruta saber cual es cada una.
    private int antenna; //TODO de momento se deja al usuario especificar si quiere realizar mediciones en 4G,3G o 2G, pero hay que ver si es correcto o si nos interesa permitir en varias a la vez
    private int interval = 10000; //TODO Se me ha ocurrido que también podriamos permitir al usuario especificar el intervalo de cada medición
    // Por ejemplo entre 5 y 30 segundos
    private TextView valueNameRoute, valueSelectedAntennas, valueReadAntennas;

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
        }

        valueNameRoute = findViewById(R.id.valueNameRoute);
        valueSelectedAntennas = findViewById(R.id.valueSelectedAntennas);
        valueReadAntennas = findViewById(R.id.valueReadAntennas);

        valueNameRoute.setText(name);
        valueSelectedAntennas.setText(antenna + "G");

        //Creamos la solicitudes periodicas de la ubicación por ejemplo cada 10 segundos //TODO Ejemplo temporal
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
                    valueAntenna = readValueAntenna();
                    ((TextView) findViewById(R.id.valueReadAntennas)).setText("Última lectura: " + valueAntenna + " dbm");
                    gM.addMarker(new MarkerOptions().position(new LatLng(locationActual.getLatitude(), locationActual.getLongitude())).title(antenna + "G: " + valueAntenna + " dbm")).setIcon(obtenerTipoMarcador(valueAntenna, antenna));
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

    private int readValueAntenna() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
        if(cellInfos!=null){
            int strength = 0;
            for (int i = 0; i<cellInfos.size(); i++){
                if (cellInfos.get(i).isRegistered()){
                    //TODO REVISAR PORQUE ESTO PETA
                    /*if(cellInfos.get(i) instanceof CellInfoWcdma){
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = cellSignalStrengthWcdma.getDbm();
                    }*/ if(cellInfos.get(i) instanceof CellInfoGsm){
                        CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = cellSignalStrengthGsm.getDbm();
                    }else if(cellInfos.get(i) instanceof CellInfoLte){
                        CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = cellSignalStrengthLte.getDbm();
                    }
                }
            }
            return strength;
        }
        return 0;
    }

    //Método para saber de que color corresponde el marcador en función de la cobertura y la antena sobre la que se este midiendo.
    private BitmapDescriptor obtenerTipoMarcador(int valueAntenna, int antenna) {
        //TODO Fuente de los datos(4G y 3G): https://www.xatakandroid.com/productividad-herramientas/como-saber-intensidad-senal-movil-android-que-significan-valores-dbm
        //TODO Fuente de los datos(2G): https://norfipc.com/redes/intensidad-nivel-senal-redes-moviles-2g-3g-4g.php
        if(antenna==4){
            if (-90 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-91 >= valueAntenna && -105 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            } else if (-106 >= valueAntenna && -110 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            } else if (-111 >= valueAntenna && -119 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            } else if (-120 >= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            }
        } else if(antenna==3){
            if (-70 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-71 >= valueAntenna && -85 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            } else if (-86 >= valueAntenna && -100 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            } else if (-101 >= valueAntenna && -109 <= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            } else if (-110 >= valueAntenna) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            }
        } else if(antenna==2){
        if (-79 <= valueAntenna) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        } else if (-80 >= valueAntenna && -95 <= valueAntenna) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        } else if (-96 >= valueAntenna && -104 <= valueAntenna) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        } else if (-105 >= valueAntenna && -112 <= valueAntenna) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        } else if (-113 >= valueAntenna) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
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

    }
}