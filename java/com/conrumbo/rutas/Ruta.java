package com.conrumbo.rutas;

import java.util.Map;

public class Ruta {

    private String nombre;
    private String tipo;
    private String duracion;
    private String distancia;
    private String historia;
    private String enlace;
    private int mapa_base;
    private int privacidad;

    //constructor
    public Ruta(){
        nombre = "";
        tipo = "";
        duracion = "";
        distancia = "";
        historia = "";
        enlace = "";
        mapa_base = 1;  //mapa básico
        privacidad = 0; //ruta privada
    }

    //constructor por parámetros
    public Ruta(String n, String t, String dur, String dis, String his, String enl, int mb, int pri){
        nombre = n;
        tipo = t;
        duracion = dur;
        distancia = dis;
        historia = his;
        enlace = enl;
        mapa_base = mb;
        privacidad = pri;
    }

    //constructor de copia
    public Ruta(Ruta r){
        nombre = r.nombre;
        tipo = r.tipo;
        duracion = r.duracion;
        distancia = r.distancia;
        historia = r.historia;
        enlace = r.enlace;
        mapa_base = r.mapa_base;
        privacidad = r.privacidad;
    }

    //constructor dado un map
    public Ruta(Map<String, Object> ruta){
        nombre = ruta.get("nombre").toString();
        tipo = ruta.get("tipo").toString();
        duracion = ruta.get("duracion").toString();
        distancia = ruta.get("distancia").toString();
        historia = ruta.get("historia").toString();
        enlace = ruta.get("enlace").toString();
        mapa_base = Integer.parseInt(ruta.get("mapaBase").toString());
        privacidad = Integer.parseInt(ruta.get("privacidad").toString());
    }


    /* GET */
    public String getNombre(){
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getDistancia() {
        return distancia;
    }

    public String getHistoria() {
        return historia;
    }

    public String getEnlace() {
        return enlace;
    }

    public int getMapaBase() {
        return mapa_base;
    }

    public int getPrivacidad() {
        return privacidad;
    }


    /* SET */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public void setDistancia(String distancia) {
        this.distancia = distancia;
    }

    public void setHistoria(String historia) {
        this.historia = historia;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public void setMapaBase(int mapa_base) {
        this.mapa_base = mapa_base;
    }

    public void setPrivacidad(int privacidad) {
        this.privacidad = privacidad;
    }

    public void setRuta(Map<String, Object> ruta){
        nombre = ruta.get("nombre").toString();
        tipo = ruta.get("tipo").toString();
        duracion = ruta.get("duracion").toString();
        distancia = ruta.get("distancia").toString();
        historia = ruta.get("historia").toString();
        enlace = ruta.get("enlace").toString();
        mapa_base = Integer.parseInt(ruta.get("mapaBase").toString());
        privacidad = Integer.parseInt(ruta.get("privacidad").toString());
    }
}
