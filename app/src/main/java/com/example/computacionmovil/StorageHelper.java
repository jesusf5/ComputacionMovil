package com.example.computacionmovil;

import android.content.Context;
import android.util.Log;

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
     * @param context Contexto (e.g. Activity) desde el que se llama al método
     */
    public static void saveStringToFile(String filename,String content, Context context) throws IOException {
        File file = new File(context.getExternalFilesDir(null), filename);
        FileWriter writer= new FileWriter(file,false);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    /**
     * Lee el contenido de un fichero y lo devuelve como String, a partir del nombre del fichero (relativo al mismo path mencionado en el método de escritura:
     * AlmacenamientoInternoCompartido>Android>Data>(nombre paquete de app)>).
     * @param filename Nombre del fichero
     * @param context Contexto (e.g. Activity) desde el que se llama al método
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
            arrayMedidas[countActualMedida] = new Medida(context.getApplicationContext(), Integer.parseInt(Objects.requireNonNull(m.group(8))), Double.parseDouble(Objects.requireNonNull(m.group(12))), Double.parseDouble(Objects.requireNonNull(m.group(10))), Integer.parseInt(Objects.requireNonNull(m.group(2))), Integer.parseInt(Objects.requireNonNull(m.group(5))));
            countActualMedida++;
        }

        //Devolvemos el array con todas las mediciones de la ruta asociada a este fichero
        return arrayMedidas;
    }

    //Lectura de medidas desde un fichero JSON
    public static Recorrido readRecorridoFromFile(String filename, Context context) throws IOException {
        Recorrido r = null;

        int nEtapa = 0;
        Etapa[] arrayEtapas;

        // Leemos el contenido de la Ruta seleccionada en su fichero
        String content = readStringFromFile(filename, context);

        // Contamos el número de etapas que hay para saber el tamaño del array que debemos reservar
        // Como sabemos que cada etapa tiene la forma:
        //         {"maxMedidaEtapa":-95,"medMedidaEtapa":-95,"minMedidaEtapa":-95,"nEtapa":1,"nMedidasEtapa":1,"medidas":[{"antena":4,"dbm":-95,"etapa":1,"latitud":37.6813364,"longitud":-1.6890507}]}
        // Simplemente contamos el número de veces que aparece la palabra nEtapa
        Pattern nEtapaPattern = Pattern.compile("nEtapa");
        Matcher nEtapaMatcher = nEtapaPattern.matcher(content);
        while(nEtapaMatcher.find()){
            nEtapa++;
        }
        arrayEtapas = new Etapa[nEtapa];

        Pattern p = Pattern.compile("\"maxMedidaEtapa\":(-?(\\d+)(\\.\\d+)?),\"medMedidaEtapa\":((-?\\d+)(\\.\\d+)?),\"minMedidaEtapa\":(-?(\\d+)(\\.\\d+)?),\"nEtapa\":((\\d+)(\\.\\d+)?),\"nMedidasEtapa\":((\\d+)(\\.\\d+)?),\"medidas\":([^\\]]*)?");
        Matcher m = p.matcher(content);

        int countActualEtapa = 0;
        //Sabiendo el número de etapas pasamos a recorrer cada una de las etapas y a obtener sus medidas
        while(m.find()) {
            int nMedidas = 0;
            Medida[] arrayMedidasEtapa;


            // Contamos el número de mediciones que hay para saber el tamaño del array que debemos reservar
            // Como sabemos que cada medición tiene la forma:
            //         {"antena":3,"dbm":-67,"etapa":1,"latitud":37.6813219,"longitud":-1.6890355}
            // Simplemente contamos el número de veces que aparece alguna de las palabras
            Pattern nMedidasPattern = Pattern.compile("antena");
            Matcher nMedidasMatcher = nMedidasPattern.matcher(Objects.requireNonNull(m.group(16)));
            Log.d("Contenido del ultimo grupo", m.group(16));
            while (nMedidasMatcher.find()) {
                nMedidas++;
            }
            arrayMedidasEtapa = new Medida[nMedidas];

            //Una vez tenemos el  array pasamos a recorrer todas las medidas del fichero y almacenarlas en nuestro array
            Pattern p2 = Pattern.compile("\"antena\":((\\d+)(\\.\\d+)?),\"dbm\":((-\\d+)(\\.\\d+)?),\"etapa\":((\\d+)(\\.\\d+)?),\"latitud\":(-?\\d+(\\.\\d+)?),\"longitud\":(-?\\d+(\\.\\d+)?)");
            Matcher m2 = p2.matcher(Objects.requireNonNull(m.group(16)));
            int countActualMedida = 0;
            while (m2.find()) {
                arrayMedidasEtapa[countActualMedida] = new Medida(context.getApplicationContext(), Integer.parseInt(Objects.requireNonNull(m2.group(8))), Double.parseDouble(Objects.requireNonNull(m2.group(12))), Double.parseDouble(Objects.requireNonNull(m2.group(10))), Integer.parseInt(Objects.requireNonNull(m2.group(2))), Integer.parseInt(Objects.requireNonNull(m2.group(5))));
                countActualMedida++;
            }

            arrayEtapas[countActualEtapa] = new Etapa(context.getApplicationContext(), Integer.parseInt(Objects.requireNonNull(m.group(11))), Integer.parseInt(Objects.requireNonNull(m.group(7))), Integer.parseInt(Objects.requireNonNull(m.group(4))), Integer.parseInt(Objects.requireNonNull(m.group(1))), Integer.parseInt(Objects.requireNonNull(m.group(14))),arrayMedidasEtapa);
            countActualEtapa++;
        }

        Pattern recorridoPattern = Pattern.compile("\"recorrido\":\\{\"maxMedida\":((-?\\d+)(\\.\\d+)?),\"medMedida\":((-?\\d+)(\\.\\d+)?),\"minMedida\":((-?\\d+)(\\.\\d+)?),\"nMedidas\":((\\d+)(\\.\\d+)?)(.*)");
        Matcher recorridoMatcher = recorridoPattern.matcher(content);
        if(recorridoMatcher.find()){
            r = new Recorrido(context,Integer.parseInt(Objects.requireNonNull(recorridoMatcher.group(7))),Integer.parseInt(Objects.requireNonNull(recorridoMatcher.group(4))),Integer.parseInt(Objects.requireNonNull(recorridoMatcher.group(1))),Integer.parseInt(Objects.requireNonNull(recorridoMatcher.group(10))),arrayEtapas);
        }

        //Devolvemos el array con todas las mediciones de la ruta asociada a este fichero
        return r;
    }


    static boolean eliminarFichero(String name, Context context){
        //Eliminamos el fichero pasado como parámetro
        File file = new File(context.getExternalFilesDir(""), name);
        return file.delete();
    }


}
