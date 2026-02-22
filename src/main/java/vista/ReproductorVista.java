/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author diotallevi
 */

    
public class ReproductorVista extends JFrame {

    public JProgressBar barraProgreso = new JProgressBar(0, 100);
    public JLabel lblTitulo = new JLabel("Título: -");
    public JLabel lblArtista = new JLabel("Artista: -");
    public JLabel lblAlbum = new JLabel("Album: -");

    //public JButton btnPlay = new JButton("Reproducir");
    //public JButton btnPausa = new JButton("Pausar");
    //public JButton btnStop = new JButton("Detener");
    //public JButton btnAbrir = new JButton("Abrir archivo");
    
    // El texto normal se reemplaza por caracteres Unicode
    public JButton btnAbrir = new JButton("\u23CF"); // ? (Carpeta abierta)
    public JButton btnAnterior = new JButton("\u23EE"); // ? (Anterior)
    public JButton btnPlay = new JButton("\u25B6");      // ? (Play)
    public JButton btnPausa = new JButton("\u23F8");     // ? (Pausa)
    public JButton btnStop = new JButton("\u23F9");      // ? (Stop)
    public JButton btnSiguiente = new JButton("\u23ED"); // ? (Siguiente)
    
    public JSlider sliderVolumen = new JSlider(0, 100, 50); // Min 0, Max 100, Inicial 50

    // Cambia esto en tus atributos de clase
    public DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Título", "Artista", "Duración"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Al retornar false, bloqueamos la edici�n manual de las celdas
            return false;
        }
    };
    public JTable tablaCanciones = new JTable(modeloTabla);
    public JScrollPane scrollTabla = new JScrollPane(tablaCanciones);
    
    // Componentes para el men� contextual de la tabla
    public JPopupMenu menuTabla = new JPopupMenu();
    public JMenuItem itemEliminar = new JMenuItem("Eliminar canción");

    public ReproductorVista() {
        super("Mp3 en JAVA!");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(10, 10)); // Separaci�n de 10px entre regiones

        // ==========================================
        // 1. PANEL IZQUIERDO (Info, Barra y Botones)
        // ==========================================
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // M�rgenes internos

        // --- Sub-panel de Informaci�n ---
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblArtista.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAlbum.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Creamos una fuente un poco m�s grande para los �conos de los botones
        Font fuenteBotones = new Font("SansSerif", Font.PLAIN, 24);

        btnAbrir.setFont(fuenteBotones);
        btnPlay.setFont(fuenteBotones);
        btnPausa.setFont(fuenteBotones);
        btnStop.setFont(fuenteBotones);
        btnAnterior.setFont(fuenteBotones);
        btnSiguiente.setFont(fuenteBotones);
        
        // Tooltips para accesibilidad y gu�a del usuario
        btnAbrir.setToolTipText("Agregar un archivo .mp3 a la lista");
        btnPlay.setToolTipText("Reproducir la canción seleccionada");
        btnPausa.setToolTipText("Pausar la reproducción actual");
        btnStop.setToolTipText("Detener la canción y reiniciar el progreso");
        btnAnterior.setToolTipText("Reproducir la canción anterior");
        btnSiguiente.setToolTipText("Reproducir la canción siguiente");

        // Aplicando tipograf�a m�s grande (estilo FlatLaf)
        lblTitulo.putClientProperty("FlatLaf.styleClass", "h2"); 
        lblArtista.putClientProperty("FlatLaf.styleClass", "h3");
        lblAlbum.putClientProperty("FlatLaf.styleClass", "h3");

        panelInfo.add(lblTitulo);
        panelInfo.add(Box.createVerticalStrut(10)); // M�s espacio de respiraci�n
        panelInfo.add(lblArtista);
        panelInfo.add(Box.createVerticalStrut(5));
        panelInfo.add(lblAlbum);

        // --- Configuraci�n Barra de Progreso ---
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("00:00");
        barraProgreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Un poco m�s gruesa

        // --- Sub-panel de Botones ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.add(btnAbrir);
        panelBotones.add(btnAnterior);
        panelBotones.add(btnPlay);
        panelBotones.add(btnPausa);
        panelBotones.add(btnStop);
        panelBotones.add(btnSiguiente);
        
        // --- Sub-panel de Volumen ---
        JPanel panelVolumen = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblVolumenIcono = new JLabel("Volumen:");
        sliderVolumen.setPreferredSize(new Dimension(150, 30));
        panelVolumen.add(lblVolumenIcono);
        panelVolumen.add(sliderVolumen);

        // --- Ensamblaje del Panel Izquierdo ---
        // Usamos 'VerticalGlue' arriba y abajo para que el contenido quede centrado verticalmente
        panelIzquierdo.add(Box.createVerticalGlue()); 
        panelIzquierdo.add(panelInfo);
        panelIzquierdo.add(Box.createVerticalStrut(25));
        panelIzquierdo.add(barraProgreso);
        panelIzquierdo.add(Box.createVerticalStrut(20));
        panelIzquierdo.add(panelBotones);
        panelIzquierdo.add(Box.createVerticalStrut(10));
        panelIzquierdo.add(panelVolumen);
        panelIzquierdo.add(Box.createVerticalGlue()); 

        // ==========================================
        // 2. PANEL DERECHO (Tabla de Canciones)
        // ==========================================
        // Le damos un ancho preferido de 450px, pero la altura se adaptar� a la ventana
        scrollTabla.setPreferredSize(new Dimension(450, 0)); 
        scrollTabla.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10)); // Margen derecho

        // ==========================================
        // 3. AGREGAR AL FRAME PRINCIPAL
        // ==========================================
        this.add(panelIzquierdo, BorderLayout.CENTER);
        this.add(scrollTabla, BorderLayout.EAST);

        // En lugar de pack(), definimos un tama�o base agradable para esta nueva disposici�n
        this.setSize(900, 400); 
        this.setLocationRelativeTo(null);
        // Ensamblaje del menú contextual
        menuTabla.add(itemEliminar);
        this.setVisible(true);
    }
}
