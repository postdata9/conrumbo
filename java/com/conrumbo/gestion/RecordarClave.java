package com.conrumbo.gestion;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.conrumbo.R;
import com.conrumbo.modelo.ModeloGestion;

public class RecordarClave extends Activity {

    //se muestra la pantalla de Recordar contraseña
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grcl_recordar_clave);

        //cuando se pulse Recordar
        findViewById(R.id.recordar).setOnClickListener(v -> {
            //obtenemos el correo
            String correo = ((EditText) findViewById(R.id.correo)).getText().toString().trim();

            //comprobamos que el correo no es nulo
            if(!("").equals(correo)){
                ModeloGestion mg = new ModeloGestion(getApplicationContext());
                mg.recordarClave(correo);
            }
            //si es nulo
            else{
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.correo_no_nulo), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
