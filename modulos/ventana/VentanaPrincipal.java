package modulos.ventana;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import modulos.calculadora.PanelCalculadora;
import modulos.grafica.PanelGrafica;
import modulos.util.Tema;

/**
 * Ventana principal: ensambla los módulos (calculadora + gráfica).
 * Para agregar un módulo nuevo, crea su panel y añádelo aquí.
 */
public class VentanaPrincipal extends JFrame {

    private static final double PROPORCION_PANTALLA = 0.90;

    public VentanaPrincipal() {
        super("Calculadora Gráfica");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Se ajusta automáticamente al tamaño de la pantalla del usuario
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (pantalla.width * PROPORCION_PANTALLA), (int) (pantalla.height * PROPORCION_PANTALLA));
        setMinimumSize(new Dimension(820, 540));
        setLocationRelativeTo(null);
        // Para abrir maximizada: setExtendedState(JFrame.MAXIMIZED_BOTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new PanelCalculadora(), new PanelGrafica());
        division.setResizeWeight(0.35);
        division.setDividerLocation((int) (getWidth() * 0.35));
        division.setBorder(null);
        division.setBackground(Tema.FONDO);
        setContentPane(division);
    }
}
