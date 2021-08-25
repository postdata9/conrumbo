package com.conrumbo.perfil;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import com.conrumbo.R;
import com.conrumbo.modelo.ModeloPerfil;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;


public class ModificarDatosPersonalesFragment extends DialogFragment {

    //elementos del perfil y la vista del fragment
    private Perfil perf = new Perfil();
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //si hay argumentos, los obtenemos
        if(getArguments() != null){ perf.setPerfil(getArguments()); }

        //mostramos el layout
        v = inflater.inflate(R.layout.perf_modificar_perfil, container, false);

        //se muestra la barra
        Toolbar tb = v.findViewById(R.id.toolbar);
        tb.setTitle(R.string.modificar_datos);
        ((AppCompatActivity) getActivity()).setSupportActionBar(tb);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancelar);
        }
        setHasOptionsMenu(true);
        inicializarDatos();

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        //se muestra la barra del menú de guardar
        inflater.inflate(R.menu.perf_guardar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //si se pulsa en guardar se registran los cambios realizados
        if(id == R.id.guardar){ modificarDatos(); return true; }

        //si no, se vuelve a la actividad
        else if(id == android.R.id.home){ dismiss(); return true; }

        return super.onOptionsItemSelected(item);
    }

    protected void inicializarDatos(){

        //inicializamos el nombre
        ((EditText)v.findViewById(R.id.nombre_modificar_perfil)).setText(perf.getNombre());

        //inicializamos la descripción
        ((EditText)v.findViewById(R.id.descripcion_modificar_perfil)).setText(perf.getDescripcion());

        //inicializamos el enlace
        ((EditText)v.findViewById(R.id.enlace_modificar_perfil)).setText(perf.getEnlace());

        //inicializamos la privacidad
        Switch pr = v.findViewById(R.id.privacidad_modificar_perfil);
        pr.setChecked(true);
        if(perf.getPrivacidad() == 0){ pr.setChecked(false); }
    }

    protected void modificarDatos(){
        Map<String, Object> datos_modificados = new HashMap<>();

        //comprobamos si el nombre ha cambiado
        if(modificarNombre()){ datos_modificados.put("nombre", perf.getNombre()); }

        //comprobamos si la descripción ha cambiado
        if(modificarDescripcion()){ datos_modificados.put("descripcion", perf.getDescripcion()); }

        //comprobar si el enlace es distinto del de antes y modificarlo
        if(modificarEnlace()){ datos_modificados.put("enlace", perf.getEnlace()); }

        //comprobamos la privacidad
        if(modificarPrivacidad()){ datos_modificados.put("privacidad", perf.getPrivacidad()); }

        //si se ha modificado algún dato
        if(!datos_modificados.isEmpty()){
            ModeloPerfil mp = new ModeloPerfil(getContext(),
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            mp.modificarDatosPersonales(datos_modificados);
        }
        //si no se ha modificado ninguno
        else {
            Toast.makeText(getContext(), getResources().getString(R.string.modificar_datos_perfil_no_modificado), Toast.LENGTH_SHORT).show();
        }

        startActivity(new Intent(getActivity(), GestionarPerfil.class));
    }


    //función para modificar el nombre
    protected boolean modificarNombre(){
        boolean modificar = false;

        String nom = ((EditText)v.findViewById(R.id.nombre_modificar_perfil)).getText().toString().trim();

        //comprobamos si es diferente al que era
        if(!nom.equals(perf.getNombre())){ perf.setNombre(nom); modificar = true; }
        return modificar;
    }

    protected boolean modificarDescripcion(){
        boolean modificar = false;
        String desc = ((EditText)v.findViewById(R.id.descripcion_modificar_perfil)).getText().toString().trim();

        //comprobamos si es diferente al que era
        if(!desc.equals(perf.getDescripcion())){ perf.setDescripcion(desc); modificar = true; }

        return modificar;
    }

    protected boolean modificarEnlace(){
        boolean modificar = false;
        String enl = ((EditText) v.findViewById(R.id.enlace_modificar_perfil)).getText().toString().trim();

        //comprobamos si es diferente al que era
        if(!enl.equals(perf.getEnlace())){ perf.setEnlace(enl); modificar = true; }

        return modificar;
    }

    protected boolean modificarPrivacidad(){
        boolean modificar = false;

        Switch priv_sw = v.findViewById(R.id.privacidad_modificar_perfil);

        //si está activo el switch y la privacidad actual es privada(0)
        if(priv_sw.isChecked() && perf.getPrivacidad() == 0){
            perf.setPrivacidad(1); //se pone pública
            modificar = true;
        }
        //si no está activo el switch y la privacidad actual es pública(1)
        else if(!priv_sw.isChecked() && perf.getPrivacidad() == 1){
            perf.setPrivacidad(0); //se pone privada
            modificar = true;
        }

        return modificar;
    }
}
