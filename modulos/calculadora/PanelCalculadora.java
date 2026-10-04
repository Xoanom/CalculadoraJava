package modulos.calculadora;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import modulos.nucleo.Evaluador;
import modulos.nucleo.ExpresionException;
import modulos.nucleo.FormatoNumero;
import modulos.util.Tema;

/** Calculadora científica básica con teclado en pantalla y entrada por teclado físico. */
public class PanelCalculadora extends JPanel {

    private static final String[][] TECLAS = {
        {"C", "⌫", "(", ")", "÷"},
        {"√", "x²", "xʸ", "log", "×"},
        {"7", "8", "9", "%", "−"},
        {"4", "5", "6", "ln", "+"},
        {"1", "2", "3", "π", "e"},
        {"0", ".", "±", "Ans", "="}
    };
    
    // Pantalla principal de la calculadora insercion de numeros
    private final JTextField pantalla = new JTextField();
    private final JLabel historial = new JLabel(" ", SwingConstants.RIGHT);
    private final JPanel teclado = new JPanel(new GridLayout(TECLAS.length, TECLAS[0].length, 6, 6));
    private final List<JButton> botones = new ArrayList<>();

    private String ultimoResultado = "0";
    private boolean mostrandoResultado = false;
       
    
    
    // Pantalla principal de la calculadora donde se insertan los numeros
    public PanelCalculadora() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setMinimumSize(new Dimension(300, 420));

        add(crearVisor(), BorderLayout.NORTH);
        crearTeclado();
        add(teclado, BorderLayout.CENTER);

        // Escala las fuentes con el tamaño de la ventana
        teclado.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarFuentes();
            }
        });
    }

    // Construcción de las variables de los estos de permisos
    private JPanel crearVisor() {
        historial.setForeground(Tema.TEXTO_SUAVE);

        pantalla.setHorizontalAlignment(SwingConstants.RIGHT);
        pantalla.setBackground(Tema.PANTALLA);
        pantalla.setForeground(Tema.TEXTO);
        pantalla.setCaretColor(Tema.TEXTO);
        pantalla.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        pantalla.addActionListener(e -> calcular());
        pantalla.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                mostrandoResultado = false;
            }
        });
        pantalla.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "limpiar");
        pantalla.getActionMap().put("limpiar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        JPanel visor = new JPanel(new BorderLayout(0, 4));
        visor.setOpaque(false);
        visor.add(historial, BorderLayout.NORTH);
        visor.add(pantalla, BorderLayout.CENTER);
        return visor;
    }

    private void crearTeclado() {
        teclado.setOpaque(false);
        for (String[] fila : TECLAS) {
            for (String etiqueta : fila) {
                JButton b = new JButton(etiqueta);
                b.setBackground(colorDe(etiqueta));
                b.setForeground(Tema.TEXTO);
                b.setOpaque(true);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setFocusable(false);
                b.addActionListener(e -> pulsar(etiqueta));
                botones.add(b);
                teclado.add(b);
            }
        }
    }

    // Ajuste de botones
    private static Color colorDe(String etiqueta) {
        switch (etiqueta) {
            case "=":
                return Tema.BTN_IGUAL;
            case "C":
                return Tema.BTN_LIMPIAR;
            case "÷": case "×": case "−": case "+":
            case "(": case ")": case "⌫": case "%":
                return Tema.BTN_OP;
            case "√": case "x²": case "xʸ": case "log":
            case "ln": case "π": case "e": case "Ans": case "±":
                return Tema.BTN_FUN;
            default:
                return Tema.BTN_NUM;
        }
    }

    private void ajustarFuentes() {
        int alto = teclado.getHeight() / TECLAS.length;
        int ancho = teclado.getWidth() / TECLAS[0].length;
        float tam = Math.max(11f, Math.min(alto, ancho) * 0.38f);
        for (JButton b : botones) {
            b.setFont(b.getFont().deriveFont(Font.BOLD, tam));
        }
        pantalla.setFont(pantalla.getFont().deriveFont(Font.BOLD, Math.max(20f, tam * 1.6f)));
        historial.setFont(historial.getFont().deriveFont(Math.max(11f, tam * 0.65f)));
    }

    // Logica principald de las teclas (Lógica de teclas)

    private void pulsar(String etiqueta) {
        switch (etiqueta) {
            case "C":
                limpiar();
                break;
            case "⌫":
                borrar();
                break;
            case "=":
                calcular();
                break;
            case "±":
                alternarSigno();
                break;
            case "Ans":
                insertar(ultimoResultado);
                break;
            case "√":
                insertar("sqrt(");
                break;
            case "x²":
                insertar("^2");
                break;
            case "xʸ":
                insertar("^");
                break;
            case "log":
                insertar("log(");
                break;
            case "ln":
                insertar("ln(");
                break;
            case "π":
                insertar("pi");
                break;
            case "÷":
                insertar("/");
                break;
            case "×":
                insertar("*");
                break;
            case "−":
                insertar("-");
                break;
            default:
                insertar(etiqueta);
        }
    }

    private void insertar(String texto) {
        // Tras un resultado: un operador continúa la cuenta, cualquier otra cosa empieza de nuevo
        // Diferencias de los operadores en la pantalla principal
        if (mostrandoResultado && !continuaExpresion(texto)) {
            pantalla.setText("");
        }
        mostrandoResultado = false;
        pantalla.replaceSelection(texto);
        pantalla.requestFocusInWindow();
        historial.setForeground(Tema.TEXTO_SUAVE);
    }

    private static boolean continuaExpresion(String t) {
        return !t.isEmpty() && "+-*/^%)".indexOf(t.charAt(0)) >= 0;
    }

    private void borrar() {
        mostrandoResultado = false;
        int pos = pantalla.getCaretPosition();
        String t = pantalla.getText();
        if (pos > 0) {
            pantalla.setText(t.substring(0, pos - 1) + t.substring(pos));
            pantalla.setCaretPosition(pos - 1);
        }
        pantalla.requestFocusInWindow();
    }

     // Fuuncion principal de los botones de la pantalla
    private void limpiar() {
        pantalla.setText("");
        historial.setText(" ");
        historial.setForeground(Tema.TEXTO_SUAVE);
        mostrandoResultado = false;
        pantalla.requestFocusInWindow();
    }

    private void alternarSigno() {
        String t = pantalla.getText().trim();
        if (t.isEmpty()) {
            return;
        }
        if (t.matches("-?[\\d.]+")) {
            t = t.startsWith("-") ? t.substring(1) : "-" + t;
        } else if (t.startsWith("-(") && t.endsWith(")")) {
            t = t.substring(2, t.length() - 1);
        } else {
            t = "-(" + t + ")";
        }
        pantalla.setText(t);
        pantalla.requestFocusInWindow();
    }
    
    // Logica principal de la calculadora

    private void calcular() {
        String expr = pantalla.getText().trim();
        if (expr.isEmpty()) {
            return;
        }
        try {
            double valor = Evaluador.evaluar(expr);
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new ExpresionException("Operación no válida (división por cero o fuera de dominio)");
            }
            String resultado = FormatoNumero.formatear(valor);
            historial.setForeground(Tema.TEXTO_SUAVE);
            historial.setText(expr + " =");
            pantalla.setText(resultado);
            ultimoResultado = resultado;
            mostrandoResultado = true;
        } catch (ExpresionException ex) {
            historial.setForeground(Tema.TEXTO_ERROR);
            historial.setText(ex.getMessage());
        }
        pantalla.requestFocusInWindow();
    }
}

// fin del codigo de la computadora 
