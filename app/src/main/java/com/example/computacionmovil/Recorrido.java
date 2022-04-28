package com.example.computacionmovil;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class Recorrido {

    private Context context;

    @Expose private int minMedida;
    @Expose private int medMedida;
    @Expose private int maxMedida;
    @Expose private int nMedidas;

    private ArrayList<LatLng> antenas;
    private Etapa[] etapas;

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public Recorrido(Context context, int minMedida, int medMedida, int maxMedida, int nMedidas, ArrayList<LatLng> antenas, Etapa[] etapas) {
        super();
        this.context = context;
        this.minMedida = minMedida;
        this.medMedida = medMedida;
        this.maxMedida = maxMedida;
        this.nMedidas = nMedidas;
        this.antenas = antenas;
        this.etapas = etapas;
    }

    public int getMinMedida() {
        return minMedida;
    }

    public int getMedMedida() {
        return medMedida;
    }

    public int getMaxMedida() {
        return maxMedida;
    }

    public int getnMedidas() {
        return nMedidas;
    }

    public Etapa[] getEtapas() {
        return etapas;
    }

    public ArrayList<LatLng> getAntenas() {
        return antenas;
    }

    public JsonElement toJson(){

        //Creamos la conversion a JSON
        JsonObject res = (JsonObject) gson.toJsonTree(this);

        //Añadimos las antenas al array JSON
        JsonArray arrayAntenasJSON=new JsonArray();
        for(LatLng a : antenas){
            arrayAntenasJSON.add(a.latitude + "," + a.longitude);
        }
        res.add("antenas",arrayAntenasJSON);

        //Añadimos cada medición al array JSON
        JsonArray arrayEtapasJSON=new JsonArray();
        for(Etapa e : etapas){
            if(e!=null){
                arrayEtapasJSON.add(e.toJson());
            }
        }
        res.add("etapas",arrayEtapasJSON);
        return res;
    }
}
