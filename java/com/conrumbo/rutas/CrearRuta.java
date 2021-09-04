package com.conrumbo.rutas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.conrumbo.R;
import com.conrumbo.modelo.ModeloRuta;
import com.conrumbo.pdi.CrearPuntoInteres;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;

public class CrearRuta extends Activity {

    //para la conexión con la bd
    private final ModeloRuta mr = new ModeloRuta(
            CrearRuta.this,
            FirebaseAuth.getInstance().getCurrentUser().getUid());

    //datos de la ruta
    private Ruta ruta = new Ruta();

    //datos para la portada de la ruta
    private Uri archivo;                                         //uri del archivo seleccionado
    private boolean imagen_establecida;                         //para comprobar si la imagen se ha establecido o no
    private final int PICK_IMAGE_REQUEST = 13;                  //código de verificación
    private com.makeramen.roundedimageview.RoundedImageView iv; //referencia a la vista

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //establecemos la vista de la información necesaria
        setContentView(R.layout.r_modificar);

        //se inicializa el botón y se obtienen los datos
        prepararBotones();
    }

    //preparamos los botones
    private void prepararBotones(){

        //obtenemos la portada de la ruta
        iv = findViewById(R.id.portada_ruta);

        //eliminamos la vista del nombre de la ruta
        (findViewById(R.id.nombre_ruta)).setVisibility(View.GONE);

        //preparamos el botón para guardar la ruta
        findViewById(R.id.guardar_ruta).setOnClickListener(v -> comprobarNombre());

        //preparamos la imagen para añadir portada
        findViewById(R.id.portada_ruta).setOnClickListener(v -> seleccionarImagen());
    }

    /* OBTENER LOS DATOS DE LA VISTA */
    //obtener los datos de la vista
    private void comprobarNombre(){

        String nombre = ((EditText)findViewById(R.id.nombre_ruta_editable)).getText().toString().trim();

        //validamos que el nombre no esté vacío
        if(nombre.isEmpty()){
            Toast.makeText(this, getResources().getText(R.string.nombre_ruta_vacia) + nombre, Toast.LENGTH_SHORT).show();
        }
        //si no está vacío, comprobamos que no existe ya
        else {
            //se obtiene la colección con dicho nombre
            FirebaseFirestore bd = FirebaseFirestore.getInstance();
            bd.collection("rutas").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection(nombre).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //si está vacío, no existe una ruta ya con ese nombre
                            if (task.getResult().isEmpty()) {
                                obtenerDatos();
                            }
                            //si no está vacío, existe ya una ruta con ese nombre
                            else {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.nombre_ruta_existe), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //obtenemos el resto de datos y registramos la ruta
    private void obtenerDatos(){
        //obtenemos el nombre de la ruta
        ruta.setNombre(((EditText)findViewById(R.id.nombre_ruta_editable)).getText().toString().trim());

        //obtenemos el tipo de la ruta
        ruta.setTipo(((EditText)findViewById(R.id.tipo_ruta)).getText().toString().trim());

        //obtenemos la privacidad
        Switch sw = findViewById(R.id.privacidad_ruta);
        int privacidad = 0;
        if(sw.isChecked()){ privacidad = 1;}
        ruta.setPrivacidad(privacidad);

        //obtenemos el mapa base
        RadioGroup rg = findViewById(R.id.mapa_base);
        int id = rg.getCheckedRadioButtonId();
        if(id == R.id.mapa_basico){ ruta.setMapaBase(1); }
        else if(id == R.id.mapa_satelite){ ruta.setMapaBase(2); }
        else if(id == R.id.mapa_terreno){ ruta.setMapaBase(3); }
        else{ ruta.setMapaBase(4); }

        //obtenemos la duración
        ruta.setDuracion(((EditText)findViewById(R.id.duracion_ruta)).getText().toString().trim());

        //obtenemos la distancia
        ruta.setDistancia(((EditText)findViewById(R.id.distancia_ruta)).getText().toString().trim());

        //obtenemos el enlace
        ruta.setEnlace(((EditText)findViewById(R.id.enlace_ruta)).getText().toString());

        //obtenemos la historia
        ruta.setHistoria(((EditText)findViewById(R.id.historia_ruta)).getText().toString().trim());

        //registramos la ruta
        registrarRuta();
    }

    //registrar la ruta en la base de datos
    private void registrarRuta(){
        //registramos la ruta
        mr.registrarRuta(ruta, "c");

        //si se ha establecido portada nueva, se registra la foto en la base de datos
        if(imagen_establecida){
            mr.registrarPortadaRuta(archivo, ruta.getNombre());
        }

        //se pasa a la actividad para crear un punto de interés
        Intent i = new Intent(CrearRuta.this, CrearPuntoInteres.class);
        i.putExtra("nombre_ruta", ruta.getNombre());
        i.putExtra("mapa_base", ruta.getMapaBase());
        i.putExtra("privacidad", ruta.getPrivacidad());
        i.putExtra("vengo_de", "crear");
        startActivity(i); finish();
    }

    /* PORTADA DE LA RUTA */
    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent,
                        getResources().getText(R.string.seleccionar_imagen).toString()),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //si el código de la petición es la misma, el resultado es correcto y los datos no son nulos
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            //se obtiene el uri de los datos
            archivo = data.getData();
            try {
                //se establece la imagen
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), archivo);
                iv.setImageBitmap(bitmap);
                imagen_establecida = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

