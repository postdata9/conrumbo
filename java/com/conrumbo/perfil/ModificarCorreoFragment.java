package com.conrumbo.perfil;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.conrumbo.R;
import com.conrumbo.modelo.ModeloPerfil;
import com.google.firebase.auth.FirebaseAuth;

public class ModificarCorreoFragment extends DialogFragment {

    //correo y vista del fragmento
    private final String correo = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //mostramos el layout
        v = inflater.inflate(R.layout.perf_modificar_correo, container, false);

        //se muestra la barra
        Toolbar tb = v.findViewById(R.id.toolbar);
        tb.setTitle(R.string.modificar_correo);
        ((AppCompatActivity) getActivity()).setSupportActionBar(tb);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancelar);
        }
        setHasOptionsMenu(true);

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
        if(id == R.id.guardar){ modificarCorreo(); return true; }

        //si no, se vuelve a la actividad
        else if(id == android.R.id.home){ dismiss(); return true; }

        return super.onOptionsItemSelected(item);
    }

    private void modificarCorreo() {
        //obtenemos el nuevo correo
        EditText correo_et = v.findViewById(R.id.correo_nuevo_modificar);
        String correo_nuevo = correo_et.getText().toString().trim();

        //obtenemos la contraseña
        String clave = ((EditText)v.findViewById(R.id.clave_modificar_correo)).getText().toString();

        //comprobamos si son nulos
        if(correo_nuevo.equals("") || clave.equals("")){
            Toast.makeText(getContext(), getResources().getString(R.string.aviso_nulo_modificar_correo), Toast.LENGTH_SHORT).show();
        }
        //si el correo es el mismo
        else if(correo.equals(correo_nuevo)){
            Toast.makeText(getContext(), getResources().getString(R.string.aviso_igual_modificar_correo), Toast.LENGTH_SHORT).show();
        }
        else{
            ModeloPerfil mp = new ModeloPerfil(getContext(),
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            mp.modificarCorreo(correo, correo_nuevo, clave);
        }
    }
}

