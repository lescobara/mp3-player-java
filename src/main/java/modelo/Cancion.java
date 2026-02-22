/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author diotallevi
 */
public class Cancion {
    
    private final String titulo;
    private final String autor;
    private final String album;
    private final String ruta;
    private final long duracion;
    private final String genero;
    private final long tamano;

    public Cancion(String titulo, String autor, String album, String ruta, 
            long duracion, long tamano, String genero) {
        this.titulo = titulo;
        this.autor = autor;
        this.album = album;
        this.ruta = ruta;
        this.duracion = duracion;
        this.tamano=tamano;
        this.genero = genero;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getAlbum() {
        return album;
    }

    public String getRuta() {
        return ruta;
    }

    public long getDuracion() {
        return duracion;
    }

    public String getGenero() {
        return genero;
    }
    
    public long getTamano(){
        return tamano;
    }
    
    // Método de conveniencia para la Vista (UI)
    public String getDuracionFormateada() {
        long segundosTotales = duracion / 1000000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
       
}
