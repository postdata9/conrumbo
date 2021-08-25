package com.conrumbo.modelo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.perfil.GestionarPerfil;
import com.conrumbo.rutas.Ruta;
import com.conrumbo.rutas.VerRuta;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeloRuta extends AppCompatActivity {

    private final FirebaseFirestore bd = FirebaseFirestore.getInstance();

    private Context cont;
    private String uid;

    public ModeloRuta(Context c, String u){
        cont = c;
        uid = u;
    }

    //registrar la ruta
    public void registrarRuta(Ruta ruta, String modalidad){
        //agregamos el nombre y las coordenadas
        //rutas -> uid -> nombre_ruta -> informacion
        bd.collection("rutas").document(uid)
                .collection(ruta.getNombre()).document("informacion")
                .set(ruta, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    //si la consulta se ha realizado
                    if(task.isSuccessful()){
                        //si se ha guardado bien la ruta, añadimos el nombre al documento
                        registrarNombreRuta(ruta.getNombre(), modalidad);
                    }
                    else{
                        Toast.makeText(cont, cont.getResources().getText(R.string.registrar_ruta_error), Toast.LENGTH_SHORT).show();
                    }
                });

        //si la ruta es pública, añadimos una referencia en la ruta de públicas
        if(ruta.getPrivacidad() == 1){
            Map<String, String> rp = new HashMap<>();
            rp.put("nombre", ruta.getNombre());
            rp.put("distancia", ruta.getDistancia());
            rp.put("duracion", ruta.getDuracion());
            rp.put("uid", uid);
            bd.collection("rutas_publicas").document(uid + ", " + ruta.getNombre()).set(rp, SetOptions.merge());
        }
    }

    private void registrarNombreRuta(String nom, String modalidad){
        Map<String, String> nombre = new HashMap<>();
        nombre.put(nom, nom);

        bd.collection("rutas").document(uid).set(nombre, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    //si la consulta se ha realizado
                    if(task.isSuccessful()){
                        if(modalidad.equals("c")) {
                            Toast.makeText(cont, cont.getResources().getText(R.string.registrar_ruta_exito), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(cont, cont.getResources().getText(R.string.modificar_ruta_exito), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(cont, cont.getResources().getText(R.string.registrar_ruta_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //registrar la foto en la bd
    public void registrarPortadaRuta(Uri archivo, String nombre_ruta){
        if (archivo != null) {
            //aviso de que la imagen se está subiendo
            Toast.makeText(cont, cont.getResources().getString(R.string.subiendo_imagen), Toast.LENGTH_SHORT).show();

            //definición del nombre de la imagen y donde se almacena
            StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child("rutas/" + uid + "/" + nombre_ruta + "/portada.jpg");
            ref.putFile(archivo)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(cont, cont.getResources().getString(R.string.imagen_subida),
                                Toast.LENGTH_SHORT).show();
                    })

                    .addOnFailureListener(e -> {
                        // mensaje de error
                        Toast.makeText(cont, cont.getResources().getString(R.string.imagen_fallo), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    //se modifica la ruta de la base de datos
    public void modificarRuta(Ruta ruta, boolean privacidad, boolean imagen, Uri archivo){

        //si la privacidad ha cambiado
        if(privacidad){
            //y ahora es 1 -> pública, la añadimos a la lista de rutas públicas
            if(ruta.getPrivacidad() == 1){
                bd.collection("rutas").document(uid).collection(ruta.getNombre()).get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                if(!task.getResult().isEmpty()){
                                    agregarRutaPublica(task.getResult().getDocuments(), ruta);
                                }
                            }
                        });
            }

            //y ahora la ruta es 0 -> privada, hay que eliminar la referencia de las rutas públicas
            else{
                bd.collection("rutas_publicas")
                        .document(uid + ", " + ruta.getNombre()).delete();
            }
        }

        //registramos la ruta
        registrarRuta(ruta, "m");

        //si se ha establecido imagen nueva
        if(imagen){
            registrarPortadaRuta(archivo, ruta.getNombre());
        }
    }

    //agregar una ruta a la lista de públicas
    private void agregarRutaPublica(List<DocumentSnapshot> doc, Ruta ruta){

        //obtenemos los datos necesarios para almacenar la ruta pública
        Map<String, Object> rp = new HashMap<>();
        rp.put("nombre", ruta.getNombre());
        rp.put("duracion", ruta.getDuracion());
        rp.put("distancia", ruta.getDistancia());
        rp.put("uid", uid);

        //obtenemos el número de puntos que hay
        int nump = doc.size() - 1;

        //si hay puntos
        if(nump >= 1){
            LatLng coord = new LatLng(
                    ((HashMap<String, Double>)doc.get(1).getData().get("coordenadas")).get("latitude"),
                    ((HashMap<String, Double>)doc.get(1).getData().get("coordenadas")).get("longitude"));
            rp.put("coordenadas", coord);
            rp.put("numero_puntos", nump);
        }
        else{
            rp.put("numero_puntos", 0);
        }

        bd.collection("rutas_publicas").document(uid + ", " + ruta.getNombre()).set(rp);
    }

    //eliminar una ruta de favoritas
    public void eliminarRutaFavorita(String referencia){
        bd.collection("usuarios").document(uid)
                .collection("rutas_favoritas").document(referencia).delete();
        Toast.makeText(cont, cont.getResources().getText(R.string.ruta_favorita_eliminada_exito), Toast.LENGTH_SHORT).show();
    }


    //eliminar ruta de la base de datos
    public void eliminarRutaBD(Ruta ruta){
        //eliminamos la colección
        //obtenemos los documentos de la colección de la ruta
        bd.collection("rutas").document(uid).collection(ruta.getNombre()).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(!task.getResult().isEmpty()){
                            //eliminamos cada punto asociado a la ruta
                            for(DocumentSnapshot t : task.getResult()){
                                bd.collection("rutas").document(uid)
                                        .collection(ruta.getNombre()).document(t.getId()).delete();
                            }
                        }
                    }
                });

        //eliminamos la referencia a la ruta en el documento
        Map<String, Object> referencia = new HashMap<>();
        referencia.put(ruta.getNombre(), FieldValue.delete());
        bd.collection("rutas").document(uid).update(referencia);

        //eliminamos la carpeta de imágenes de firestorage
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("rutas/" + uid + "/" + ruta.getNombre());
        ref.delete();

        //si la ruta es pública, eliminamos la referencia en la bd
        if(ruta.getPrivacidad() == 1){
            String nombre_doc = uid + ", " + ruta.getNombre();
            bd.collection("rutas_publicas").document(nombre_doc).delete();
        }
    }

    //actualizar la ruta
    public void actualizarRutaPublica(String nombre, int num, LatLng coordenadas){
        Map<String, Object> rp = new HashMap<>();
        rp.put("numero_puntos", num);
        rp.put("coordenadas", coordenadas);
        bd.collection("rutas_publicas").document(uid + ", " + nombre).set(rp, SetOptions.merge());
    }

    //elimina una ruta de públicas
    public void eliminarRutaPublica(String nombre_ruta){
        bd.collection("rutas_publicas").document(uid + ", " + nombre_ruta).delete();
    }
}
