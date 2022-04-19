package com.example.computacionmovil;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class Medida {

    private Context context;

    @Expose private int etapa;
    @Expose private double longitud;
    @Expose private double latitud;
    @Expose private int antena;
    @Expose private int dbm;

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * No args constructor for use in serialization
     *
     */

    public Medida(Context context, int etapa, double longitud, double latitud, int antena, int dbm) {
        super();
        this.context = context;
        this.etapa = etapa;
        this.longitud = longitud;
        this.latitud = latitud;
        this.antena = antena;
        this.dbm = dbm;
    }

    public int getEtapa() {
        return etapa;
    }

    public String getString() {
        return context.getString(R.string.medida_textDescription1) + " " + this.getEtapa() + " " + context.getString(R.string.medida_textDescription2) + " " +  this.getAntena() + " " + context.getString(R.string.medida_textDescription3) + " (" + this.getLatitud() + "," + this.getLongitud() + ") " + context.getString(R.string.medida_textDescription4) + " " + this.getDbm() + "dbm\n\n";
    }

    public double getLongitud() {
        return longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public int getAntena() {
        return antena;
    }

    public int getDbm() {
        return dbm;
    }

    public JsonElement toJson(){
        return gson.toJsonTree(this);
    }

}
