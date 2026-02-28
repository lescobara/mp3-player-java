/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import modelo.Cancion;
import modelo.ListaReproduccion;
import modelo.ReproductorModelo;
import vista.ReproductorVista;

/**
 *
 * @author diotallevi
 */

import java.util.Map;
import javax.swing.JOptionPane;
import javazoom.jlgui.basicplayer.BasicPlayer;

/**
 * CONTROLADOR CENTRAL (Patrón Mediador)
 * --------------------------------------
 * Esta clase actúa como el "Director de Orquesta". Es la única que conoce 
 * el estado general del reproductor y coordina a los sub-controladores 
 * (Botones, Tabla, Reproducción) para que trabajen juntos sin acoplarse entre sí.
 */
public class Controlador {
    
    // === COMPONENTES PRINCIPALES ===
    private final ReproductorModelo modelo;
    private final ReproductorVista vista;
    private final ListaReproduccion lista;
    
    // === ESTADO COMPARTIDO ===
    // Estas variables son privadas para forzar a los sub-controladores a usar getters/setters.
    // Esto asegura que nadie cambie el estado sin que el Controlador Central se entere.
    private int contadorIndice; //atributo empleado para ir asignando índices en el hashmap
    private int indiceActual = 0; //atributo empleado para saber qué canción se va a reproducir
    protected boolean estaPausado = false;
    protected boolean esNuevaCarga = false;
    protected long tamanoArchivo;
    
    // === SUB-CONTROLADORES ===
    private final ControladorBotones ctrlBotones;
    private final ControladorTabla ctrlTabla;
    private final ControladorReproduccion ctrlReproduccion;
    
    /**
     * Constructor: Inicializa la aplicación y distribuye las responsabilidades
     * instanciando a los sub-controladores especializados.
     */
    public Controlador(ReproductorVista vista) {
        this.modelo = ReproductorModelo.getInstance();
        this.vista = vista;
        this.lista = modelo.getLista();
        
        // Se instancian los sub-controladores pasándoles una referencia de este orquestador (this)
        this.ctrlReproduccion = new ControladorReproduccion(this, modelo, vista, lista);
        this.ctrlBotones = new ControladorBotones(this, vista);
        this.ctrlTabla = new ControladorTabla(this, vista, lista);
    }
    
    // =========================================================================
    //                    MÉTODOS DE MEDIACIÓN Y ORQUESTACIÓN
    // =========================================================================
    
    /**
     * MEDIADOR PRINCIPAL DE REPRODUCCIÓN
     * Cambia la canción actual y coordina a todos los componentes visuales y de audio.
     * * @param indice El ID de la canciÓn que se desea reproducir.
     */
    public void reproducirIndice(int indice) {
        try {
            // 1. Actualiza el estado central
            this.indiceActual = indice;
            Cancion cancion = lista.obtenerCancion(this.indiceActual);

            if (cancion != null) {
                this.esNuevaCarga = false;
                // 2. ORQUESTA AL MODELO: Prepara y da play al audio
                modelo.prepararDesdeLista(this.indiceActual);
                modelo.reproducir();
                this.estaPausado = false;
                // 3. ORQUESTA A REPRODUCCIÓN: Le pide que actualice los textos (Labels)
                ctrlReproduccion.actualizarInfo(cancion);
                // 4. ORQUESTA A LA TABLA: Le pide que mueva el scroll y resalte la fila
                ctrlTabla.resaltarCancionEnTabla(this.indiceActual);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Error: " + ex.getMessage());
        }
    }
     
    /**
     * MEDIADOR DE CARGA DE ARCHIVOS
     * Procesa un nuevo archivo MP3, lo agrega a la lista y decide si debe 
     * reproducirlo automáticamente o solo dejarlo en cola.
     */
    public void procesarNuevoArchivo(String ruta) {
        try {
            // 1. Extrae metadatos y crea el objeto
            Map props = modelo.extraerMetadatosSilencioso(ruta);
            Cancion nueva = ctrlReproduccion.crearCancionDesdeMap(props, ruta);

            // 2. Guardamos el índice que le va a tocar a esta nueva canci�n
            int indiceNuevo = this.contadorIndice; 
            
            // 3. SIEMPRE agregamos a la lista (HashMap) e incrementamos el contador
            lista.agregarCancion(nueva, indiceNuevo);
            this.contadorIndice++;

            // 4. SIEMPRE actualizamos la tabla para mostrar la nueva canción en pantalla
            ctrlTabla.actualizarTabla();

            // ==========================================
            // 5. LÓGICA DE AUTO-REPRODUCCIÓN
            // ==========================================
            int estado = modelo.getEstado();
            
            // Si es la primera canción que agregamos, o si el reproductor está detenido
            if (estado == -1 || estado == 2) { 
                // Reproducimos automáticamente la que acabamos de agregar
                reproducirIndice(indiceNuevo);
            } 
        } catch (Exception ex) {
            System.err.println("Error al procesar metadatos: " + ex.getMessage());
        }
    }
    
    /**
     * MEDIADOR DE PARADA DE REPRODUCCIÓN
     * Detiene el audio, resetea estados lógicos y coordina la limpieza visual.
     */
    public void detenerReproduccion() {
        try {
            // 1. Forzamos el reset de estados lógicos de inmediato
            this.estaPausado = false; 
            
            // ORQUESTA A REPRODUCCIÓN: Limpieza visual
            ctrlReproduccion.limpiarLabels();

            // 2. Solo activamos el 'freno' y llamamos a detener si realmente se está reproduciendo o pausado
            // Esto evita que el comando se quede "atrapado"
            if (modelo.getEstado() != BasicPlayer.STOPPED) {
                modelo.detener();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Error al detener: " + ex.getMessage());
        }
    }
    
    // =========================================================================
    //                           LÓGICA DE NAVEGACIÓN
    // =========================================================================

    /**
     * Calcula cuál es la siguiente pista válida (saltando las que se hayan eliminado).
     * Una vez encontrada, le pide al método orquestador (reproducirIndice) que haga su trabajo.
     */
    public void reproducirSiguiente() {
        if (lista.totalCanciones() == 0) return;

        int siguienteIndice = this.indiceActual + 1;
        boolean encontrado = false;

        // Buscamos hacia adelante saltando los "huecos" (nulls) dejados por borrados
        while (siguienteIndice < this.contadorIndice) {
            if (lista.obtenerCancion(siguienteIndice) != null) {
                encontrado = true;
                break;
            }
            siguienteIndice++; // Si es null, pasamos al siguiente número
        }

        if (encontrado) {
            // Encontramos una canción válida más adelante
            this.esNuevaCarga = false;
            reproducirIndice(siguienteIndice);
        } else {
            // Llegamos al final del mapa. Hacemos bucle a la primera canción válida.
            reproducirIndice(obtenerPrimerIndiceValido());
        }
    }

    /**
     * Calcula cuál es la pista anterior válida (saltando las eliminadas).
     * Una vez encontrada, le pide al método orquestador (reproducirIndice) que haga su trabajo.
     */
    public void reproducirAnterior() {
        if (lista.totalCanciones() == 0) return;

        int anteriorIndice = this.indiceActual - 1;
        boolean encontrado = false;

        // Buscamos hacia atrás saltando los "huecos" (nulls)
        while (anteriorIndice >= 0) {
            if (lista.obtenerCancion(anteriorIndice) != null) {
                encontrado = true;
                break;
            }
            anteriorIndice--;
        }

        if (encontrado) {
            this.esNuevaCarga = false;
            reproducirIndice(anteriorIndice);
        } else {
            // Llegó al principio, hace un bucle saltando a la última canción válida
            reproducirIndice(obtenerUltimoIndiceValido());
        }
    }
    
    // =========================================================================
    //                    MÉTODOS AUXILIARES DE BÚSQUEDA
    // =========================================================================

    private int obtenerPrimerIndiceValido() {
        for (int i = 0; i < this.contadorIndice; i++) {
            if (lista.obtenerCancion(i) != null) {
                return i;
            }
        }
        return 0; // Retorno de seguridad
    }

    private int obtenerUltimoIndiceValido() {
        // Buscamos de reversa desde el índice más alto generado
        for (int i = this.contadorIndice - 1; i >= 0; i--) {
            if (lista.obtenerCancion(i) != null) {
                return i;
            }
        }
        return 0; // Retorno de seguridad
    }
    
    // =========================================================================
    //         GETTERS Y SETTERS (Acceso controlado para sub-controladores)
    // =========================================================================
    public ReproductorModelo getModelo() { return modelo; }
    public ReproductorVista getVista() { return vista; }
    public ListaReproduccion getLista() { return lista; }
    
    public int getContadorIndice() { return contadorIndice; }
    public int getIndiceActual() { return indiceActual; }
    
    public long getTamanoArchivo() { return tamanoArchivo; }
    public void setTamanoArchivo(long tamanoArchivo) { this.tamanoArchivo = tamanoArchivo; }
    public void setEstaPausado(boolean estaPausado) { this.estaPausado = estaPausado; }
    public boolean isEstaPausado() { return estaPausado; }
       
}