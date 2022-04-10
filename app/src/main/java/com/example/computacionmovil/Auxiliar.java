package com.example.computacionmovil;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Auxiliar {

    public static BitmapDescriptor obtenerTipoMarcador(int valueMeasured, int antenna) {
        //TODO Fuente de los datos(4G y 3G): https://www.xatakandroid.com/productividad-herramientas/como-saber-intensidad-senal-movil-android-que-significan-valores-dbm
        //TODO Fuente de los datos(2G): https://norfipc.com/redes/intensidad-nivel-senal-redes-moviles-2g-3g-4g.php

        //Método para obtener el marcador de color correcto en función del valor medido en dbm y de la antena en la que se ha realizado la medición.
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
            //Y así seguimos, podemos operar más facil si hacemos ((-)MinValue)+(valueMeasured)
            int valorRealDBM = 120+valueMeasured;
            if (-90 <= valueMeasured) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-119 <= valueMeasured) {
                return BitmapDescriptorFactory.defaultMarker(valorRealDBM*4);
            } else {
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
            //Y así seguimos, podemos operar más facil si hacemos ((-)MinValue)+(valueMeasured)
            int valorRealDBM = 110+valueMeasured;
            if (-70 <= valueMeasured) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-109 <= valueMeasured) {
                return BitmapDescriptorFactory.defaultMarker(valorRealDBM*3);
            } else {
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
            //Y así seguimos, podemos operar más facil si hacemos ((-)MinValue)+(valueMeasured)
            int valorRealDBM = 120+valueMeasured;
            if (-80 <= valueMeasured) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (-119 <= valueMeasured) {
                return BitmapDescriptorFactory.defaultMarker(valorRealDBM*3);
            } else {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
        }
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
    }

}
