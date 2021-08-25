package com.conrumbo.rutas;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.conrumbo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Map;

public class RutasPropiasFragment extends Fragment {

    //uid del usuario
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //datos necesarios para mostrar las rutas propias
    private ArrayList<String> nombre_rutas = new ArrayList<>();
    private GridView grid;
    private View view;

    public RutasPropiasFragment() {
        // Required empty public constructor
    }

    /*public static RutasPropiasFragment newInstance() {
        RutasPropiasFragment fragment = new RutasPropiasFragment();
        return fragment;
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.r_grid, container, false);
        grid = view.findViewById(R.id.rutas_grid);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //obtenemos la lista de rutas públicas
        obtenerRutasPropias();
    }


    //obtenemos las rutas propias
    private void obtenerRutasPropias(){
        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        //obtenemos los nombres de todas las rutas propias del usuario
        bd.collection("rutas").document(uid).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()) {
                            prepararGrid(task.getResult().getData());
                        }
                    }
                });
    }

    //preparamos el grid para la vista
    private void prepararGrid(Map<String, Object> rp){

        //obtenemos el nombre de las rutas
        for(Map.Entry<String, Object> dat : rp.entrySet()){
            nombre_rutas.add(dat.getKey());
        }

        //las añadimos al grid
        grid = view.findViewById(R.id.rutas_grid);
        grid.setAdapter(new RutasPropiasAdaptador(getContext(), nombre_rutas));
        prepararInteraccionGrid();
    }

    //preparamos la interacción con el grid, si se clica una ruta, se lleva a verla
    private void prepararInteraccionGrid(){

        //si se clica en una ruta,
        grid.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(getActivity(), VerRuta.class);
            i.putExtra("nombre", nombre_rutas.get(position));
            i.putExtra("uid", uid);
            startActivity(i);
        });
    }
}