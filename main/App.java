package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import modulos.ventana.VentanaPrincipal;

/**
 * Punto de entrada de la aplicación
 * Solo arranca la interfaz; toda la lógica vive en los módulos
 */
public class App {

    public static void main(String[] args) {
        try {
            // Look & Feel multiplataforma: respeta los colores del tema en Windows/Linux/Mac (Sistema operativo windows)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Si falla, se usa el L&F por defecto
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
