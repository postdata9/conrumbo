package com.conrumbo.lugares;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.conrumbo.R;

import java.util.ArrayList;

public class LugarVisitadoAdaptador extends BaseAdapter {

    private ArrayList<String> lv;
    private Context context;

    public LugarVisitadoAdaptador(Context cont, ArrayList<String> list){
        context = cont;
        lv = list;
    }

    @Override
    public int getCount() {
        return lv.size();
    }

    @Override
    public String getItem(int position) {
        return lv.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //obtenemos el nombre del lugar
        String lugar_visitado = getItem(position);

        //adaptamos y añadimos a la vista
        convertView = LayoutInflater.from(context).inflate(R.layout.lv_item, null);
        TextView tv = convertView.findViewById(R.id.lugar_visitado_nombre);
        tv.setText(lugar_visitado);

        return convertView;
    }
}
