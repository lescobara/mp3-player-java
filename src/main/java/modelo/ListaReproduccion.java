/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author diotallevi
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaReproduccion {
    
    // Usamos la ruta (String) como clave para evitar duplicados
    private Map<Integer, Cancion> mapaCanciones;

    public ListaReproduccion() {
        this.mapaCanciones = new HashMap<>();
    }

    /**
     * Agrega una canción al mapa.
     * @param cancion Objeto Cancion con metadatos.
     */
    public void agregarCancion(Cancion entradaObjetoCancion,int entradaIndice) {
        mapaCanciones.put(entradaIndice, entradaObjetoCancion);
    }

    /**
     * Elimina una canción mediante su ruta.
     */
    public void eliminarCancion(int entradaIndice) {
        mapaCanciones.remove(entradaIndice);
    }

    /**
     * Retorna el objeto Cancion dada una ruta.
     */
    public Cancion obtenerCancion(int entradaIndice) {
        return mapaCanciones.get(entradaIndice);
    }
        
    /**
     * Retorna los valores como una List (interfaz).
     * Es más flexible que retornar específicamente un ArrayList.
     */
    public List<Cancion> getListaCanciones() {
        // Convertimos la colección de valores del Map a un ArrayList nuevo
        return new ArrayList<>(mapaCanciones.values());
    }
      
    public int totalCanciones() {
        return mapaCanciones.size();
    }
    
    public void vaciarLista() {
        mapaCanciones.clear();
    }
}
