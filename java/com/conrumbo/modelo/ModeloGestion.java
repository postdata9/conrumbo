package com.conrumbo.modelo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.gestion.IniciarSesion;
import com.conrumbo.gestion.Registrarse;
import com.conrumbo.perfil.GestionarPerfil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import javax.crypto.interfaces.PBEKey;

public class ModeloGestion extends AppCompatActivity {

    //para iniciar sesión y recordar clave
    private final FirebaseFirestore bd = FirebaseFirestore.getInstance();
    private final FirebaseAuth user = FirebaseAuth.getInstance();
    private Context cont;

    public ModeloGestion(Context c){
        cont = c;
    }

    /* INICIAR SESIÓN */
    public void inicioSesion(String correo, String clave){
        //iniciamos sesión
        user.signInWithEmailAndPassword(correo, clave).addOnCompleteListener(task -> {
            //si las credenciales son correctas
            if (task.isSuccessful()) {
                cont.startActivity(new Intent(cont, GestionarPerfil.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            //si no son correctas, muestra un mensaje de error
            else {
                Toast.makeText(cont, cont.getResources().getString(R.string.mensaje_inicio_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* RECORDAR CLAVE */
    public void recordarClave(String correo){

        //se envía el correo al usuario
        user.sendPasswordResetEmail(correo).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(cont, cont.getResources().getString(R.string.correo_enviado), Toast.LENGTH_SHORT).show();
                cont.startActivity(new Intent(cont, IniciarSesion.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
            else{
                //mensaje de error
                String error = task.getException().getMessage();
                if(error.equals(cont.getResources().getString(R.string.invalid_email))){
                    Toast.makeText(cont, cont.getResources().getString(R.string.correo_no_valido), Toast.LENGTH_SHORT).show();
                }
                else if(error.equals(cont.getResources().getString(R.string.email_not_exits))){
                    Toast.makeText(cont, cont.getResources().getString(R.string.correo_no_existe), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(cont, cont.getResources().getString(R.string.recordar_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* REGISTRAR USUARIO */
    public void registrarUsuario(String correo, String clave){
        //se registra la cuenta
        user.createUserWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(this, task -> {
                    //si se ha podido crear
                    if (task.isSuccessful()) {
                        enviarCorreoVerificacion(cont);

                        //pasamos a la siguiente actividad para registrar los datos personales
                        Intent i = new Intent(cont, Registrarse.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("accion", "datos_personales");
                        cont.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        //mostramos los diferentes errores que han podido pasar
                        String error = task.getException().getMessage();

                        if(error.equals(cont.getResources().getString(R.string.email_already_use))){
                            Toast.makeText(cont, cont.getResources().getString(R.string.correo_ya_en_uso), Toast.LENGTH_SHORT).show();
                        }
                        else if(error.equals(cont.getResources().getString(R.string.invalid_email))){
                            Toast.makeText(cont, cont.getResources().getString(R.string.correo_no_valido), Toast.LENGTH_SHORT).show();
                        }
                        else if(error.equals(cont.getResources().getString(R.string.weak_password))){
                            Toast.makeText(cont, cont.getResources().getString(R.string.clave_debil), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(cont, cont.getResources().getString(R.string.registro_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //registra los datos
    public void registrarDatosUsuario(Map<String, Object> us){
        bd.collection("usuarios").document(user.getCurrentUser().getUid()).set(us);
    }

    //registrar la foto en la bd
    public void registrarFoto(Context cont, Uri archivo){
        if (archivo != null) {
            //aviso de que la imagen se está subiendo
            Toast.makeText(cont, cont.getResources().getString(R.string.subiendo_imagen), Toast.LENGTH_SHORT).show();

            //definición del nombre de la imagen y donde se almacena
            StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child(user.getCurrentUser().getUid() + "/perfil.jpg");
            ref.putFile(archivo)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(cont, cont.getResources().getString(R.string.imagen_subida),
                                Toast.LENGTH_SHORT).show();
                        cont.startActivity(new Intent(cont, GestionarPerfil.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    })

                    .addOnFailureListener(e -> {
                        // mensaje de error
                        Toast.makeText(cont, cont.getResources().getString(R.string.imagen_fallo), Toast.LENGTH_SHORT).show();
                        cont.startActivity(new Intent(cont, GestionarPerfil.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    });
        }
    }

    private void enviarCorreoVerificacion(Context cont){
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(cont, cont.getResources().getString(R.string.correo_verificacion_enviado), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
