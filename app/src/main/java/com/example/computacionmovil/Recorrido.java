package com.example.computacionmovil;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class Recorrido {

    private Context context;

    @Expose private int minMedida;
    @Expose private int medMedida;
    @Expose private int maxMedida;
    @Expose private int nMedidas;

    private Etapa[] etapas;

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public Recorrido(Context context, int minMedida, int medMedida, int maxMedida, int nMedidas, Etapa[] etapas) {
        super();
        this.context = context;
        this.minMedida = minMedida;
        this.medMedida = medMedida;
        this.maxMedida = maxMedida;
        this.nMedidas = nMedidas;
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

    public JsonElement toJson(){
        JsonArray arrayEtapasJSON=new JsonArray();

        //Añadimos cada medición al array JSON
        for(Etapa e : etapas){
            if(e!=null){
                arrayEtapasJSON.add(e.toJson());
            }
        }

        JsonObject res = (JsonObject) gson.toJsonTree(this);
        res.add("etapas",arrayEtapasJSON);
        return res;
    }
}
