/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import vista.ReproductorVista;

/**
 * CONTROLADOR DE BOTONES (Sub-controlador)
 * ----------------------------------------
 * Su única responsabilidad es escuchar los clics del usuario en los botones
 * principales de la interfaz (Play, Pause, Stop, Anterior, Siguiente, Abrir).
 * No toma decisiones lógicas ni cambia la vista directamente; todo se lo 
 * delega al Controlador Central (Mediador).
 */
public class ControladorBotones implements ActionListener{
    
    private final Controlador central;
    private final ReproductorVista vista;
    
    public ControladorBotones(Controlador central, ReproductorVista vista) {
        this.central = central;
        this.vista = vista;
        
        // Asignar listeners
        this.vista.btnPlay.addActionListener(this);
        this.vista.btnPausa.addActionListener(this);
        this.vista.btnStop.addActionListener(this);
        this.vista.btnAbrir.addActionListener(this);
        this.vista.btnAnterior.addActionListener(this);
        this.vista.btnSiguiente.addActionListener(this);
    }
    
    /**
     * Captura los eventos de clic y notifica al Orquestador (Central) 
     * sobre la intención del usuario.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == vista.btnAbrir) {
                String rutaArchivo = seleccionarArchivo();
                if (rutaArchivo != null) {
                    central.procesarNuevoArchivo(rutaArchivo);
                }
            }else if (e.getSource() == vista.btnPlay) {
                if (central.estaPausado) {
                    central.getModelo().reanudar();
                    central.estaPausado = false;
                }else{
                    central.reproducirIndice(central.getIndiceActual());
                }
               
            }else if (e.getSource() == vista.btnPausa) {
                central.getModelo().pausar();
                central.estaPausado = true;
            }else if (e.getSource() == vista.btnStop) {
                // El botón solo avisa al Central que el usuario quiere detener la música
                central.detenerReproduccion();
            } else if (e.getSource() == vista.btnAnterior) {
                central.reproducirAnterior();
            } else if (e.getSource() == vista.btnSiguiente) {
                central.reproducirSiguiente();
            }
        
       }catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Error: " + ex.getMessage());
       }  
    }
    
    /**
     * Método auxiliar local para abrir la ventana de selección de archivos.
     * @return La ruta absoluta del archivo MP3 seleccionado o null si se cancela.
     */
    public String seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        // Filtro para que solo se vean archivos MP3
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos MP3", "mp3"));
        int seleccion = fileChooser.showOpenDialog(vista);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }
}
