package com.example.computacionmovil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.IOException;
import java.util.Objects;

public class MapsLoadRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    //Declaramos un array que contendrá todas las medidas de la ruta cargada
    private Medida[] arrayMedidas;

    //Variable con el nombre de la ruta cargada
    private String name;

    //Intervalo de tiempo entre las actualizaciones de nuestra ubicación
    private int interval = 5000;

    //Creamos la variables necesarias para obtener la latitud y longitud de nuestra ubicación actual
    private FusedLocationProviderClient client;
    LocationCallback callback;
    LocationRequest request;

    //TODO SUPER PROVISIONAL PARA COMPROBAR SI FUNCIONA
    private boolean loadedMark;
    private GoogleMap gM;
    private MapView mMapView;

    //Creamos una lista y un array para mostrar textualmente las medidas
    private ListView listMeasures;
    private ArrayAdapter<String> arrayLecturas;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_loadroute);

        //Establecemos que inicialmente los marcadores no han sido instalados, para que se inicializaen una vez que el mapa este listo
        loadedMark=false;

        //Inicializamos el mapa
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

        //Obtenemos los parámetros pasados por la activity anterior(En este caso el nombre del fichero/ruta a cargar)
        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            name = getIntent().getStringExtra("name");
        }

        //Obtenemos las medidas para la ruta seleccionada
        try {
            arrayMedidas = StorageHelper.readMedidasFromFile(name,getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Inicializamos la lista con los valores del las medidas
        listMeasures =findViewById(R.id.mapsRoute_Text_listViewMeasure);
        arrayLecturas = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(10);
                return textView;
            }
        };


        for (Medida m : arrayMedidas){
            if(m!=null){
                String text = getApplicationContext().getString(R.string.medida_textDescription1) + " " + m.getEtapa() + " " + getApplicationContext().getString(R.string.medida_textDescription2) + " " +  m.getAntena() + "G " + getApplicationContext().getString(R.string.medida_textDescription4) + " " + m.getDbm() + "dbm" + getApplicationContext().getString(R.string.medida_textDescription3) + " (" + m.getLatitud() + "," + m.getLongitud() + ") " + "\n\n";
                arrayLecturas.add(text);
            }
        }

        listMeasures.setAdapter(arrayLecturas);

        //Creamos la solicitudes periodicas de la ubicación cada cierto tiempo
        client = LocationServices.getFusedLocationProviderClient(this);
        request = LocationRequest.create();

        request.setInterval(interval);
        request.setFastestInterval(interval - 2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        //Iniciamos la actualización de la ubicación
        requestLocationUpdates();


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gM = googleMap;
        startShowingLocation();

        //En cuanto el mapa este listo, establecemos los marcadores de todas las mediciones realizadas
        for (Medida m : arrayMedidas){
            if(m!=null){
                String text = getApplicationContext().getString(R.string.medida_textDescription1) + " " + m.getEtapa() + " " + getApplicationContext().getString(R.string.medida_textDescription2) + " " +  m.getAntena() + "G " + getApplicationContext().getString(R.string.medida_textDescription4) + " " + m.getDbm() + "dbm\n\n";
                Objects.requireNonNull(gM.addMarker(new MarkerOptions().position(new LatLng(m.getLatitud(), m.getLongitud())).title(text))).setIcon(Auxiliar.obtenerTipoMarcador(m.getDbm(), m.getAntena()));
            }
        }
    }

    private void requestLocationUpdates(){
        //Comprobamos los permisos y en caso de no disponer de ellos, los solicitamos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            client.requestLocationUpdates(request,callback, null);
        }
    }


    private void startShowingLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            gM.setMyLocationEnabled(true);
        }
    }

    public void exitRoute(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
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
}
