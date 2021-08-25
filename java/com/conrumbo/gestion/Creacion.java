package com.conrumbo.gestion;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.conrumbo.R;
import com.conrumbo.lugares.AgregarLugar;
import com.conrumbo.perfil.GestionarPerfil;
import com.conrumbo.rutas.CrearRuta;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Creacion extends AppCompatActivity implements OnMapReadyCallback {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gc_creacion);

        //iniciamos el mapa
        iniciarMapa();

        //se prepara el botón con las opciones
        iniciarOpciones();

        //se prepara el navigation bottom
        iniciarNavigationBottom();
    }

    /* Métodos OnMapReadyCallBack */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
    }

    //se inicia el mapa en el layout
    private void iniciarMapa(){
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(Creacion.this);
    }

    //prepara el botón de opciones
    private void iniciarOpciones(){
        //establecemos el botón de opciones
        Button opciones = findViewById(R.id.opciones_crear);
        opciones.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(Creacion.this, opciones);
            popup.getMenuInflater().inflate(R.menu.gc_opciones, popup.getMenu());
            popup.setGravity(0);
            popup.setOnMenuItemClickListener(item -> {

                int id = item.getItemId();
                if(id == R.id.crear_rutas){
                    startActivity(new Intent(this, CrearRuta.class).putExtra("accion", "crear_ruta"));
                }
                else if(id == R.id.add_ciudad){
                    startActivity(new Intent(this, AgregarLugar.class));
                }
                else{
                    Toast.makeText(Creacion.this, getResources().getText(R.string.opciones_nada), Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popup.show();
        });
    }

    //prepara el navigation bottom
    private void iniciarNavigationBottom(){
        BottomNavigationView bnv = findViewById(R.id.bottom_bar);
        bnv.setOnItemSelectedListener(item -> {
            barra_nav(item.getItemId());
            return true;
        });
    }

    //para la barra de navegación
    private void barra_nav(int id){
        if(id == R.id.buscador){
            startActivity(new Intent(this, Buscador.class));
        }
        else if(id == R.id.perfil){
            startActivity(new Intent(this, GestionarPerfil.class));
        }
    }
}