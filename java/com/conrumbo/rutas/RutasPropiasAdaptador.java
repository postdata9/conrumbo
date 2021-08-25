package com.conrumbo.rutas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.conrumbo.R;

import java.util.ArrayList;

public class RutasPropiasAdaptador extends BaseAdapter {

    private ArrayList<String> rutas_propias;
    private Context context;

    RutasPropiasAdaptador(Context cont, ArrayList<String> l){
        rutas_propias = l;
        context = cont;
    }

    @Override
    public int getCount() {
        return rutas_propias.size();
    }

    @Override
    public Object getItem(int position) {
        return rutas_propias.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        convertView = LayoutInflater.from(context).inflate(R.layout.r_item, null);

        TextView tv = convertView.findViewById(R.id.nombre_ruta_propia);
        tv.setText(rutas_propias.get(position));

        return convertView;
    }
}
