package com.example.computacionmovil;

import android.content.Context;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class Medida {

    //private Context context;

    private int etapa;
    private double longitud;
    private double latitud;
    private int antena;
    private int dbm;

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /**
     * No args constructor for use in serialization
     *
     */

    public Medida(Context context, int etapa, double longitud, double latitud, int antena, int dbm) {
        super();
        //this.context = context;
        this.etapa = etapa;
        this.longitud = longitud;
        this.latitud = latitud;
        this.antena = antena;
        this.dbm = dbm;
    }

    public int getEtapa() {
        return etapa;
    }

    /*public String getTexto() {
        Resources r = Resources.getSystem();
        return r.getString(R.string.medida_textDescription1) + " " + this.getEtapa() + " " + r.getString(R.string.medida_textDescription2) + " " +  this.getAntena() + " " + r.getString(R.string.medida_textDescription3) + " (" + this.getLatitud() + "," + this.getLongitud() + ") " + r.getString(R.string.medida_textDescription4) + " " + this.getDbm() + "dbm\n\n";
    }*/

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
