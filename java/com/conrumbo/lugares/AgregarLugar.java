package com.conrumbo.lugares;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.conrumbo.R;
import com.conrumbo.modelo.ModeloLugar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarLugar extends AppCompatActivity implements OnMapReadyCallback {

    //datos para el mapa
    private GoogleMap map;

    //datos necesarios para la adición de un lugar como visitado
    private Button agregar_lugar;
    private EditText buscador;
    private MarkerOptions marcador = new MarkerOptions();
    private Marker marcador_mapa;
    private String nombre_lugar;
    private LatLng coordenadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lv_agregar);

        //iniciamos el mapa
        iniciarMapa();

        //se prepara para buscar
        iniciarBuscar();
    }

    /* Métodos OnMapReadyCallBack */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;

        //cuando se clica en un punto de interés
        map.setOnPoiClickListener(pointOfInterest -> {
            nombre_lugar = pointOfInterest.name;
            coordenadas = pointOfInterest.latLng;
            addMarcador();
            iniciarAgregarLugar();
        });
    }


    /* INICIAR MAPA */
    //iniciar el mapa
    private void iniciarMapa(){
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(AgregarLugar.this);
    }

    //localiza el lugar obtenido del cajón de búsqueda
    private void geolocalizar(){
        //obtenemos el lugar
        String lugar = buscador.getText().toString();

        if(!lugar.isEmpty()) {
            Geocoder geocoder = new Geocoder(AgregarLugar.this);
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
                nombre_lugar = lugar;
                coordenadas = new LatLng(ad.getLatitude(), ad.getLongitude());

                //añadimos un marcador
                addMarcador();

                //habilitamos el botón de añadir lugar
                iniciarAgregarLugar();

                //movemos la cámara al marcador
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 13f));

                //cerrar el teclado
                cerrarTeclado();
            }
        }
        else{
            Toast.makeText(this, getResources().getText(R.string.geolicalizar_vacio), Toast.LENGTH_SHORT).show();
        }
    }

    /* añadir marcador al mapa en las coordenadas ll (latitud, longitud)*/
    private void addMarcador(){
        if(marcador_mapa != null){
            marcador_mapa.remove();
        }

        marcador.position(coordenadas);
        marcador_mapa = map.addMarker(marcador);
    }

    //para cerrar el teclado
    private void cerrarTeclado(){
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(buscador.getWindowToken(), 0);
    }

    /* INICIAR BUSCAR */
    //se prepara el cajón de búsqueda y los botones necesarios
    private void iniciarBuscar(){
        //ocultamos el botón de añadir ciudad
        agregar_lugar = findViewById(R.id.agregar_lugar);
        agregar_lugar.setVisibility(View.GONE);

        //si se clica la imagen de buscar, se gelocaliza el lugar
        (findViewById(R.id.lupa_buscador)).setOnClickListener(v -> geolocalizar());

        //cuando se escribe en el cajón y se pulsa una tecla del teclado
        // que sea enter/buscar, se geolocaliza el lugar
        buscador = findViewById(R.id.input_buscador);
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

    //cuando se clica en agregar lugar
    private void iniciarAgregarLugar(){
        //habilitamos el botón de añadir lugar
        agregar_lugar.setVisibility(View.VISIBLE);
        agregar_lugar.setOnClickListener(v -> {

            //los datos a agregar
            Map<String, Object> lugar_visitado = new HashMap<>();
            lugar_visitado.put("nombre", nombre_lugar);
            lugar_visitado.put("coordenadas", new GeoPoint(coordenadas.latitude, coordenadas.longitude));

            ModeloLugar ml = new ModeloLugar(AgregarLugar.this, FirebaseAuth.getInstance().getCurrentUser().getUid());
            ml.agregarLugar(nombre_lugar, lugar_visitado);
        });
    }
}
