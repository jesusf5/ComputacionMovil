package com.example.computacionmovil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.IOException;
import java.util.ArrayList;


public class LoadRouteDataActivity extends AppCompatActivity {
    //Lista con los valores que se muestran en el gráfico
    private ArrayList barArraylist;
    BarDataSet barDataSet;
    BarChart barChart;
    private ListView listRoutes;

    //String con el nombre de la ruta cargada
    private String name;

    //Lista de etapas
    private ArrayAdapter<String> arrayEtapas;
    private Etapa[] etapas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_route_data);

        //Obtenemos los parámetros pasados por la activity anterior(En este caso el nombre del fichero/ruta a cargar)
        Bundle extras = getIntent().getExtras();
        if (!extras.isEmpty()) {
            name = getIntent().getStringExtra("name");
        }

        //Cargamos todas las etapas de la ruta
        try {
            etapas = StorageHelper.readRecorridoFromFile(name,getApplicationContext()).getEtapas();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Inicializamos el gráfico
        barChart = findViewById(R.id.LoadRouteData_BarChart);

        //Inicializamos lista con las medidas que podemos cargar
        listRoutes = findViewById(R.id.LoadRouteData_Text_listViewStages);
        listRoutes.setClickable(true);

        //Establecemos un listener para poder seleccionar los distintos elementos de la lista
        listRoutes.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            listRoutes.setSelector(R.drawable.selecteditem);
            listRoutes.setSelection(position);
            listRoutes.setSelected(true);
            setData(etapas[position]);
        });

        //Cargamos la lista de etapas
        arrayEtapas = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        assert etapas != null;
        for(Etapa e : etapas){
            if(e!=null){
                arrayEtapas.add(getApplicationContext().getString(R.string.loadRouteData_text_DescriptionList) + " " + e.getnEtapa());
            }
        }
        listRoutes.setAdapter(arrayEtapas);

    }

    private void setData(Etapa e){
        ((TextView)findViewById(R.id.LoadRouteData_Text_Data)).setText(getApplicationContext().getString(R.string.loadRouteData_text_Description1) + " " + e.getnEtapa() + "\n" + getApplicationContext().getString(R.string.loadRouteData_text_Description2) + " " + e.getMinMedidaEtapa() + "dbm\n" + getApplicationContext().getString(R.string.loadRouteData_text_Description3) + " " + e.getMaxMedidaEtapa() + "dbm\n" + getApplicationContext().getString(R.string.loadRouteData_text_Description4) + " " + e.getnMedidasEtapa() + "\n" +  getApplicationContext().getString(R.string.loadRouteData_text_Description5) + " " + e.getMedMedidaEtapa() + "dbm");
        int[] colors = new int[100];
        int indexMeasurements = 0;
        barArraylist = new ArrayList();
        for(Medida m : e.getMedidasEtapa()){
            if(m!=null){
                //Convertimos el color HSV obtenido por el método de color de los marcadores a RGB Color
                float[] arrayHSV = new float[3];
                arrayHSV[0]=Auxiliar.obtenerTipoMarcador(m.getDbm(),m.getAntena());
                arrayHSV[1]=100;
                arrayHSV[2]=100;
                colors[indexMeasurements] = Color.HSVToColor(arrayHSV);

                barArraylist.add(new BarEntry(indexMeasurements,m.getDbm()));
                indexMeasurements++;
            }
        }

        //Declaramos los parámetros del gráfico
        barDataSet = new BarDataSet(barArraylist, "Gráfico de etapa seleccionada");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barDataSet.setColors(colors);

        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        //Métodos para que el gráfico se actualice automaticamente
        barChart.notifyDataSetChanged();
        barChart.invalidate();

        //Establecemos que mientras que estemos en esta actividad, no se pueda apagar la pantalla
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void volver(View v){
        Intent intent=new Intent(this, MapsLoadRouteActivity.class);

        //Pasamos los parametros seleccionados en la carga de la ruta(En nuestro caso solo el nombre del fichero)
        Bundle b = new Bundle();
        b.putString("name", name);
        intent.putExtras(b);

        startActivity(intent);
    }

    public void salir(View w){
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
