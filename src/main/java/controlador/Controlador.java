/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import modelo.Cancion;
import modelo.ListaReproduccion;
import modelo.ReproductorModelo;
import vista.ReproductorVista;

/**
 *
 * @author diotallevi
 */
public class Controlador implements ActionListener, BasicPlayerListener{
    
    private final ReproductorModelo modelo;
    private final ReproductorVista vista;
    private final ListaReproduccion lista;
    
    //private String rutaActual;
    private int contadorIndice; //atributo empleado para ir asignando índices en el hashmap
    private int indiceActual = 1; //atributo empleado para saber qué canción se va a reproducir
    
    private boolean estaPausado=false;
    private boolean esNuevaCarga = false;
    private long tamanoArchivo;
    
    //Constructor
    public Controlador (ReproductorVista entradaObjetoVista){
        this.modelo=ReproductorModelo.getInstance();
        this.vista=entradaObjetoVista;
        this.lista = modelo.getLista(); //Usamos la lista que viene de la vista
        
        this.modelo.setControlador(this); // Activar eventos de la librería de audio para que pueda funcionar la barra de progreso
        
        //Agregando Eventos
        this.vista.btnPlay.addActionListener(this);
        this.vista.btnPausa.addActionListener(this);
        this.vista.btnStop.addActionListener(this);
        this.vista.btnAbrir.addActionListener(this);
        this.vista.btnAnterior.addActionListener(this);
        this.vista.btnSiguiente.addActionListener(this);
        
        configurarEventosTabla();
        
        configurarEventosVolumen();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == vista.btnAbrir) {
                String rutaArchivo = seleccionarArchivo();
                if (rutaArchivo != null) {
                    procesarNuevoArchivo(rutaArchivo);
                }
            }else if (e.getSource() == vista.btnPlay) {
                if (estaPausado) {
                    modelo.reanudar();
                    estaPausado = false;
                }else{
                    reproducirIndiceActual(this.indiceActual);
                }
               
            }else if (e.getSource() == vista.btnPausa) {
                modelo.pausar();
                estaPausado = true;
            }else if (e.getSource() == vista.btnStop) {
                // 1. Forzamos el reset de estados visuales y l�gicos de inmediato
                estaPausado = false;
                limpiarLabels();

                // 2. Solo activamos el 'freno' y llamamos a detener si realmente est� sonando o pausado
                // Esto evita que el comando se quede "atrapado"
                if (modelo.getEstado() != BasicPlayer.STOPPED) {
                    modelo.detener();
                }
            } else if (e.getSource() == vista.btnAnterior) {
                reproducirAnterior();
            } else if (e.getSource() == vista.btnSiguiente) {
                reproducirSiguiente();
            }
        
       }catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Error: " + ex.getMessage());
       }  
    }

    //Método empleado para seleccionar el archivo, retorna la ruta como un string
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
    
    @Override
    public void opened(Object source, Map properties) {
        // Al empezar a sonar, actualizamos los labels con la canci�n actual
        Cancion actual = lista.obtenerCancion(this.indiceActual);
        actualizarInfo(actual);

        // Guardamos el tama�o para la barra de progreso
        if (properties.containsKey("audio.length.bytes")) {
            tamanoArchivo = Long.parseLong(properties.get("audio.length.bytes").toString());
        }
    }
    
    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
        // 1. Calcular porcentaje para la barra
        if (tamanoArchivo > 0) {
            float progreso = (bytesread * 100.0f) / tamanoArchivo;
            vista.barraProgreso.setValue((int) progreso);
        }

        // 2. Convertir microsegundos a Minutos:Segundos
        long segundosTotales = microseconds / 1000000;
        long minutos = segundosTotales / 60;
        long segundos = segundosTotales % 60;

        // 3. Escribir el tiempo sobre la barra (ej: "02:45")
        vista.barraProgreso.setString(String.format("%02d:%02d", minutos, segundos));
    }

    @Override
    public void stateUpdated(BasicPlayerEvent bpe) {
        int estado = bpe.getCode();

        // 1. EOM (End Of Media): Se dispara SOLO cuando la pista termina de forma natural
        if (estado == BasicPlayerEvent.EOM) {
            System.out.println("DEBUG: Fin de canción (EOM) detectado. Saltando a la siguiente...");

            // EL SECRETO: Lanzar la siguiente canci�n en un hilo nuevo.
            // Al usar JDK 21, podemos aprovechar la ligereza de los Virtual Threads,
            // o usar un Thread convencional. Esto evita el bloqueo del motor de audio.
            Thread.startVirtualThread(() -> {
                reproducirSiguiente();
            });
        }

        // 2. STOPPED o UNKNOWN: Se dispara al pulsar "Detener" o despu�s de un EOM
        if (estado == BasicPlayerEvent.STOPPED || estado == BasicPlayerEvent.UNKNOWN) {
            // Limpieza visual segura (mandamos el cambio a la interfaz gr�fica)
            SwingUtilities.invokeLater(() -> {
                vista.barraProgreso.setValue(0);
                vista.barraProgreso.setString("00:00");
                estaPausado = false;
            });
        }
    }

    @Override
    public void setController(BasicController bc) {
        throw new UnsupportedOperationException(""); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    // Método auxiliar para evitar el "null"
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
    
    private void reproducirIndiceActual(int entradaIndice) {
        try {

            this.indiceActual = entradaIndice;
            Cancion cancion = lista.obtenerCancion(this.indiceActual);

            if (cancion != null) {
                this.esNuevaCarga = false;
                modelo.prepararDesdeLista(this.indiceActual);
                modelo.reproducir();
                estaPausado = false;
                actualizarInfo(cancion);
                resaltarCancionEnTabla(this.indiceActual);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Error: " + ex.getMessage());
        }
    }
    
    //Método para actualizar la tabla
    private void actualizarTabla(){        
        //Limpiar tabla
        this.vista.modeloTabla.setRowCount(0);
        
        //obtener acceso a la lista de canciones
        for (int i=0;i<this.contadorIndice;i++){
            Cancion c=lista.obtenerCancion(i);
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
        // 3. Actualizar los labels solo con la canción que se acaba de cargar (la �ltima)
        // Buscamos la canción usando el contadorIndice - 1
        actualizarInfo(lista.obtenerCancion(this.indiceActual));
        
        vista.pack();
    } 
    
    private void actualizarInfo(Cancion actual){
        if (actual != null) {
            vista.lblTitulo.setText("Título: " + actual.getTitulo());
            vista.lblArtista.setText("Artista: " + actual.getAutor());
            vista.lblAlbum.setText("Álbum: " + actual.getAlbum());
        }
    }
    
    private void configurarEventosTabla() {
    
        // 1. Asignar la acci�n al bot�n "Eliminar" del men� emergente
        this.vista.itemEliminar.addActionListener(e -> {
            eliminarCancionSeleccionada();
        });

        // 2. Escuchar los eventos del rat�n en la tabla
        this.vista.tablaCanciones.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Doble clic (Aseguramos que sea con el bot�n izquierdo)
                if (evt.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(evt)) { 
                    int fila = vista.tablaCanciones.getSelectedRow();
                    if (fila != -1) {
                        int idSeleccionado = (int) vista.tablaCanciones.getValueAt(fila, 0);
                        reproducirIndiceActual(idSeleccionado);
                    }
                }
            }

            // Detecci�n multiplataforma del clic derecho (Popup Trigger)
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
                    // Obtenemos la fila exacta bajo el puntero del rat�n
                    int fila = vista.tablaCanciones.rowAtPoint(evt.getPoint());

                    if (fila >= 0 && fila < vista.tablaCanciones.getRowCount()) {
                        // Forzamos la selecci�n visual de esa fila
                        vista.tablaCanciones.setRowSelectionInterval(fila, fila);

                        // Desplegamos el men� en las coordenadas del rat�n
                        vista.menuTabla.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            }
        });
    }
    
     /**
     * Busca el ID de la canci�n en la tabla, selecciona la fila y hace scroll hacia ella.
     */
    private void resaltarCancionEnTabla(int idCancion) {
        // Como tocamos la interfaz gr�fica, nos aseguramos de estar en el hilo de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < vista.tablaCanciones.getRowCount(); i++) {
                // Obtenemos el ID almacenado en la columna 0 de esa fila
                int idFila = (int) vista.tablaCanciones.getValueAt(i, 0);

                if (idFila == idCancion) {
                    // Seleccionamos la fila visualmente
                    vista.tablaCanciones.setRowSelectionInterval(i, i);

                    // Forzamos el scroll para que la fila seleccionada sea visible
                    vista.tablaCanciones.scrollRectToVisible(vista.tablaCanciones.getCellRect(i, 0, true));
                    break; // Encontramos la canci�n, no necesitamos seguir buscando
                }
            }
        });
    }
    
    private void procesarNuevoArchivo(String ruta) {
        try {
            // 1. Extraer metadatos sin interrumpir lo que suena
            Map props = modelo.extraerMetadatosSilencioso(ruta);

            // 2. Crear el objeto Cancion (podemos delegar esto a otro m�todo si quieres m�s limpieza)
            Cancion nueva = crearCancionDesdeMap(props, ruta);

            // 3. Registrar en el modelo
            lista.agregarCancion(nueva, this.contadorIndice);
            this.contadorIndice++;

            // 4. Actualizar la interfaz de forma selectiva
            actualizarTabla();

            // Mantener la informaci�n de la canci�n que est� sonando actualmente
            actualizarInfo(lista.obtenerCancion(this.indiceActual));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Error al procesar metadatos: " + ex.getMessage());
        }
    }
    
    private Cancion crearCancionDesdeMap(Map props, String ruta) {
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
     * Configura los listeners para los controles de audio deslizables.
     */
    private void configurarEventosVolumen() {
        this.vista.sliderVolumen.addChangeListener(e -> {
            int valorSlider = vista.sliderVolumen.getValue();
            double volumen = valorSlider / 100.0;
            try {
                if (modelo.getEstado() != BasicPlayer.UNKNOWN) {
                    modelo.setVolumen(volumen);
                }
            } catch (Exception ex) {
                System.err.println("Error al cambiar volumen: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Salta a la siguiente canci�n v�lida en el HashMap saltando los "huecos" eliminados. 
     * Si llega al final, hace un bucle y vuelve a la primera canci�n v�lida.
     */
    private void reproducirSiguiente() {
        if (lista.totalCanciones() == 0) return;

        int siguienteIndice = this.indiceActual + 1;
        boolean encontrado = false;

        // Buscamos hacia adelante saltando los nulls
        while (siguienteIndice < this.contadorIndice) {
            if (lista.obtenerCancion(siguienteIndice) != null) {
                encontrado = true;
                break;
            }
            siguienteIndice++; // Si es null, pasamos al siguiente n�mero
        }

        if (encontrado) {
            // Encontramos una canci�n v�lida m�s adelante
            this.esNuevaCarga = false;
            reproducirIndiceActual(siguienteIndice);
        } else {
            // Llegamos al final del mapa. Hacemos bucle a la primera canci�n v�lida.
            reproducirIndiceActual(obtenerPrimerIndiceValido());
        }
    }

    /**
     * Retrocede a la canci�n anterior.
     * Si est� en la primera, salta a la �ltima de la lista.
     */
    /**
     * Retrocede a la canci�n anterior saltando los huecos.
     * Si est� en la primera, hace un bucle hacia la �ltima canci�n v�lida.
     */
    private void reproducirAnterior() {
        if (lista.totalCanciones() == 0) return;

        int anteriorIndice = this.indiceActual - 1;
        boolean encontrado = false;

        // Buscamos hacia atr�s saltando los nulls
        while (anteriorIndice >= 0) {
            if (lista.obtenerCancion(anteriorIndice) != null) {
                encontrado = true;
                break;
            }
            anteriorIndice--;
        }

        if (encontrado) {
            this.esNuevaCarga = false;
            reproducirIndiceActual(anteriorIndice);
        } else {
            // Si retrocede m�s all� del 0, vamos a la �ltima canci�n v�lida
            reproducirIndiceActual(obtenerUltimoIndiceValido());
        }
    }
    
    // ================= M�TODOS AUXILIARES DE B�SQUEDA =================

    private int obtenerPrimerIndiceValido() {
        for (int i = 0; i < this.contadorIndice; i++) {
            if (lista.obtenerCancion(i) != null) {
                return i;
            }
        }
        return 0; // Retorno de seguridad
    }

    private int obtenerUltimoIndiceValido() {
        // Buscamos de reversa desde el �ndice m�s alto generado
        for (int i = this.contadorIndice - 1; i >= 0; i--) {
            if (lista.obtenerCancion(i) != null) {
                return i;
            }
        }
        return 0; // Retorno de seguridad
    }
    
    /**
    * Se invoca desde el men� contextual para borrar la fila seleccionada.
    */
    private void eliminarCancionSeleccionada() {
        int filaSeleccionada = vista.tablaCanciones.getSelectedRow();

        if (filaSeleccionada != -1) {
            // 1. Obtenemos el ID real desde la columna 0
            int idSeleccionado = (int) vista.tablaCanciones.getValueAt(filaSeleccionada, 0);

            // 2. Caso cr�tico: Si est�n eliminando la canci�n que est� sonando
            if (idSeleccionado == this.indiceActual && modelo.getEstado() != BasicPlayer.STOPPED) {
                // Detenemos la m�sica y limpiamos la interfaz simulando un clic en Stop
                this.vista.btnStop.doClick(); 
            }

            // 3. Eliminamos del HashMap en el Modelo
            lista.eliminarCancion(idSeleccionado);

            // 4. Redibujamos la tabla (esto borra la fila visualmente)
            actualizarTabla();

            // 5. (Opcional) Si qued� otra canci�n sonando de fondo, aseguramos 
            // que la tabla mantenga esa fila resaltada tras redibujar
            if (lista.obtenerCancion(this.indiceActual) != null && modelo.getEstado() != BasicPlayer.STOPPED) {
                resaltarCancionEnTabla(this.indiceActual);
            }
        }
    }
}