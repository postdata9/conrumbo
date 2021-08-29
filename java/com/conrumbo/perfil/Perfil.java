package com.conrumbo.perfil;

import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class Perfil {

    private String nombre;
    private String descripcion;
    private String enlace;
    private int privacidad;
    private Bitmap imagen;

    public Perfil(){
        nombre = "";
        descripcion = "";
        enlace = "";
        privacidad = 0;
        imagen = null;
    }

    public Perfil(String n, String d, String e, int p, Bitmap i){
        nombre = n;
        descripcion = d;
        enlace = e;
        privacidad = p;
        imagen = i;
    }

    public Perfil(Perfil p){
        nombre = p.nombre;
        descripcion = p.descripcion;
        enlace = p.enlace;
        privacidad = p.privacidad;
        imagen = p.imagen;
    }

    public Perfil(Map<String, Object> perf){
        nombre = perf.get("nombre").toString();
        descripcion = perf.get("descripcion").toString();
        enlace = perf.get("enlace").toString();
        privacidad = Integer.parseInt(perf.get("privacidad").toString());
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEnlace() {
        return enlace;
    }

    public int getPrivacidad() {
        return privacidad;
    }

    public Bitmap getImagen() {
        return imagen;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public void setPrivacidad(int privacidad) {
        this.privacidad = privacidad;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }

    public void setPerfil(Map<String, Object> perf){
        if (perf.get("nombre") != null) { nombre = perf.get("nombre").toString(); }
        if(perf.get("descripcion") != null){ descripcion = perf.get("descripcion").toString(); }
        if(perf.get("enlace") != null){ enlace = perf.get("enlace").toString(); }
        if(perf.get("privacidad") != null){ privacidad = Integer.parseInt(perf.get("privacidad").toString()); }
    }

    public void setPerfil(Bundle b){
        if(b.get("nombre") != null){ nombre = b.getString("nombre");}
        if(b.get("descripcion") != null){ descripcion = b.getString("descripcion");}
        if(b.get("enlace") != null){ enlace = b.getString("enlace");}
        if(b.get("privacidad") != null){ privacidad = b.getInt("privacidad");}
    }

    public Bundle getBundle(){
        Bundle datos = new Bundle();
        datos.putString("nombre", nombre);
        datos.putString("descripcion", descripcion);
        datos.putString("enlace", enlace);
        datos.putInt("privacidad", privacidad);

        return datos;
    }

    public Map<String, Object> getMap(){
        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("descripcion", descripcion);
        user.put("enlace", enlace);
        user.put("privacidad", privacidad);

        return user;
    }
}
