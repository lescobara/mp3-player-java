/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.io.File;
import java.util.Map;
import javax.swing.SwingUtilities;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import modelo.ListaReproduccion;
import modelo.ReproductorModelo;
import modelo.Cancion;
import vista.ReproductorVista;

/**
 * CONTROLADOR DE REPRODUCCIÓN Y AUDIO (Sub-controlador)
 * -----------------------------------------------------
 * Se encarga exclusivamente de la librería de audio (BasicPlayer).
 * Actualiza los textos de la canción (Labels), la barra de progreso, 
 * maneja el control de volumen y escucha cuando una canción finaliza.
 */
public class ControladorReproduccion implements BasicPlayerListener {
    
    private final Controlador central;
    private final ReproductorVista vista;
    private final ReproductorModelo modelo;
    private final ListaReproduccion lista;
    
    //Constructor
    public ControladorReproduccion(Controlador central, ReproductorModelo modelo, ReproductorVista vista, ListaReproduccion lista) {
        this.central = central;
        this.modelo = modelo;
        this.vista = vista;
        this.lista = lista;
        
        configurarEventosVolumen();
        // Activar eventos de la librer�a de audio
        this.modelo.setControlador(this); 
    }

    /**
     * Evento de BasicPlayer: Se dispara cuando el archivo de audio se abre exitosamente.
     * Aquí se lee el tamaño en bytes del archivo para calcular el progreso.
     */
    @Override
    public void opened(Object source, Map properties) {
        Cancion actual = lista.obtenerCancion(central.getIndiceActual());
        actualizarInfo(actual);

        if (properties.containsKey("audio.length.bytes")) {
            central.tamanoArchivo = Long.parseLong(properties.get("audio.length.bytes").toString());
        }
    }

    /**
     * Evento de BasicPlayer: Se dispara continuamente mientras la canción se reproduce.
     * Mueve la barra de progreso y el cronómetro de la vista.
     */
    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
        if (central.tamanoArchivo > 0) {
            float progreso = (bytesread * 100.0f) / central.tamanoArchivo;
            vista.barraProgreso.setValue((int) progreso);
        }
        long segundosTotales = microseconds / 1000000;
        vista.barraProgreso.setString(String.format("%02d:%02d", segundosTotales / 60, segundosTotales % 60));
    }

    /**
     * Evento de BasicPlayer: Informa sobre cambios en el motor (Pausado, Detenido, Fin de medio).
     */
    @Override
    public void stateUpdated(BasicPlayerEvent bpe) {
        int estado = bpe.getCode();
        // EOM (End Of Media) significa que la pista terminó de forma natural
        if (estado == BasicPlayerEvent.EOM) {
            // DELEGA: Le avisa al Central que busque y ponga la siguiente pista
            Thread.startVirtualThread(() -> central.reproducirSiguiente());
        }
        // Si el usuario presionó STOP o el motor se detuvo
        if (estado == BasicPlayerEvent.STOPPED || estado == BasicPlayerEvent.UNKNOWN) {
            SwingUtilities.invokeLater(() -> {
                vista.barraProgreso.setValue(0);
                vista.barraProgreso.setString("00:00");
                central.estaPausado = false;
            });
        }
    }

    @Override
    public void setController(BasicController bc) { 
        throw new UnsupportedOperationException(""); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /**
     * Configura el listener para el slider de volumen (Control deslizable).
     */
    private void configurarEventosVolumen() {
        vista.sliderVolumen.addChangeListener(e -> {
            double volumen = vista.sliderVolumen.getValue() / 100.0;
            try {
                if (modelo.getEstado() != BasicPlayer.UNKNOWN) modelo.setVolumen(volumen);
            } catch (Exception ex) {
                 System.err.println("Error al cambiar volumen: " + ex.getMessage());
            }
        });
    }

    /**
     * Actualiza los Labels visuales (Título, Artista, Álbum) con la información de la pista.
     * Es invocado por el orquestador cuando cambia de canción.
     * @param actual El objeto Cancion que se va a mostrar.
     */
    public void actualizarInfo(Cancion actual) {
        if (actual != null) {
            vista.lblTitulo.setText("Título: " + actual.getTitulo());
            vista.lblArtista.setText("Artista: " + actual.getAutor());
            vista.lblAlbum.setText("Álbum: " + actual.getAlbum());
        }
    }
    
    /**
     * Toma los metadatos (ID3 Tags) leídos por la librería de audio y construye 
     * un objeto Cancion para guardarlo en la estructura de datos.
     */
    public Cancion crearCancionDesdeMap(Map props, String ruta) {
        String titulo = obtenerPropiedad(props, "title", "Desconocido");
        String artista = obtenerPropiedad(props, "author", "Desconocido");
        String album = obtenerPropiedad(props, "album", "Desconocido");
        String genero = obtenerPropiedad(props, "genre", "Desconocido");

        long duracion = props.containsKey("duration") ? (long) props.get("duration") : 0;

        // Si el mapa trae el tama�o en bytes, lo usamos; si no, calculamos del archivo f�sico
        long bytes = 0;
        if (props.containsKey("audio.length.bytes")) {
            bytes = Long.parseLong(props.get("audio.length.bytes").toString());
        } else {
            bytes = new File(ruta).length();
        }

        return new Cancion(titulo, artista, album, ruta, duracion, bytes, genero);
    }  
    
    /**
     * Método auxiliar para extraer una propiedad del Mapa de metadatos de forma segura (sin NullPointer).
     */
    private String obtenerPropiedad(Map props, String llave, String valorDefecto) {
        Object valor = props.get(llave);
        return (valor != null && !valor.toString().isEmpty()) ? valor.toString() : valorDefecto;
    }
    
    //Método para limpiar labels cuando se oprime detener
    public void limpiarLabels() {
        vista.lblTitulo.setText("Título: -");
        vista.lblArtista.setText("Artista: -");
        vista.lblAlbum.setText("Álbum: -");
        vista.barraProgreso.setValue(0);
        vista.barraProgreso.setString("00:00");
    }
}