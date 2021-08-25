package com.conrumbo.gestion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import com.conrumbo.modelo.ModeloGestion;
import com.conrumbo.perfil.GestionarPerfil;
import com.conrumbo.R;
import com.conrumbo.perfil.Perfil;
import java.io.IOException;

public class Registrarse extends Activity {

    //datos necesarios
    private final ModeloGestion mgu = new ModeloGestion(Registrarse.this);
    private final int PICK_IMAGE_REQUEST = 22;

    //datos para la imagen
    public Uri archivo;
    private boolean imagen_establecida;
    private com.makeramen.roundedimageview.RoundedImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //se muestra la interfaz de registrarse
        super.onCreate(savedInstanceState);

        //obtenemos los argumentos del intent
        String accion = getIntent().getExtras().getString("accion");

        //si es datos personales, muestra el layout para registrar los datos personales
        if (accion.equals("datos_personales")) {
            setContentView(R.layout.greg_datos_opcionales);
            iv = findViewById(R.id.imagen);
            imagen_establecida = false;
            datosPersonales();
        }
        //si no, muestra el layout para registrar el correo y la contraseña
        else {
            setContentView(R.layout.greg_datos_obligatorios);
            registrarse();
        }
    }

    /* REGISTRAR USUARIO */
    private void registrarse() {
        //cuando se clica el botón
        findViewById(R.id.sig).setOnClickListener(v -> {
            //obtenemos el correo
            String correo = ((EditText)findViewById(R.id.correo)).getText().toString().trim();

            //obtenemos la clave
            String passwd = ((EditText)findViewById(R.id.passwd)).getText().toString().trim();

            //comprobamos si el correo o la clave son nulas
            if(!("").equals(correo) && !("").equals(passwd)) {
                //si no son nulos, registramos el correo
                mgu.registrarUsuario(getApplicationContext(), correo, passwd);
            }
            //si alguno de los dos es nulo, mostramos el mensaje
            else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registro_nulo), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /* REGISTRAR DATOS PERSONALES USUARIO */
    private void datosPersonales(){

        //si se pulsa el botón de crear perfil
        //cuando se clica el botón
        findViewById(R.id.crear_perfil).setOnClickListener(v -> registrarDatosPersonales());

        //si se pulsa la imagen, se selecciona
        findViewById(R.id.imagen).setOnClickListener(v -> seleccionarImagen());
    }

    //registrar los datos personales
    private void registrarDatosPersonales(){

        Perfil perf = new Perfil();
        //obtenemos el nombre de usuario
        perf.setNombre(((EditText) findViewById(R.id.usuario)).getText().toString().trim());

        //obtenemos la descripción
        perf.setDescripcion(((EditText) findViewById(R.id.descripcion)).getText().toString());

        //obtenemos el enlace
        perf.setEnlace(((EditText) findViewById(R.id.enlace)).getText().toString());

        //obtenemos la privacidad
        Switch privacidad_sw = (Switch) findViewById(R.id.privacidad);
        perf.setPrivacidad(0);
        if(privacidad_sw.isChecked()){
            perf.setPrivacidad(1);
        }

        //se registran los datos del usuario
        mgu.registrarDatosUsuario(perf.getMap());

        //almacenamos la foto en la bd
        if(imagen_establecida){
            mgu.registrarFoto(getApplicationContext(), archivo);
        }
        //si no se ha almacenado imagen, se va al perfil
        else{
            startActivity(new Intent(Registrarse.this, GestionarPerfil.class)); finish();
        }
    }


    //seleccinar la imagen
    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent,
                         getResources().getText(R.string.seleccionar_imagen)),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

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
