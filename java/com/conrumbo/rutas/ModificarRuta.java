package com.conrumbo.rutas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.modelo.ModeloRuta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;

public class ModificarRuta extends AppCompatActivity{

    //para la conexión con la bd y el uid del usuario
    private final ModeloRuta mr = new ModeloRuta(
            ModificarRuta.this,
            FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //ruta con la que se va a trabajar
    private Ruta ruta = new Ruta();
    private boolean privacidad_cambiada = false;

    //datos para la portada de la ruta
    public Uri archivo;
    private Bitmap portada;
    private boolean imagen_establecida = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_modificar);

        //eliminamos el edittext para modificar el nombre
        (findViewById(R.id.nombre_ruta_editable)).setVisibility(View.GONE);

        //obtenemos el nombre de la ruta
        ruta.setNombre(getIntent().getExtras().get("nombre").toString());

        //obtenemos los puntos de la ruta
        obtenerRuta();
    }

    /* OBTENEMOS LOS DATOS DE LA RUTA */
    //obtenemos los puntos de la ruta
    private void obtenerRuta(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("rutas").document(uid).collection(ruta.getNombre()).document("informacion").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            ruta.setRuta(task.getResult().getData());
                            actualizarRuta();
                        }
                        else{
                            Toast.makeText(ModificarRuta.this, getResources().getText(R.string.no_ruta), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(ModificarRuta.this, getResources().getText(R.string.error_ruta), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    //actualizamos en la vista
    private void actualizarRuta(){

        //añadimos la imagen
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("rutas/" + uid + "/" + ruta.getNombre() + "/portada.jpg");
        sr.getBytes(2048*2048)
                .addOnSuccessListener(bytes -> {
                    portada = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ((RoundedImageView) findViewById(R.id.portada_ruta)).setImageBitmap(portada);
                });

        //añadimos el nombre de la ruta
        ((TextView) findViewById(R.id.nombre_ruta)).setText(ruta.getNombre());

        //añadimos el tipo
        ((TextView) findViewById(R.id.tipo_ruta)).setText(ruta.getTipo());

        //privacidad de la ruta
        int priv = ruta.getPrivacidad();
        if(priv == 1){ ((Switch)findViewById(R.id.privacidad_ruta)).setChecked(true); }
        else{((Switch)findViewById(R.id.privacidad_ruta)).setChecked(false);}


        //añadimos el tipo de mapa base
        int mb = ruta.getMapaBase();
        if(mb == 1){ ((RadioButton) findViewById(R.id.mapa_basico)).setChecked(true);}
        else if(mb == 2){ ((RadioButton) findViewById(R.id.mapa_satelite)).setChecked(true);}
        else if(mb == 3){ ((RadioButton) findViewById(R.id.mapa_terreno)).setChecked(true);}
        else if(mb == 4){ ((RadioButton) findViewById(R.id.mapa_hibrido)).setChecked(true);}

        //añadimos la duración
        ((TextView) findViewById(R.id.duracion_ruta)).setText(ruta.getDuracion());

        //añadimos la distancia
        ((TextView) findViewById(R.id.distancia_ruta)).setText(ruta.getDistancia());

        //añadimos el enlace
        ((TextView) findViewById(R.id.enlace_ruta)).setText(ruta.getEnlace());

        //añadimos la historia
        ((TextView) findViewById(R.id.historia_ruta)).setText(ruta.getHistoria());

        /* PREPARAMOS LA INTERACCIÓN */
        //preparamos el botón para guardar la ruta
        findViewById(R.id.guardar_ruta).setOnClickListener(v -> obtenerDatos());

        //preparamos la imagen para añadir portada
        findViewById(R.id.portada_ruta).setOnClickListener(v -> seleccionarImagen());
    }

    //obtenemos los datos de la vista
    private void obtenerDatos(){

        //obtenemos el tipo de la ruta
        ruta.setTipo(((EditText)findViewById(R.id.tipo_ruta)).getText().toString().trim());

        //obtenemos la privacidad
        Switch sw = findViewById(R.id.privacidad_ruta);
        int privacidad = 0;
        if(sw.isChecked()){ privacidad = 1;}

        //si la privacidad ha cambiado
        privacidad_cambiada = true;
        ruta.setPrivacidad(privacidad);

        //obtenemos el mapa base
        RadioGroup rg = findViewById(R.id.mapa_base);
        int id = rg.getCheckedRadioButtonId();
        if(id == R.id.mapa_basico){
            ruta.setMapaBase(1);
        }
        else if(id == R.id.mapa_satelite){
            ruta.setMapaBase(2);
        }
        else if(id == R.id.mapa_terreno){
            ruta.setMapaBase(3);
        }
        else{
            ruta.setMapaBase(4);
        }

        //obtenemos la duración
        ruta.setDuracion(((EditText)findViewById(R.id.duracion_ruta)).getText().toString().trim());

        //obtenemos la distancia
        ruta.setDistancia(((EditText)findViewById(R.id.distancia_ruta)).getText().toString().trim());

        //obtenemos el enlace
        ruta.setEnlace(((EditText)findViewById(R.id.enlace_ruta)).getText().toString());

        //obtenemos la historia
        ruta.setHistoria(((EditText)findViewById(R.id.historia_ruta)).getText().toString().trim());

        registrarRuta();
    }

    //registramos la ruta
    private void registrarRuta(){
        mr.modificarRuta(ruta, privacidad_cambiada, imagen_establecida, archivo);
        Intent i = new Intent(ModificarRuta.this, VerRuta.class);
        i.putExtra("nombre", ruta.getNombre());
        i.putExtra("uid", uid);
        startActivity(i); finish();
    }


    /* PORTADA DE LA RUTA */
    /*modificar imagen*/
    ActivityResultLauncher<Intent> fotografia_perfil = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        //se obtiene el uri de los datos
                        archivo = data.getData();
                        try {
                            //se obtiene el bitmap
                            portada = MediaStore.Images.Media.getBitmap(getContentResolver(), archivo);
                            ((RoundedImageView) findViewById(R.id.portada_ruta)).setImageBitmap(portada);
                            imagen_establecida = true;
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fotografia_perfil.launch(Intent.createChooser(intent,
                getResources().getText(R.string.seleccionar_imagen).toString()));
    }
}
