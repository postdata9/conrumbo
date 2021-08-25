package com.conrumbo.modelo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.gestion.IniciarSesion;
import com.conrumbo.perfil.GestionarPerfil;
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
        cont.startActivity(new Intent(cont, IniciarSesion.class));
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

        //se elimina la foto
        eliminarFoto(imagen);

        //se eliminan las rutas que haya creado
        eliminarRutasPropias();

        //se eliminan las referencias de rutas públicas
        eliminarReferencias();

        //se eliminan las fotos de las rutas
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("rutas/" + uid + "/");
        ref.delete();

        terminarEliminar();
    }

    private void eliminarDatosPerfil(){
        //se eliminan los datos asociados en la bd
        bd.collection("usuarios").document(uid).delete()
                .addOnSuccessListener(unused -> Toast.makeText(cont, cont.getResources().getString(R.string.usuario_eliminado), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(cont, cont.getResources().getString(R.string.eliminar_error), Toast.LENGTH_SHORT).show());

    }

    private void eliminarRutasPropias(){
        bd.collection("rutas").document(uid).delete();
    }

    private void eliminarReferencias(){
        bd.collection("rutas_publicas").whereEqualTo("uid", uid).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        eliminarRutasPublicas(task.getResult().getDocuments());
                    }
                });
    }

    private void terminarEliminar(){

        //se cierra sesión y se elimina la cuenta
        FirebaseAuth.getInstance().getCurrentUser().delete();
        FirebaseAuth.getInstance().signOut();

        cont.startActivity(new Intent(cont, IniciarSesion.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
                        cont.startActivity(new Intent(cont, GestionarPerfil.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
