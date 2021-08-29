package com.conrumbo.modelo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.gestion.IniciarSesion;
import com.conrumbo.perfil.GestionarPerfil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeloPerfil extends AppCompatActivity {

    private final FirebaseFirestore bd = FirebaseFirestore.getInstance();
    private Context cont;
    private String uid;

    //variables necesarias para eliminar las rutas
    int num_rutas = 0, j = 0;
    String nombre_ruta = "";

    public ModeloPerfil(Context c, String u){
        cont = c;
        uid = u;
    }

    /* PERFIL */
    public void registrarFoto(Uri archivo){
        if (archivo != null) {
            //aviso de que la imagen se está subiendo
            Toast.makeText(cont, cont.getResources().getString(R.string.subiendo_imagen), Toast.LENGTH_SHORT).show();

            //definición del nombre de la imagen y donde se almacena
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("fotos_perfil/" + uid + ".jpg");
            ref.putFile(archivo)
                    .addOnSuccessListener(taskSnapshot ->
                            Toast.makeText(cont, cont.getResources().getString(R.string.imagen_subida), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Toast.makeText(cont, cont.getResources().getString(R.string.imagen_fallo), Toast.LENGTH_SHORT).show(); });
        }
    }

    //cerrar sesión
    public void cerrarSesion(){
        FirebaseAuth.getInstance().signOut();
        cont.startActivity(new Intent(cont, IniciarSesion.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }

    //eliminar nombre
    public void eliminarNombre(String nombre){
        if(nombre != null){
            Map<String, Object> nom = new HashMap<>();
            nom.put("nombre", "");
            bd.collection("usuarios").document(uid).set(nom, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(cont, cont.getResources().getString(R.string.nombre_eliminar_exito), Toast.LENGTH_SHORT).show();
                            cont.startActivity(new Intent(cont, GestionarPerfil.class));
                        }
                        else{
                            Toast.makeText(cont, cont.getResources().getString(R.string.nombre_eliminar_error), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    //eliminar descripción
    public void eliminarDescripcion(String descripcion){
        if(descripcion != null){
            Map<String, Object> desc = new HashMap<>();
            desc.put("descripcion", "");
            bd.collection("usuarios").document(uid).set(desc, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(cont, cont.getResources().getString(R.string.descripcion_eliminar_exito), Toast.LENGTH_SHORT).show();
                            cont.startActivity(new Intent(cont, GestionarPerfil.class));
                        }
                        else{
                            Toast.makeText(cont, cont.getResources().getString(R.string.descripcion_eliminar_error), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    //eliminar enlace
    public void eliminarEnlace(String enlace){
        if(enlace != null){
            Map<String, Object> enl = new HashMap<>();
            enl.put("enlace", "");
            bd.collection("usuarios").document(uid).set(enl, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(cont, cont.getResources().getString(R.string.enlace_eliminar_exito), Toast.LENGTH_SHORT).show();
                            cont.startActivity(new Intent(cont, GestionarPerfil.class));
                        }
                        else{
                            Toast.makeText(cont, cont.getResources().getString(R.string.enlace_eliminar_error), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    //eliminar foto
    public void eliminarFoto(Bitmap imagen){
        if(imagen != null){
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("fotos_perfil/" + uid + ".jpg");
            ref.delete().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    cont.startActivity(new Intent(cont, GestionarPerfil.class));
                    Toast.makeText(cont, cont.getResources().getString(R.string.foto_eliminar_exito), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(cont, cont.getResources().getString(R.string.foto_eliminar_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //eliminar cuenta usuario
    public void eliminarCuentaUsuario(Bitmap imagen){

        //eliminamos los datos asociados al perfil
        eliminarDatosPerfil();

        //eliminar imágenes
        eliminarImagenes(imagen);

        //eliminar las referencias a las rutas públicas
        eliminarReferencias();

        //eliminar rutas
        eliminarRutasPropias();
    }

    private void eliminarDatosPerfil(){
        //eliminamos los lugares visitados si tiene
        bd.collection("usuarios").document(uid).collection("lugares_visitados").get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && !task.getResult().isEmpty()){
                for(DocumentSnapshot doc : task.getResult().getDocuments()){
                    bd.collection("usuarios").document(uid)
                            .collection("lugares_visitados").document(doc.getId()).delete();
                }
            }
        });

        //eliminamos las rutas favoritas si tiene
        bd.collection("usuarios").document(uid).collection("rutas_favoritas").get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && !task.getResult().isEmpty()){
                for(DocumentSnapshot doc : task.getResult().getDocuments()){
                    bd.collection("usuarios").document(uid)
                            .collection("rutas_favoritas").document(doc.getId()).delete();
                }
            }
        });

        //eliminamos el documento
        bd.collection("usuarios").document(uid).delete();
    }

    private void eliminarImagenes(Bitmap imagen){
        //se elimina la foto
        eliminarFoto(imagen);

        //se eliminan las fotos de las rutas
        eliminarFotoRutas();
    }

    private void eliminarRutasPropias(){

        //obtenemos las rutas propias del usuario
        bd.collection("rutas").document(uid).get().addOnCompleteListener(task -> {
            //si no ha habido error y el resultado existe
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    //obtenemos el número de rutas
                    num_rutas = task.getResult().getData().size();

                    //si tiene rutas
                    if(num_rutas > 0){
                        //para cada ruta, obtenemos los puntos de interés y la información almacenada
                        for(Map.Entry<String, Object> rutas : task.getResult().getData().entrySet()){
                            nombre_ruta = rutas.getValue().toString();
                            bd.collection("rutas").document(uid)
                                    .collection(rutas.getValue().toString())
                                    .get().addOnCompleteListener(task1 -> {
                                //si no ha habido error
                                if(task1.isSuccessful()){

                                    //si vamos a recorrer los puntos de la última ruta
                                    if(nombre_ruta.equals(rutas.getValue().toString())) {
                                        //comprobamos que tiene puntos de interés y los eliminamos
                                        if(!task1.getResult().getDocuments().isEmpty()){
                                            int num_puntos = task1.getResult().getDocuments().size();

                                            //se elimina todos los documentos de la ruta
                                            for(DocumentSnapshot doc : task1.getResult().getDocuments()){
                                                bd.collection("rutas").document(uid)
                                                        .collection(rutas.getValue().toString()).document(doc.getId())
                                                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            j++;
                                                            if(num_puntos == j){
                                                                terminarEliminar();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        //si no tiene puntos eliminamos el documento
                                        else{
                                            terminarEliminar();
                                        }
                                    }
                                    //si no es la última ruta
                                    else{
                                        //comprobamos que tiene puntos de interés y los eliminamos
                                        if(!task1.getResult().getDocuments().isEmpty()){
                                            //se elimina todos los documentos de la ruta
                                            for(DocumentSnapshot doc : task1.getResult().getDocuments()){
                                                bd.collection("rutas").document(uid)
                                                        .collection(rutas.getValue().toString()).document(doc.getId())
                                                        .delete();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                    //si no tiene rutas, eliminamos el documento
                    else{
                        terminarEliminar();
                    }
                }
                else{
                    terminarEliminar();
                }
            }
            else{
                terminarEliminar();
            }
        });
    }

    private void eliminarReferencias(){
        //eliminamos las referencias que tenga de rutas públicas
        bd.collection("rutas_publicas").whereEqualTo("uid", uid).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        eliminarRutasPublicas(task.getResult().getDocuments());
                    }
                });
    }

    private void eliminarFotoRutas(){
        //se eliminan las fotos de las rutas
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("rutas/" + uid + "/");
        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            //si existen fotos de las rutas
            ref.delete();
        })
        .addOnFailureListener(e -> Log.println(Log.INFO, "Aviso: ", "No hay imágenes de rutas"));
    }


    private void terminarEliminar(){
        //mensaje que la cuenta se ha eliminado con éxito
        Toast.makeText(cont, getResources().getText(R.string.cuenta_eliminada_exito), Toast.LENGTH_SHORT).show();

        //eliminamos la referencia
        bd.collection("rutas").document(uid).delete();

        //se cierra sesión y se elimina la cuenta
        FirebaseAuth.getInstance().getCurrentUser().delete();
        FirebaseAuth.getInstance().signOut();
        cont.startActivity(new Intent(cont, IniciarSesion.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void eliminarRutasPublicas(List<DocumentSnapshot> docs){
        for(DocumentSnapshot doc : docs){
            bd.collection("rutas_publicas").document(doc.getId()).delete();
        }
    }

    //modificar datos personales
    public void modificarDatosPersonales(Map<String, Object> datos_modificados){
        bd.collection("usuarios").document(uid).set(datos_modificados, SetOptions.merge());
        Toast.makeText(cont, cont.getResources().getString(R.string.modificar_datos_modificar_exito), Toast.LENGTH_SHORT).show();
    }

    //modificar el correo
    public void modificarCorreo(String correo, String correo_nuevo, String clave){
        //verificamos la contraseña
        AuthCredential credenciales  = EmailAuthProvider.getCredential(correo, clave);
        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credenciales)
                .addOnCompleteListener(task -> {

            //si la contraseña es correcta, actual
            if(task.isSuccessful()){
                FirebaseAuth.getInstance().getCurrentUser().updateEmail(correo_nuevo).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        Toast.makeText(cont, cont.getResources().getString(R.string.correo_modificado_exito), Toast.LENGTH_SHORT).show();
                        cont.startActivity(new Intent(cont, GestionarPerfil.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    else{
                        Toast.makeText(cont, cont.getResources().getString(R.string.correo_modificado_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(cont, cont.getResources().getString(R.string.correo_modificar_no_autenticado), Toast.LENGTH_SHORT).show();
            }
        });
    }
}