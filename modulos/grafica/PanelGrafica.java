package modulos.grafica;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modulos.nucleo.Evaluador;
import modulos.nucleo.ExpresionException;
import modulos.util.Tema;

/** Panel de graficación: campos de funciones + lienzo interactivo. */
public class PanelGrafica extends JPanel {

    private static final int RANURAS = 4;

    private final JTextField[] campos = new JTextField[RANURAS];
    private final LienzoGrafica lienzo = new LienzoGrafica();
    private final JLabel mensaje = new JLabel(" ");
    private final JLabel coordenadas = new JLabel(" ");

    public PanelGrafica() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setMinimumSize(new Dimension(400, 420));

        add(crearControles(), BorderLayout.NORTH);
        add(lienzo, BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        lienzo.setAlMover(coordenadas::setText);

        campos[0].setText("sin(x)");
        campos[1].setText("x^2/5");
        graficar();
    }

    private JPanel crearControles() {
        JPanel filas = new JPanel(new GridLayout(RANURAS, 1, 0, 4));
        filas.setOpaque(false);

        for (int i = 0; i < RANURAS; i++) {
            JLabel etiqueta = new JLabel("  f" + (i + 1) + "(x) =  ");
            etiqueta.setForeground(Tema.COLORES_FUNCION[i]);
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 14f));

            JTextField campo = new JTextField();
            campo.setBackground(Tema.CAMPO);
            campo.setForeground(Tema.TEXTO);
            campo.setCaretColor(Tema.TEXTO);
            campo.setFont(campo.getFont().deriveFont(15f));
            campo.setToolTipText("Ej: x^2 - 3, sin(x), sqrt(x), log(x), 2x+1, abs(x), 1/x");
            campo.addActionListener(e -> graficar());
            campos[i] = campo;

            JPanel fila = new JPanel(new BorderLayout());
            fila.setOpaque(false);
            fila.add(etiqueta, BorderLayout.WEST);
            fila.add(campo, BorderLayout.CENTER);
            filas.add(fila);
        }

        JButton bGraficar = boton("Graficar", Tema.BTN_IGUAL);
        JButton bLimpiar = boton("Limpiar", Tema.BTN_LIMPIAR);
        JButton bCentrar = boton("Centrar vista", Tema.BTN_OP);
        bGraficar.addActionListener(e -> graficar());
        bLimpiar.addActionListener(e -> limpiar());
        bCentrar.addActionListener(e -> lienzo.centrar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.setOpaque(false);
        botones.add(bGraficar);
        botones.add(bLimpiar);
        botones.add(bCentrar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(filas, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        return norte;
    }

    private JPanel crearBarraEstado() {
        mensaje.setForeground(Tema.TEXTO_SUAVE);
        coordenadas.setForeground(Tema.TEXTO_SUAVE);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(mensaje, BorderLayout.WEST);
        barra.add(coordenadas, BorderLayout.EAST);
        return barra;
    }

    private static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Tema.TEXTO);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    // ------------------------------------------------------------------ Lógica

    private void graficar() {
        List<Funcion> lista = new ArrayList<>();
        String error = "";

        for (int i = 0; i < RANURAS; i++) {
            campos[i].setBackground(Tema.CAMPO);
            String texto = campos[i].getText().trim();
            if (texto.isEmpty()) {
                continue;
            }
            try {
                lista.add(new Funcion(texto, Evaluador.compilar(texto), Tema.COLORES_FUNCION[i]));
            } catch (ExpresionException ex) {
                campos[i].setBackground(Tema.ERROR);
                error = "f" + (i + 1) + ": " + ex.getMessage();
            }
        }
        lienzo.setFunciones(lista);
        mensaje.setForeground(error.isEmpty() ? Tema.TEXTO_SUAVE : Tema.TEXTO_ERROR);
        mensaje.setText(error.isEmpty()
                ? "Rueda: zoom  |  Arrastrar: mover  |  Doble clic: centrar"
                : error);
    }

    private void limpiar() {
        for (JTextField c : campos) {
            c.setText("");
        }
        graficar();
    }
}
