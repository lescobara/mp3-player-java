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
 *
 * @author diotallevi
 */
public class ReproductorModelo {
    
    // 1. Instancia est�tica privada (volatile garantiza seguridad entre hilos)
    private static volatile ReproductorModelo instancia;
    
    private BasicPlayer reproductor;
    private ListaReproduccion lista;
    private BasicPlayerListener listener; // Lo usaremos para comunicar eventos al controlador
    
    // 2. Constructor PRIVADO (nadie más puede usar 'new ReproductorModelo()')
    private ReproductorModelo() {
        this.reproductor = new BasicPlayer();
        this.lista = new ListaReproduccion(); // El Singleton inicializa su propia lista
    }
    
    public void setRutaDirecta (String entradaRuta) throws Exception{
        this.reproductor.open(new File (entradaRuta));
    }

    // Busca en el HashMap y abre el archivo
    public void prepararDesdeLista(int indice) throws Exception {
        Cancion c = lista.obtenerCancion(indice);
        if (c != null) {
            this.reproductor.open(new File(c.getRuta()));
        } else {
            throw new Exception("La canción con índice " + indice + " no existe.");
        }
    }
    
    // Método para que el controlador se suscriba a los eventos
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
    
    public int getEstado(){
        return this.reproductor.getStatus();
    }
    
    // 3. M�todo est�tico para obtener la �nica instancia (Double-Checked Locking)
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
    
    public void setVolumen(double valor) throws Exception {
        // Verificamos que el control de ganancia est� disponible en el archivo actual
        if (this.reproductor.hasGainControl()) {
            this.reproductor.setGain(valor);
        }
    }
}
