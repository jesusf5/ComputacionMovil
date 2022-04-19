package com.example.computacionmovil;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class Etapa {
    private Context context;

    @Expose private int nEtapa;
    @Expose private int minMedidaEtapa;
    @Expose private int medMedidaEtapa;
    @Expose private int maxMedidaEtapa;
    @Expose private int nMedidasEtapa;

    private Medida[] medidasEtapa;

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public Etapa(Context context,int nEtapa, int minMedida, int medMedida, int maxMedida, int nMedidas, Medida[] medidas) {
        super();
        this.context = context;
        this.nEtapa = nEtapa;
        this.minMedidaEtapa = minMedida;
        this.medMedidaEtapa = medMedida;
        this.maxMedidaEtapa = maxMedida;
        this.nMedidasEtapa = nMedidas;
        this.medidasEtapa = medidas;
    }

    public int getnEtapa() {
        return nEtapa;
    }

    public int getMinMedidaEtapa() {
        return minMedidaEtapa;
    }

    public int getMedMedidaEtapa() {
        return medMedidaEtapa;
    }

    public int getMaxMedidaEtapa() {
        return maxMedidaEtapa;
    }

    public int getnMedidasEtapa() {
        return nMedidasEtapa;
    }

    public Medida[] getMedidasEtapa() {
        return medidasEtapa;
    }

    public JsonElement toJson(){
        JsonArray arrayMedidasJSON=new JsonArray();

        //Añadimos cada medición al array JSON
        if(medidasEtapa!=null){
            for(Medida m : medidasEtapa){
                if(m!=null){
                    arrayMedidasJSON.add(m.toJson());
                }
            }
        }

        JsonObject res = (JsonObject) gson.toJsonTree(this);
        res.add("medidas",arrayMedidasJSON);
        return res;
    }
}
