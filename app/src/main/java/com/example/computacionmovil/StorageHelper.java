package com.example.computacionmovil;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageHelper {

    /**
     * Escribe (sobrescribiendo si el archivo ya existÃ­a) el contenido "content" en un fichero llamado "filename" en el alamacenamiento "externo" de la app. Este se podrÃ¡
     * encontrar a travÃ©s de un explorador de archivos (PC o el del propio telÃ©fono) en AlmacenamientoInternoCompartido>Android>Data>(nombre paquete de app)>.
     * Los ficheros asÃ­ almacenados se borrarÃ¡n si la aplicaciÃ³n se desinstala.
     * @param filename Nombre del fichero
     * @param content Contenido que se escribirÃ¡ en el fichero
     * @param context Contexto (e.g. Activity) desde el que se llama al mÃ©todo
     */
    public static void saveStringToFile(String filename,String content, Context context) throws IOException {
        File file = new File(context.getExternalFilesDir(null), filename);
        FileWriter writer= new FileWriter(file,false);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    /**
     * Lee el contenido de un fichero y lo devuelve como String, a partir del nombre del fichero (relativo al mismo path mencionado en el mÃ©todo de escritura:
     * AlmacenamientoInternoCompartido>Android>Data>(nombre paquete de app)>).
     * @param filename Nombre del fichero
     * @param context Contexto (e.g. Activity) desde el que se llama al mÃ©todo
     */
    public static String readStringFromFile(String filename, Context context) throws IOException {
        File file=new File(context.getExternalFilesDir(null),filename);
        FileInputStream inputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line).append("\n");
        }
        bufferedReader.close();
        inputStream.close();
        inputStreamReader.close();
        return content.toString();
    }

    /***------------------------------------------------------------------------------------------------------------------------------------***/
    //Lectura de medidas desde un fichero JSON
    public static Medida[] readMedidasFromFile(String filename, Context context) throws IOException {
        int nMedidas = 0;
        Medida[] arrayMedidas;

        // Leemos el contenido de la Ruta seleccionada en su fichero
        String content = readStringFromFile(filename, context);

        // Contamos el número de mediciones que hay para saber el tamaño del array que debemos reservar
        // Como sabemos que cada medición tiene la forma:
        //         {"antena":3,"dbm":-67,"etapa":1,"latitud":37.6813219,"longitud":-1.6890355}
        // Simplemente contamos el número de veces que aparece alguna de las palabras
        Pattern nMedidasPattern = Pattern.compile("antena");
        Matcher nMedidasMatcher = nMedidasPattern.matcher(content);
        while(nMedidasMatcher.find()){
            nMedidas++;
        }
        arrayMedidas = new Medida[nMedidas];

        //Una vez tenemos el  array pasamos a recorrer todas las medidas del fichero y almacenarlas en nuestro array
        Pattern p = Pattern.compile("\"antena\":((\\d+)(\\.\\d+)?),\"dbm\":((-\\d+)(\\.\\d+)?),\"etapa\":((\\d+)(\\.\\d+)?),\"latitud\":(-?\\d+(\\.\\d+)?),\"longitud\":(-?\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(content);
        int countActualMedida = 0;
        while(m.find()){
            arrayMedidas[countActualMedida] = new Medida(context.getApplicationContext(), Integer.valueOf(Objects.requireNonNull(m.group(8))), Double.valueOf(Objects.requireNonNull(m.group(12))), Double.valueOf(Objects.requireNonNull(m.group(10))), Integer.valueOf(Objects.requireNonNull(m.group(2))), Integer.valueOf(Objects.requireNonNull(m.group(5))));
            countActualMedida++;
        }

        //Devolvemos el array con todas las mediciones de la ruta asociada a este fichero
        return arrayMedidas;
    }

    static boolean eliminarFichero(String name, Context context){
        //Eliminamos el fichero pasado como parámetro
        File file = new File(context.getExternalFilesDir(""), name);
        return file.delete();
    }


}
