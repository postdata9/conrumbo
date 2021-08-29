package com.conrumbo.gestion;

import static android.content.ContentValues.TAG;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.conrumbo.R;
import com.conrumbo.perfil.GestionarPerfil;
import com.conrumbo.rutas.VerRuta;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Buscador extends AppCompatActivity implements OnMapReadyCallback {

    //datos para el mapa
    private GoogleMap map;
    private EditText buscador;

    //datos necesarios para la gestión de rutas públicas
    private Map<String, Map<String, Object>> rutas_publicas = new HashMap<>();
    private boolean ruta_favorita = false;
    private View info_rp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gb_buscador);

        //iniciamos el mapa
        iniciarMapa();

        //se prepara el botón con las opciones
        iniciarBuscar();

        //obtener rutas públicas
        obtenerRutasPublicas();

        //iniciamos la barra de navegación
        iniciarNavigationBottom();
    }

    /* Métodos OnMapReadyCallBack */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        //cuando se clica en un marcador del mapa
        map.setOnMarkerClickListener(marker -> {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 13f));
            //cuando se clica en un marcador, que es una ruta
            //muestra el bottom sheet con la información
            final BottomSheetDialog bs = new BottomSheetDialog(Buscador.this,
                    R.style.BottomSheetDialogTheme);

            //obtenemos la vista y la mostramos
            info_rp = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.gb_ruta_publica,
                            findViewById(R.id.informacion_ruta_publica));

            //obtenemos la información de la ruta pública
            informacionRutaPublica(marker.getId());

            //si clica en ver ruta
            info_rp.findViewById(R.id.ver_ruta).setOnClickListener(v -> {
                Intent i = new Intent(Buscador.this, VerRuta.class);
                i.putExtra("nombre", rutas_publicas.get(marker.getId()).get("nombre").toString());
                i.putExtra("uid", rutas_publicas.get(marker.getId()).get("uid").toString());
                startActivity(i);
            });

            //si clica en añadir ruta como favorita
            info_rp.findViewById(R.id.buscador_favorita).setOnClickListener(v -> rutaFavorita(marker.getId()));
            bs.setContentView(info_rp);
            bs.show();
            return false;
        });
    }


    /* INICIAR MAPA */
    //se inicia el mapa en el layout
    private void iniciarMapa(){
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_buscador);
        mapFragment.getMapAsync(Buscador.this);
    }

    //obtenemos la información de la ruta pública y la mostramos
    private void informacionRutaPublica(String id){

        //si no hay un marcador con dicho id
        if(rutas_publicas.get(id) != null){
            //si tiene imagen de portada, la establece, si no, elimina la imagen
            StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child("rutas/" + rutas_publicas.get(id).get("uid") + "/" +
                            rutas_publicas.get(id).get("nombre") + "/portada.jpg");
            ref.getBytes(1024*1024)
                    .addOnSuccessListener(bytes -> ((ImageView)info_rp.findViewById(R.id.buscador_imagen_ruta)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)))
                    .addOnFailureListener(e -> (info_rp.findViewById(R.id.buscador_imagen_ruta)).setVisibility(View.GONE));

            //obtenemos el nombre de la ruta
            ((TextView) info_rp.findViewById(R.id.buscador_nombre_ruta)).setText(rutas_publicas.get(id).get("nombre").toString());

            //obtenemos el número de puntos
            ((TextView) info_rp.findViewById(R.id.buscador_npuntos))
                    .setText(getResources().getText(R.string.num_puntos) + rutas_publicas.get(id).get("numero_puntos").toString());

            //obtenemos la duración
            String duracion = rutas_publicas.get(id).get("duracion").toString();

            //si no está vacía la actualizamos en la vista
            if (!duracion.isEmpty()) { ((TextView) info_rp.findViewById(R.id.buscador_duracion)).setText(duracion); }
            //si está vacía, quitamos la vista
            else { (info_rp.findViewById(R.id.buscador_duracion)).setVisibility(View.GONE); }

            //obtenemos la duración
            String distancia = rutas_publicas.get(id).get("distancia").toString();

            //si no está vacía la actualizamos en la vista
            if (!distancia.isEmpty()) { ((TextView) info_rp.findViewById(R.id.buscador_distancia)).setText(distancia); }
            //si está vacía, quitamos la vista
            else { (info_rp.findViewById(R.id.buscador_distancia)).setVisibility(View.GONE); }

            //comprobamos si la ruta ya está añadida a favoritas
            String nombre = rutas_publicas.get(id).get("nombre").toString();
            String uid = rutas_publicas.get(id).get("uid").toString();

            FirebaseFirestore bd = FirebaseFirestore.getInstance();
            bd.collection("usuarios").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("rutas_favoritas")
                    .document(uid + ", " + nombre).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            //si el resultado existe, se establece el corazón relleno
                            if(task.getResult().exists()){
                                ruta_favorita = true;
                                (info_rp.findViewById(R.id.marcado_fav)).setBackground(getDrawable(R.drawable.ic_fav_relleno));
                                ((TextView) info_rp.findViewById(R.id.texto_fav)).setText(R.string.eliminar_ruta_fav);
                            } else{ ruta_favorita = false; }
                        }
                    });
        }
        else{ Toast.makeText(Buscador.this, getResources().getText(R.string.error_informacion_ruta),Toast.LENGTH_SHORT).show(); }
    }

    //mostrar mensaje para añadir o eliminar una ruta
    private void rutaFavorita(String id){
        AlertDialog.Builder adb = new AlertDialog.Builder(Buscador.this);

        //si está marcado como ruta favorita
        //se muestra la opción para eliminarla
        if(ruta_favorita) {
            adb.setTitle(R.string.eliminar_ruta_fav);
            adb.setMessage(R.string.eliminar_ruta_fav_mensaje);
            adb.setPositiveButton(R.string.eliminar, (dialog, which) ->
                    eliminarRutaFavorita(
                            rutas_publicas.get(id).get("nombre").toString(),
                            rutas_publicas.get(id).get("uid").toString()));
            adb.setNegativeButton(R.string.cancelar, (dialog, which) ->
                    Toast.makeText(Buscador.this, getResources().getText(R.string.ruta_no_eliminada_favorita).toString(),
                            Toast.LENGTH_SHORT).show());
        }
        //si no está marcada como favorita
        //se muestra la opción para añadirla como favorita
        else{
            adb.setTitle(R.string.agregar_ruta_fav);
            adb.setMessage(R.string.agregar_ruta_fav);
            adb.setPositiveButton(R.string.favorita, (dialog, which) ->
                    agregarRutaFavorita(
                            rutas_publicas.get(id).get("nombre").toString(),
                            rutas_publicas.get(id).get("uid").toString(),
                            rutas_publicas.get(id).get("numero_puntos").toString()));

            adb.setNegativeButton(R.string.cancelar, (dialog, which) ->
                    Toast.makeText(Buscador.this, getResources().getText(R.string.ruta_no_agregada_favorita).toString(),
                            Toast.LENGTH_SHORT).show());
        }

        AlertDialog dialogo = adb.create();
        dialogo.show();
    }

    private void agregarRutaFavorita(String nombre, String uid, String num_puntos){
        Map<String, String> rf = new HashMap<>();
        rf.put("nombre", nombre);
        rf.put("uid", uid);
        rf.put("numero_puntos", num_puntos);

        //agregamos el nombre y datos necesarios
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("usuarios").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("rutas_favoritas")
                .document(uid + ", " + nombre).set(rf, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    //si la consulta se ha realizado
                    if(task.isSuccessful()){
                        (info_rp.findViewById(R.id.marcado_fav)).setBackground(getDrawable(R.drawable.ic_fav_relleno));
                        ((TextView)info_rp.findViewById(R.id.texto_fav)).setText(getResources().getText(R.string.eliminar_ruta_fav));
                        ruta_favorita = true;
                    }
                    else{ Toast.makeText(Buscador.this, getResources().getText(R.string.ruta_favorita_agregada_error), Toast.LENGTH_SHORT).show(); }
                });
    }

    private void eliminarRutaFavorita(String nombre, String uid){

        //eliminamos la ruta pública de la lista de favoritas
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("usuarios").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("rutas_favoritas")
                .document(uid + ", " + nombre).delete()
                .addOnCompleteListener(task -> {
                    //si la consulta se ha realizado
                    if(task.isSuccessful()){
                        (info_rp.findViewById(R.id.marcado_fav)).setBackground(getDrawable(R.drawable.ic_fav));
                        ((TextView)info_rp.findViewById(R.id.texto_fav)).setText(getResources().getText(R.string.add_fav));
                        ruta_favorita = false;
                    } else{ Toast.makeText(Buscador.this, getResources().getText(R.string.ruta_favorita_eliminada_error), Toast.LENGTH_SHORT).show(); }
                });
    }


    /* INICIAR BUSCADOR */
    //se prepara el cajón de búsqueda y los botones necesarios
    private void iniciarBuscar(){

        //si se clica la imagen de buscar, se gelocaliza el lugar
        (findViewById(R.id.lupa_buscador_buscador)).setOnClickListener(v -> geolocalizar());

        //cuando se escribe en el cajón y se pulsa una tecla del teclado
        // que sea enter/buscar, se geolocaliza el lugar
        buscador = findViewById(R.id.input_buscador_buscador);
        buscador.setOnEditorActionListener((v, actionId, event) -> {

            //si la acción es BUSCAR o ENTER
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.KEYCODE_ENTER){

                //localizamos el lugar
                geolocalizar();
            }
            return false;
        });
    }

    //localiza el lugar obtenido del cajón de búsqueda
    private void geolocalizar(){
        //obtenemos el lugar
        String lugar = buscador.getText().toString();

        if(!lugar.isEmpty()) {
            Geocoder geocoder = new Geocoder(Buscador.this);
            List<Address> list = new ArrayList<>();
            try {
                list = geocoder.getFromLocationName(lugar, 1);
            } catch (IOException e) {
                Log.e(TAG, "geolocalizar: IOException: " + e.getMessage());
            }

            //si la lista no está vacía
            if(!list.isEmpty()){
                //obtenemos la primera localización, mostramos el lugar en pantalla y movemos la cámara
                Address ad = list.get(0);

                //almacenamos el nombre del lugar y las coordenadas
                LatLng lugar_buscado = new LatLng(ad.getLatitude(), ad.getLongitude());

                //movemos la cámara al marcador
                moverCamaraMapa(lugar_buscado, 13f);

                //cerrar el teclado
                cerrarTeclado();
            }
            else{ Toast.makeText(Buscador.this, getResources().getText(R.string.buscador_lista_vacia), Toast.LENGTH_SHORT).show(); }
        }
        else{ Toast.makeText(this, getResources().getText(R.string.geolicalizar_vacio), Toast.LENGTH_SHORT).show(); }
    }

    //mover la cámara
    private void moverCamaraMapa(LatLng ll, float z){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, z));
    }

    //para cerrar el teclado
    private void cerrarTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(buscador.getWindowToken(), 0);
    }


    /* OBTENER RUTAS PÚBLICAS */
    //obtener las rutas públicas
    private void obtenerRutasPublicas(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("rutas_publicas").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(Buscador.this, getResources().getText(R.string.buscador_si_rutas), Toast.LENGTH_SHORT).show();
                actualizarMapaRutasPublicas(task.getResult().getDocuments());
            }
            else{ Toast.makeText(Buscador.this, getResources().getText(R.string.buscador_no_rutas), Toast.LENGTH_SHORT).show(); }
        });
    }

    //añadimos las rutas públicas al mapa
    private void actualizarMapaRutasPublicas(List<DocumentSnapshot> rp){
        Marker marcador_mapa;
        MarkerOptions marcador = new MarkerOptions();
        Map<String, Object> datos;
        Double longitud, latitud;
        LatLng posicion;
        float color = 0.0f;

        for(int i = 0; i < rp.size(); i++){

            //obtenemos los datos
            datos = rp.get(i).getData();

            //si hay algún punto asociado a la ruta
            if(datos.get("coordenadas") != null) {
                //obtenemos la posición en el mapa
                latitud = ((HashMap<String, Double>) datos.get("coordenadas")).get("latitude");
                longitud = ((HashMap<String, Double>) datos.get("coordenadas")).get("longitude");
                posicion = new LatLng(latitud, longitud);

                //obtenemos las coordenadas para añadir el marcador al mapa
                marcador.position(posicion);
                marcador.icon(BitmapDescriptorFactory.defaultMarker(color));
                color = (color + 30.0f) % 330.0f;

                //añadimos el marcador
                marcador_mapa = map.addMarker(marcador);

                //añadimos a las rutas públicas el id del marcador y los datos de la ruta
                rutas_publicas.put(marcador_mapa.getId(), datos);
            }
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

    // para la barra de navegación
    private void barra_nav(int id){
        if(id == R.id.creacion){
            startActivity(new Intent(this, Creacion.class)); finish();
        }
        else if(id == R.id.perfil){
            startActivity(new Intent(this, GestionarPerfil.class)); finish();
        }
        else if(id == R.id.buscador){
            finish();
            startActivity(getIntent());
        }
    }
}
