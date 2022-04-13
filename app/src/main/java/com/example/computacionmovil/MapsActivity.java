package com.example.computacionmovil;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.computacionmovil.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONObject;

import java.io.IOException;

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
        Call<CellsPositionRes> call = service.listLocation(1.1, "open", 250, 2, 7840, 200719106);
        Log.d("call", String.valueOf(call));

        call.enqueue(new Callback<CellsPositionRes>() {
            TextView texto = findViewById(R.id.textViewXD);
            @Override
            public void onResponse(Call<CellsPositionRes> call, Response<CellsPositionRes> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "Error" + response.code());
                } else {
                    CellsPositionRes cellsPositions = response.body();
                    texto.setText(cellsPositions.getData().toString());
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
        //client.removeLocationUpdates(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}