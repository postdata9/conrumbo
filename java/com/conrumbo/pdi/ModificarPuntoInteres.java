package com.conrumbo.pdi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.rutas.VerRuta;

public class ModificarPuntoInteres extends AppCompatActivity {

    private String id;
    private String vengo_de = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdi_modificar);
        if(getIntent().getExtras().get("vengo_de") != null){
            vengo_de = getIntent().getExtras().get("vengo_de").toString();
        }

        //obtener datos del intent y actualizarlos en el layout
        iniciarInformacionPDI();

        //inicia los botones
        iniciarBotonesInformacionPDI();
    }

    //obtenemos los datos pasados al intent y los actualizamos en la vista
    private void iniciarInformacionPDI(){

        //inicializamos el pdi con los datos obtenidos del extra
        Bundle datos = getIntent().getExtras();
        if(datos != null){

            //obtenemos el id del marcador
            id = datos.getString("id_marcador");

            ((EditText) findViewById(R.id.nombre_punto)).setText(datos.getString("nombre"));
            ((EditText) findViewById(R.id.historia_punto)).setText(datos.getString("historia"));
            ((EditText) findViewById(R.id.datoscuriosos_punto)).setText(datos.getString("datosCuriosos"));
            ((EditText) findViewById(R.id.horario_punto)).setText(datos.getString("horario"));
            ((EditText) findViewById(R.id.precio_punto)).setText(datos.getString("precio"));
            ((EditText) findViewById(R.id.enlace_punto)).setText(datos.getString("enlace"));
            ((EditText) findViewById(R.id.audioguia_punto)).setText(datos.getString("audioguia"));
            ((EditText) findViewById(R.id.opinion_punto)).setText(datos.getString("opinion"));
        }
    }

    //preparamos los botones con las acciones correspondientes
    private void iniciarBotonesInformacionPDI(){

        //si clica en siguiente, se guarda la información
        (findViewById(R.id.siguiente_punto)).setOnClickListener(v -> {
            obtenerInformacionPDI();
        });

        //si se clica en la papelera, se elimina el punto
        (findViewById(R.id.eliminar_punto)).setOnClickListener(v -> {
            eliminarPDI();
        });
    }

    //obtenemos los datos del layout y creamos el intent
    private void obtenerInformacionPDI(){
        Intent i;
        //actualizamos la información
        if(vengo_de.equals("ver")) {
            i = new Intent(this, VerRuta.class);
        }
        else{
            i = new Intent(this, CrearPuntoInteres.class);
        }

        i.putExtra("id_marcador", id);
        i.putExtra("nombre", ((EditText)findViewById(R.id.nombre_punto)).getText().toString());
        i.putExtra("historia", ((EditText)findViewById(R.id.historia_punto)).getText().toString());
        i.putExtra("datosCuriosos", ((EditText)findViewById(R.id.datoscuriosos_punto)).getText().toString());
        i.putExtra("horario", ((EditText)findViewById(R.id.horario_punto)).getText().toString());
        i.putExtra("precio", ((EditText)findViewById(R.id.precio_punto)).getText().toString());
        i.putExtra("enlace", ((EditText)findViewById(R.id.enlace_punto)).getText().toString());
        i.putExtra("audioguia", ((EditText)findViewById(R.id.audioguia_punto)).getText().toString());
        i.putExtra("opinion", ((EditText)findViewById(R.id.opinion_punto)).getText().toString());

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    private void eliminarPDI(){
        Intent i;

        if(vengo_de.equals("ver")) { i = new Intent(this, VerRuta.class); }
        else{ i = new Intent(this, CrearPuntoInteres.class); }

        i.putExtra("eliminar", id);
        setResult(Activity.RESULT_OK, i);
        finish();
    }
}
