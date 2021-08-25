package com.conrumbo.modelo;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.pdi.PuntoInteres;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ModeloPunto extends AppCompatActivity {

    private final FirebaseFirestore bd = FirebaseFirestore.getInstance();
    private Context cont;
    private String uid;

    public ModeloPunto(Context c, String u){
        cont = c;
        uid = u;
    }

    //eliminamos el punto de interés de la ruta
    public void eliminarPunto(String nombre_ruta, String pdi){
        bd.collection("rutas").document(uid).collection(nombre_ruta).document(pdi).delete();
        Toast.makeText(cont, cont.getResources().getText(R.string.pdi_eliminado).toString(), Toast.LENGTH_SHORT).show();
    }

    //registramos un punto de interés en la ruta en la bd
    public void registrarPunto(String nombre_ruta, PuntoInteres pdi){
        //lo añadimos a la bd
        bd.collection("rutas").document(uid).collection(nombre_ruta).document(pdi.getId()).set(pdi);
    }


}
