package com.example.computacionmovil;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.computacionmovil.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Creamos una variable para el mapa
    private MapView mMapView;

    //TODO SUPER PROVISIONAL PARA COMPROBAR SI FUNCIONA
    private GoogleMap gM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Inicializamos el mapa
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(null);
        mMapView.getMapAsync(this);

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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gM = googleMap;

        // Add a marker in Sydney and move the camera
        startShowingLocation();
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mostrarAntenas();
    }

    private void startShowingLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            gM.setMyLocationEnabled(true);
        }
    }

    private void mostrarAntenas() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.mylnikov.org/geolocation/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CellsPosition service = retrofit.create(CellsPosition.class);

        //OBTENEMOS VALORES DE LOS PARAMETROS
        List<Integer> parametros = getParameters();
        //

        Call<CellsPositionRes> call = service.listLocation(1.1, "open", parametros.get(0), parametros.get(1), parametros.get(2), parametros.get(3));
        Log.d("call", String.valueOf(call));

        call.enqueue(new Callback<CellsPositionRes>() {
            TextView texto = findViewById(R.id.textViewXD);
            @Override
            public void onResponse(Call<CellsPositionRes> call, Response<CellsPositionRes> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "Error" + response.code());
                } else {
                    CellsPositionRes cellsPositions = response.body();
                    texto.setText(cellsPositions.getData().toString()+"mcc: " + parametros.get(0)+", mnc: " + parametros.get(1)+ ", cellid: " + parametros.get(2)+ ", lac: " + parametros.get(3));
                    LatLng murcia = new LatLng(cellsPositions.getData().getLat(), cellsPositions.getData().getLon());
                    gM.addMarker(new MarkerOptions().position(murcia).title("Primera marca"));
                    //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    gM.moveCamera(CameraUpdateFactory.newLatLng(murcia));
                }
            }

            @Override
            public void onFailure(Call<CellsPositionRes> call, Throwable t) {
                Log.e("error", t.toString());
            }
        });
    }

    private List<Integer> getParameters() {

        //Declaramos el manager para solicitarle información sobre el servicio de telefonía
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //Comprobamos los permisos necesarios y en caso de no tenerlos, los solicitamos
        //TODO REVISAR POR A VECES HACE COSAS RARAS AL PEDIR LOS PERMISOS LA PRIMERA VEZ
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }else  if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        }

        String networkOperator = telephonyManager.getNetworkOperator();
        int mcc = 0, mnc = 0;
        if (networkOperator != null) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
            mnc = Integer.parseInt(networkOperator.substring(3));
        }

        final GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
        int lac = 0, cellid = 0;
        if (location != null) {
            lac = location.getLac();
            cellid = location.getCid();
        }

        List<Integer> parametros = new LinkedList<>();
        parametros.add(mcc);
        parametros.add(mnc);
        parametros.add(lac);
        parametros.add(cellid);
        return parametros;
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
        //client.removeLocationUpdates(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}