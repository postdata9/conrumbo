package com.conrumbo.rutas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conrumbo.R;
import com.conrumbo.modelo.ModeloRuta;
import com.conrumbo.perfil.GestionarPerfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RutasFavoritas extends AppCompatActivity {

    //conexión con la base de datos y el uid del usuario
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final ModeloRuta mr = new ModeloRuta(
            RutasFavoritas.this, uid);

    //array con la información de las rutas favoritas
    private ArrayList<Object> info_rutas = new ArrayList<>();

    //array con el nombre de cada ruta favorita
    private ArrayList<String> lista_rutas_favoritas = new ArrayList<>();

    //grid para la vista
    private GridView grid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_grid);
        grid = findViewById(R.id.rutas_grid);

        //obtenemos la lista de rutas públicas
        obtenerRutasFavoritas();

        //prepara la interacción con el grid
        prepararInteraccionGrid();
    }


    /* OBTENER RUTAS FAVORITAS*/
    private void obtenerRutasFavoritas(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        //obtenemos los lugares que ha visitado
        bd.collection("usuarios").document(uid).collection("rutas_favoritas")
                .get().addOnCompleteListener(task -> {

            //si se ha realizado la consulta
            if(task.isSuccessful()){
                //y el resultado no está vacío
                if(!task.getResult().isEmpty()){
                    prepararGrid(task.getResult().getDocuments());
                }
                else{
                    Toast.makeText(RutasFavoritas.this, getResources().getText(R.string.rutas_favoritas_vacio), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RutasFavoritas.this, GestionarPerfil.class)); finish();
                }
            }
        });
    }

    /* PREPARAR LA VISTA */
    //añade las rutas a la vista
    private void prepararGrid(List<DocumentSnapshot> rutas_favoritas){

        //para cada documento, obtenemos el nombre y los datos
        for(DocumentSnapshot doc : rutas_favoritas){
            info_rutas.add(doc.getData());
            lista_rutas_favoritas.add(doc.getData().get("nombre").toString());
        }

        grid.setAdapter(new RutasPropiasAdaptador(this, lista_rutas_favoritas));
    }

    //prepara los botones para la interacción
    private void prepararInteraccionGrid(){

        //si clica en la ruta, se lleva a la actividad para ver la ruta
        grid.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(RutasFavoritas.this, VerRuta.class);
            i.putExtra("nombre", ((Map<String, Object>) info_rutas.get(position)).get("nombre").toString());
            i.putExtra("uid", ((Map<String, Object>) info_rutas.get(position)).get("uid").toString());
            startActivity(i);
        });

        //si realiza un clic largo, da la opción de eliminar la ruta
        grid.setOnItemLongClickListener((parent, view, position, id) -> {

            //creamos el popup
            PopupMenu popup = new PopupMenu(RutasFavoritas.this, view, Gravity.END);
            popup.getMenuInflater().inflate(R.menu.r_borrar_favorita, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {

                //obtenemos el nombre del documento de la ruta favorita
                String referencia = ((Map<String, Object>) info_rutas.get(position)).get("uid").toString()
                + ", " + ((Map<String, Object>) info_rutas.get(position)).get("nombre").toString();

                //eliminamos el documento de la bd y de la lista de favs y actualizamos el grid
                mr.eliminarRutaFavorita(referencia);
                lista_rutas_favoritas.remove(position);
                grid.setAdapter(new RutasPropiasAdaptador(RutasFavoritas.this, lista_rutas_favoritas));
                return true;
            });

            //mostramos el popup
            popup.show();
            return true;
        });
    }
}
