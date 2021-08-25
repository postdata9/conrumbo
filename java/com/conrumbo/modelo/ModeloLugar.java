package com.conrumbo.modelo;

import com.conrumbo.lugares.LugaresVisitados;
import com.conrumbo.pdi.PuntoInteres;
import android.content.Context;
import android.net.Uri;
import android.os.storage.OnObbStateChangeListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.rutas.Ruta;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Field;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ModeloLugar extends AppCompatActivity {

    private final FirebaseFirestore bd = FirebaseFirestore.getInstance();
    private Context cont;
    private String uid;

    public ModeloLugar(Context c, String u){
        cont = c;
        uid = u;
    }

    //agregar un lugar
    public void agregarLugar(String nombre_lugar, Map<String, Object> lugar_visitado){
        //agregamos el nombre y las coordenadas
        bd.collection("usuarios").document(uid).collection("lugares_visitados")
                .document(nombre_lugar).set(lugar_visitado, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    //si la consulta se ha realizado
                    if(task.isSuccessful()){
                        Toast.makeText(cont, nombre_lugar + " " + cont.getResources().getText(R.string.lugar_agregado_exito), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(cont, cont.getResources().getText(R.string.lugar_agregado_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //agregar un lugar
    public void eliminarLugar(String nombre_lugar){
        bd.collection("usuarios").document(uid)
                .collection("lugares_visitados").document(nombre_lugar)
                .delete();
        Toast.makeText(cont, cont.getResources().getText(R.string.lugar_visitado_eliminado_exito), Toast.LENGTH_SHORT).show();
    }
}
