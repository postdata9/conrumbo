package com.conrumbo.lugares;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.conrumbo.R;
import com.conrumbo.modelo.ModeloLugar;
import com.conrumbo.perfil.GestionarPerfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LugaresVisitados extends AppCompatActivity {

    //datos necesarios
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ListView lv;
    private LugarVisitadoAdaptador lv_adapter;
    private ArrayList<String> lugares_visitados = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lv_lista);
        lv = findViewById(R.id.lista_lugares_visitados);
        obtenerLugares();
        prepararInteraccionLista();
    }

    //se obtienen los lugares visitados del usuario
    private void obtenerLugares(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        //obtenemos los lugares que ha visitado
        bd.collection("usuarios").document(uid).collection("lugares_visitados")
                .get().addOnCompleteListener(task -> {

                    //si se ha realizado la consulta
                    if(task.isSuccessful()){
                        //y el resultado no está vacío
                        if(!task.getResult().isEmpty()){
                            prepararLista(task.getResult().getDocuments());
                        }
                        else{
                            Toast.makeText(LugaresVisitados.this, getResources().getText(R.string.lugares_visitados_vacio), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LugaresVisitados.this, GestionarPerfil.class)); finish();
                        }
                    }
                });
    }

    //preparamos la lista de lugares visitados y los mostramos en el listview
    private void prepararLista(List<DocumentSnapshot> lugares){
        //para cada documento, obtenemos el nombre y los datos
        for(DocumentSnapshot doc : lugares){
            //info_lugares.put(doc.getId(), doc.getData());
            lugares_visitados.add(doc.getId());
        }

        lv_adapter = new LugarVisitadoAdaptador(this, lugares_visitados);
        lv.setAdapter(lv_adapter);
    }

    //preparamos la interacción con la lista
    private void prepararInteraccionLista(){
        //si realiza un clic largo
        lv.setOnItemLongClickListener((parent, view, position, id) -> {

            //preparamos el popup para mostrar la opción de eliminar lugar
            PopupMenu popup = new PopupMenu(LugaresVisitados.this, view, Gravity.END);
            popup.getMenuInflater().inflate(R.menu.lv_borrar, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {

                //si clica se elimina el lugar de la base de datos
                ModeloLugar ml = new ModeloLugar(LugaresVisitados.this, uid);
                ml.eliminarLugar(lugares_visitados.get(position));

                //se elimina el lugar de la vista
                lugares_visitados.remove(position);
                lv_adapter = new LugarVisitadoAdaptador(LugaresVisitados.this, lugares_visitados);
                lv.setAdapter(lv_adapter);
                return true;
            });
            //se muestra el popup
            popup.show();
            return true;
        });
    }
}