# Reproductor MP3 en Java 🎵

Una aplicación de escritorio robusta y moderna para reproducir archivos MP3, construida en Java. Este proyecto destaca por su enfoque en **Clean Code** y la implementación estricta de patrones de diseño de ingeniería de software.

## 🚀 Características Principales

* **Arquitectura MVC:** Separación total entre la lógica de negocio (Modelo), la interfaz gráfica (Vista) y la gestión de eventos (Controlador).
* **Gestión de Instancias Segura:** Implementación del patrón **Singleton** para el motor de audio y enlace a un socket local (interfaz *loopback*) para evitar la ejecución de múltiples instancias a nivel del sistema operativo.
* **Interfaz Gráfica Moderna (UI/UX):** Integración con **FlatLaf** para un diseño oscuro, nativo y escalable, superando las limitaciones visuales del Swing tradicional.
* **Controles Multimedia Universales:** Botones de reproducción interactivos (Play, Pausa, Stop, Siguiente, Anterior) renderizados mediante caracteres Unicode, asegurando compatibilidad en cualquier distribución Linux, Windows o macOS.
* **Lista de Reproducción Dinámica:** Gestión de canciones mediante un `JTable` interactivo que incluye:
  * Resaltado automático de la pista en reproducción.
  * Navegación en bucle (Siguiente/Anterior).
  * Menú contextual (clic derecho) para eliminar pistas de forma segura.
* **Control de Audio Real:** Ajuste de ganancia (volumen) en tiempo real y barra de progreso sincronizada.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java (Compatible con JDK 8+, probado en entornos modernos como JDK 21)
* **Gestor de Dependencias:** Maven
* **Librerías Principales:**
  * `basicplayer`: Motor principal para la decodificación y reproducción de audio.
  * `flatlaf`: Look and Feel para la modernización de los componentes de Swing.
  * `slf4j`: Gestión controlada de los logs del sistema.

## 📐 Arquitectura del Proyecto

El código fuente está estrictamente dividido para garantizar su escalabilidad y fácil mantenimiento:

1. **Modelo (`ReproductorModelo.java`):** Encapsula el motor `BasicPlayer`, maneja los hilos de reproducción y notifica los cambios de estado (como el evento de Fin de Medio `EOM`).
2. **Vista (`ReproductorVista.java`):** Construida con Java Swing puro. Utiliza `BorderLayout` y `BoxLayout` para un diseño responsivo dividido en un panel de control multimedia y una tabla de visualización.
3. **Controlador (`Controlador.java`):** Orquesta la comunicación. Implementa oyentes (`ActionListeners`, `MouseListeners`, `ChangeListeners`) y mantiene la coherencia de los datos en un `HashMap` personalizado.

## 💻 Instalación y Ejecución

Asegúrate de tener instalado **Java (JDK)** y **Maven** en tu sistema.

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/lescobara/mp3-player-java.git](https://github.com/lescobara/mp3-player-java.git)
   cd mp3-player-java
   ```

2. **Descargar dependencias y empaquetar el proyecto:**
Este comando le dice a Maven que limpie compilaciones previas, descargue las librerías (basicplayer, flatlaf, etc.) y compile el código fuente.

    ```bash
    mvn clean package
    ```

3. **Ejecutar la aplicación:**
Puedes lanzarlo directamente usando el plugin de ejecución de Maven.
(Nota importante: Asegúrate de cambiar paquete.principal.Mp3Player por la ruta real de tus carpetas donde se encuentra el método main).
    ```bash
    mvn exec:java -Dexec.mainClass="paquete.principal.Mp3Player"
    ```

## 📝 Próximas Mejoras (Roadmap)
El proyecto está en constante evolución. Las siguientes características están planificadas para futuras actualizaciones:

[X] Dividir en Controlador al implementar el patrón delegación. 

[X] Vaciar Lista de Reproducción: Añadir una opción al menú contextual o un botón dedicado para limpiar todo el HashMap y redibujar la tabla vacía en un solo clic.

[ ] Lectura de Metadatos ID3: Extraer la información real incrustada en el archivo MP3 (Nombre del álbum original, año, y carátula) en lugar de depender solo del nombre del archivo.

[ ] Persistencia de Datos: Implementar guardado automático de la lista de reproducción (en un archivo local JSON o XML) para que las canciones sigan ahí al cerrar y volver a abrir la aplicación.

[ ] Implementar algoritmo de Machine Learning para recomendaciones de canciones, según gustos del usuario.
