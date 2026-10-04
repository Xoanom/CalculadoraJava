package modulos.grafica;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;
import modulos.nucleo.FormatoNumero;
import modulos.util.Tema;

/**
 * Lienzo que dibuja cuadrícula, ejes y funciones.
 * Interacción: arrastrar = desplazar, rueda del mouse = zoom, doble clic = centrar.
 */
public class LienzoGrafica extends JPanel {

    private static final double ESCALA_INICIAL = 50; // píxeles por unidad

    private List<Funcion> funciones = List.of();
    private double cx = 0;   // coordenada x del centro del lienzo
    private double cy = 0;   // coordenada y del centro del lienzo
    private double escala = ESCALA_INICIAL;

    private Point ultimoPunto;
    private Consumer<String> alMover = s -> { };

    public LienzoGrafica() {
        setBackground(Tema.LIENZO);
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ultimoPunto = e.getPoint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    centrar();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (ultimoPunto != null) {
                    cx -= (e.getX() - ultimoPunto.x) / escala;
                    cy += (e.getY() - ultimoPunto.y) / escala;
                    ultimoPunto = e.getPoint();
                    repaint();
                }
                informar(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                informar(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double dx = e.getX() - getWidth() / 2.0;
                double dy = e.getY() - getHeight() / 2.0;
                double wx = cx + dx / escala;
                double wy = cy - dy / escala;
                escala = Math.max(1e-3, Math.min(1e6, escala * Math.pow(1.15, -e.getPreciseWheelRotation())));
                cx = wx - dx / escala;   // mantiene fijo el punto bajo el cursor
                cy = wy + dy / escala;
                repaint();
                informar(e);
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
        addMouseWheelListener(raton);
    }

    // API

    public void setFunciones(List<Funcion> funciones) {
        this.funciones = funciones;
        repaint();
    }

    public void centrar() {
        cx = 0;
        cy = 0;
        escala = ESCALA_INICIAL;
        repaint();
    }

    /** Recibe un texto con las coordenadas bajo el cursor. */
    public void setAlMover(Consumer<String> alMover) {
        this.alMover = alMover;
    }

    private void informar(MouseEvent e) {
        double x = cx + (e.getX() - getWidth() / 2.0) / escala;
        double y = cy - (e.getY() - getHeight() / 2.0) / escala;
        alMover.accept("x = " + FormatoNumero.formatear(redondear(x)) + "   y = " + FormatoNumero.formatear(redondear(y)));
    }

    private static double redondear(double v) {
        return Math.round(v * 1e6) / 1e6;
    }

    // Conversión

    private int aPx(double x) {
        return (int) Math.round(getWidth() / 2.0 + (x - cx) * escala);
    }

    private int aPy(double y) {
        return (int) Math.round(getHeight() / 2.0 - (y - cy) * escala);
    }

    // Dibujo

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        dibujarCuadricula(g);
        dibujarFunciones(g);
        g.dispose();
    }

    private void dibujarCuadricula(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        double paso = pasoAgradable(90 / escala);
        double xmin = cx - w / 2.0 / escala, xmax = cx + w / 2.0 / escala;
        double ymin = cy - h / 2.0 / escala, ymax = cy + h / 2.0 / escala;

        int ejeX = Math.max(14, Math.min(h - 6, aPy(0)));  // fila donde van las etiquetas del eje X
        int ejeY = Math.max(4, Math.min(w - 40, aPx(0)));  // columna donde van las etiquetas del eje Y

        g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g.setStroke(new BasicStroke(1f));

        for (long i = (long) Math.floor(xmin / paso); i * paso <= xmax; i++) {
            int px = aPx(i * paso);
            g.setColor(Tema.CUADRICULA);
            g.drawLine(px, 0, px, h);
            if (i != 0) {
                g.setColor(Tema.TEXTO_SUAVE);
                g.drawString(FormatoNumero.formatear(redondear(i * paso)), px + 3, ejeX - 3);
            }
        }
        for (long j = (long) Math.floor(ymin / paso); j * paso <= ymax; j++) {
            int py = aPy(j * paso);
            g.setColor(Tema.CUADRICULA);
            g.drawLine(0, py, w, py);
            if (j != 0) {
                g.setColor(Tema.TEXTO_SUAVE);
                g.drawString(FormatoNumero.formatear(redondear(j * paso)), ejeY + 4, py - 3);
            }
        }

        // Ejes
        g.setColor(Tema.EJES);
        g.setStroke(new BasicStroke(1.6f));
        g.drawLine(0, aPy(0), w, aPy(0));
        g.drawLine(aPx(0), 0, aPx(0), h);
    }

    private static double pasoAgradable(double crudo) {
        double mag = Math.pow(10, Math.floor(Math.log10(crudo)));
        double r = crudo / mag;
        double f = r <= 1 ? 1 : r <= 2 ? 2 : r <= 5 ? 5 : 10;
        return f * mag;
    }

    private void dibujarFunciones(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Funcion f : funciones) {
            Path2D.Double trazo = new Path2D.Double();
            boolean lapizAbajo = false;
            double pyAnterior = 0;

            for (int px = 0; px <= w; px++) {
                double x = cx + (px - w / 2.0) / escala;
                double y = f.en(x);
                if (Double.isNaN(y) || Double.isInfinite(y)) {
                    lapizAbajo = false;   // fuera del dominio
                    continue;
                }
                double py = h / 2.0 - (y - cy) * escala;
                if (Math.abs(py) > 1e6) {
                    lapizAbajo = false;
                    continue;
                }
                if (lapizAbajo && Math.abs(py - pyAnterior) > h * 4) {
                    lapizAbajo = false;   // asíntota (ej. tan x): no unir los extremos
                }
                if (!lapizAbajo) {
                    trazo.moveTo(px, py);
                    lapizAbajo = true;
                } else {
                    trazo.lineTo(px, py);
                }
                pyAnterior = py;
            }
            g.setColor(f.getColor());
            g.draw(trazo);
        }
    }
}
