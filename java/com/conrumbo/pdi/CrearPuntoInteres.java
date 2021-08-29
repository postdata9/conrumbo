package com.conrumbo.pdi;

import static android.content.ContentValues.TAG;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.conrumbo.R;
import com.conrumbo.gestion.Creacion;
import com.conrumbo.modelo.ModeloPunto;
import com.conrumbo.modelo.ModeloRuta;
import com.conrumbo.rutas.VerRuta;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearPuntoInteres extends AppCompatActivity implements OnMapReadyCallback {

    //datos necesarios
    private static final float ZOOM_DEFECTO = 15f;
    private static final float ZOOM_DEFECTO_PUNTO = 20f;
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final ModeloRuta mr = new ModeloRuta(CrearPuntoInteres.this, uid);
    private final ModeloPunto mp = new ModeloPunto(CrearPuntoInteres.this, uid);

    //datos necesarios
    private GoogleMap map;
    private int mapa_base;
    private String nombre_ruta;
    private int privacidad_ruta;

    //datos necesarios para buscar lugar por el cajón de búsqueda
    private EditText buscador;
    private MarkerOptions marcador = new MarkerOptions();   //se añade al mapa

    //datos para la gestión de los puntos de interés
    private Map<String, PuntoInteres> puntos_ruta = new HashMap<>();    //map con el id del marcador y el pdi
    private Map<String, Marker> puntos_mapa = new HashMap<>();          //map con el id del marcador y el marcador en el mapa
    private PuntoInteres pdi;                                           //pdi auxiliar
    private int id_punto = 0;                                           //id del pdi

    //para obtener el pdi con la información
    ActivityResultLauncher<Intent> informacionPDI = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle datos = result.getData().getExtras();

                    //si está eliminar, eliminamos el pdi
                    if(datos.getString("eliminar") != null){ eliminarPDI(datos.getString("eliminar")); }
                    //por el contrario, actualizamos los datos
                    else{ actualizarPDI(datos); }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdi_crear);
        Toast.makeText(getApplicationContext(), getResources().getText(R.string.aviso_creacion_pdi), Toast.LENGTH_LONG).show();

        //obtenemos el nombre de la ruta
        Bundle datos = getIntent().getExtras();
        if(datos != null) {
            if (datos.getInt("mapa_base") != 0) mapa_base = datos.getInt("mapa_base");
            if (datos.getString("nombre_ruta") != null) nombre_ruta = datos.getString("nombre_ruta");
            if(datos.getInt("privacidad") != 0) privacidad_ruta = datos.getInt("privacidad");
            if(datos.getString("vengo_de") != null){
                //si vengo de ver, se obtienen los puntos actuales de la ruta
                if(datos.getString("vengo_de").equals("ver")){ obtenerPuntos(); }
            }
        }

        //iniciamos el mapa
        iniciarMapa();

        //iniciamos el buscador
        iniciarBuscar();

        //iniciamos el botón de terminar
        iniciarTerminar();
    }

    /* Métodos OnMapReadyCallBack */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(mapa_base);

        //cuando se clica en el mapa, se añade un marcador
        map.setOnMapLongClickListener(latLng ->{
            addMarcador(latLng, null, null);
        });

        //cuando se clica en un punto de interés, se añade un marcador
        map.setOnPoiClickListener(pointOfInterest -> addMarcador(null, pointOfInterest, null));

        //cuando se clica en un marcador del mapa
        map.setOnMarkerClickListener(marker -> {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ZOOM_DEFECTO_PUNTO));
            //cuando se clica en un marcador
            //se lleva a la pantalla para modificar la información
            informacionPDI(marker.getId());
            return false;
        });
    }


    /* INICIAR EL MAPA */
    private void iniciarMapa(){
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(CrearPuntoInteres.this);
    }

    /* añadir marcador al mapa en las coordenadas ll (latitud, longitud)*/
    private void addMarcador(LatLng ll, PointOfInterest poi, Address ad){

        //creamos un punto de interés con las coordenadas
        PuntoInteres p = new PuntoInteres();

        //si es un punto de interés, se obtiene el nombre
        if(poi != null){
            p.setNombre(poi.name);
            p.setCoordenadas(poi.latLng);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, ZOOM_DEFECTO_PUNTO));
            ll = poi.latLng;
        }
        //si es un lugar buscado
        else if(ad != null){
            p.setNombre(ad.getFeatureName());
            p.setCoordenadas(new LatLng(ad.getLatitude(), ad.getLongitude()));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(p.getCoordenadas(), ZOOM_DEFECTO_PUNTO));
            ll = p.getCoordenadas();
        }
        else{
            p.setCoordenadas(ll);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, ZOOM_DEFECTO_PUNTO));
        }

        //posicionamos el marcador aux en la posición
        marcador.position(ll);

        //se añade el marcador al mapa
        Marker marcador_mapa = map.addMarker(marcador);

        //actualizamos el id del punto
        p.setId(id_punto + "");
        id_punto++;

        //añadimos el punto a la ruta y el marcador a los puntos del mapa
        puntos_ruta.put(marcador_mapa.getId(), p);
        puntos_mapa.put(marcador_mapa.getId(), marcador_mapa);

        //registramos el punto en la bd
        mp.registrarPunto(nombre_ruta, p);
    }

    //muestra la ventana para modificar la información de un punto de interés
    private void informacionPDI(String id){

        pdi = puntos_ruta.get(id);

        //se llama a modificar punto de interés con un activity result
        Intent i = new Intent(this, ModificarPuntoInteres.class);
        i.putExtra("nombre", pdi.getNombre());
        i.putExtra("historia", pdi.getHistoria());
        i.putExtra("datosCuriosos", pdi.getDatosCuriosos());
        i.putExtra("horario", pdi.getHorario());
        i.putExtra("precio", pdi.getPrecio());
        i.putExtra("enlace", pdi.getEnlace());
        i.putExtra("audioguia", pdi.getAudioguia());
        i.putExtra("opinion", pdi.getOpinion());
        i.putExtra("id_marcador", id);

        informacionPDI.launch(i);
    }


    /* OBTENER PUNTOS */
    //si se va a crear un punto sobre una ruta que ya tiene puntos de interés
    private void obtenerPuntos(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        bd.collection("rutas").document(uid).collection(nombre_ruta).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(!task.getResult().isEmpty()){ actualizarMapa(task.getResult()); }
                        else{
                            Toast.makeText(CrearPuntoInteres.this,
                                    getResources().getText(R.string.no_puntos_en_ruta), Toast.LENGTH_SHORT).show(); finish(); } }
                    else{
                        Toast.makeText(CrearPuntoInteres.this, getResources().getText(R.string.error_punto), Toast.LENGTH_SHORT).show();
                        finish(); }
                });
    }

    //actualizamos el mapa con los puntos
    private void actualizarMapa(QuerySnapshot documentos_ruta){
        int tam = documentos_ruta.size()-1;
        int j = 0;
        PuntoInteres punto;
        Marker marcador_mapa;
        float color = 0.0f;
        MarkerOptions marcador = new MarkerOptions();

        for(DocumentSnapshot doc : documentos_ruta){
            //obtenemos los documentos de los puntos de interés
            if(!doc.getId().equals("informacion")){
                j++;
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

                if(tam == j){
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(punto.getCoordenadas(), ZOOM_DEFECTO));
                }
            }
        }

        //buscamos cuál es el que tiene el id más alto
        int i, max = 0;
        for(Map.Entry<String, PuntoInteres> m : puntos_ruta.entrySet()){
            i = Integer.parseInt((m.getValue()).getId());
            max = Math.max(i, max);
        }
        id_punto = max + 1;
    }


    /* INICIAR BUSCAR */
    //se prepara el cajón de búsqueda y los botones necesarios
    private void iniciarBuscar(){
        //si se clica la imagen de buscar, se gelocaliza el lugar
        findViewById(R.id.lupa_buscador_punto).setOnClickListener(v -> geolocalizar());

        //cuando se escribe en el cajón y se pulsa una tecla del teclado
        // que sea enter/buscar, se geolocaliza el lugar
        buscador = findViewById(R.id.input_buscador_punto);
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
            Geocoder geocoder = new Geocoder(CrearPuntoInteres.this);
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

                //añadimos un marcador
                addMarcador(null, null, ad);

                //cerrar el teclado
                cerrarTeclado();
            }
            else{
                Toast.makeText(this, getResources().getText(R.string.lugar_no_encontrado), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, getResources().getText(R.string.geolicalizar_vacio), Toast.LENGTH_SHORT).show();
        }
    }

    //para cerrar el teclado
    private void cerrarTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(buscador.getWindowToken(), 0);
    }


    /* INICIAR TERMINAR LA RUTA */
    //cuando termina la ruta
    private void iniciarTerminar(){
        findViewById(R.id.terminar_creacion_ruta).setOnClickListener(v -> {
            //si la ruta es pública y la ruta tiene puntos
            if(privacidad_ruta == 1 && puntos_ruta.size() > 0){
                //añadimos el número de puntos y la localización del primer punto
                mr.actualizarRutaPublica(nombre_ruta, puntos_ruta.size(),
                        ((PuntoInteres)puntos_ruta.values().toArray()[0]).getCoordenadas());
            }
            //si la ruta es pública pero el usuario no ha añadido ningún punto, se elimina de las rutas públicas
            else if(privacidad_ruta == 1 && puntos_ruta.size() == 0){
                mr.eliminarRutaPublica(nombre_ruta);
            }

            //si hay puntos en la ruta
            if(puntos_ruta.size() > 0){ Toast.makeText(CrearPuntoInteres.this, getResources().getText(R.string.exito_creacion_puntos), Toast.LENGTH_SHORT).show();}


            if(getIntent().getExtras().getString("vengo_de").equals("ver")){
                Intent i = new Intent(CrearPuntoInteres.this, VerRuta.class);
                i.putExtra("nombre", nombre_ruta);
                i.putExtra("uid", uid);
                startActivity(i); finish();
            }
            else{
                startActivity(new Intent(CrearPuntoInteres.this, Creacion.class)); finish();
            }
        });
    }


    /* PUNTO DE INTERÉS */
    //actualizar los datos en la bd
    private void actualizarPDI(Bundle datos){

        //obtenemos el pdi, lo actualizamos y lo registramos en la bd
        pdi = puntos_ruta.get(datos.getString("id_marcador"));
        pdi.setPunto(datos);
        mp.registrarPunto(nombre_ruta, pdi);
    }

    //eliminamos el pdi de la ruta y del mapa
    private void eliminarPDI(String id){

        //eliminamos el pdi de la ruta
        mp.eliminarPunto(nombre_ruta, (puntos_ruta.get(id)).getId());
        puntos_ruta.remove(id);

        //eliminamos el pdi del mapa
        Marker m = puntos_mapa.get(id);
        m.remove();
        puntos_mapa.remove(id);
    }
}
