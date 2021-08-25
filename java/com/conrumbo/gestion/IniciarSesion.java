package com.conrumbo.gestion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.conrumbo.R;
import com.conrumbo.modelo.ModeloGestion;

public class IniciarSesion extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gis_iniciar_sesion);

        //para iniciar sesión
        iniciarSesion();

        //para recordar la clave
        recordarClave();

        //para registrarse
        registrarse();
    }

    //para iniciar sesión
    private void iniciarSesion(){
        //cuando se pulse Iniciar Sesión, se comprueban los datos
        findViewById(R.id.boton_sesion).setOnClickListener(v -> {
            //obtenemos el correo
            String nombre = ((EditText) findViewById(R.id.nombre_usuario_sesion)).getText().toString().trim();

            //obtenemos la contraseña
            String passwd = ((EditText) findViewById(R.id.clave_sesion)).getText().toString().trim();

            //comprobamos que no son nulos
            if (!("").equals(nombre) && !("").equals(passwd)) {
                //si no son nulos, iniciamos sesión
                ModeloGestion mg = new ModeloGestion(IniciarSesion.this);
                mg.inicioSesion(nombre, passwd);
            }
            //si es nulo, se muestra un mensaje
            else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.mensaje_inicio_nulo), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //para recordar la clave
    private void recordarClave(){
        //cuando se pulse Recordar Contraseña, se lleva a la actividad de Recordar contraseña
        findViewById(R.id.boton_recordar_clave).setOnClickListener(v -> {
            startActivity(new Intent(IniciarSesion.this, RecordarClave.class));
        });
    }

    //para registrarse
    private void registrarse(){
        //cuando se pulse Registrarse, se lleva a la actividad de Registrarse
        findViewById(R.id.boton_registrarse).setOnClickListener(v -> {
            startActivity(new Intent(IniciarSesion.this, Registrarse.class).putExtra("accion", "registrar"));
        });
    }
}
