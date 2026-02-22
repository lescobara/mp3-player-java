/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package controlador;

import com.formdev.flatlaf.FlatDarkLaf;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import vista.ReproductorVista;

/**
 *
 * @author diotallevi
 */
public class Mp3Player {
    
    private static final int PUERTO_UNICO = 9999; 
    private static ServerSocket socketApp; 

    public static void main(String[] args) {
        verificarInstanciaUnica();
        configurarLogs();
        configurarTemaVisual();
        iniciarAplicacion();
    }

    /**
     * Garantiza que solo exista un proceso del reproductor en el sistema operativo.
     */
    private static void verificarInstanciaUnica() {
        try {
            socketApp = new ServerSocket(PUERTO_UNICO, 1, InetAddress.getLoopbackAddress());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                    "El reproductor ya se está ejecutando.", 
                    "Instancia duplicada", 
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0); 
        }
    }

    /**
     * Configura el nivel de ruido de la consola para las librer�as de terceros.
     */
    private static void configurarLogs() {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
    }

    /**
     * Aplica el Look and Feel moderno antes de renderizar cualquier ventana.
     */
    private static void configurarTemaVisual() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); 
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex.getMessage());
        }
    }

    /**
     * Instancia el patrón MVC e inicia la interfaz gráfica.
     */
    private static void iniciarAplicacion() {
        ReproductorVista objetoVista = new ReproductorVista();
        new Controlador(objetoVista);  
    }
}
