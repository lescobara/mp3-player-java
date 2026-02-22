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
   git clone [https://github.com/TU_USUARIO/mp3-player-java.git](https://github.com/lescobara/mp3-player-java.git)
   cd mp3-player-java
