package com.conrumbo.perfil;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.conrumbo.R;
import com.conrumbo.lugares.LugaresVisitados;
import com.conrumbo.rutas.RutasFavoritas;

public class FavoritosPerfilFragment extends Fragment {

    //vista del layout
    private View view;

    public FavoritosPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //se prepara la vista asociada a la clase
        view = inflater.inflate(R.layout.perf_favoritos, container, false);

        //se gestiona la acción de las rutas, puntos y lugares
        gestionarRutas();
        gestionarLugares();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void gestionarRutas(){
        view.findViewById(R.id.rutas_favoritas).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), RutasFavoritas.class)));
    }

    private void gestionarLugares(){
        view.findViewById(R.id.lugares_visitados).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LugaresVisitados.class)));
    }
}