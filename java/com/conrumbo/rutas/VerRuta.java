package com.conrumbo.rutas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.conrumbo.R;
import com.conrumbo.gestion.Buscador;
import com.conrumbo.gestion.Creacion;
import com.conrumbo.modelo.ModeloPunto;
import com.conrumbo.modelo.ModeloRuta;
import com.conrumbo.pdi.CrearPuntoInteres;
import com.conrumbo.pdi.ModificarPuntoInteres;
import com.conrumbo.pdi.PuntoInteres;
import com.conrumbo.perfil.GestionarPerfil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class VerRuta extends AppCompatActivity implements OnMapReadyCallback {

    //modelo de ruta y punto y el uid del usuario
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final ModeloPunto mp = new ModeloPunto(VerRuta.this, uid);
    private final ModeloRuta mr = new ModeloRuta(VerRuta.this, uid);

    //datos de la ruta y pdi
    private String uid_ruta;
    private Ruta ruta = new Ruta();
    private View info_pdi, info_ruta;
    private PuntoInteres pdi;
    private BottomSheetDialog bs;

    //datos para añadir los puntos al mapa
    private Map<String, PuntoInteres> puntos_ruta = new HashMap<>();
    private Map<String, Marker> puntos_mapa = new HashMap<>();
    int tam_coleccion = 0;

    //mapa en el que se muestra la ruta y los puntos
    private GoogleMap map;

    //para obtener el pdi con la información modificada
    ActivityResultLauncher<Intent> informacionPDI = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle datos = result.getData().getExtras();
                    //si está eliminar, actualizamos los datos del pdi
                    if(datos.getString("eliminar") != null){
                        //si está eliminar, se elimina el marcador
                        eliminarPDI(datos.getString("eliminar"));
                    }
                    //por el contrario, actualizamos los datos
                    else{
                        //actualizamos los datos del PDI
                        actualizarPDI(datos);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_ver);

        //recogemos los datos necesarios para obtener la ruta
        ruta.setNombre(getIntent().getExtras().get("nombre").toString());
        uid_ruta = getIntent().getExtras().get("uid").toString();

        //iniciamos el mapa
        iniciarMapa();

        //iniciamos la barra de navegación
        iniciarNavigationBottom();

        //obtenemos los puntos de la ruta
        obtenerRuta();

        //iniciamos el botón de información de la ruta
        iniciarInformacionRuta();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        //cuando se clica un punto de interés, aparece la información en un bottom sheet
        map.setOnMarkerClickListener(marker -> {

            //cuando se clica en un marcador, que es una ruta
            //muestra el bottom sheet con la información
            bs = new BottomSheetDialog(VerRuta.this,
                    R.style.BottomSheetDialogTheme);

            //obtenemos la vista y la mostramos
            info_pdi = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.pdi_informacion,
                            findViewById(R.id.informacion_pdi));

            //obtenemos la información de la ruta pública
            informacionPuntoInteres(marker.getId());

            bs.setContentView(info_pdi);
            bs.show();

            return false;
        });
    }

    //se inicia el mapa en el layout
    private void iniciarMapa(){
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(VerRuta.this);
    }


    /* PUNTO DE INTERÉS */
    private void informacionPuntoInteres(String id){
        int i = 0;

        //si el id del punto no es nulo
        if(puntos_ruta.get(id) != null) {
            //obtenemos la información del punto
            pdi = puntos_ruta.get(id);

            //si el usuario no es el creador del punto/ruta
            //eliminamos la opción de modificar y eliminar el punto
            if(!uid_ruta.equals(uid)){
                info_pdi.findViewById(R.id.modificar_punto).setVisibility(View.GONE);
                info_pdi.findViewById(R.id.eliminar_punto).setVisibility(View.GONE);
            }
            //si es el usuario el creador, preparamos los botones
            else{
                info_pdi.findViewById(R.id.modificar_punto).setOnClickListener(v -> {
                    prepararModificarPDI(id);
                });
                info_pdi.findViewById(R.id.eliminar_punto).setOnClickListener(v -> eliminarPDI(id));
            }

            /* Actualizamos en la vista los datos */
            //actualizamos el nombre
            if(!("").equals(pdi.getNombre())){((TextView) info_pdi.findViewById(R.id.nombre_punto)).setText(pdi.getNombre()); }
            else{ info_pdi.findViewById(R.id.nombre_punto).setVisibility(View.GONE); i++;}

            //actualizamos el horario
            if(!("").equals(pdi.getHorario())){ ((TextView) info_pdi.findViewById(R.id.horario_punto)).setText(pdi.getHorario()); }
            else{ info_pdi.findViewById(R.id.ll_horario_punto).setVisibility(View.GONE); i++; }

            //actualizamos el precio
            if(!("").equals(pdi.getPrecio())){ ((TextView) info_pdi.findViewById(R.id.precio_punto)).setText(pdi.getPrecio()); }
            else{ info_pdi.findViewById(R.id.ll_precio_punto).setVisibility(View.GONE); i++; }

            //actualizamos el enlace
            if(!("").equals(pdi.getEnlace())){ ((TextView) info_pdi.findViewById(R.id.enlace_punto)).setText(pdi.getEnlace()); }
            else{ info_pdi.findViewById(R.id.ll_enlace_punto).setVisibility(View.GONE); i++; }

            //actualizamos el audioguia
            if(!("").equals(pdi.getAudioguia())){ ((TextView) info_pdi.findViewById(R.id.audioguia_punto)).setText(pdi.getAudioguia()); }
            else{ info_pdi.findViewById(R.id.ll_audioguia_punto).setVisibility(View.GONE); i++; }

            //actualizamos la historia
            if(!("").equals(pdi.getHistoria())){ ((TextView) info_pdi.findViewById(R.id.historia_punto)).setText(pdi.getHistoria()); }
            else{ info_pdi.findViewById(R.id.ll_historia_punto).setVisibility(View.GONE); i++; }

            //actualizamos los datos curiosos
            if(!("").equals(pdi.getDatosCuriosos())){ ((TextView) info_pdi.findViewById(R.id.datoscuriosos_punto)).setText(pdi.getDatosCuriosos()); }
            else{ info_pdi.findViewById(R.id.ll_datoscuriosos_punto).setVisibility(View.GONE); i++; }

            //actualizamos la opinión
            if(!("").equals(pdi.getOpinion())){ ((TextView) info_pdi.findViewById(R.id.opinion_punto)).setText(pdi.getOpinion()); }
            else{ info_pdi.findViewById(R.id.ll_opinion_punto).setVisibility(View.GONE); i++; }

            //si el punto no contiene información pero sí tiene el nombre, muestra el mensaje de punto vacío
            if(i >= 8 || (i == 7 && !("").equals(pdi.getNombre()))){
                info_pdi.findViewById(R.id.separador1).setVisibility(View.GONE);
                info_pdi.findViewById(R.id.separador2).setVisibility(View.GONE);
            }
            //si el punto sí tiene información, ocultamos el aviso
            else{
                //eliminamos el aviso
                info_pdi.findViewById(R.id.aviso_punto_vacio).setVisibility(View.GONE);

                //si no tiene opinión, eliminamos el separador de arriba
                if(("").equals(pdi.getOpinion())){ info_pdi.findViewById(R.id.separador1).setVisibility(View.GONE); }

                //si no tiene historia ni datos curiosos, eliminamos el separador de arriba
                if(("").equals(pdi.getHistoria()) && ("").equals(pdi.getDatosCuriosos())){
                    info_pdi.findViewById(R.id.separador2).setVisibility(View.GONE);
                }
            }
        }
    }

    //se prepara el PDI para llamar a la actividad de modificar punto
    private void prepararModificarPDI(String id){
        Intent i = new Intent(VerRuta.this, ModificarPuntoInteres.class);
        i.putExtra("id_marcador", id);
        i.putExtra("nombre", pdi.getNombre());
        i.putExtra("historia", pdi.getHistoria());
        i.putExtra("datos_curiosos", pdi.getDatosCuriosos());
        i.putExtra("horario", pdi.getHorario());
        i.putExtra("precio", pdi.getPrecio());
        i.putExtra("enlace", pdi.getEnlace());
        i.putExtra("audioguia", pdi.getAudioguia());
        i.putExtra("opinion", pdi.getOpinion());
        i.putExtra("vengo_de", "ver");
        informacionPDI.launch(i);
    }

    //eliminamos el pdi de la ruta y del mapa
    private void eliminarPDI(String id){

        //eliminamos el pdi de la ruta
        mp.eliminarPunto(ruta.getNombre(), (puntos_ruta.get(id)).getId());
        puntos_ruta.remove(id);

        //eliminamos el pdi del mapa
        Marker m = puntos_mapa.get(id);
        m.remove();
        puntos_mapa.remove(id);

        tam_coleccion = tam_coleccion - 1;
        /*if(tam_coleccion > 1){
        ((TextView) info_ruta.findViewById(R.id.npuntos_ruta))
                .setText(getResources().getText(R.string.num_puntos) + num);
        }
        else{
            ((TextView) info_ruta.findViewById(R.id.npuntos_ruta))
                    .setText(getResources().getText(R.string.num_puntos) + "0");
        }*/

        //cerramos el bottomsheet
        bs.dismiss();
    }

    //actualizamos el punto de interés en la bd y en la lista de puntos
    private void actualizarPDI(Bundle datos){
        //se actualiza en la bd
        pdi.setPunto(datos);
        mp.registrarPunto(ruta.getNombre(), pdi);

        //cerramos el bottomsheet
        bs.dismiss();

        tam_coleccion = tam_coleccion + 1;
        String num = tam_coleccion + "";
        ((TextView) info_ruta.findViewById(R.id.npuntos_ruta)).setText(getResources().getText(R.string.num_puntos) + num);
    }


    /* RUTA */
    //obtenemos los puntos de la ruta
    private void obtenerRuta(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("rutas").document(uid_ruta).collection(ruta.getNombre()).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(!task.getResult().isEmpty()){
                            tam_coleccion = task.getResult().size();
                            actualizarMapa(task.getResult());
                        }
                        else{
                            Toast.makeText(VerRuta.this, getResources().getText(R.string.no_ruta), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(VerRuta.this, getResources().getText(R.string.error_ruta), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    //actualizamos el mapa con los puntos
    private void actualizarMapa(QuerySnapshot documentos_ruta){
        float color = 0.0f;                             //color del marcador
        PuntoInteres punto;                             //punto de interés auxiliar
        Marker marcador_mapa;                           //marcador del punto en el mapa
        MarkerOptions marcador = new MarkerOptions();   //marcador

        //para cada documento
        for(DocumentSnapshot doc : documentos_ruta){

            //si no es información, es el documento del punto
            if(!doc.getId().equals("informacion")){
                //obtenemos el punto
                punto = new PuntoInteres(doc.getData());

                //obtenemos las coordenadas para añadir el marcador al mapa
                marcador.position(punto.getCoordenadas());
                marcador.icon(BitmapDescriptorFactory.defaultMarker(color));
                color = (color + 30.0f) % 330.0f;

                //añadimos el marcador
                marcador_mapa = map.addMarker(marcador);

                //añadimos a los puntos el id del marcador y los datos de la ruta
                puntos_ruta.put(marcador_mapa.getId(), punto);
                puntos_mapa.put(marcador_mapa.getId(), marcador_mapa);
            }
            //si es información, obtenemos la información de la ruta
            else{
                ruta = new Ruta(doc.getData());
                map.setMapType(ruta.getMapaBase());

                //si la privacidad de la ruta es privada y el usuario no es el creador
                if(ruta.getPrivacidad() == 0 && !uid_ruta.equals(uid)){
                    Toast.makeText(VerRuta.this, getResources().getText(R.string.ruta_favorita_privada).toString(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

        //si la ruta tiene algún punto de interés, movemos la cámara a la zona de los puntos
        if(documentos_ruta.size() > 1){
            //obtenemos las coordenadas del primer punto de interés
            Map.Entry<String, PuntoInteres> entry = puntos_ruta.entrySet().iterator().next();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(entry.getValue().getCoordenadas(), 6f));
        }
    }

    //preparamos la información de la ruta
    private void iniciarInformacionRuta(){

        //cuando se clica en un marcador, que es una ruta
        findViewById(R.id.informacion_ruta).setOnClickListener(v -> {

            //muestra el bottom sheet con la información
            final BottomSheetDialog bs = new BottomSheetDialog(VerRuta.this,
                    R.style.BottomSheetDialogTheme);

            //obtenemos la vista y la mostramos
            info_ruta = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.r_informacion,
                            findViewById(R.id.ruta_informacion));

            //si la ruta es del propietario, preparamos los botones
            if(uid_ruta.equals(uid)){ prepararRutaPropia(); }

            //si la ruta no es del propietario, eliminamos los botones que la editan
            else{ info_ruta.findViewById(R.id.operaciones_ruta_propia).setVisibility(View.GONE); }

            //obtenemos la información de la ruta pública y mostramos el bottomsheet
            informacionRuta();
            bs.setContentView(info_ruta);
            bs.show();
        });
    }

    //preparamos los botones de ruta propia
    private void prepararRutaPropia(){
        info_ruta.findViewById(R.id.eliminar_ruta).setOnClickListener(v -> eliminarRuta());

        info_ruta.findViewById(R.id.modificar_ruta).setOnClickListener(v -> {
            Intent i = new Intent(VerRuta.this, ModificarRuta.class);
            i.putExtra("nombre", ruta.getNombre());
            startActivity(i);
        });

        info_ruta.findViewById(R.id.agregar_puntos).setOnClickListener(v -> {
            Intent i = new Intent(VerRuta.this, CrearPuntoInteres.class);
            i.putExtra("nombre_ruta", ruta.getNombre());
            i.putExtra("mapa_base", ruta.getMapaBase());
            i.putExtra("privacidad", ruta.getPrivacidad());
            i.putExtra("vengo_de", "ver");
            startActivity(i);
        });
    }

    //muestra un diálogo de confirmación para eliminar la ruta
    private void eliminarRuta(){
        //construimos el diálogo
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.eliminar_ruta);
        adb.setMessage(getResources().getText(R.string.eliminar_ruta_mensaje));
        adb.setPositiveButton(getResources().getText(R.string.eliminar),
                (dialog, which) -> eliminarRutaBD());

        //creamos el diálogo y lo mostramos
        AlertDialog dialogo = adb.create();
        dialogo.show();
    }

    //eliminar ruta de la base de datos
    private void eliminarRutaBD(){
        //eliminamos la ruta y volvemos al perfil
        mr.eliminarRutaBD(ruta);
        startActivity(new Intent(VerRuta.this, GestionarPerfil.class));
        finish();
    }


    //añadimos la información de la ruta al bottomsheet
    private void informacionRuta(){
        int i = 0;

        //si tiene imagen de portada, la establece, si no, elimina la imagen
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("rutas/" + uid_ruta + "/" + ruta.getNombre() + "/portada.jpg");
        ref.getBytes(1024*1024)
                .addOnSuccessListener(bytes ->
                        //si la ruta tiene portada, se obtiene y se actualiza en la vista
                        ((ImageView)info_ruta.findViewById(R.id.portada_ruta))
                        .setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)))
                .addOnFailureListener(e ->
                        //si la ruta no tiene portada, eliminamos la imagen de la vista
                        (info_ruta.findViewById(R.id.portada_ruta)).setVisibility(View.GONE));

        //obtenemos el nombre de la ruta
        ((TextView) info_ruta.findViewById(R.id.nombre_ruta)).setText(ruta.getNombre());

        //obtenemos el tipo de la ruta
        if(!("").equals(ruta.getTipo())){
            ((TextView) info_ruta.findViewById(R.id.tipo_ruta)).setText(
                    getResources().getText(R.string.tipo_ruta_info) + ruta.getTipo());
        }else{ info_ruta.findViewById(R.id.tipo_ruta).setVisibility(View.GONE); }

        //obtenemos el número de puntos
        String num = (tam_coleccion - 1) + "";
        ((TextView) info_ruta.findViewById(R.id.npuntos_ruta))
                .setText(getResources().getText(R.string.num_puntos) + num);

        //obtenemos la duración
        if(!("").equals(ruta.getDuracion())){
            ((TextView) info_ruta.findViewById(R.id.duracion_ruta)).setText(ruta.getDuracion());
        }else{ info_ruta.findViewById(R.id.ll_duracion_ruta).setVisibility(View.GONE); i++; }

        //obtenemos la distancia
        if(!("").equals(ruta.getDistancia())){
            ((TextView) info_ruta.findViewById(R.id.distancia_ruta)).setText(ruta.getDistancia());
        }else{ info_ruta.findViewById(R.id.ll_distancia_ruta).setVisibility(View.GONE); i++; }

        //obtenemos el enlace
        if(!("").equals(ruta.getEnlace())){
            ((TextView) info_ruta.findViewById(R.id.enlace_ruta)).setText(ruta.getEnlace());
        }else{ info_ruta.findViewById(R.id.ll_enlace_ruta).setVisibility(View.GONE); i++; }

        if(i == 3){ info_ruta.findViewById(R.id.separador1).setVisibility(View.GONE); }

        //obtenemos la historia
        if(!("").equals(ruta.getHistoria())){
            ((TextView) info_ruta.findViewById(R.id.historia_punto)).setText(ruta.getHistoria());
        }else{
            info_ruta.findViewById(R.id.ll_historia_punto).setVisibility(View.GONE);
            info_ruta.findViewById(R.id.separador2).setVisibility(View.GONE);
        }
    }


    /* NAVIGATION BOTTOM */
    //prepara el navigation bottom
    private void iniciarNavigationBottom(){
        BottomNavigationView bnv = findViewById(R.id.bottom_bar);
        bnv.setOnItemSelectedListener(item -> {
            barra_nav(item.getItemId());
            return true;
        });
    }

    //barra de navegación
    private void barra_nav(int id){
        if(id == R.id.creacion){ startActivity(new Intent(this, Creacion.class)); finish(); }
        else if(id == R.id.buscador){ startActivity(new Intent(this, Buscador.class)); finish(); }
        else if(id == R.id.perfil){ startActivity(new Intent(this, GestionarPerfil.class)); finish(); }
    }
}
