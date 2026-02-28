/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import javax.swing.SwingUtilities;
import modelo.Cancion;
import modelo.ListaReproduccion;
import vista.ReproductorVista;
import javazoom.jlgui.basicplayer.BasicPlayer;


/**
 * CONTROLADOR DE TABLA (Sub-controlador)
 * --------------------------------------
 * Su única responsabilidad es manejar la visualización del JTable (la lista de canciones),
 * escuchar los clics sobre las filas (doble clic o clic derecho) y repintar los datos.
 */

public class ControladorTabla {
    
    private final Controlador central;
    private final ReproductorVista vista;
    private final ListaReproduccion lista;

    //constructor
    public ControladorTabla(Controlador central, ReproductorVista vista, ListaReproduccion lista) {
        this.central = central;
        this.vista = vista;
        this.lista = lista;
        
        configurarEventosTabla();
    }
    
    private void configurarEventosTabla() {
        // 1. Asignar la acción al botón "Eliminar" del menú emergente
        this.vista.itemEliminar.addActionListener(e -> {
            eliminarCancionSeleccionada();
        });

        // 2. Escuchar los eventos del ratón en la tabla
        this.vista.tablaCanciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Doble clic (Aseguramos que sea con el botón izquierdo)
                if (evt.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(evt)) { 
                    int fila = vista.tablaCanciones.getSelectedRow();
                    if (fila != -1) {
                        int idSeleccionado = (int) vista.tablaCanciones.getValueAt(fila, 0);
                        central.reproducirIndice(idSeleccionado);
                    }
                }
            }

            // Detección multiplataforma del clic derecho (Popup Trigger)
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                verificarMenuEmergente(evt);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                verificarMenuEmergente(evt);
            }

            private void verificarMenuEmergente(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    // Obtenemos la fila exacta bajo el puntero del ratón
                    int fila = vista.tablaCanciones.rowAtPoint(evt.getPoint());

                    if (fila >= 0 && fila < vista.tablaCanciones.getRowCount()) {
                        // Forzamos la selección visual de esa fila
                        vista.tablaCanciones.setRowSelectionInterval(fila, fila);
                        // Desplegamos el menú en las coordenadas del ratón
                        vista.menuTabla.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            }
        });
    }
    
    /**
     * Busca el ID de la canción en la tabla, selecciona la fila visualmente 
     * y hace scroll automático hacia ella. Es invocado por el Orquestador.
     * @param idCancion El identificador de la canción a resaltar.
     */
    public void resaltarCancionEnTabla(int idCancion) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < vista.tablaCanciones.getRowCount(); i++) {
                int idFila = (int) vista.tablaCanciones.getValueAt(i, 0);

                if (idFila == idCancion) {
                    vista.tablaCanciones.setRowSelectionInterval(i, i);
                    vista.tablaCanciones.scrollRectToVisible(vista.tablaCanciones.getCellRect(i, 0, true));
                    break; 
                }
            }
        });
    }
    
    /**
     * Redibuja completamente la tabla leyendo la información de la lista de reproducción.
     * Se invoca cuando se agrega o se elimina una canción.
     */
    public void actualizarTabla(){        
        //Limpiar tabla
        this.vista.modeloTabla.setRowCount(0);
        
        // Consulta al central cuántas canciones se han procesado en total
        for (int i=0; i < central.getContadorIndice(); i++){
            Cancion c = lista.obtenerCancion(i);
            if (c != null){
                Object[] fila = {
                    i, 
                    c.getTitulo(), 
                    c.getAutor(), 
                    c.getDuracionFormateada()
                };
                vista.modeloTabla.addRow(fila);
            }
        }
        
        // CORRECCI�N: Borr� la llamada a "actualizarInfo" que estaba aqu�. 
        // El ControladorTabla solo debe encargarse de la tabla. Los labels ya se 
        // actualizan desde procesarNuevoArchivo en el Controlador central.
        
        vista.pack();
    } 
    
    /**
     * Se invoca localmente desde el menú emergente para borrar la fila seleccionada.
     */
    private void eliminarCancionSeleccionada() {
        int filaSeleccionada = vista.tablaCanciones.getSelectedRow();

        if (filaSeleccionada != -1) {
            int idSeleccionado = (int) vista.tablaCanciones.getValueAt(filaSeleccionada, 0);

            // Si están intentando borrar la canci�n que está sonando actualmente
            if (idSeleccionado == central.getIndiceActual() && central.getModelo().getEstado() != BasicPlayer.STOPPED) {
                // Detenemos la reproducción y limpiamos la interfaz simulando un clic en Stop
                // DELEGA: Le avisa al central que debe detener la m�sica
                central.detenerReproduccion();
            }

            lista.eliminarCancion(idSeleccionado);
            actualizarTabla();

            // Si quedó otra canción resaltada tras redibujar, se mantiene la selección
            if (lista.obtenerCancion(central.getIndiceActual()) != null && central.getModelo().getEstado() != BasicPlayer.STOPPED) {
                resaltarCancionEnTabla(central.getIndiceActual());
            }
        }
    }
}