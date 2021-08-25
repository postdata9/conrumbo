package com.conrumbo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.conrumbo.gestion.IniciarSesion;
import com.conrumbo.perfil.GestionarPerfil;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        //mostramos la pantalla de iniciar sesión
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startActivity(new Intent(MainActivity.this, Creacion.class)); finish();
        //si el usuario ya está loggeado, se muestra la pantalla del perfil
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, GestionarPerfil.class));
            finish();
        }
        //si no está loggeado, se lleva a iniciar sesión
        else{
            startActivity(new Intent(MainActivity.this, IniciarSesion.class));
            finish();
        }
    }
}