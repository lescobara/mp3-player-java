/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.io.File;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

/**
 * MODELO DE REPRODUCCIÓN (Patrón Singleton)
 * ----------------------------------------
 * Se encarga de la lógica "física" del audio y de mantener la lista de canciones.
 * 
 * * ¿POR QUÉ ES SINGLETON?
 * La tarjeta de sonido y el flujo de audio son recursos compartidos. Si permitiéramos 
 * crear múltiples instancias de esta clase (usando 'new'), podríamos tener dos 
 * canciones sonando al mismo tiempo en reproductores distintos, o podríamos enviar 
 * la orden de "Pausar" al reproductor equivocado. El patrón Singleton garantiza que 
 * TODA la aplicación se comunique con el mismo y único motor de audio.
 */
public class ReproductorModelo {
    
    // 1. Instancia estática privada. 
    // La palabra reservada 'volatile' garantiza que los cambios realizados en esta variable 
    // por un hilo sean visibles inmediatamente para los demás hilos, evitando que se creen dos instancias.
    private static volatile ReproductorModelo instancia;
    
    private BasicPlayer reproductor;
    private ListaReproduccion lista;
    private BasicPlayerListener listener; // Lo usaremos para comunicar eventos al controlador
    
    /**
     * 2. Constructor PRIVADO.
     * Al ser privado, ninguna otra clase puede instanciar este modelo accidentalmente.
     * Solo esta misma clase puede inicializarse a sí misma.
     */
    private ReproductorModelo() {
        this.reproductor = new BasicPlayer();
        this.lista = new ListaReproduccion(); // El Singleton inicializa su propia lista
    }
    
    /**
     * 3. Método de acceso global a la única instancia (Double-Checked Locking).
     * @return La única instancia en memoria de ReproductorModelo.
     */
    public static ReproductorModelo getInstance() {
        if (instancia == null) {
            synchronized (ReproductorModelo.class) {
                if (instancia == null) {
                    instancia = new ReproductorModelo();
                }
            }
        }
        return instancia;
    }
    
    // =========================================================================
    //                        MÉTODOS DE AUDIO Y CONTROL
    // =========================================================================
    
    /**
     * Carga un archivo directamente al motor de audio mediante su ruta.
     */
    public void setRutaDirecta (String entradaRuta) throws Exception{
        this.reproductor.open(new File (entradaRuta));
    }

    /**
     * Busca la canción en la estructura de datos (Lista) según su ID y la carga en el motor.
     */
    public void prepararDesdeLista(int indice) throws Exception {
        Cancion c = lista.obtenerCancion(indice);
        if (c != null) {
            this.reproductor.open(new File(c.getRuta()));
        } else {
            throw new Exception("La canción con índice " + indice + " no existe.");
        }
    }
    
    /**
     * Permite que el Controlador (específicamente el sub-controlador de reproducción) 
     * se conecte al motor para escuchar los eventos de progreso, fin de canción, etc.
     */
    public void setControlador(BasicPlayerListener entradaListener) {
        this.listener=entradaListener;
        this.reproductor.addBasicPlayerListener(this.listener);
    }
      
    //Método para reproducir
    public void reproducir () throws Exception{
        this.reproductor.play();
    }
    
    //Método para pausar la reproducción
    public void pausar() throws Exception {
        this.reproductor.pause();
    }

    //Método para reanudar la reproducción
    public void reanudar() throws Exception {
        this.reproductor.resume();
    }

    //Método para detener la reproducción
    public void detener() throws Exception {
        this.reproductor.stop();
    }

    public ListaReproduccion getLista() {
        return lista;
    }
    
    /**
    * Extrae los metadatos de un archivo sin interrumpir la reproducci�n actual.
    */
    public Map<String, Object> extraerMetadatosSilencioso(String rutaArchivo) throws Exception {
        File file = new File(rutaArchivo);
        // AudioSystem accede directamente al archivo usando el SPI de MP3
        AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
        return baseFileFormat.properties();
    }
    
    /**
     * Consulta el estado actual del motor de audio (Ej: Reproduciendo, Pausado, Detenido).
     * @return El código de estado proporcionado por BasicPlayer.
     */
    public int getEstado(){
        return this.reproductor.getStatus();
    }
    
    /**
     * Ajusta el volumen del motor de audio.
     * @param valor Un número decimal representando el volumen (ej. 0.5 para 50%).
     */
    public void setVolumen(double valor) throws Exception {
        // Verificamos que el control de ganancia está disponible en el archivo actual
        if (this.reproductor.hasGainControl()) {
            this.reproductor.setGain(valor);
        }
    }
}
