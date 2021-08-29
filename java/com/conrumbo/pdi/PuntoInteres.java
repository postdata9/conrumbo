package com.conrumbo.pdi;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.OAuthCredential;

import java.util.HashMap;
import java.util.Map;

public class PuntoInteres {

    private String id;
    private String nombre;
    private String historia;
    private String datos_curiosos;
    private LatLng coordenadas;
    private String horario;
    private String precio;
    private String enlace;
    private String audioguia;
    private String opinion;

    /* CONSTRUCTORES */
    //constructor
    public PuntoInteres(){
        id = "";
        nombre = "";
        historia = "";
        datos_curiosos = "";
        horario = "";
        precio = "";
        enlace = "";
        audioguia = "";
        coordenadas = new LatLng(0,0);
        opinion = "";
    }

    //constructor por parámetros
    public PuntoInteres(String i, String nom, String his, String dat, LatLng coord, String hor, String prec, String enl, String aud, String op){
        id = i;
        nombre = nom;
        historia = his;
        datos_curiosos = dat;
        coordenadas = new LatLng(coord.latitude, coord.longitude);
        horario = hor;
        precio = prec;
        enlace = enl;
        audioguia = aud;
        opinion = op;
    }

    //constructor de copia
    public PuntoInteres(PuntoInteres pdi){
        id = pdi.id;
        nombre = pdi.nombre;
        historia = pdi.historia;
        datos_curiosos = pdi.datos_curiosos;
        coordenadas = new LatLng(pdi.coordenadas.latitude, pdi.coordenadas.longitude);
        horario = pdi.horario;
        precio = pdi.precio;
        enlace = pdi.enlace;
        audioguia = pdi.audioguia;
        opinion = pdi.opinion;
    }

    public PuntoInteres(Map<String, Object> punto){
        if(punto.get("id") != null){ id = punto.get("id").toString();}
        if(punto.get("nombre") != null){ nombre = punto.get("nombre").toString();}
        if(punto.get("historia") != null){ historia = punto.get("historia").toString();}
        if(punto.get("datosCuriosos") != null){ datos_curiosos = punto.get("datosCuriosos").toString();}
        if(punto.get("coordenadas") != null){
            Double latitud, longitud;
            latitud = ((HashMap<String, Double>) punto.get("coordenadas")).get("latitude");
            longitud = ((HashMap<String, Double>) punto.get("coordenadas")).get("longitude");
            coordenadas = new LatLng(latitud, longitud);
        }
        if(punto.get("horario") != null){ horario = punto.get("horario").toString(); }
        if(punto.get("precio") != null) {precio = punto.get("precio").toString(); }
        if(punto.get("enlace") != null) { enlace = punto.get("enlace").toString(); }
        if(punto.get("audioguia") != null) {audioguia = punto.get("audioguia").toString(); }
        if(punto.get("opinion") != null) { opinion = punto.get("opinion").toString(); }
    }


    /* GET */
    public String getId(){ return id; }

    public String getNombre(){
        return nombre;
    }

    public String getHistoria(){
        return historia;
    }

    public String getDatosCuriosos(){
        return datos_curiosos;
    }

    public LatLng getCoordenadas(){
        return coordenadas;
    }

    public String getHorario(){
        return horario;
    }

    public String getPrecio(){
        return precio;
    }

    public String getEnlace(){
        return enlace;
    }

    public String getAudioguia(){
        return audioguia;
    }

    public String getOpinion() { return opinion; }


    /* SET */
    public void setId(String i){ id = i; }

    public void setNombre(String n){
        nombre = n;
    }

    public void setHistoria(String h){
        historia = h;
    }

    public void setDatosCuriosos(String dc){
        datos_curiosos = dc;
    }

    public void setCoordenadas(LatLng cd){
        coordenadas = cd;
    }

    public void setHorario(String h){
        horario = h;
    }

    public void setPrecio(String p){
        precio = p;
    }

    public void setEnlace(String e){
        enlace = e;
    }

    public void setAudioguia(String a){
        audioguia = a;
    }

    public void setOpinion(String o) { opinion = o; }

    public void setPunto(Map<String, Object> punto){
        if(punto.get("id") != null){ id = punto.get("id").toString();}
        if(punto.get("nombre") != null){ nombre = punto.get("nombre").toString();}
        if(punto.get("historia") != null){ historia = punto.get("historia").toString();}
        if(punto.get("datosCuriosos") != null){ datos_curiosos = punto.get("datosCuriosos").toString();}
        if(punto.get("coordenadas") != null){
            Double latitud, longitud;
            latitud = ((HashMap<String, Double>) punto.get("coordenadas")).get("latitude");
            longitud = ((HashMap<String, Double>) punto.get("coordenadas")).get("longitude");
            coordenadas = new LatLng(latitud, longitud);
        }
        if(punto.get("horario") != null){ horario = punto.get("horario").toString(); }
        if(punto.get("precio") != null) {precio = punto.get("precio").toString(); }
        if(punto.get("enlace") != null) { enlace = punto.get("enlace").toString(); }
        if(punto.get("audioguia") != null) {audioguia = punto.get("audioguia").toString(); }
        if(punto.get("opinion") != null) { opinion = punto.get("opinion").toString(); }
    }

    public void setPunto(Bundle datos){
        //actualizamos los datos
        if(datos.get("nombre") != null) {nombre = datos.getString("nombre");}
        if(datos.get("historia") != null) { historia = datos.getString("historia");}
        if(datos.get("datosCuriosos") != null){ datos_curiosos = datos.get("datosCuriosos").toString();}
        if(datos.get("horario") != null){ horario = datos.get("horario").toString(); }
        if(datos.get("precio") != null) { precio = datos.get("precio").toString(); }
        if(datos.get("enlace") != null) { enlace = datos.get("enlace").toString(); }
        if(datos.get("audioguia") != null) { audioguia = datos.get("audioguia").toString(); }
        if(datos.get("opinion") != null) { opinion = datos.get("opinion").toString(); }
    }
}
